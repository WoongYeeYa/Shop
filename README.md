# My Shop

JSP/Servlet MVC와 순수 JavaScript를 함께 보여 주는 포트폴리오용 쇼핑몰입니다.

## 현재 구현 범위

- 반응형 홈/상품 목록 UI
- Servlet Controller → Service → MyBatis Mapper → JSP 흐름
- MySQL 초기 스키마와 샘플 상품
- BCrypt 회원가입/로그인, CSRF, 세션·관리자 권한 인증
- MySQL 장바구니와 fetch 기반 수량 동기화
- 재고 잠금, 주문 스냅샷, 모의 결제 트랜잭션
- 관리자 상품 등록/수정/소프트 삭제
- 상품·본인 주문 문의, 관리자 답변 승인, 정책 관리
- 선택적 AI 답변 초안과 근거·생성 이력, 장애 시 수동 처리
- 프로젝트 진행 중 계속 갱신할 설계/개발/UI 가이드

## 빠른 시작

필요 도구: JDK 25 이상, Maven 3.9+, MySQL 8, Tomcat 9

1. `docs/SETUP.md`에 따라 DB와 환경 변수를 준비합니다.
2. Windows에서는 `scripts\build.cmd`, 그 외 환경에서는 `mvn clean package`를 실행합니다.
3. 생성된 `target/my-shop.war`를 Tomcat 9에 배포합니다.
4. `http://localhost:8080/my-shop/`에 접속합니다.

## Docker로 실행

Docker Desktop을 실행한 뒤 프로젝트 루트에서 다음 명령을 실행합니다.

```bash
docker compose up --build
```

MySQL 스키마와 샘플 상품은 최초 실행 시 자동으로 생성됩니다. 준비가 끝나면
`http://localhost:8080/my-shop/`에 접속합니다. 비밀번호와 포트를 바꾸려면
`.env.example`을 `.env`로 복사한 뒤 값을 수정합니다.

Docker MySQL은 기본적으로 호스트의 `3307` 포트에 연결됩니다. DBeaver에서는
호스트 `localhost`, 포트 `3307`, 데이터베이스 `my_shop`, 사용자 `myshop`를
사용합니다.

```bash
docker compose down
```

DB 데이터까지 초기화하려면 `docker compose down -v`를 사용합니다. 이 명령은
Docker 볼륨의 모든 쇼핑몰 데이터를 삭제합니다.

## 문서

- [개발 가이드](docs/DEVELOPMENT_GUIDE.md)
- [아키텍처 결정 기록](docs/ARCHITECTURE.md)
- [DB 설계](docs/DATABASE.md)
- [UI 가이드](docs/UI_GUIDE.md)
- [환경 설정](docs/SETUP.md)
- [로드맵](docs/ROADMAP.md)
- [AI 고객 문의 설정·설계·검증](docs/AI_SUPPORT.md)

기능을 추가할 때 관련 문서와 `docs/ROADMAP.md`의 완료 조건도 함께 갱신합니다.

## 데모 시나리오

1. 회원가입 후 상품을 검색하고 상세 화면에서 장바구니에 담습니다.
2. 장바구니에서 수량을 바꾸고 모의 결제를 완료한 뒤 주문 내역을 확인합니다.
3. 관리자 계정으로 `/admin/products`에서 상품을 등록·수정·숨김 처리합니다.

개발 계정을 관리자로 바꾸려면 MySQL에서 다음을 실행합니다.

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```
