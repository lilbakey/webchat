import {createRouter, createWebHistory} from 'vue-router'
import SigninView from '../views/SigninView.vue'
import SignupView from '../views/SignupView.vue'
import UsersView from '../views/UsersView.vue'
import ChatView from '../views/ChatView.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import AccessDenied from "@/views/AccessDenied.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'registration',
            component: SignupView,
        },
        {
            path: '/signin',
            name: 'signin',
            component: SigninView
        },
        {
            path: '/users',
            name: 'users',
            component: UsersView
        },
        {
            path: '/chat/:username',
            name: 'chat',
            component: ChatView
        },
        {
            path: '/users/:username',
            name: 'UserProfile',
            component: UserProfileView
        },
        {
            path: '/denied',
            name: 'AccessDenied',
            component: AccessDenied
        }
    ],
})

export default router
