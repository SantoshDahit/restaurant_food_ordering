<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  LayoutDashboard, Store, Armchair, ClipboardList, CreditCard,
  Users, CalendarCheck, Wallet, ChefHat, UtensilsCrossed,
  ListTree, Pizza, ChevronDown, Menu as MenuIcon, X, LogOut,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const menuOpen = ref(true)
const sidebarOpen = ref(false)

const navItems = [
  { label: 'Dashboard',   path: '/dashboard',            icon: LayoutDashboard },
  { label: 'Restaurant',  path: '/dashboard/restaurant', icon: Store },
  { label: 'Tables',      path: '/dashboard/tables',     icon: Armchair },
  { label: 'Orders',      path: '/dashboard/orders',     icon: ClipboardList },
  { label: 'Payments',    path: '/dashboard/payments',   icon: CreditCard },
  { label: 'Employees',   path: '/dashboard/employees',  icon: Users },
  { label: 'Attendance',  path: '/dashboard/attendance', icon: CalendarCheck },
  { label: 'Payroll',     path: '/dashboard/payroll',    icon: Wallet },
  { label: 'Waiter Mode', path: '/dashboard/waiter',     icon: ChefHat },
]

const menuSubItems = [
  { label: 'Categories', path: '/dashboard/menu/categories', icon: ListTree },
  { label: 'Items',      path: '/dashboard/menu/items',      icon: Pizza },
]

function isActive(path: string) {
  return route.path === path || (path !== '/dashboard' && route.path.startsWith(path))
}

const pageTitle = computed(() =>
  (route.name?.toString() ?? '').replace(/-/g, ' ')
)

const userInitials = computed(() => {
  const name = auth.user?.fullName ?? ''
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map(s => s[0]?.toUpperCase() ?? '').join('') || '·'
})

function logout() {
  auth.logout()
  router.push('/login')
}

// Close sidebar when route changes on mobile
watch(() => route.path, () => { sidebarOpen.value = false })
</script>

<template>
  <div class="flex h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30">
    <!-- Backdrop for mobile sidebar -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-30 bg-slate-900/40 backdrop-blur-sm md:hidden"
      @click="sidebarOpen = false"
      aria-hidden="true"
    />

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 w-64 bg-slate-900 text-slate-100 flex flex-col transform transition-transform duration-200 ease-out',
        'md:static md:translate-x-0 md:flex-shrink-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
    >
      <!-- Brand -->
      <div class="px-5 py-5 flex items-center justify-between border-b border-white/10">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-400 to-fuchsia-500 flex items-center justify-center shadow-lg shadow-violet-500/30">
            <UtensilsCrossed class="w-5 h-5 text-white" />
          </div>
          <div>
            <div class="text-base font-semibold leading-tight">RestaurantOS</div>
            <div class="text-[10px] text-slate-400 uppercase tracking-widest">Manager</div>
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
              ? 'bg-gradient-to-r from-violet-500/20 to-fuchsia-500/10 text-white shadow-sm'
              : 'text-slate-300 hover:bg-white/5 hover:text-white'
          ]"
        >
          <span
            v-if="isActive(item.path)"
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 rounded-r-full bg-gradient-to-b from-violet-400 to-fuchsia-500"
            aria-hidden="true"
          />
          <component :is="item.icon" class="w-[18px] h-[18px] flex-shrink-0"
            :class="isActive(item.path) ? 'text-violet-400' : 'text-slate-400 group-hover:text-slate-200'" />
          <span>{{ item.label }}</span>
        </router-link>

        <!-- Menu collapsible -->
        <div class="pt-1">
          <button
            @click="menuOpen = !menuOpen"
            :class="[
              'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
              route.path.startsWith('/dashboard/menu')
                ? 'bg-gradient-to-r from-violet-500/20 to-fuchsia-500/10 text-white'
                : 'text-slate-300 hover:bg-white/5 hover:text-white'
            ]"
          >
            <UtensilsCrossed class="w-[18px] h-[18px] flex-shrink-0"
              :class="route.path.startsWith('/dashboard/menu') ? 'text-violet-400' : 'text-slate-400'" />
            <span class="flex-1 text-left">Menu</span>
            <ChevronDown class="w-4 h-4 text-slate-400 transition-transform"
              :class="menuOpen ? 'rotate-0' : '-rotate-90'" />
          </button>
          <div v-if="menuOpen" class="ml-3 mt-1 pl-3 border-l border-white/10 space-y-0.5">
            <router-link
              v-for="sub in menuSubItems"
              :key="sub.path"
              :to="sub.path"
              :class="[
                'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-colors',
                isActive(sub.path) ? 'text-white bg-white/5' : 'text-slate-400 hover:text-white hover:bg-white/5'
              ]"
            >
              <component :is="sub.icon" class="w-4 h-4" />
              <span>{{ sub.label }}</span>
            </router-link>
          </div>
        </div>
      </nav>

      <!-- User card -->
      <div class="border-t border-white/10 p-3">
        <div class="flex items-center gap-2.5 px-2 py-2">
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-violet-400 to-fuchsia-500 text-white text-sm font-semibold flex items-center justify-center shadow-md flex-shrink-0">
            {{ userInitials }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="text-sm font-medium text-white truncate">{{ auth.user?.fullName ?? '—' }}</div>
            <div class="text-[11px] text-slate-400 uppercase tracking-wide">{{ auth.user?.role ?? '' }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <!-- Header -->
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
          <div class="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-slate-100/70 ring-1 ring-slate-200/60">
            <div class="w-7 h-7 rounded-full bg-gradient-to-br from-violet-400 to-fuchsia-500 text-white text-xs font-semibold flex items-center justify-center">
              {{ userInitials }}
            </div>
            <span class="text-sm text-slate-700 truncate max-w-[10rem]">{{ auth.user?.fullName }}</span>
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

      <!-- Content -->
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
