# API명세서 (MVP)

버전: v1.1
상태: 확정
기반 문서: 설계_v3(확정, Auth 섹션 제외), 요구사항명세서.md, 기능명세서.md

> **v1.1 갱신**: 12~13단계(작업순서.md)에서 세션 기반 인증(Auth API), Cart/Order API의 세션 인증 전환, 주문 취소·포인트 검증을 도입했다. 기존 "인증 없음" 전제와 관련된 서술을 모두 갱신했다.

## 0. Auth(Member) API (v1.1 신규)

인증/인가는 REQ-001~003에서 최초 MVP는 제외했으나, v1.1에서 세션(HttpSession) 기반으로 부분 도입했다(JWT 아님). 로그인 성공 시 서버가 `Set-Cookie: JSESSIONID=...`를 발급하며, 이후 Cart/Order API 요청은 이 세션 쿠키를 그대로 전달해야 한다.

| Method | URL | 설명 | 성공 코드 |
| --- | --- | --- | --- |
| POST | /api/members | 회원가입 | 201 |
| POST | /api/members/login | 로그인(세션 쿠키 발급) | 200 |
| POST | /api/members/logout | 로그아웃(세션 무효화) | 204 |

**POST /api/members**

- Request: `{ "loginId": "string", "password": "string", "name": "string" }`
- Response: `{ memberId, loginId, name, point }`, 201 (`point`는 가입 시 지급되는 초기 포인트 1,000,000)
- 에러: 400 `VALIDATION_ERROR`, 409 `DUPLICATE_LOGIN_ID`

**POST /api/members/login**

- Request: `{ "loginId": "string", "password": "string" }`
- Response: `{ memberId, loginId, name }`, 200 + `Set-Cookie: JSESSIONID=...`
- 비고: 응답에는 `point`를 포함하지 않는다(회원가입 시 1회 확인하는 값으로 충분하며, 로그인마다 매번 조회할 필요는 없다고 판단). 현재 보유 포인트를 확인할 별도의 "내 정보 조회" API는 이번 라운드 범위 밖이다
- 에러: 401 `INVALID_CREDENTIALS`(아이디 또는 비밀번호 불일치)

**POST /api/members/logout**

- Response: 204(본문 없음), 세션 무효화
- 비고: 세션이 없어도 오류 없이 204를 반환한다

## 1. 공통 규칙

- Base URL: `/api`
- **인증(v1.1)**: Book API와 Admin API는 인증 없이 공개된다. **Cart API와 Order API는 세션 인증이 필요**하며, 로그인 후 발급된 세션 쿠키(`JSESSIONID`)가 없으면 401 `UNAUTHORIZED`를 반환한다. (최초 MVP 전제였던 "인증 없음, 고정 더미 회원(id=1) 기준"은 v1.1부터 로그인 회원 기준으로 대체됨)
- 응답: 성공 시 리소스를 직접 반환(불필요한 래핑 없음), 실패 시 아래 공통 에러 포맷 사용

```json
{
  "timestamp": "2026-07-31T09:00:00",
  "status": 404,
  "code": "BOOK_NOT_FOUND",
  "message": "도서를 찾을 수 없습니다.",
  "path": "/api/books/999"
}
```

- 목록 조회 페이징(Spring Data 방식): 요청 `?page=0&size=20&sort=createdAt,desc`, 응답 `{ content, page, size, totalElements, totalPages }`
- 상태 코드: 200(조회/수정), 201(생성), 204(삭제), 400(검증 실패), 401(미인증, v1.1부터 Cart/Order API에 적용), 404(리소스 없음), 409(충돌, 재고부족·포인트부족 등)

## 2. Book API

| Method | URL | 설명 | 성공 코드 |
| --- | --- | --- | --- |
| GET | /api/books | 목록 조회 (페이징, keyword/categoryId 필터) | 200 |
| GET | /api/books/{id} | 상세 조회 | 200 |
| POST | /api/books | 도서 등록 | 201 |
| PUT | /api/books/{id} | 도서 수정 | 200 |
| DELETE | /api/books/{id} | 도서 삭제 | 204 |

**GET /api/books?keyword=&categoryId=&page=&size=&sort=**

- Response: `{ content: [{ id, title, author, price, stock, categoryName }], page, size, totalElements, totalPages }`

**GET /api/books/{id}**

- Response: `{ id, title, author, categoryId, categoryName, price, stock, description, createdAt }`
- 에러: 404 `BOOK_NOT_FOUND`

**POST /api/books**

- Request: `{ "title": "string", "author": "string", "categoryId": 1, "price": 15000, "stock": 100, "description": "string" }`
- Response: 등록된 도서 상세(GET 상세와 동일 스키마), 201
- 에러: 400 `VALIDATION_ERROR`(필수값 누락/형식 오류), 404 `CATEGORY_NOT_FOUND`

**PUT /api/books/{id}**

- Request: POST와 동일 스키마(전체 필드 교체)
- Response: 수정된 도서 상세, 200
- 에러: 404 `BOOK_NOT_FOUND`, 404 `CATEGORY_NOT_FOUND`, 400 `VALIDATION_ERROR`

**DELETE /api/books/{id}**

- Response: 204 (본문 없음)
- 에러: 404 `BOOK_NOT_FOUND`
- 비고: 이미 주문된 도서도 삭제 가능. 관련 `OrderItem`은 스냅샷 필드로 이력을 유지한다(기능명세서 2.5 참조)

## 3. Cart API

전제(v1.1): 모든 요청은 **세션 인증된 로그인 회원**을 기준으로 처리된다. 로그인(`POST /api/members/login`)으로 발급된 세션 쿠키가 없으면 401 `UNAUTHORIZED`를 반환한다. (최초 MVP 전제였던 "고정 더미 회원(id=1), 인증 헤더 불필요"는 대체됨 — 더미 회원 `dummy`/`dummy1234`로 로그인하면 이전과 동일하게 동작한다)

| Method | URL | 설명 | 성공 코드 |
| --- | --- | --- | --- |
| POST | /api/cart/items | 장바구니에 도서 담기 | 201 |
| GET | /api/cart | 내 장바구니 조회 | 200 |
| PATCH | /api/cart/items/{itemId} | 수량 수정 | 200 |
| DELETE | /api/cart/items/{itemId} | 항목 삭제 | 204 |

**POST /api/cart/items**

- Request: `{ "bookId": 1, "quantity": 2 }`
- Response: `{ itemId, bookId, title, price, quantity, stock }`, 201
- 처리 규칙: 동일 `bookId`가 이미 장바구니에 있으면 **수량을 합산**하여 갱신(신규 행 생성 아님). 합산된 총수량이 현재 재고를 초과하면 실패(기능명세서 3.1 참조)
- 에러: 401 `UNAUTHORIZED`, 404 `BOOK_NOT_FOUND`, 409 `INSUFFICIENT_STOCK`

**GET /api/cart**

- Response: `{ items: [{ itemId, bookId, title, price, quantity, stock }], totalPrice }`
- 비고: `price`/`stock`은 항상 최신 도서 값(주문 전이므로 스냅샷 아님)
- 에러: 401 `UNAUTHORIZED`

**PATCH /api/cart/items/{itemId}**

- Request: `{ "quantity": 3 }`
- Response: `{ itemId, bookId, title, price, quantity, stock }`, 200
- 에러: 401 `UNAUTHORIZED`, 404 `CART_ITEM_NOT_FOUND`, 409 `INSUFFICIENT_STOCK`

**DELETE /api/cart/items/{itemId}**

- Response: 204
- 에러: 401 `UNAUTHORIZED`, 404 `CART_ITEM_NOT_FOUND`

## 4. Order API

전제(v1.1): 아래 세 엔드포인트는 **세션 인증된 로그인 회원**을 기준으로 동작한다(미인증 시 401 `UNAUTHORIZED`). Admin API(5절)는 이전과 동일하게 인증 없이 공개된다.

| Method | URL | 설명 | 성공 코드 |
| --- | --- | --- | --- |
| POST | /api/orders | 장바구니 기반 주문 생성 (재고 차감 + 포인트 차감) | 201 |
| GET | /api/orders | 내 주문 목록 조회 | 200 |
| GET | /api/orders/{id} | 내 주문 상세 조회 | 200 |
| POST | /api/orders/{id}/cancel | 주문 취소 (재고 복구 + 포인트 환급) | 200 |

**POST /api/orders**

- Request: 없음(현재 장바구니 전체를 주문으로 전환) 또는 `{ "cartItemIds": [1,2] }`(일부 주문 시)
- Response: `{ orderId, status: "주문완료", totalPrice, items: [{ bookId, title, price, quantity, subtotal }], orderedAt }`, 201
- 에러: 401 `UNAUTHORIZED`, 400 `INVALID_REQUEST`(대상 항목 없음), 409 `INSUFFICIENT_STOCK`(재고 부족, 트랜잭션 롤백), 409 `INSUFFICIENT_POINT`(포인트 부족, 트랜잭션 롤백, v1.1) — 부분 성공 없음
- 부수 효과: 성공 시 각 대상 도서의 재고가 차감되고, 회원의 포인트가 총액만큼 차감되며, 주문으로 전환된 장바구니 항목은 삭제된다(v1.1: 포인트 차감 추가)

**GET /api/orders?page=&size=&sort=**

- Response: `{ content: [{ orderId, status, totalPrice, orderedAt }], page, size, totalElements, totalPages }`
- 에러: 401 `UNAUTHORIZED`

**GET /api/orders/{id}**

- Response: POST 응답과 동일 상세 스키마
- 에러: 401 `UNAUTHORIZED`, 404 `ORDER_NOT_FOUND`

**POST /api/orders/{id}/cancel** (v1.1 신규)

- Request: 없음
- Response: 취소 후 상태(`status: "주문취소"`)가 반영된 주문 상세(POST /api/orders와 동일 스키마), 200
- 처리 규칙: 주문에 포함된 각 도서의 재고를 주문 수량만큼 복구하고(단, 이미 삭제된 도서는 제외), 회원 포인트에 `totalPrice`만큼 환급한다. 주문 단위 전체 취소만 지원(항목 단위 부분 취소 없음)
- 에러: 401 `UNAUTHORIZED`, 404 `ORDER_NOT_FOUND`(본인 주문이 아니거나 존재하지 않음), 409 `ORDER_ALREADY_CANCELED`(이미 취소된 주문)

## 5. Admin API

| Method | URL | 설명 | 성공 코드 |
| --- | --- | --- | --- |
| GET | /api/admin/orders | 전체 주문 목록 조회 (페이징, 회원 필터 없음) | 200 |
| GET | /api/admin/orders/{id} | 주문 상세 조회 (회원 필터 없음) | 200 |

- 응답 스키마는 Order API와 동일
- 비고: v1.1에서 Cart/Order API에 세션 인증이 도입된 이후에도 Admin API는 그대로 인증 없이 공개 상태를 유지한다. **엔드포인트를 별도로 유지**하여, 추후 관리자 권한 체계 도입 시 이 두 엔드포인트에만 ADMIN 권한 검사를 추가하면 되도록 하기 위함(기능명세서 4.3 참조)
- 에러: 404 `ORDER_NOT_FOUND`

## 6. 에러 코드 전체 목록

| code | status | 상황 |
| --- | --- | --- |
| BOOK_NOT_FOUND | 404 | 존재하지 않는 도서 id 조회/수정/삭제 |
| CATEGORY_NOT_FOUND | 404 | 존재하지 않는 categoryId로 도서 등록/수정 |
| CART_ITEM_NOT_FOUND | 404 | 존재하지 않는 장바구니 항목 수정/삭제 |
| ORDER_NOT_FOUND | 404 | 존재하지 않는 주문 조회/취소 |
| ORDER_ALREADY_CANCELED | 409 | 이미 취소된 주문을 재취소 시도(v1.1) |
| INSUFFICIENT_STOCK | 409 | 요청 수량이 현재 재고를 초과(장바구니 담기/수정/주문 생성) |
| INSUFFICIENT_POINT | 409 | 주문 생성 시 총액이 보유 포인트를 초과(v1.1) |
| DUPLICATE_LOGIN_ID | 409 | 이미 사용 중인 아이디로 회원가입 시도(v1.1) |
| INVALID_CREDENTIALS | 401 | 로그인 시 아이디 또는 비밀번호 불일치(v1.1) |
| UNAUTHORIZED | 401 | 세션 인증 없이 Cart/Order API 호출(v1.1) |
| INVALID_REQUEST | 400 | 주문 생성 시 대상 항목이 없는 등 비즈니스 규칙 위반 |
| VALIDATION_ERROR | 400 | 필수값 누락, 형식/범위 오류(Bean Validation 실패) |

## 7. 재고 초과 주문 방지 설계 (REQ-017 상세)

본 절은 기능명세서 5절의 내용을 API 계약과 향후 구현 설계 관점에서 구체화한다. **MVP 코드에는 7.1(기본 검증)만 반영되며, 7.2(동시성 강화)는 이번 라운드에는 설계만 진행하고 구현하지 않는다.**

### 7.1 기본 검증 (MVP 구현 범위)

3개 API(`POST /api/cart/items`, `PATCH /api/cart/items/{itemId}`, `POST /api/orders`) 모두 재고 초과 시 아래 형식으로 409를 반환한다:

```json
{
  "timestamp": "2026-07-31T09:00:00",
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "요청하신 수량이 재고를 초과합니다. (도서: 클린 코드, 요청 수량: 5, 가용 재고: 2)",
  "path": "/api/cart/items"
}
```

메시지에는 도서명, 요청 수량, 가용 재고를 포함하여 클라이언트가 구체적인 피드백을 사용자에게 보여줄 수 있도록 한다.

### 7.2 동시성 강화 설계 (차기 라운드 구현 예정)

**문제**: 기본 검증(단순 조회 후 비교)은 여러 요청이 동시에 같은 도서를 주문할 때 lost update로 인한 초과 판매를 막지 못한다(기능명세서 5.3 참조).

**설계 방안**:

1. **비관적 쓰기 락**: `OrderService.createOrder()` 트랜잭션 내에서, 재고 확인·차감 직전에 대상 `Book` row에 대해 `SELECT ... FOR UPDATE`(JPA: `BookRepository`에 `@Lock(LockModeType.PESSIMISTIC_WRITE)`를 적용한 조회 메서드, 예: `findByIdForUpdate(Long id)`)를 실행한다. 동일 도서에 대한 동시 트랜잭션은 락이 해제될 때까지 대기하므로, 검증과 차감이 사실상 순차적으로 이루어져 lost update가 방지된다.
2. **낙관적 락과의 트레이드오프**: `@Version` 기반 낙관적 락은 충돌 시 재시도가 필요하고 인기 도서일수록 재시도 빈도가 높아 사용자 경험이 나빠질 수 있다. 재고 차감처럼 충돌 가능성이 높은 케이스에는 비관적 락을 우선한다.
3. **데드락 회피**: 한 주문에 여러 도서가 포함된 경우, 잠글 `bookId` 목록을 **오름차순 정렬 후 그 순서대로 락을 순차 획득**한다(`cartItems.stream().map(CartItem::getBookId).distinct().sorted()...`). 모든 트랜잭션이 동일한 전역 순서로 락을 요청하도록 강제하여 순환 대기(교착 상태) 조건을 원천적으로 제거한다.
4. **최종 실패 시 에러 계약**: 락 획득 후에도 실제로는 재고가 부족한 것으로 판명되면 7.1과 동일한 `INSUFFICIENT_STOCK` 포맷으로 응답하고 트랜잭션을 롤백한다.

**시퀀스 (텍스트 서술)**

- *정상 흐름*: 요청 → 트랜잭션 시작 → 대상 Book row 락 획득(대기 없음) → 재고 확인 통과 → 차감 → Order/OrderItem 저장 → 커밋(락 해제) → 201 응답
- *경쟁 흐름*: 요청 A, B가 동시에 같은 책 주문 → A가 먼저 락 획득 및 커밋 완료 → B는 A의 커밋까지 대기 → 락 해제 후 B가 락 획득, 이미 감소한 최신 재고를 다시 읽음 → 재고 부족으로 판명되면 409 반환 및 롤백(반대로 재고가 충분하면 정상 처리)

**환경 제약 및 향후 검증 계획**

- H2 인메모리 DB도 `PESSIMISTIC_WRITE`/`FOR UPDATE`를 지원하지만, 운영 DB(PostgreSQL/MySQL 등) 대비 락 타임아웃·교착 상태 탐지 동작이 단순화되어 있을 수 있어 운영 전환 시 재검증이 필요하다.
- 단일 프로세스/단일 커넥션 기준 테스트로는 동시성 버그가 은폐되기 쉬우므로, 실제 구현 시 `ExecutorService` 등으로 동시 주문 요청을 시뮬레이션하는 멀티스레드 통합 테스트를 별도로 작성해야 한다.
- v1.1에서 세션 기반 인증이 도입되어 여러 회원이 각자 로그인해 실제로 동시에 주문할 수 있는 환경이 이미 갖춰졌으므로, 서로 다른 회원 간 동시 경쟁이 이제는 실질적으로 발생할 수 있다. 이 설계는 다음 라운드에서 구현 우선순위를 재검토한다.
