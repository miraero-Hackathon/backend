# k6 성능 테스트

백엔드 API의 Smoke, Load, Stress 테스트를 같은 조건으로 반복하고, 성능 개선 전후를 비교하기 위한 디렉터리입니다.

모든 API를 테스트 대상으로 삼지는 않습니다. 호출 빈도, 쿼리 복잡도, 데이터 처리량, 동시성 및 비즈니스 중요도를 고려하여 성능 테스트 대상 API를 선정합니다.

성능 테스트의 기본 용어와 지표 해석은 [API 성능 테스트 가이드](docs/PERFORMANCE_TEST_GUIDE.md)를 먼저 참고합니다.

API별 Threshold와 부하 Profile은 [API 성능 등급](docs/API_PERFORMANCE_LEVELS.md)을 참고합니다.

## 디렉터리 구조

```text
performance/k6/
├─ common/                     # 인증과 환경변수 등 공통 기능
├─ config/                     # 부하 프로필과 통과 기준
├─ docs/                       # 기본 개념과 지표 해석 가이드
├─ scenarios/                  # API별 실행 시나리오
│  └─ peer-average/
│     ├─ smoke.js
│     ├─ load.js
│     └─ stress.js
├─ results/                    # 사람이 작성한 결과 요약
│  ├─ TEMPLATE.md
│  └─ peer-average/
└─ .env.example
```

## 문서 역할

- 이 README: 실행 방법, 파일명, 결과 저장과 팀 작업 절차
- [API 성능 테스트 가이드](docs/PERFORMANCE_TEST_GUIDE.md): 용어, 테스트 종류와 지표 해석
- [API 성능 등급](docs/API_PERFORMANCE_LEVELS.md): API 분류, 초기 Profile과 Threshold

## 프리티어 환경의 테스트 순서

현재 DEV 환경은 프리티어 사양이므로 처음부터 큰 부하를 주지 않습니다.

### 1. Smoke 실행

- `1 VU`의 낮은 부하로 인증, 요청 URL, 응답 구조와 check를 확인합니다.
- 기능 오류, 인증 오류 또는 잘못된 환경변수가 있으면 먼저 수정하고 탐색 결과로 저장하지 않습니다.

### 2. 공통 Profile로 초기 탐색

- API 등급에 해당하는 `config/profiles.js`의 초기 Load Profile을 한 번 실행합니다.
- 대상 API 태그 기준 요청 수, p95, p99, 실패율과 실제 RPS를 확인합니다.
- 같은 시간 구간의 EC2, RDS와 CPU Credit 지표를 확인합니다.

### 3. API별 기준 workload 탐색

- 공통 Profile이 너무 약해 자원과 응답시간의 변화가 거의 없으면 부하를 소폭 높여 다시 탐색할 수 있습니다.
- 이미 지연, 실패 또는 자원 포화가 나타나면 부하를 낮춥니다.
- 최대 안정 VU를 찾는 것이 목적은 아닙니다. 개선 전후를 반복 비교할 수 있는 구간을 선택합니다.
- 지연 급증, Timeout, 5xx 또는 지속적인 자원 포화가 시작되는 구간은 Load에서 제외하고 Stress 분석 대상으로 구분합니다.

### 4. API별 Load Profile 확정

다음 조건을 확인하고 VU 또는 RPS, 단계별 시간, Think Time을 고정합니다.

- check가 모두 성공하고 실패율이 API 등급 Threshold 이내입니다.
- Timeout과 예상하지 않은 5xx가 없습니다.
- 서버와 DB 지표에 측정 가능한 변화가 있고 지속적인 포화 상태는 아닙니다.
- 대상 API 요청 수가 p95와 p99를 비교할 수 있을 만큼 충분합니다.
- Profile 선정 근거를 결과 문서에 설명할 수 있습니다.

### 5. Load Baseline 측정

- 확정한 Profile을 같은 조건으로 3회 실행합니다.
- 각 실행 사이에는 EC2와 RDS 지표가 평상시 수준으로 회복될 시간을 둡니다.
- 실행마다 대상 API 태그 기준 RPS, 평균 응답시간, p95, p99와 실패율을 기록합니다.
- 각 지표의 세 측정값에서 중앙값을 구해 Baseline 대표값으로 저장합니다.
- 세 실행 중 한 실행 전체를 대표 결과로 선택하지 않습니다.

```text
p95: 180ms / 195ms / 185ms → 대표 p95 185ms
RPS: 16.8 / 17.2 / 16.9    → 대표 RPS 16.9
```

### 6. 개선 후 재측정

- 한 번에 하나의 개선 가설을 적용합니다.
- Baseline과 동일한 Profile과 환경 조건으로 다시 3회 실행합니다.
- 각 지표의 중앙값과 CloudWatch 지표를 Baseline과 비교합니다.
- 기능 결과와 실패율이 악화되지 않았는지도 함께 확인합니다.

### 7. Stress Test 선택 실행

- 핵심 API의 처리 한계, 병목 또는 포화 지점을 확인해야 할 때만 실행합니다.
- 프리티어 환경에서는 짧고 점진적으로 실행하고 테스트 후 서버 회복을 확인합니다.
- Load Baseline과 Stress 결과는 목적이 다르므로 별도 문서로 기록합니다.

## 실행 방법

프로젝트 루트에서 필요한 값을 `-e` 옵션으로 전달합니다.

```powershell
k6 run `
  -e BASE_URL=http://localhost:8080 `
  -e TEST_EMAIL=test@example.com `
  -e TEST_PASSWORD=password `
  performance/k6/scenarios/peer-average/smoke.js
```

실제 비밀번호와 토큰은 스크립트, 결과 문서, Git에 저장하지 않습니다.

`.env.example`은 필요한 환경변수 목록을 공유하기 위한 예시 파일이며 k6가 자동으로 읽지 않습니다. 실제 값은 Git에 커밋하지 않고, 실행할 때 `-e` 옵션 또는 팀에서 정한 환경변수 주입 방식을 사용합니다.

## 여러 사람의 개선 실험 관리

API마다 최초 기준 결과를 하나 정하고, 모든 개선 결과가 그 기준 문서를 참조하도록 관리합니다.

```text
results/peer-average/
├─ 2026-08-17-load-baseline.md
├─ 2026-08-18-kim-add-index.md
└─ 2026-08-19-lee-query-rewrite.md
```

파일명은 다음 규칙을 사용합니다.

```text
YYYY-MM-DD-작성자-실험내용.md
```

- 기준 결과는 `YYYY-MM-DD-load-baseline.md`로 작성합니다.
- 개선 결과에는 관련 이슈 또는 PR, 브랜치, 커밋 SHA를 기록합니다.
- 개선 결과에는 비교한 기준 결과 문서의 경로를 반드시 기록합니다.
- 같은 개선을 여러 번 측정하면 파일을 덮어쓰지 말고 실행 번호를 붙입니다.

```text
2026-08-18-kim-add-index-run1.md
2026-08-18-kim-add-index-run2.md
2026-08-18-kim-add-index-run3.md
```

## 팀 공통 규칙

### 1. 기준 결과

- 개선 전에 Smoke를 실행하고 Load Baseline을 작성합니다.
- Stress Baseline은 아래 선택 기준에 해당하는 API에만 작성합니다.
- Baseline에는 애플리케이션 커밋 SHA, 테스트 환경, 데이터 건수, 시나리오 파일을 반드시 기록합니다.
- 서버 사양, 데이터량 또는 공통 시나리오가 크게 변경되면 기존 Baseline을 덮어쓰지 않고 새로운 날짜의 Baseline을 작성합니다.
- 기준 workload의 VU 또는 RPS와 산정 근거를 결과 문서에 기록합니다.
- 운영 지표나 테스트 환경이 바뀌어 기준 workload를 다시 정하면 새로운 Baseline을 작성합니다.

Baseline과 Load workload의 의미는 [API 성능 테스트 가이드](docs/PERFORMANCE_TEST_GUIDE.md)를 참고합니다.

Stress Baseline은 다음 중 하나에 해당할 때 작성합니다.

- 서비스 핵심 API의 처리 한계와 실패 시작 구간을 비교해야 하는 경우
- 개선 목적이 최대 처리량, 병목 구간 또는 장애 회복 특성의 변화인 경우
- Load 구간은 정상이나 부하 증가 시 지연이나 오류가 급격히 증가하는 경우
- EC2, RDS, Connection Pool 또는 Lock의 포화 지점을 확인할 필요가 있는 경우

프리티어 인프라 자체의 한계가 먼저 나타난 경우에는 애플리케이션 한계로 단정하지 않고 결과에 명시합니다.

```text
results/peer-average/
├─ 2026-08-17-smoke.md
├─ 2026-08-17-load-baseline.md
└─ 2026-08-17-stress-baseline.md
```

### 2. 개선 실험

- 한 번의 실험에서는 가능한 한 하나의 개선 가설만 검증합니다.
- 개선 전후에 동일한 시나리오, VU, 실행 시간, 서버 사양과 데이터 조건을 사용합니다.
- 중요한 실험은 동일 조건에서 3회 이상 실행하고, 각 실행에서 측정된 주요 지표(p95, 실제 RPS, 실패율 등)의 중앙값을 대표값으로 비교합니다.
- 결과 문서에는 비교한 Baseline, 관련 이슈 또는 PR, 브랜치, 커밋 SHA를 기록합니다.
- p95만 개선되었더라도 실패율, p99, 처리량과 인프라 지표가 악화되지 않았는지 함께 확인합니다.
- 성능 개선으로 응답 데이터나 비즈니스 동작이 달라지지 않았는지 기능 테스트도 확인합니다.

### 3. Git에 기록할 결과

다음 결과는 Git에 커밋합니다.

- API별 Smoke 실행 결과와 Load Baseline
- 필요한 API의 Stress Baseline
- 최종 채택한 성능 개선 결과
- 개선을 취소하는 판단 근거가 된 유의미한 실패 결과
- 서버 한계, 오류 증가 또는 병목 지점을 확인한 결과

다음 결과는 Git에 커밋하지 않습니다.

- 환경변수 누락, 잘못된 URL 등 실행 오류가 있었던 결과
- 단순 연습 또는 재현 가치가 없는 반복 결과
- 민감정보가 포함된 결과
- 용량이 큰 원본 JSON 및 CSV 파일

### 4. 동시 작업과 리뷰

- 작업 시작 전에 담당 API와 개선 방법을 이슈 또는 팀 채널에 공유합니다.
- 다른 팀원의 결과 파일을 수정하거나 덮어쓰지 않습니다.
- 파일명에 날짜, 작성자, 실험 내용을 포함해 충돌을 방지합니다.
- 같은 API를 동시에 개선하면 각 결과가 동일한 Baseline을 참조하는지 확인합니다.
- PR에는 변경 코드와 개선 결과 문서를 함께 포함하고, 결과 문서 경로를 PR 본문에 작성합니다.
- 최종 채택 여부는 단일 지표가 아니라 응답시간, 처리량, 실패율, 자원 사용량과 코드 복잡도를 함께 보고 결정합니다.

## 공정한 비교 원칙

성능 개선 전후에는 다음 조건을 최대한 동일하게 유지합니다.

- 동일한 k6 시나리오와 Threshold
- 동일한 EC2와 RDS 사양
- 유사한 데이터 건수와 분포
- 동일한 테스트 실행 위치
- 동일한 테스트 시간대 또는 외부 트래픽이 적은 시간대
- 동일한 VU, 실행 시간, `sleep` 설정
- 동일한 기준 workload와 산정 방식

CloudWatch 지표는 k6의 실제 부하 발생 시간과 동일한 구간을 기준으로 확인합니다. CPU, 메모리, DB 연결 수와 지연시간은 결과 문서에 평균과 최대값을 함께 기록하고 집계 주기를 명시합니다.

## 결과 작성 방법

`results/TEMPLATE.md`를 API 결과 디렉터리에 복사한 뒤 파일명 규칙에 맞게 작성합니다.

작성 예시는 다음 문서를 참고합니다. 예시는 형식 안내용이며 실제 측정 결과가 아닙니다.

- [Load Baseline 예시](docs/LOAD_BASELINE_EXAMPLE.md)
- [성능 개선 결과 예시](docs/PERFORMANCE_IMPROVEMENT_EXAMPLE.md)

기록 대상은 다음과 같습니다.

- 대상 API 태그 기준 응답시간(p95/p99), 처리량(RPS), 실패율
- 테스트와 동일한 시간대의 EC2 및 RDS 지표
- 변경 내용과 개선 가설
- 기준 결과 대비 증감률
- 부작용, 오류, 추가 작업

로그인처럼 `setup()`에서 실행되는 요청은 전체 HTTP 통계에 포함될 수 있으므로, 성능 판정에는 대상 API 태그 지표를 사용합니다.

Arrival Rate 시나리오를 사용하는 경우 목표 처리량과 실제 처리량을 구분하여 기록합니다.

```text
목표 RPS: 50 req/s
실제 RPS: 47.8 req/s
```

## Git 관리

요약 Markdown은 커밋할 수 있지만, 용량이 큰 원본 JSON과 CSV 결과는 커밋하지 않습니다. 결과 문서에는 비밀번호, Access Token, API Key 등 민감정보를 포함하지 않습니다.
