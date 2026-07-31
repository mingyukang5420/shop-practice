# shop-practice

SKALA 실습 — 도서 쇼핑몰 백엔드 REST API(Spring Boot) + 프론트엔드(Vue) 실습 프로젝트.

## 프로젝트 구성

- `skala-shop/` — Spring Boot 백엔드 (Gradle, base URL `/api`, 기본 포트 8080)
- `skala-shop-front/` — Vue 3 + TypeScript 프론트엔드 (Vite, Vue Router, Pinia)
- `docs/` — 요구사항명세서/기능명세서/API명세서/작업순서 등 설계 문서

## MVP 스코프 요약

- **도메인**: Book(도서) · Category(카테고리) · Member(회원) · CartItem(장바구니) · Order/OrderItem(주문)
- **인증**: 세션(HttpSession) 기반 로그인. `POST /api/members/login` 성공 시 `JSESSIONID` 쿠키가 발급되며, 이후 Cart/Order API 요청에 이 쿠키가 필요하다(JWT 아님, OAuth·권한 구분은 이번 라운드 범위 밖).
- **포인트(예치금)**: 회원가입 시 1,000,000 지급. 주문 생성 시 총액만큼 차감, 주문 취소 시 환급.
- **재고 관리**: 장바구니 담기/수정/주문 생성 3단계에서 재고를 검증하며, 주문 취소 시 재고를 복구한다. 동시 주문 시 재고 초과 방지를 위한 비관적 락 설계는 `docs/API명세서.md` 7.2절에 설계만 되어 있고 이번 라운드에는 구현하지 않는다.
- **Admin API**: `/api/admin/orders`는 회원 필터 없이 전체 주문을 조회하며, 세션 인증 없이 공개된다(관리자 권한 체계는 향후 과제).
- **범위 밖**: JWT, Docker, Google OAuth, 관리자 프론트엔드 — 자세한 배경은 `docs/요구사항명세서.md` 참고.

## 백엔드 실행

```bash
cd skala-shop
./gradlew bootRun
```

- H2 인메모리 DB를 사용하므로 서버를 재시작하면 데이터는 `data.sql` 시딩 상태로 초기화된다.
- 더미 회원 계정: `dummy` / `dummy1234` (초기 포인트 1,000,000)

### H2 콘솔

`http://localhost:8080/h2-console` 접속 후 JDBC URL은 `skala-shop/src/main/resources/application.yml`의 `spring.datasource.url` 값을 그대로 사용한다(사용자 `sa`, 비밀번호 없음).

### Swagger UI

`http://localhost:8080/swagger-ui/index.html` (OpenAPI 스펙 JSON은 `/v3/api-docs`). Cart/Order API는 세션 인증이 필요하다는 점과 각 엔드포인트의 에러코드별 응답 예시가 함께 표시된다.

### 로그인 → 장바구니 → 주문 → 취소 흐름 (curl 예시)

세션 쿠키를 파일에 저장하고(`-c`) 이후 요청에 함께 전달한다(`-b`).

```bash
# 1) 로그인 (세션 쿠키 저장)
curl -c cookies.txt -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"dummy","password":"dummy1234"}'

# 2) 장바구니에 담기 (쿠키 전달)
curl -b cookies.txt -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"quantity":2}'

# 3) 주문 생성 (재고 차감 + 포인트 차감)
curl -b cookies.txt -X POST http://localhost:8080/api/orders

# 4) 주문 취소 (재고 복구 + 포인트 환급, 실제 orderId로 교체)
curl -b cookies.txt -X POST http://localhost:8080/api/orders/1/cancel
```

더 다양한 happy path/에러 시나리오는 `skala-shop/requests.http`(VS Code REST Client 또는 IntelliJ HTTP Client 확장)를 참고한다.

## 테스트

```bash
cd skala-shop
./gradlew test
```

## 프론트엔드 실행

```bash
cd skala-shop-front
npm install
npm run dev
```

`/api`로 시작하는 요청은 Vite dev server 프록시를 통해 `http://localhost:8080`(백엔드)으로 전달된다(`vite.config.ts` 참고). 백엔드를 함께 실행하려면 위 백엔드 실행 절 참고.

## 설계 문서

- [요구사항명세서](docs/요구사항명세서.md) — MVP 범위 결정, REQ 목록, 도메인 용어
- [기능명세서](docs/기능명세서.md) — 기능별 입력/처리흐름/예외, 재고 초과 방지 상세 설계
- [API명세서](docs/API명세서.md) — 엔드포인트별 요청/응답 스키마, 에러코드 전체 목록
- [작업순서](docs/작업순서.md) — 실습 진행 체크리스트
