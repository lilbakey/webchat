<template>
  <nav class="flex items-center justify-between bg-[#333333] shadow px-8 py-4 w-auto mx-auto">
    <div class="text-3xl font-bold text-[#ec572f]">Web Chat</div>

    <div class="relative" ref="menuRef">
      <div v-if="!user || !user.photo">
        <CircleUserRound @click="toggleMenu" alt="avatar"
                         class="w-14 h-14 rounded-full cursor-pointer border-2 border-[#ec572f]"/>
      </div>
      <div v-else>
        <img @click="toggleMenu" :src="`${photoUrl}`" alt="User photo"
             class="w-14 h-14 rounded-full object-cover cursor-pointer border-2 border-[#ec572f]"/>
      </div>
      <transition name="fade">
        <ul v-if="menuOpen"
            class="absolute right-0 mt-2 w-40 bg-white rounded shadow-lg border border-gray-200 py-2 text-sm z-50">
          <li>
            <button @click="goToAccount"
                    class="block w-full px-4 py-2 hover:bg-[#ec572f] hover:text-white text-left">
              Аккаунт
            </button>
          </li>
          <li>
            <button @click="goToUsers"
                    class="block w-full px-4 py-2 hover:bg-[#ec572f] hover:text-white text-left">
              Пользователи
            </button>
          </li>
          <li>
            <div class="m-1 border-t border-gray-200 mb-3"></div>
            <button @click="logout"
                    class="block w-full px-4 py-2 hover:bg-[#ec0606] hover:text-white text-left text-red-600">
              Выход
            </button>
          </li>
        </ul>
      </transition>
    </div>

  </nav>
</template>

<script setup>
import {ref, onMounted, onBeforeUnmount} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api/api.js'
import {CircleUserRound} from 'lucide-vue-next'


const router = useRouter()
const menuOpen = ref(false)
const menuRef = ref(null)
const user = ref(null)
const photoUrl = ref(null)
const photo = ref("")

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


function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function handleClickOutside(event) {
  if (menuRef.value && !menuRef.value.contains(event.target)) {
    menuOpen.value = false
  }
}

const loadPhoto = async (filename) => {
  try {
    photo.value = filename
    const res = await api.get(`${filename}`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    photoUrl.value = url
  } catch (err) {
    console.error('Ошибка загрузки фото', err)
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  const token = localStorage.getItem('jwt')
  const payload = parseJwt(token)
  const username = payload?.id
  const response = await api.get(`/api/users/id/${username}`)
  user.value = response.data
  photo.value = response.data?.photo
  if (photo.value != null) await loadPhoto(photo.value)

})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

function goToAccount() {
  const token = localStorage.getItem('jwt')
  const payload = parseJwt(token)
  const username = payload?.id
  router.push(`/users/${username}`)
  menuOpen.value = false
}

function goToUsers() {
  router.push('/users')
  menuOpen.value = false
}

function logout() {
  localStorage.removeItem('jwt')
  router.push('/signin')
  menuOpen.value = false
}
</script>