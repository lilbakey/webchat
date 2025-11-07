<template>
  <div class="p-8 max-w-3xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <button @click="$router.back()"
              class="bg-[#ec3606] text-white px-4 py-2 rounded hover:bg-[#ec572f] transition-transform transform hover:scale-105 shadow">
        ← Назад
      </button>
      <h1 class="text-3xl font-bold text-center flex-1">Профиль пользователя</h1>
    </div>
    <div class="flex items-center justify-center mb-6">
      <label class="relative cursor-pointer group">
        <div v-if="!user || !user.photo">
          <CircleUserRound class="w-32 h-32 text-gray-600 group-hover:text-[#ec3606] transition"/>
        </div>
        <div v-else>
          <img :src="photoUrl" alt="UserPhoto"
               class="w-32 h-32 rounded-full object-cover"/>
        </div>
        <input type="file" accept="image/*" @change="uploadPhoto" class="absolute inset-0 opacity-0 cursor-pointer"/>
      </label>
      <div v-if="photoUrl != null">
        <div @click="deletePhoto" class="relative cursor-pointer translate-y-[-50px]">
          <SquareX class="text-[#ec3606]"/>
        </div>
      </div>
    </div>
    <form autocomplete="off">
      <div v-if="user" class="bg-white p-6 rounded shadow border border-gray-300">
        <p class="font-bold my-2">Логин</p>
        <input
            class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
            type="text" name="username" required v-model="user.username" autocomplete="off" placeholder="Имя">
        <p class="font-bold my-2">Email</p>
        <input
            class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
            type="text" name="email" required v-model="user.email" autocomplete="off" placeholder="Email">
        <p class="font-bold my-2">Роль</p>
        <div v-if="user.role === 'ROLE_USER'">
          <input
              class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              type="text" name="username" required value="Пользователь" readonly autocomplete="off" placeholder="Email">
        </div>
        <div v-else>
          <input
              class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              type="text" name="username" required value="Администратор" readonly autocomplete="off"
              placeholder="Email">
        </div>
      </div>
      <span class="m-5"></span>

      <div v-if="user" class="bg-white p-6 rounded shadow border border-gray-300">
        <p class="text-xl text-center">Изменение пароля</p>
        <span class="font-bold">Текущий пароль</span>
        <div class="relative my-2">
          <input
              class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              :type="showPasswordOld ? 'text' : 'password'" name="password" required autocomplete="new-password"
              placeholder="Пароль"/>
          <button type="button"
                  class="w-8 h-auto absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer text-[#3c3c3d]"
                  @click="togglePasswordOld">
            <component :is="showPasswordOld ? EyeOff : Eye"/>
          </button>
        </div>
        <span class="font-bold">Новый пароль</span>
        <div class="relative my-2">
          <input
              class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              :type="showPasswordNew ? 'text' : 'password'" name="password" required placeholder="Введите новый пароль"
              autocomplete="new-password"/>
          <button type="button"
                  class="w-8 h-auto absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer text-[#3c3c3d]"
                  @click="togglePasswordNew">
            <component :is="showPasswordNew ? EyeOff : Eye"/>
          </button>
        </div>
        <span class="font-bold">Подтверждение пароля</span>
        <div class="relative my-2">
          <input v-model="newPassword"
                 class="w-full pl-2 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
                 :type="showPasswordAccept ? 'text' : 'password'" name="password" required autocomplete="new-password"
                 placeholder="Подтвердите новый пароль"/>
          <button type="button"
                  class="w-8 h-auto absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer text-[#3c3c3d]"
                  @click="togglePasswordAccept">
            <component :is="showPasswordAccept ? EyeOff : Eye"/>
          </button>
        </div>
      </div>
      <div v-else class="text-center text-gray-500">Загрузка профиля...</div>
    </form>
    <button @click="updateUser(user)"
            class="mt-5 w-full text-white bg-[#ec3606] hover:bg-[#ec572f] font-medium py-2 px-4 rounded transition-transform transform hover:scale-105">
      Сохранить изменения
    </button>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import api from '@/api/api.js'
import {Eye, EyeOff, CircleUserRound, SquareX} from 'lucide-vue-next'
import router from "@/router/index.js";

const user = ref(null)
const newUser = ref(null)
const currentUser = ref("")
const showPasswordOld = ref(false)
const showPasswordNew = ref(false)
const showPasswordAccept = ref(false)
const newPassword = ref("")
const photoUrl = ref(null)
const photo = ref(null)


const deletePhoto = async () => {
  try {
    if (photo.value != null) {
      await api.delete(`${photo.value}`)
      user.photo = null
      photoUrl.value = null
      photo.value = null
      window.location.reload()
    }
  } catch (err) {
    console.error("Ошибка удаления фото: ", err)
  }
}

const updateUser = async (user) => {
  try {
    console.log(user)
    newUser.value = user
    newUser.value.real = newPassword.value
    console.log("newUser = ", newUser.value)
    const res = await api.put(`/api/users/${user.id}`, newUser.value)
    console.log("res", res)
  } catch (err) {
    console.log("Ошибка обновления данных", err)
  }
}

const uploadPhoto = async (event) => {
  const file = event.target.files[0]
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  const token = localStorage.getItem('jwt')
  const payload = parseJwt(token)
  const username = payload?.id

  try {
    const res = await api.post(`/api/users/${username}/photo`, formData)
    window.location.reload()
  } catch (err) {
    console.error("Ошибка загрузки фото", err)
  }
}

const loadPhoto = async (filename) => {
  try {
    const res = await api.get(`${filename}`, {responseType: 'blob'})
    const url = URL.createObjectURL(res.data)
    photoUrl.value = url
    photo.value = filename
  } catch (err) {
    console.error('Ошибка загрузки фото', err)
  }
}

function togglePasswordOld() {
  showPasswordOld.value = !showPasswordOld.value;
}

function togglePasswordNew() {
  showPasswordNew.value = !showPasswordNew.value;
}

function togglePasswordAccept() {
  showPasswordAccept.value = !showPasswordAccept.value;
}

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

onMounted(async () => {
  try {
    const token = localStorage.getItem('jwt')
    const payload = parseJwt(token)
    const userId = payload?.id

    if (!userId) throw new Error('Не удалось получить имя пользователя из JWT')

    currentUser.value = userId
    const response = await api.get(`/api/users/id/${userId}`)
    user.value = response.data
    if (response.data?.photo != null) await loadPhoto(response.data?.photo)
  } catch (err) {
    console.error('Ошибка загрузки данных пользователя', err)
  }
})


</script>
