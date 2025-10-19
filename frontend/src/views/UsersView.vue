<template>
  <div class="p-6 max-w-6xl mx-auto">
    <h1 class="text-[#ec3606] text-5xl text-center font-bold mb-10">Пользователи</h1>

    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
      <div v-for="user in users" :key="user.id"
           class="bg-white shadow-lg rounded-xl p-6 border border-gray-200 hover:shadow-xl transition-shadow duration-300">
        <div class="flex items-center justify-between">
          <div class="mb-2">
            <p class="text-xl font-semibold text-[#ec3606] break-words">{{ user.username }}</p>
          </div>
          <div v-if="!user.photo">
            <CircleUserRound class="w-16 h-16 text-gray-600 group-hover:text-[#ec3606] transition"/>
          </div>
          <div v-else>
            <img :src="photoUrls[user.id]" alt="User Photo"
                 class="w-16 h-16 rounded-full object-cover"/>
          </div>
        </div>
        <div class="mb-1">
          <p class="text-sm text-gray-500 break-all"><strong>Email:</strong> {{ user.email }}</p>
        </div>
        <div class="mb-4">
          <div v-if="user.role === 'ROLE_USER'">
            <p class="text-sm text-gray-600"><strong>Роль: </strong>Пользователь</p>
          </div>
          <div v-else>
            <p class="text-sm text-gray-600"><strong>Роль: </strong>Администратор</p>
          </div>
        </div>

        <div>
          <button v-if="user.username !== currentUser" @click="openChatWithUser(user.username)"
                  class="w-full text-white bg-[#ec3606] hover:bg-[#ec572f] font-medium py-2 px-4 rounded transition-transform transform hover:scale-105">
            Написать
          </button>
          <div v-else class="text-center text-sm text-black-500 py-2 border border-gray-300 bg-gray-100 rounded">Это Вы!
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import api from '@/api/api.js'
import router from "@/router/index.js"
import {CircleUserRound} from 'lucide-vue-next'

const users = ref([])
const currentUser = ref('')
const photoUrls = reactive({})

function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
        atob(base64)
            .split('')
            .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (e) {
    router.push('/denied')
    return null
  }
}

const loadPhoto = async (user) => {
  try {
    const res = await api.get(user.photo, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    photoUrls[user.id] = url
  } catch (err) {
    console.error('Ошибка загрузки фото', err)
  }
}

const loadUsers = async () => {
  const res = await api.get('/api/users')
  users.value = res.data

  await Promise.all(
      users.value.map(async (user) => {
        if (user.photo) {
          await loadPhoto(user)
        }
      })
  )
}

const openChatWithUser = async (username) => {
  await router.push(`/chat/${username}`)
}

onMounted(async () => {
  const token = localStorage.getItem('jwt')
  const payload = parseJwt(token)
  currentUser.value = payload?.sub || ''
  await loadUsers()



})
</script>