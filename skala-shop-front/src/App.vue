<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <nav>
      <RouterLink to="/">도서</RouterLink>
      <RouterLink to="/cart">장바구니</RouterLink>
      <RouterLink to="/orders">주문 내역</RouterLink>
    </nav>
  </header>

  <RouterView />
</template>

<style scoped>
nav {
  width: 100%;
  font-size: 14px;
  padding: 1rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
  font-weight: bold;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}
</style>
