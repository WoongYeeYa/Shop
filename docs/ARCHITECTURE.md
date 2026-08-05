# 아키텍처

## 요청 흐름

`Browser → Filter → Servlet Controller → Service → MyBatis Mapper → MySQL`

조회 결과는 Controller가 request attribute에 담아 JSP로 전달한다. 장바구니 수량 변경 같은 부분 상호작용은 JavaScript `fetch`로 JSON Servlet을 호출한다.

## 주요 결정

### 단일 애플리케이션

포트폴리오 범위에서 배포와 디버깅을 단순화하고, MVC 계층과 트랜잭션 설계에 집중하기 위해 모놀리스를 사용한다.

### Tomcat 9 + javax.servlet

현재 개발 환경의 Java 8과 JSP 학습 목적에 맞춘다. Tomcat 10은 `jakarta.servlet` 패키지를 사용하므로 그대로 혼용하지 않는다.

### MyBatis

SQL을 직접 설명할 수 있으면서 JDBC 반복 코드는 줄이기 위해 선택했다. 트랜잭션은 Service 계층에서 `SqlSession` 단위로 관리한다.

### JSP 보호

JSP는 `/WEB-INF/views` 아래에 두어 URL로 직접 접근할 수 없게 한다.

### 세션 인증과 요청 위조 방지

로그인 사용자는 비밀번호 해시를 제외한 `SessionUser`만 세션에 저장한다. 인증 성공 시 `changeSessionId()`로 세션 고정 공격을 방지하고 로그아웃은 POST로만 처리한다. 모든 POST 요청은 세션별 CSRF 토큰을 폼 필드 또는 `X-CSRF-Token` 헤더로 전달해야 한다. `/account/*`는 로그인 사용자만, `/admin/*`는 `ADMIN` 역할만 접근한다.

### 주문 트랜잭션

결제 요청은 하나의 `SqlSession`에서 장바구니와 상품 행을 `FOR UPDATE`로 잠근다. 재고를 다시 검증한 후 주문과 주문 상품 스냅샷을 생성하고, 조건부 재고 차감과 장바구니 삭제를 수행한다. 어느 단계든 실패하면 커밋하지 않아 전체 작업이 롤백된다.
