# 데이터베이스 설계

핵심 관계는 `users 1:N orders`, `orders 1:N order_items`, `products 1:N order_items`, `users 1:N cart_items`이다.

- 금액은 부동소수점 오차를 피하기 위해 `DECIMAL(12,2)`를 사용한다.
- 주문 상품에는 구매 당시 이름과 단가를 스냅샷으로 저장한다.
- 재고 차감과 주문 생성은 하나의 트랜잭션에서 처리한다.
- 이메일과 SKU에는 UNIQUE 제약을 둔다.
- 비밀번호는 평문이 아닌 BCrypt 해시만 저장한다.

전체 DDL과 개발용 샘플 데이터는 `src/main/resources/db/schema.sql`에 있다.

## 고객 문의 확장

- users 1:N inquiries, products/orders 1:N inquiries (선택 연결).
- inquiries 1:N inquiry_ai_runs. 원본 초안·근거 스냅샷·토큰·처리 시간을 보관한다.
- support_policies는 활성 여부와 version으로 관리한다.
- 문의 answer는 관리자 승인 후에만 저장하며 draft와 구분한다.
- version은 답변/정책 동시 수정의 덮어쓰기를 막는다.
- ai_token은 생성 작업을 식별하여 늦은 결과가 새 결과나 승인 답변을 덮어쓰지 못하게 한다.
- 신규 DDL은 schema.sql, 기존 설치의 추가 테이블 생성은 db/support.sql을 사용한다.
