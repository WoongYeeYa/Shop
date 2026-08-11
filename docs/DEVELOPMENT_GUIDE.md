# 개발 가이드

## 목적

이 프로젝트는 기능 수보다 설계 이유, 계층 분리, 트랜잭션, 보안, 사용자 경험을 설명할 수 있는 결과물을 목표로 한다.

## 기능 개발 규칙

1. 요구사항과 완료 조건을 `ROADMAP.md`에 먼저 기록한다.
2. DB 변경은 `schema.sql`에 반영하고 `DATABASE.md`에 의도를 남긴다.
3. HTTP 처리는 Controller, 업무 규칙은 Service, SQL은 Mapper에 둔다.
4. JSP에서는 조회와 표현만 수행하며 Java scriptlet을 쓰지 않는다.
5. AJAX 응답은 `{ success, data, message }` 형태를 사용한다.
6. 사용자 입력은 서버에서 다시 검증하고 출력은 JSTL `c:out`으로 이스케이프한다.
7. 기능 완료 후 정상/실패 경로를 테스트하고 문서를 갱신한다.

## 패키지 규칙

- `controller`: URL, 파라미터, 응답/뷰 선택
- `service`: 유스케이스와 트랜잭션 경계
- `domain`: DTO/VO
- `mapper`: MyBatis 인터페이스
- `config`: DB와 애플리케이션 설정
- `filter`: 인증, 인코딩 등 공통 요청 처리

## Git 커밋 예시

- `feat: 상품 목록 필터 추가`
- `fix: 주문 재고 차감 동시성 오류 수정`
- `docs: 장바구니 API 명세 추가`
- `refactor: 인증 검증을 필터로 이동`

## Definition of Done

- 모바일과 데스크톱에서 핵심 흐름이 동작한다.
- 실패 메시지가 사용자에게 설명 가능하다.
- SQL/비밀번호 같은 민감 정보가 로그나 저장소에 노출되지 않는다.
- 관련 문서, 테스트, 접근성 레이블이 갱신되었다.

## 지속적인 정리와 일관성 규칙

1. 작업을 시작하기 전에 전체 검색으로 기존 명칭, 중복 구현, 임시 파일과 생성 산출물을 확인한다.
2. 브랜드명은 화면의 `My Shop`, Maven·배포 경로의 `my-shop`, DB의 `my_shop`, Java 패키지의 `com.myshop`으로 구분해 일관되게 사용한다.
3. `target`, 로그, IDE 캐시처럼 다시 만들 수 있는 산출물은 커밋하지 않고 `.gitignore`와 `.dockerignore`로 제외한다.
4. 파일이나 코드를 삭제하기 전에 참조 검색과 빌드·테스트로 미사용 여부를 확인한다. 용도가 불명확한 사용자 파일은 임의로 삭제하지 않는다.
5. 새 의존성·설정·파일을 추가할 때 기존 구성으로 해결할 수 있는지 먼저 확인하고, 더 이상 쓰지 않는 대체 구현은 같은 작업에서 제거한다.
6. 이름·DB 스키마·실행 방법이 바뀌면 코드, Docker, 환경 변수 예시, README와 관련 문서를 같은 변경에서 갱신한다.
7. 완료 전에는 이전 명칭과 남아 있는 미완료 표식을 다시 검색하고, 테스트 및 패키징과 Docker Compose 구성 검증을 수행한다.

## 카탈로그 URL 규칙

- 목록과 검색: `GET /products?keyword=lamp&category=Living`
- 상세: `GET /product?id=1`
- 검색어는 최대 100자로 정규화하고 MyBatis 바인딩 파라미터로 전달한다.
- 존재하지 않는 상품은 404, 잘못된 ID는 400으로 응답한다.

## 인증 규칙

- 세션 사용자 키는 `loginUser`, CSRF 토큰 키는 `csrfToken`이다.
- 비밀번호는 BCrypt cost 12로 해시하며 DB나 로그에 평문을 남기지 않는다.
- 로그인 실패 메시지는 이메일 존재 여부를 노출하지 않는다.
- 상태 변경은 POST로 처리하고 CSRF 토큰을 반드시 전송한다.

## 장바구니 API

- 조회: `GET /account/cart/api`
- 변경: `POST /account/cart/api`
- 요청 필드: `action=add|update|remove`, `productId`, `quantity`
- AJAX 요청은 `X-CSRF-Token` 헤더를 사용한다.
- 응답: `{ "success": true, "count": 3, "message": null }`
