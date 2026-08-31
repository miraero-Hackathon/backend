# Project: 미래로(Miraero) Backend

## 1. 프로젝트 개요

미래로는 20·30대 사회초년생을 위한 AI 기반 개인화 자산관리 및 목표 로드맵 서비스입니다.
사용자의 자산, 소득, 소비, 부채, 목표 금액과 기간을 분석하여 금융 로드맵과 예·적금 상품을 추천합니다.

답변과 코드 설명은 한국어로 작성합니다.

## 2. 기술 스택과 절대 제약 

- Java, Spring Framework 5.3.x, Spring MVC, Spring Security 5.8.x
- MyBatis, MySQL 8.0, Redis
- Gradle 기반 WAR, HikariCP, Log4j2, Lombok
- JWT 0.11.5, Docker Compose
- 프론트엔드: Vue 3, JavaScript, Pinia, Axios, Bootstrap 5
- 외부 API: 금융감독원 금융상품 한눈에 Open API, OpenAI API
- AI 모델: `gpt-5-nano`, `gpt-4o-mini`
- 마이데이터는 실제 연동 대신 목데이터를 사용합니다.

다음 제약을 반드시 지킵니다.

- Spring Boot, `@SpringBootApplication`, JPA, FastAPI, TypeScript를 사용하지 않습니다.
- Spring Legacy Java Config와 `application.properties` 방식을 유지합니다.
- 기존 기술 스택을 교체하거나 새 라이브러리를 임의로 추가하지 않습니다.
- OpenAI API는 Spring 백엔드에서 직접 연동합니다.
- 기능별 모델과 기본 모델은 코드가 아닌 설정으로 관리합니다.
- JSON 필드명은 `camelCase`를 사용합니다.

## 3. 작업 시작 원칙

1. 관련 파일, 기존 구현 패턴, 실제 DDL을 먼저 확인합니다.
2. 존재하지 않는 클래스, 메서드, 테이블, 컬럼을 임의로 가정하지 않습니다.
3. 요청 범위 밖의 대규모 리팩터링을 하지 않습니다.
4. 기존 코드와 팀원의 변경사항을 임의로 삭제하지 않습니다.
5. 정보가 부족하면 관련 파일을 더 확인하거나 사용자에게 질문합니다.
6. 새 라이브러리가 필요하면 추가 전에 필요성과 영향을 설명합니다.

## 4. 작업별 상세 규칙

작업을 시작하기 전에 해당 문서를 반드시 읽고 적용합니다.

- Mapper, Mapper XML, SQL, DDL, DB, 금융 계산:
  `docs/agent-rules/mybatis-database.md`
- Controller, DTO, API, 예외, JWT, Redis, CORS, 외부 API:
  `docs/agent-rules/api-security.md`
- 설정, Docker, Git, 테스트, 문서화, 완료 검증:
  `docs/agent-rules/testing-operations.md`

여러 영역을 수정하면 관련 문서를 모두 읽습니다. 상세 문서는 이 파일을 보완하며,
규칙이 충돌하면 사용자 요청 → 이 파일 → 상세 규칙 → 기존 구현 패턴 순으로 판단합니다.
기존 코드나 DDL과 규칙이 충돌하면 임의로 바꾸지 말고 충돌 내용과 영향 범위를 먼저 설명합니다.

## 5. 패키지와 계층 구조

패키지는 도메인 기준으로 분리합니다.

```text
member
├── controller
├── service
├── mapper
├── domain
├── dto
│   ├── request
│   └── response
└── exception
```

공통 기능은 `global/config`, `global/exception`, `global/response`,
`global/security`에 둡니다.

다음 흐름을 유지합니다.

```text
Controller → Service → Mapper → Database
```

- Controller: HTTP 요청·응답, 요청값 검증, Service 호출
- Service: 비즈니스 로직, 검증, 상태 변경, 트랜잭션
- Mapper: MyBatis 기반 DB 접근
- Request/Response DTO: API 입·출력
- Domain: DB 테이블에 대응하는 내부 객체

Controller에서 Mapper를 직접 호출하지 않습니다. Controller에 금융 계산이나 복잡한
비즈니스 로직을 두지 않고, Mapper에 비즈니스 판단을 두지 않습니다. Domain 객체를
API 응답으로 직접 반환하지 않습니다.

## 6. Java와 Service 규칙

- 들여쓰기는 공백 4칸을 사용합니다.
- 클래스는 `PascalCase`, 변수·메서드는 `camelCase`, 상수는 `UPPER_SNAKE_CASE`를 사용합니다.
- 필드 주입 대신 생성자 주입과 `@RequiredArgsConstructor`를 우선합니다.
- Lombok은 `@RequiredArgsConstructor`, `@Getter` 중심으로 사용합니다.
- Domain에 무분별한 `@Data`, `@Setter`를 사용하지 않습니다.
- 와일드카드와 사용하지 않는 import를 허용하지 않습니다.
- 매직 넘버와 문자열은 상수 또는 Enum으로 분리합니다.
- 메서드는 하나의 명확한 책임을 가지며 동작과 대상이 드러나는 이름을 사용합니다.
- 주석은 동작 반복이 아니라 구현 이유가 필요한 경우에만 작성합니다.

Service는 인터페이스와 구현체로 분리하고 Controller는 인터페이스에 의존합니다.
조회는 기본적으로 `@Transactional(readOnly = true)`, 등록·수정·삭제는
`@Transactional`을 Service 계층에 적용합니다.

## 7. DTO, 응답, 예외 핵심 규칙

- Request DTO와 Response DTO를 분리하고 `Request`, `Response` 접미사를 사용합니다.
- 요청 검증은 Request DTO에서 처리합니다.
- 비밀번호와 내부 상태 등 불필요한 정보는 응답하지 않습니다.
- DTO 변환 방식은 `MemberResponse.from(member)` 같은 프로젝트 방식을 통일합니다.
- 모든 API 응답은 원칙적으로 `ApiResponse<T>` 형식을 사용합니다.
- 예상 가능한 오류는 `ErrorCode`를 가진 `BusinessException`으로 처리합니다.
- `@RestControllerAdvice`에서 예외를 공통 처리하고 Controller에 반복적인 `try-catch`를 작성하지 않습니다.
- 내부 예외, SQL 오류, 스택 트레이스를 클라이언트에 노출하지 않습니다.

## 8. 로깅과 민감정보

- `System.out.println()` 대신 Log4j2를 사용합니다.
- 로그는 문자열 결합 대신 `{}` 파라미터 치환 방식을 사용합니다.
- `ERROR`는 장애, `WARN`은 주의, `INFO`는 주요 흐름, `DEBUG`는 개발 상세에 사용합니다.
- 비밀번호, 토큰, 주민등록번호, 전체 계좌번호, API Secret을 로그에 남기지 않습니다.
- `.env`, `.env.local`은 API Key가 포함될 수 있으므로 읽거나 수정하지 않습니다.
- 비밀번호, JWT Secret, API Key를 코드나 Git 저장소에 포함하지 않습니다.

## 9. 변경과 완료 원칙

- DB 구조, API 응답, 환경 변수 변경은 작업 전에 팀에 공유할 사항으로 표시합니다.
- 다른 기능에 영향을 주는 변경을 임의로 진행하지 않습니다.
- 충돌 해결 시 다른 팀원의 코드를 임의로 삭제하지 않습니다.
- 코드 수정 후 import, 타입, 메서드 시그니처와 컴파일 가능성을 확인합니다.
- SQL 수정 후 DDL, Mapper 인터페이스와 XML의 일치 여부를 확인합니다.
- API 변경 시 URL, 요청·응답, 상태 코드, 오류 코드와 문서를 함께 확인합니다.
- 보안 또는 금융 계산 변경은 근거와 영향 범위를 설명합니다.
- 안전하고 관련 있는 빌드·테스트를 실행하고 결과를 보고합니다.
- 변경 파일과 핵심 변경사항, 실행하지 못한 검증이 있다면 그 이유를 설명합니다.

