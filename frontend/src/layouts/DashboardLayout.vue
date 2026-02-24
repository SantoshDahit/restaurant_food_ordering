<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const menuOpen = ref(true)

const navItems = [
  { label: 'Dashboard', path: '/admin', icon: '📊' },
  { label: 'Restaurant', path: '/admin/restaurant', icon: '🏪' },
  { label: 'Tables', path: '/admin/tables', icon: '🪑' },
  { label: 'Orders', path: '/admin/orders', icon: '📋' },
  { label: 'Payments', path: '/admin/payments', icon: '💳' },
  { label: 'Employees', path: '/admin/employees', icon: '👥' },
  { label: 'Attendance', path: '/admin/attendance', icon: '📅' },
  { label: 'Payroll', path: '/admin/payroll', icon: '💰' },
  { label: 'Waiter Mode', path: '/admin/waiter', icon: '🧑‍🍳' },
]

const menuSubItems = [
  { label: 'Categories', path: '/admin/menu/categories' },
  { label: 'Items', path: '/admin/menu/items' },
]

function isActive(path: string) {
  return route.path === path || (path !== '/admin' && route.path.startsWith(path))
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen bg-gray-100">
    <!-- Sidebar -->
    <aside class="w-64 bg-gray-900 text-white flex flex-col flex-shrink-0">
      <div class="px-6 py-5 text-xl font-bold border-b border-gray-700">
        🍽️ RestaurantOS
      </div>
      <nav class="flex-1 px-3 py-4 overflow-y-auto space-y-1">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2 rounded text-sm transition-colors"
          :class="isActive(item.path) ? 'bg-gray-700 text-white' : 'text-gray-300 hover:bg-gray-800 hover:text-white'"
        >
          <span>{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>

        <!-- Menu collapsible -->
        <div>
          <button
            @click="menuOpen = !menuOpen"
            class="w-full flex items-center gap-3 px-3 py-2 rounded text-sm text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
            :class="(route.path.startsWith('/admin/menu')) ? 'bg-gray-700 text-white' : ''"
          >
            <span>🍴</span>
            <span class="flex-1 text-left">Menu</span>
            <span class="text-xs">{{ menuOpen ? '▾' : '▸' }}</span>
          </button>
          <div v-if="menuOpen" class="ml-6 mt-1 space-y-1">
            <router-link
              v-for="sub in menuSubItems"
              :key="sub.path"
              :to="sub.path"
              class="block px-3 py-1.5 rounded text-sm transition-colors"
              :class="isActive(sub.path) ? 'bg-gray-700 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'"
            >
              {{ sub.label }}
            </router-link>
          </div>
        </div>
      </nav>

      <div class="px-4 py-3 border-t border-gray-700 text-xs text-gray-400">
        {{ auth.user?.role }}
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header -->
      <header class="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between flex-shrink-0">
        <h1 class="text-lg font-semibold text-gray-800 capitalize">
          {{ route.name?.toString().replace(/-/g, ' ') }}
        </h1>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-600">{{ auth.user?.fullName }}</span>
          <button
            @click="logout"
            class="text-sm px-3 py-1.5 rounded bg-red-100 text-red-600 hover:bg-red-200 transition-colors"
          >
            Logout
          </button>
        </div>
      </header>

      <!-- Content -->
      <main class="flex-1 overflow-y-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
