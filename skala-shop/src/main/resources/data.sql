-- password는 'dummy1234'를 BCrypt로 암호화한 값(로그인 테스트용 계정)
-- point는 회원가입 시 지급되는 초기 포인트(1,000,000)와 동일한 값으로 시딩한다.
INSERT INTO members (id, login_id, password, name, point) VALUES (1, 'dummy', '$2a$10$jADQvSDOEm9pdG6YmMDJbeneZlTRn1s7y5P5o4cGDZXCkXUx4guR2', '더미 회원', 1000000);
ALTER TABLE members ALTER COLUMN id RESTART WITH 2;

INSERT INTO categories (id, name) VALUES (1, '소설');
INSERT INTO categories (id, name) VALUES (2, '에세이');
INSERT INTO categories (id, name) VALUES (3, 'IT/프로그래밍');

INSERT INTO books (id, title, author, category_id, price, stock, description, created_at, updated_at) VALUES
  (1, '클린 코드', '로버트 마틴', 3, 30000, 20, '읽기 좋은 코드를 작성하는 방법에 대한 고전', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '이펙티브 자바', '조슈아 블로크', 3, 36000, 15, '자바 개발자를 위한 필독서', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'object', '조영호', 3, 29000, 2, '객체지향과 자바 언어 관점에서 설계 원리를 다루는 책', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, '칼의 노래', '김훈', 1, 13000, 8, '이순신을 소재로 한 소설', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, '태백산맥 1', '조정래', 1, 12000, 1, '한국 근현대사를 다룬 대하소설', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, '여행의 이유', '김영하', 2, 14000, 10, '여행을 소재로 한 에세이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, '언어의 온도', '이기주', 2, 13000, 0, '말과 글에 대한 에세이 (품절)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 명시적 id로 시딩한 뒤에는 IDENTITY 시퀀스가 자동으로 따라오지 않으므로, 이후 API로 생성되는 도서 id가 시딩 데이터와 충돌하지 않도록 재조정한다.
ALTER TABLE books ALTER COLUMN id RESTART WITH 8;
