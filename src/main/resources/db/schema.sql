CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  role ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  
);

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  category VARCHAR(50) NOT NULL,
  image_url VARCHAR(500),
  featured BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_product_price CHECK (price >= 0),
  CONSTRAINT chk_product_stock CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS cart_items (
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, product_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS orders (
  id INT  AUTO_INCREMENT PRIMARY KEY,
  user_id INT  NOT NULL,
  order_number VARCHAR(40) NOT NULL UNIQUE,
  status ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
  total_amount DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT chk_order_total CHECK (total_amount >= 0),
  INDEX idx_orders_user_created (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  product_name VARCHAR(120) NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  quantity INT NOT NULL,
  CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT chk_order_item_price CHECK (unit_price >= 0),
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
);

INSERT INTO products (sku, name, description, price, stock, category, image_url, featured) VALUES
('LIVING-001', '아크 테이블 램프', '부드러운 곡선과 따뜻한 빛을 담은 테이블 램프', 89000, 18, '생활', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80', TRUE),
('KITCHEN-001', '선데이 머그 세트', '매일 손이 가는 차분한 질감의 머그 두 개', 42000, 32, '주방', 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=900&q=80', TRUE),
('STATIONERY-001', '패브릭 기록 노트', '생각을 가볍게 붙잡아 두는 패브릭 노트', 18000, 50, '문구', 'https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=900&q=80', FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), category = VALUES(category);

CREATE TABLE IF NOT EXISTS support_policies (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  version INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS inquiries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  product_id INT NULL,
  order_id INT NULL,
  category VARCHAR(20) NOT NULL,
  title VARCHAR(120) NOT NULL,
  body TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  answer TEXT NULL,
  answered_by INT NULL,
  answered_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  ai_state VARCHAR(20) NOT NULL DEFAULT 'DISABLED',
  ai_token VARCHAR(36) NULL,
  ai_started_at TIMESTAMP NULL,
  draft TEXT NULL,
  needs_review BOOLEAN NOT NULL DEFAULT TRUE,
  review_reason VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inquiry_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_inquiry_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_inquiry_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_inquiry_admin FOREIGN KEY (answered_by) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS inquiry_ai_runs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  inquiry_id BIGINT NOT NULL,
  state VARCHAR(20) NOT NULL,
  model VARCHAR(100) NOT NULL,
  prompt_version VARCHAR(30) NOT NULL,
  draft TEXT NULL,
  needs_review BOOLEAN NOT NULL DEFAULT TRUE,
  reason VARCHAR(1000) NULL,
  sources MEDIUMTEXT NOT NULL,
  citations TEXT NOT NULL,
  elapsed_ms BIGINT NOT NULL,
  input_tokens INT NOT NULL DEFAULT 0,
  output_tokens INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_run_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiries(id)
);
