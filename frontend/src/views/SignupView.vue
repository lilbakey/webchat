<template>
  <div class="h-screen overflow-hidden">
    <div class="flex flex-col items-center">
      <p class="text-[#ec3606] hover:text-[#ec572f] font-medium" v-if="errors.username">
        {{ errors.username }}</p>
      <p class="text-[#ec3606] hover:text-[#ec572f] font-medium" v-if="errors.email">{{ errors.email }}</p>
      <p class="text-[#ec3606] hover:text-[#ec572f] font-medium" v-if="errors.password">{{ errors.password }}</p>
    </div>
    <section class="min-h-screen flex items-center justify-center">
      <div class="w-full max-w-md p-4 bg-white rounded-lg shadow-lg">
        <form @submit.prevent="registration" autocomplete="off">
          <h1 class="text-2xl text-center">Регистрация</h1>
          <h1 class="m-5 text-center text-xl"></h1>
          <div class="relative">
            <UserRound class="absolute inset-y-2 left-3 flex items-center pointer-events-none text-[#3c3c3d]" />
            <input
              class="w-full pl-10 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              type="text" name="username" required v-model="username" autocomplete="off" placeholder="Имя" />
          </div>
          <span class="m-5"></span>
          <div class="relative">
            <Mail class="absolute inset-y-2 left-3 flex items-center pointer-events-none text-[#3c3c3d]" />
            <input
              class="w-full pl-10 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              type="text" name="username" required v-model="email" autocomplete="off" placeholder="Email" />
          </div>
          <span class="m-5"></span>
          <div class="relative">
            <LockKeyhole class="absolute inset-y-2 left-3 flex items-center pointer-events-none text-[#3c3c3d]" />
            <input
              class="w-full pl-10 pr-3 py-2 border border-[#3c3c3d] focus:border-[#ec572f] rounded-md bg-white text-[#3c3c3d] outline-none transition-colors duration-200"
              :type="showPassword ? 'text' : 'password'" name="password" required v-model="password"
              autocomplete="new-password" placeholder="Пароль" />
            <button type="button"
              class="w-8 h-auto absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer text-[#3c3c3d]"
              @click="togglePassword">
              <component :is="showPassword ? EyeOff : Eye" />
            </button>
          </div>
          <span class="m-5"></span>
          <button
            class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-white bg-[#ec3606] hover:bg-[#ec572f] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#ec572f] transition-transform transition-colors duration-300 ease-in-out transform hover:scale-105 whitespace-nowrap cursor-pointer">
            Регистрация
          </button>
          <span class="m-5"></span>
          <div class="text-center">
            <p>Есть аккаунт? <a class="text-[#ec3606] hover:text-[#ec572f] font-medium" href="/signin"> Вход</a></p>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { UserRound, Mail, LockKeyhole, Eye, EyeOff } from 'lucide-vue-next';
import { ref } from 'vue';
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'
import { signUp } from '@/api/auth';
import router from '@/router';

const showPassword = ref(false)
const schema = yup.object({
  username: yup.string().required('Имя обязательно').min(5).max(25),
  email: yup.string().required('Электронная почта обязательна').email('Неверная электронная почта'),
  password: yup.string().required('Пароль обязателен').min(8).max(255)
})

const { handleSubmit, errors } = useForm({
  validationSchema: schema,
})

const { value: username } = useField('username')
const { value: email } = useField('email')
const { value: password } = useField('password')

const registration = handleSubmit(async (values) => {
  try {
    await signUp(values)
    alert('Регистрация прошла успешно. Не забудьте зайти на почту, чтобы подтвердить email!');
    await router.push("/signin")
  } catch (err) {
    if (err.response && err.response.data) {
      const serverErrors = err.response.data
      Object.entries(serverErrors).forEach(([field, message]) => {
        alert(`${field}: ${message}`)
      })
    } else {
      alert('Произошла ошибка регистрации')
    }
  }
})
function togglePassword() {
  showPassword.value = !showPassword.value;
}

</script>