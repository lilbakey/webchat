<template>
  <div class="p-8 max-w-7xl mx-auto">
    <div v-if="isLoading" class="flex flex-col items-center justify-center h-screen">
      <svg class="animate-spin h-10 w-10 text-[#ec3606]" xmlns="http://www.w3.org/2000/svg" fill="none"
           viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor"
              d="M4 12a8 8 0 018-8v8z"></path>
      </svg>
      <p class="mt-4 text-gray-600 text-lg">Загрузка чата...</p>
    </div>
    <div v-else>
      <div class="flex items-center justify-between w-full py-4 px-2 border-b border-gray-200">
        <button
            @click="$router.back()"
            class="bg-[#ec3606] text-white px-4 py-2 rounded hover:bg-[#ec572f] transition-transform transform hover:scale-105 shadow text-sm sm:text-base"
        >
          ← Назад
        </button>
        <div class="absolute left-1/2 transform -translate-x-1/2 text-center">
          <div v-if="isReceiverTyping" class="animate-pulse">
            <h1 class="text-xl sm:text-2xl font-semibold text-gray-700 italic">
              {{ receiver }} печатает...
            </h1>
          </div>
          <div v-else>
            <h1 class="text-2xl sm:text-3xl font-bold text-gray-800">{{ receiver }}</h1>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex flex-col items-end leading-tight">
            <div v-if="statusUser === 'Online'">
              <span class="text-green-500 text-sm sm:text-base font-medium">В сети</span>
            </div>
            <div v-else>
        <span class="text-red-400 text-sm sm:text-base font-medium">
          Был(а) в сети {{ convertLastSeen(receiverInfo.lastSeen) }}
        </span>
            </div>
          </div>
          <div v-if="!receiverInfo || !receiverInfo.photo">
            <CircleUserRound class="w-12 h-12 sm:w-16 sm:h-16 text-gray-600 group-hover:text-[#ec3606] transition"/>
          </div>
          <div v-else>
            <img
                :src="photoUrl"
                alt="User Photo"
                class="w-12 h-12 sm:w-16 sm:h-16 rounded-full object-cover border border-gray-300 shadow-sm"
            />
          </div>
        </div>
      </div>
      <div
          ref="messageContainer"
          class="relative bg-white rounded shadow p-4 mb-4 overflow-y-auto border border-gray-300
         h-[50vh] min-h-[300px] max-h-[80vh]
         sm:h-[45vh] md:h-[50vh] lg:h-[55vh] xl:h-[50vh]">
        <div v-for="(msg, index) in messages" :key="index" class="flex"
             :class="msg.sender === currentUser ? 'justify-end' : 'justify-start'">
          <div :class="[
          'mb-2 px-4 py-2 rounded-md border whitespace-pre-wrap break-words text-left',
          msg.sender === currentUser
            ? 'bg-blue-100 border-blue-300 text-gray-800'
            : 'bg-gray-200 border-gray-300 text-black'
        ]" style="max-width: 30%; word-break: break-word;"
               @contextmenu.prevent="showContextMenu($event, msg)">
            <div v-if="msg.attachmentName">
              <div v-if="isImage(msg.attachmentName)">
                <img v-if="msg.attachmentUrl" :src="msg.attachmentUrl" alt="image" @load="scrollToBottom"/>
              </div>
              <div v-else-if="isVideo(msg.attachmentName)">
                <video :src="msg.attachmentUrl" controls class="mt-2 max-w-xs rounded shadow"/>
              </div>
              <div v-else-if="isAudio(msg.attachmentName)">
                <audio :src="msg.attachmentUrl" controls/>
              </div>
              <div v-else>
                <a :href="getFileUrl(msg.attachmentId)" target="_blank" class="text-blue-500 underline mt-2 block">
                  Скачать файл: {{ msg.attachmentName }}
                </a>
              </div>
            </div>
            <div v-else>
              {{ msg.content }}
            </div>
            <div class="text-xs text-gray-500 mt-1">
              {{ formatTime(msg.timestamp) }}
            </div>
          </div>
        </div>
      </div>
      <transition name="fade">
        <ul v-if="contextMenuOpen"
            ref="menuRef"
            class="absolute bg-white rounded shadow-lg border border-gray-200 py-2 text-sm z-50"
            :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }"
        >
          <li>
            <button @click="deleteMessage"
                    class="block w-full px-4 py-2 hover:bg-[#ec0606] hover:text-white text-left text-red-600">
              Удалить сообщение
            </button>
          </li>
        </ul>
      </transition>
      <div class="flex items-center gap-2 mb-2">
        <input
            type="file"
            ref="fileInput"
            @change="handleFileChange"
            class="hidden"
        />
        <Paperclip
            @click="triggerFileSelect"
            class="cursor-pointer w-6 h-6 text-gray-700"
        />
        <textarea
            v-model="message"
            placeholder="Введите сообщение..."
            @input="handleTyping"
            ref="textareaRef"
            rows="1"
            @keydown="handleKeydown"
            class="flex-1 min-h-[2.5rem] border border-gray-300 rounded p-2 resize-none overflow-hidden box-border transition-[height] duration-150 ease-in-out"
        />
        <div class="relative flex-shrink-0" ref="emojiTriggerRef">
          <SmilePlus
              @click="toggleEmojiPicker"
              class="w-7 h-7 cursor-pointer"
          />
          <div
              v-if="showEmojiPicker"
              ref="emojiPickerRef"
              class="absolute bottom-12 right-0 z-50"
          >
            <emoji-picker @emoji-click="addEmoji"/>
          </div>
        </div>
        <button
            @click="sendMessage"
            class="bg-[#ec3606] text-white px-4 py-2 rounded hover:bg-[#ec572f] transition"
        >
          Отправить
        </button>
        <div v-if="uploadedFileUrl" class="text-sm">
          <p>Файл загружен!</p>
          <a
              :href="uploadedFileUrl"
              target="_blank"
              class="text-blue-500 underline"
          >
            Скачать
          </a>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import 'emoji-picker-element'
import {nextTick, onMounted, ref, onBeforeUnmount} from 'vue'
import {useRoute} from 'vue-router'
import SockJS from 'sockjs-client';
import {Client, Stomp} from '@stomp/stompjs'
import api from '@/api/api.js'
import {SmilePlus, Paperclip, CircleUserRound} from 'lucide-vue-next';
import axios from "axios";
import router from "@/router/index.js";

const route = useRoute()
const receiver = route.params.username
const receiverInfo = ref('')

const contextMenuOpen = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const selectedMessage = ref(null)
const menuRef = ref(null)
const showEmojiPicker = ref(false)
const stompClient = ref(null)
const messageContainer = ref(null)
const messages = ref([])
const message = ref('')
const currentUser = ref('')
const emojiPickerRef = ref(null)
const emojiTriggerRef = ref(null)
const textareaRef = ref(null)
const selectedFile = ref(null)
const uploadedFileUrl = ref(null)
const fileId = ref(null)
const fileInput = ref(null)
const photoUrl = ref("")
const statusUser = ref("")
const onlineUsers = ref(new Map())
const isLoading = ref(false)
const isReceiverTyping = ref(false)
let typingTimeout = false


function showContextMenu(event, msg = null) {
  event.preventDefault();

  selectedMessage.value = msg;
  contextMenuOpen.value = true;

  contextMenuX.value = event.clientX;
  contextMenuY.value = event.clientY;
}

function handleClickOutside(event) {
  if (event.button !== 0) return;
  if (menuRef.value && !menuRef.value.contains(event.target)) {
    contextMenuOpen.value = false;
  }
}


async function loadReceiverInfo(token) {
  try {
    const res = await api.get(`/api/users/username/${receiver}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    receiverInfo.value = res.data
  } catch (err) {
    console.error('Ошибка получения информации о пользователе', err)
  }
}

function getFileUrl(id) {
  return `${api.defaults.baseURL}/api/files/${id}`;
}

function isImage(name) {
  return /\.(jpe?g|png|gif|bmp|webp)$/i.test(name);
}

function isVideo(name) {
  return /\.(mp4|webm|ogg)$/i.test(name);
}

function isAudio(name) {
  return /\.(mp3|wav|ogg|flac)$/i.test(name);
}

function handleFileChange(event) {
  selectedFile.value = event.target.files[0]
  uploadFile()
}

function triggerFileSelect() {
  const input = fileInput.value
  if (input) {
    input.click()
  }
}

async function uploadFile() {
  if (!selectedFile.value) return

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const res = await api.post(`/api/files/upload`, formData)
    fileId.value = res.data.id
    uploadedFileUrl.value = `${api.defaults.baseURL}/api/files/${res.data.id}`
  } catch (err) {
    console.log("Error upload", err)
  }
}

function autoResize() {
  const textarea = textareaRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

function handleTyping() {
  if (stompClient.value && stompClient.value.connected) {
    stompClient.value.publish({
          destination: '/app/typing',
          body: JSON.stringify({
            username: currentUser.value,
            receiver: receiver,
            typing: true,
          })
        }
    )
  }
  clearTimeout(typingTimeout)
  typingTimeout = setTimeout(() => {
    if (stompClient.value && stompClient.value.connected) {
      stompClient.value.publish({
        destination: '/app/typing',
        body: JSON.stringify({
          username: currentUser.value,
          receiver: receiver,
          typing: false,
        })
      })
      isReceiverTyping.value = false
    }
  }, 1500)
}

function convertLastSeen(lastSeen) {
  const diffMs = Date.now() - new Date(lastSeen).getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHours = Math.floor(diffMin / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSec < 5) return "только что";
  if (diffSec < 60) return `${diffSec} сек. назад`;
  if (diffMin < 60) return `${diffMin} мин. назад`;
  if (diffHours < 24) return `${diffHours} ч. назад`;
  if (diffDays === 1) return "вчера";
  if (diffDays < 7) return `${diffDays} дн. назад`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)} нед. назад`;
  if (diffDays < 365) return `${Math.floor(diffDays / 30)} мес. назад`;
  return `${Math.floor(diffDays / 365)} г. назад`;
}

function formatTime(isoString) {
  const date = new Date(isoString)
  return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})
}


const deleteMessage = async () => {
  try {
    contextMenuOpen.value = false
    await api.delete(`/api/messages/${selectedMessage.value.id}`)
    messages.value = messages.value.filter(msg => msg.id !== selectedMessage.value.id)
  } catch (err) {
    console.error("Ошибка удаления сообщения!", err)
  }
}

function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value;
}

function addEmoji(event) {
  message.value += event.detail.unicode;
  showEmojiPicker.value = false
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageContainer.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
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

const loadPhoto = async (user) => {
  if (receiverInfo && receiverInfo.value.photo) {
    try {
      const res = await api.get(user.value.photo, {responseType: 'blob'})
      const url = URL.createObjectURL(res.data)
      photoUrl.value = url
    } catch (err) {
      console.error('Ошибка загрузки фото', err)
    }
  }
}

const loadMessageHistory = async () => {
  try {
    const res = await api.get(`/api/messages/${receiver}`)
    messages.value = await Promise.all(
        res.data.map(async (msg) => {
          if (msg.attachmentId && (isImage(msg.attachmentName) || isAudio(msg.attachmentName) || isVideo(msg.attachmentName))) {
            try {
              const fileRes = await api.get(`/api/files/${msg.attachmentId}`, {
                responseType: "blob",
              })
              const blobUrl = URL.createObjectURL(fileRes.data)
              return {...msg, attachmentUrl: blobUrl}
            } catch (e) {
              console.error("Ошибка загрузки вложения", e)
              return {...msg, attachmentUrl: null}
            }
          }
          return {...msg, attachmentUrl: null}
        })
    )
  } catch (err) {
    console.error("Ошибка загрузки истории", err)
  }
}

let heartBeatInterval = null

onMounted(async () => {

  isLoading.value = true

  if (Notification.permission !== "granted" && Notification.permission !== "denied") {
    await Notification.requestPermission()
  }
  document.addEventListener('mousedown', (e) => {
    if (e.button !== 0) return
    handleClickOutside(e)
  })
  document.addEventListener('mousedown', (e) => {
    if (!emojiPickerRef.value?.contains(e.target) &&
        !emojiTriggerRef.value?.contains(e.target)) {
      showEmojiPicker.value = false
    }
  })

  const token = localStorage.getItem('jwt')
  const payload = parseJwt(token)
  currentUser.value = payload?.sub || 'anonymous'

  await loadReceiverInfo(token)

  await loadMessageHistory()
  scrollToBottom()

  stompClient.value = new Client({
    webSocketFactory: () => new SockJS(`http://localhost:8080/ws?token=${token}`),
    // debug: (msg) => console.log("debug = " + msg),
    reconnectDelay: 5000,
    onConnect: async (frame) => {

      await subscribeToMessages()
      await subscribeToDeleteMessage()
      await subscribeTyping()
      await getOnlineUsers()
      await connectToChat()
      heartBeatInterval = setInterval(sendPing, 1000)

    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
    },
  })
  isLoading.value = false
  stompClient.value.activate()
  await nextTick(() => {
    autoResize()
  })
  await loadPhoto(receiverInfo)
  scrollToBottom()
})

async function subscribeToMessages() {
  stompClient.value.subscribe('/user/' + currentUser.value + "/queue/messages", async (msg) => {
    const messageBody = JSON.parse(msg.body);

    if (messageBody.attachmentId && isImage(messageBody.attachmentName)) {
      try {
        const fileRes = await api.get(`/api/files/${messageBody.attachmentId}`, {responseType: 'blob'})
        const blobUrl = URL.createObjectURL(fileRes.data)
        messageBody.attachmentUrl = blobUrl
      } catch (err) {
        console.error("Ошибка загрузки изображения", err)
        messageBody.attachmentUrl = null
      }
    }

    messages.value = [...messages.value, messageBody];
    scrollToBottom()

    if (document.visibilityState !== 'visible' && Notification.permission === 'granted') {
      new Notification(`Новое сообщение от ${messageBody.sender}`, {
        body: messageBody.content,
        icon: '/loginicon.ico'
      })
    }
  })
}

async function subscribeToDeleteMessage() {
  stompClient.value.subscribe('/user/' + currentUser.value + '/queue/delete-message', (frame) => {
    const body = JSON.parse(frame.body)
    messages.value = messages.value.filter(m => m.id !== body.id)
  })
}

async function subscribeTyping() {
  stompClient.value.subscribe('/user/' + currentUser.value + "/queue/typing", (msg) => {
        const body = JSON.parse(msg.body);
        if (body.username === receiver) {
          isReceiverTyping.value = body.typing
        }
      }
  )
}

async function getOnlineUsers() {
  stompClient.value.subscribe(`/user/${currentUser.value}/queue/online-users`, (msg) => {
    const body = JSON.parse(msg.body)
    if (body.type === "online-list" && body.users) {
      onlineUsers.value = new Map(Object.entries(body.users))
      statusUser.value = onlineUsers.value.has(receiver) ? "Online" : "Offline"
    }
  })
}

async function connectToChat() {
  stompClient.value.subscribe('/topic/public', (msg) => {
    const body = JSON.parse(msg.body)

    if (body.type === 'connect') {
      onlineUsers.value.set(body.username, body.time)
    } else if (body.type === 'disconnect') {
      onlineUsers.value.delete(body.username)
      if (body.username === receiver) {
        const token = localStorage.getItem('jwt')
        loadReceiverInfo(token)
      }
    }

    statusUser.value = onlineUsers.value.has(receiver) ? 'Online' : 'Offline'

  })

  stompClient.value.publish({
    destination: '/app/connect',
    body: currentUser.value,
  })

}


function sendPing() {
  if (stompClient.value && stompClient.value.connected) {
    stompClient.value.publish({
      destination: '/app/update-ping',
      body: currentUser.value
    })
  }
}


onBeforeUnmount(() => {
  if (heartBeatInterval) clearInterval(heartBeatInterval)
  disconnectFromChat()
})

function disconnectFromChat() {
  if (stompClient.value && stompClient.value.connected) {
    stompClient.value.publish({
      destination: '/app/disconnect',
      body: currentUser.value,
    })
    stompClient.value.deactivate();
  }
}

function handleKeydown(event) {
  if (event.key === "Enter") {
    if (event.shiftKey) {
      message.value += '\n'
      event.preventDefault()
      nextTick(() => autoResize())
    } else {
      event.preventDefault()
      sendMessage()
    }
  }
}

async function sendMessage() {

  if (!message.value.trim() && !fileId.value) return;

  const tempUrl = selectedFile.value ? URL.createObjectURL(selectedFile.value) : null

  const msg = {
    sender: currentUser.value,
    receiver: receiver,
    content: message.value,
    fileId: fileId.value,
    attachmentId: fileId.value,
    attachmentName: selectedFile.value?.name || null,
    attachmentUrl: tempUrl,
    timestamp: new Date().toISOString()
  }
  console.log("msg = ", msg)
  stompClient.value.publish({
    destination: '/app/chat',
    body: JSON.stringify(msg),
  })

  isReceiverTyping.value = false

  await nextTick();
  scrollToBottom();

  message.value = ''
  fileId.value = null
  selectedFile.value = null
  uploadedFileUrl.value = null

  await nextTick(() => {
    const textarea = textareaRef.value
    if (textarea) textarea.style.height = 'auto'
  })
}

</script>
