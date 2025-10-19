<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-100">
    <div class="bg-white shadow-lg rounded-2xl p-8 text-center max-w-md">
      <h1 class="text-3xl font-bold text-red-600 mb-4">Доступ запрещён</h1>
      <p class="text-gray-600 mb-6">
        У вас нет прав для просмотра этой страницы.<br/>
        Пожалуйста, авторизуйтесь.
      </p>
      <router-link
          to="/signin"
          class="px-4 py-2 bg-[#ec3606] text-white rounded-xl hover:border-[#ec572f] transition"
      >
        Перейти к логину
      </router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
</style>
