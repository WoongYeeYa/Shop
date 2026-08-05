# 데이터베이스 설계

핵심 관계는 `users 1:N orders`, `orders 1:N order_items`, `products 1:N order_items`, `users 1:N cart_items`이다.

- 금액은 부동소수점 오차를 피하기 위해 `DECIMAL(12,2)`를 사용한다.
- 주문 상품에는 구매 당시 이름과 단가를 스냅샷으로 저장한다.
- 재고 차감과 주문 생성은 하나의 트랜잭션에서 처리한다.
- 이메일과 SKU에는 UNIQUE 제약을 둔다.
- 비밀번호는 평문이 아닌 BCrypt 해시만 저장한다.

전체 DDL과 개발용 샘플 데이터는 `src/main/resources/db/schema.sql`에 있다.
