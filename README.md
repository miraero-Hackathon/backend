# 📌 Miraero Backend

> 미래로(Miraero) 20·30대 사회초년생을 위한 개인화 자산 관리·목표 로드맵 서비스의 백엔드입니다.

사용자의 자산·소비·부채 데이터를 바탕으로 금융 목표를 관리하고, 금융 상품·청년 정책을 연계하며, AI 금융 코치 기능을 REST API로 제공합니다.

## 👨‍💻 Contributors

| <img src="https://github.com/leeyoungheon.png" width="100" alt="이영헌" /> | <img src="https://github.com/itleo29.png" width="100" alt="김영진" /> | <img src="https://github.com/tmj5574.png" width="100" alt="탁민주" /> | <img src="https://github.com/SongCodeMaster.png" width="100" alt="송승윤" /> |
| :---: | :---: | :---: | :---: |
| **이영헌** | **김영진** | **탁민주** | **송승윤** |
| [GitHub](https://github.com/leeyoungheon) | [GitHub](https://github.com/itleo29) | [GitHub](https://github.com/tmj5574) | [GitHub](https://github.com/SongCodeMaster) |


## ✨ 주요 기능

| 기능 | 설명 |
| --- | --- |
| 🔐 **인증 및 사용자 관리** | 회원가입·로그인·토큰 재발급·로그아웃 및 사용자 프로필을 관리합니다. |
| 🏦 **자산 연동** | MyData 목 서버 OAuth 연동 후 계좌와 거래 내역을 동기화합니다. |
| 🎯 **목표 및 자산 관리** | 금융 목표를 생성하고 계좌·저금통 자산을 연결하여 달성 현황을 관리합니다. |
| 💳 **자동이체 및 저금통** | 목표 달성을 위한 자동이체를 실행하고 저금통 자산을 관리합니다. |
| 📊 **소비 분석 및 페이스메이커** | 지출 시뮬레이션·또래 평균·목표 달성 페이스를 제공합니다. |
| 💰 **금융 상품·청년 정책** | 예금·적금 상품과 청년 정책을 조회·추천하고 데이터를 정기 동기화합니다. |
| 🤖 **AI 금융 코치** | OpenAI Responses API를 사용해 금융 맥락 기반 상담과 분석 응답을 제공합니다. |

## 🛠 Tech Stack

| Language | Framework | Security | Data | Cache | Build / Deploy |
| :---: | :---: | :---: | :---: | :---: | :---: |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="48" alt="Java" /><br />Java 17 | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="48" alt="Spring" /><br />Spring MVC · Spring Security | <img src="https://jwt.io/img/pic_logo.svg" width="48" alt="JWT" /><br />JWT | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="42" alt="MySQL" /> <img src="https://mybatis.org/images/mybatis-logo.png" width="52" alt="MyBatis" /> <img src="https://cdn.simpleicons.org/flyway/CC0200" width="42" alt="Flyway" /><br />MySQL 8 · MyBatis · Flyway | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg" width="48" alt="Redis" /><br />Redis 7 · Lettuce | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gradle/gradle-original.svg" width="48" alt="Gradle" /><br />Gradle · WAR · Tomcat 9 · EC2 · GitHub Actions |

**External APIs** · MyData Mock Server · 금융감독원 금융상품 API · 온통청년 정책 API · OpenAI Responses API

## 🏛 백엔드 시스템 아키텍처

![Miraero Backend System Architecture](https://github.com/user-attachments/assets/cad78211-2d46-4695-8ffb-d5bccd602549)

## 🗄 데이터베이스

![Miraero ERD](https://github.com/user-attachments/assets/b25f8102-1b7c-4430-9c49-d31d35c36e1b)

## ⚙️ 환경 설정

`src/main/resources/application-example.properties`와 `.env.example`을 참고해 로컬 환경 변수를 구성합니다. 실제 비밀값이 포함된 `.env` 파일은 저장소에 커밋하지 않습니다.

```properties
# Local profile
SPRING_PROFILES_ACTIVE=local

# MySQL (Docker Compose)
MYSQL_PORT=3307
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=miraero
MYSQL_USER=miraero
MYSQL_PASSWORD=
DB_URL=jdbc:mysql://localhost:3307/miraero?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=miraero
DB_PASSWORD=

# Redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=

# External APIs
FSS_API_KEY=
OPENAI_API_KEY=
MYDATA_OAUTH_CLIENT_SECRET=
YOUTH_POLICY_API_KEY=
```

## 🚀 설치 및 실행

### Prerequisites

- JDK 17
- Docker Desktop (MySQL·Redis를 Docker로 실행하는 경우)
- Apache Tomcat 9.x 또는 IntelliJ Tomcat 실행 구성

### 1. Clone repository

```bash
git clone https://github.com/team-miraero/backend.git
cd backend
```

### 2. Configure environment variables

`.env.example`을 복사해 `.env` 파일을 만든 뒤, 위 환경 설정의 필수값을 채웁니다.

```bash
# macOS / Linux
cp .env.example .env

# Windows PowerShell
Copy-Item .env.example .env
```

### 3. Run MySQL and Redis

```bash
docker compose up -d
docker compose ps
```

MySQL은 기본적으로 `localhost:3307`, Redis는 `localhost:6379`에서 실행됩니다. 중지하려면 `docker compose down`을 실행합니다.

### 4. Build WAR

```bash
# macOS / Linux
./gradlew clean build

# Windows
gradlew.bat clean build
```

### 5. Deploy and run

빌드 결과물 `build/libs/*.war`를 Apache Tomcat 9의 `webapps` 디렉터리에 배포합니다. IntelliJ를 사용할 경우 Tomcat 실행 구성에 동일한 WAR 아티팩트를 등록해 실행할 수 있습니다.

서버 실행 후 다음 주소에서 Swagger UI와 API 동작을 확인합니다.

```text
http://localhost:8080/swagger-ui.html
```

## 📚 API 명세

| Domain | Base path | Description |
| --- | --- | --- |
| Auth | `/api/auth` | 회원가입, 로그인, 토큰 재발급, 로그아웃 |
| User | `/api/users` | 사용자 프로필 조회 |
| MyData | `/api/mydata` | MyData 연결 및 자산 데이터 동기화 |
| Account / Transaction | `/api/accounts`, `/api/transactions` | 계좌 및 거래 내역 조회 |
| Goal | `/api/goals` | 목표, 연결 자산, 마일스톤 관리 |
| Money Box / Auto Transfer | `/api/money-boxes`, `/api/auto-transfers` | 저금통과 자동이체 관리 |
| Product / Youth Policy | `/api/deposits`, `/api/savings`, `/api/youth-policies` | 금융상품 및 청년 정책 조회·추천 |
| Expense Analysis | `/api/expense-analysis` | 소비 분석, 지출 시뮬레이션, 또래 평균 |
| Pace Maker | `/api/pace-maker` | 목표 달성 페이스 관리 |
| AI Coach | `/api/ai-coach/conversations` | AI 코치 대화 및 메시지 스트리밍 |

## 🌿 Contributing

작업은 최신 `main` 브랜치에서 분기하고, 변경 범위에 맞는 브랜치와 커밋 메시지를 사용합니다.

```bash
git switch main
git pull origin main
git switch -c docs/#이슈번호-readme
```

## 📜 License

This project was developed as part of the KB IT's Your Life 7th Final Project.
