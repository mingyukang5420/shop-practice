# shop-practice
Spring boot practice constructing backend hierarchy structure

## 프로젝트 구성

- `skala-shop/` — Spring Boot 백엔드 (Gradle, base URL `/api`, 기본 포트 8080)
- `skala-shop-front/` — Vue 3 + TypeScript 프론트엔드 (Vite, Vue Router, Pinia)

## 프론트엔드 실행

```bash
cd skala-shop-front
npm install
npm run dev
```

`/api`로 시작하는 요청은 Vite dev server 프록시를 통해 `http://localhost:8080`(백엔드)으로 전달된다(`vite.config.ts` 참고). 백엔드를 함께 실행하려면 `skala-shop/` 에서 `./gradlew bootRun`.
