<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  LayoutDashboard, Store, Users, ShieldCheck,
  Menu as MenuIcon, X, LogOut,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const sidebarOpen = ref(false)

const navItems = [
  { label: 'Overview',    path: '/admin',             icon: LayoutDashboard },
  { label: 'Restaurants', path: '/admin/restaurants', icon: Store },
  { label: 'Users',       path: '/admin/users',       icon: Users },
]

function isActive(path: string) {
  return route.path === path || (path !== '/admin' && route.path.startsWith(path))
}

const pageTitle = computed(() => {
  const name = route.name?.toString() ?? ''
  return name.replace(/^admin-/, '').replace(/-/g, ' ')
})

const userInitials = computed(() => {
  const name = auth.user?.fullName ?? ''
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map(s => s[0]?.toUpperCase() ?? '').join('') || '·'
})

function logout() {
  auth.logout()
  router.push('/login')
}

watch(() => route.path, () => { sidebarOpen.value = false })
</script>

<template>
  <div class="flex h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30">
    <!-- Mobile backdrop -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-30 bg-slate-900/40 backdrop-blur-sm md:hidden"
      @click="sidebarOpen = false"
      aria-hidden="true"
    />

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 w-64 bg-slate-950 text-slate-100 flex flex-col transform transition-transform duration-200 ease-out',
        'md:static md:translate-x-0 md:flex-shrink-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
    >
      <!-- Brand -->
      <div class="px-5 py-5 flex items-center justify-between border-b border-white/10">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center shadow-lg shadow-amber-500/30">
            <ShieldCheck class="w-5 h-5 text-white" />
          </div>
          <div>
            <div class="text-base font-semibold leading-tight">RestaurantOS</div>
            <div class="text-[10px] text-amber-300 uppercase tracking-widest">Platform Admin</div>
          </div>
        </div>
        <button
          @click="sidebarOpen = false"
          class="md:hidden p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-colors"
          aria-label="Close menu"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-4 overflow-y-auto space-y-0.5">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          :class="[
            'group relative flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all',
            isActive(item.path)
              ? 'bg-gradient-to-r from-amber-500/20 to-orange-500/10 text-white shadow-sm'
              : 'text-slate-300 hover:bg-white/5 hover:text-white'
          ]"
        >
          <span
            v-if="isActive(item.path)"
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 rounded-r-full bg-gradient-to-b from-amber-400 to-orange-500"
            aria-hidden="true"
          />
          <component :is="item.icon" class="w-[18px] h-[18px] flex-shrink-0"
            :class="isActive(item.path) ? 'text-amber-300' : 'text-slate-400 group-hover:text-slate-200'" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- User card -->
      <div class="border-t border-white/10 p-3">
        <div class="flex items-center gap-2.5 px-2 py-2">
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-white text-sm font-semibold flex items-center justify-center shadow-md flex-shrink-0">
            {{ userInitials }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="text-sm font-medium text-white truncate">{{ auth.user?.fullName ?? '—' }}</div>
            <div class="text-[11px] text-amber-300 uppercase tracking-wide">{{ auth.user?.role ?? '' }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <header class="bg-white/80 backdrop-blur border-b border-slate-200/60 px-3 sm:px-6 py-3 flex items-center justify-between flex-shrink-0 gap-2">
        <div class="flex items-center gap-2 min-w-0">
          <button
            @click="sidebarOpen = true"
            class="md:hidden p-2 -ml-1 rounded-lg hover:bg-slate-100 text-slate-700 transition-colors flex-shrink-0"
            aria-label="Open menu"
          >
            <MenuIcon class="w-5 h-5" />
          </button>
          <h1 class="text-base sm:text-lg font-semibold text-slate-800 capitalize truncate">
            {{ pageTitle }}
          </h1>
        </div>
        <div class="flex items-center gap-2 sm:gap-3 flex-shrink-0">
          <div class="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-amber-50 ring-1 ring-amber-200/60">
            <div class="w-7 h-7 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-white text-xs font-semibold flex items-center justify-center">
              {{ userInitials }}
            </div>
            <span class="text-sm text-amber-900 truncate max-w-[10rem]">{{ auth.user?.fullName }}</span>
          </div>
          <button
            @click="logout"
            class="inline-flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg bg-rose-50 text-rose-600 hover:bg-rose-100 ring-1 ring-rose-200/60 transition-colors"
          >
            <LogOut class="w-4 h-4" />
            <span class="hidden sm:inline">Logout</span>
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-3 sm:p-6">
        <router-view :key="route.fullPath" />
      </main>
    </div>
  </div>
</template>

<style scoped>
.page-enter-active,
.page-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
