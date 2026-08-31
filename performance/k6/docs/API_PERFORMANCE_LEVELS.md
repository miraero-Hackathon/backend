# API 성능 등급

API 성격에 따라 Threshold와 부하 Profile을 일관되게 적용하기 위한 팀 기준입니다.

실제 실행에 사용되는 숫자의 원본은 `config/thresholds.js`와 `config/profiles.js`입니다. 이 문서와 설정이 다르면 config를 기준으로 판단합니다.

## 등급 기준

| 등급 | 적용 대상 | p95 | p99 | 실패율 | 기준 Load 상한 VU |
| --- | --- | ---: | ---: | ---: | ---: |
| `fastRead` | 화면에서 즉시 필요한 단순 조회 | 300ms | 600ms | 0.5% 미만 | 20 |
| `complexRead` | JOIN, 집계 또는 대량 조회 | 500ms | 1000ms | 1% 미만 | 20 |
| `transaction` | 상태나 금액을 변경하는 트랜잭션 | 700ms | 1500ms | 0.1% 미만 | 10 |
| `externalSync` | 외부 API 호출과 대량 저장 | 3000ms | 5000ms | 1% 미만 | 5 |

Smoke Test는 모든 등급에서 `1 VU`, `30초`, 실패율 `0%`를 사용합니다. 응답시간 등급은 Load와 Stress Test에 적용합니다.

아래 Load 수치는 운영 데이터가 없는 현재 단계에서 개선 전후를 비교하기 위한 초기 기준 workload입니다. 시스템이 안정적으로 처리할 수 있는 최대 VU나 실제 운영 트래픽을 의미하지 않습니다. API별 예상 호출 패턴이 다르면 담당자가 산정 근거를 기록하고 Profile을 조정할 수 있습니다.

## 등급별 부하

### Load

| 등급 | VU 단계 | 각 단계 유지 |
| --- | --- | ---: |
| `fastRead` | 5 → 10 → 20 | 2분 |
| `complexRead` | 5 → 10 → 20 | 2분 |
| `transaction` | 2 → 5 → 10 | 2분 |
| `externalSync` | 1 → 3 → 5 | 2분 |

각 단계 전환에는 30초를 사용하고 마지막에 30초 동안 0 VU로 감소합니다.

### Stress

| 등급 | VU 단계 | 각 단계 |
| --- | --- | ---: |
| `fastRead` | 20 → 40 → 60 → 100 | 2분 |
| `complexRead` | 20 → 30 → 50 → 75 | 2분 |
| `transaction` | 10 → 20 → 30 | 2분 |
| `externalSync` | 5 → 10 → 15 | 2분 |

Stress는 한계 분석이 필요한 API에만 수행하며, 마지막에 1분 동안 0 VU로 감소합니다.

## 현재 API 분류

| API | 등급 | 이유 |
| --- | --- | --- |
| `GET /api/users/profile` | `fastRead` | 화면에서 즉시 필요한 사용자 단건 조회 |
| `GET /api/pace-maker` | `fastRead` | 페이스메이커 상태와 설정 확인 |
| `GET /api/mydata/connections` | `fastRead` | 연결 기관 상태 목록 조회 |
| `GET /api/expense-analysis/peer-average` | `complexRead` | 사용자 및 거래 데이터 집계 |
| `GET /api/expense-analysis/dashboard` | `complexRead` | 소비 카테고리와 기간별 집계 |
| `GET /api/pace-maker/dashboard` | `complexRead` | 저금통과 스트릭 통계 조합 |
| `GET /api/pace-maker/goals` | `complexRead` | 목표, 자산과 계좌 데이터 조합 |
| `GET /api/goals/{goalId}/available-money/daily` | `complexRead` | 날짜별 가용금액 집계 |
| `PATCH /api/pace-maker/{autoSavingId}/status` | `transaction` | 자동저축 상태 변경 |
| `PATCH /api/pace-maker/{autoSavingId}/max-amount` | `transaction` | 자동저축 상한액 변경 |
| `POST /api/pace-maker/deposits` | `transaction` | 잔액 변경과 정합성 보장 필요 |
| `PATCH /api/users/me/password` | `transaction` | 비밀번호 검증과 변경 |
| `POST /api/mydata/connect` | `externalSync` | 외부 목 서버 인증과 연결 저장 |
| `POST /api/mydata/sync` | `externalSync` | 외부 호출 후 계좌와 거래내역 저장 |

## 적용 방법

시나리오에서 API 등급을 명시적으로 선택합니다.

```javascript
const API_NAME = 'peer-average';
const API_LEVEL = 'complexRead';

export const options = {
  ...loadProfile(API_LEVEL),
  thresholds: apiThresholds(API_NAME, API_LEVEL),
};
```

새 API 시나리오를 추가할 때 이 문서의 분류표도 함께 갱신합니다. API의 요구사항이나 Baseline 결과에 따라 등급을 변경할 수 있으며, 변경 이유를 PR에 기록합니다.

Load Profile은 예상 사용자 규모와 호출 빈도를 바탕으로 정하고, 탐색 테스트에서 확인한 최대 안정 부하를 그대로 사용하지 않습니다. 시스템의 한계 구간은 API별 Stress Profile에서 별도로 탐색합니다.
