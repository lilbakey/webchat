<script setup>
import {computed} from 'vue'
import {RouterView, useRoute} from 'vue-router'
import Navbar from '../components/Navbar.vue'

const route = useRoute()
const hideOnRoutes = ['/signin', '/', '/signup', '/denied']
const showNavBar = computed(() => !hideOnRoutes.includes(route.path))


</script>

<template>
  <Navbar v-if="showNavBar"/>
  <RouterView/>
</template>

<style></style>


