<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  LayoutDashboard, Store, Armchair, ClipboardList, CreditCard,
  Users, CalendarCheck, Wallet, ChefHat, UtensilsCrossed,
  ListTree, Pizza, ChevronDown, Menu as MenuIcon, X, LogOut,
  BellRing,
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
  { label: 'Kitchen',     path: '/dashboard/kitchen',    icon: ChefHat },
  { label: 'Pickup Board', path: '/dashboard/pickup-board', icon: BellRing },
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
  <div class="flex h-screen bg-background">
    <!-- Backdrop for mobile sidebar -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-30 bg-foreground/40 backdrop-blur-sm md:hidden"
      @click="sidebarOpen = false"
      aria-hidden="true"
    />

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 w-64 bg-sage text-sage-foreground flex flex-col transform transition-transform duration-200 ease-out',
        'md:static md:translate-x-0 md:flex-shrink-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
    >
      <!-- Brand -->
      <div class="px-5 py-5 flex items-center justify-between border-b border-primary-foreground/10">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-primary-foreground/10 flex items-center justify-center shadow-soft">
            <UtensilsCrossed class="w-5 h-5 text-primary-foreground" />
          </div>
          <div>
            <div class="text-base font-semibold leading-tight">RestaurantOS</div>
            <div class="text-[10px] text-primary-foreground/60 uppercase tracking-widest">Manager</div>
          </div>
        </div>
        <button
          @click="sidebarOpen = false"
          class="md:hidden p-1.5 rounded-lg hover:bg-primary-foreground/10 text-primary-foreground/60 hover:text-primary-foreground transition-colors"
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
              ? 'bg-accent text-primary shadow-soft'
              : 'text-primary-foreground/80 hover:bg-primary-foreground/5 hover:text-primary-foreground'
          ]"
        >
          <span
            v-if="isActive(item.path)"
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 rounded-r-full bg-accent"
            aria-hidden="true"
          />
          <component :is="item.icon" class="w-[18px] h-[18px] flex-shrink-0"
            :class="isActive(item.path) ? 'text-primary' : 'text-primary-foreground/60 group-hover:text-primary-foreground/80'" />
          <span>{{ item.label }}</span>
        </router-link>

        <!-- Menu collapsible -->
        <div class="pt-1">
          <button
            @click="menuOpen = !menuOpen"
            :class="[
              'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
              route.path.startsWith('/dashboard/menu')
                ? 'bg-accent text-primary'
                : 'text-primary-foreground/80 hover:bg-primary-foreground/5 hover:text-primary-foreground'
            ]"
          >
            <UtensilsCrossed class="w-[18px] h-[18px] flex-shrink-0"
              :class="route.path.startsWith('/dashboard/menu') ? 'text-primary' : 'text-primary-foreground/60'" />
            <span class="flex-1 text-left">Menu</span>
            <ChevronDown class="w-4 h-4 text-primary-foreground/60 transition-transform"
              :class="menuOpen ? 'rotate-0' : '-rotate-90'" />
          </button>
          <div v-if="menuOpen" class="ml-3 mt-1 pl-3 border-l border-primary-foreground/10 space-y-0.5">
            <router-link
              v-for="sub in menuSubItems"
              :key="sub.path"
              :to="sub.path"
              :class="[
                'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-colors',
                isActive(sub.path) ? 'text-primary bg-accent' : 'text-primary-foreground/60 hover:text-primary-foreground hover:bg-primary-foreground/5'
              ]"
            >
              <component :is="sub.icon" class="w-4 h-4" />
              <span>{{ sub.label }}</span>
            </router-link>
          </div>
        </div>
      </nav>

      <!-- User card -->
      <div class="border-t border-primary-foreground/10 p-3">
        <div class="flex items-center gap-2.5 px-2 py-2">
          <div class="w-9 h-9 rounded-full bg-accent text-primary text-sm font-semibold flex items-center justify-center shadow-soft flex-shrink-0">
            {{ userInitials }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="text-sm font-medium text-primary-foreground truncate">{{ auth.user?.fullName ?? '—' }}</div>
            <div class="text-[11px] text-primary-foreground/60 uppercase tracking-wide">{{ auth.user?.role ?? '' }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <!-- Header -->
      <header class="bg-card/80 backdrop-blur border-b border-border px-3 sm:px-6 py-3 flex items-center justify-between flex-shrink-0 gap-2">
        <div class="flex items-center gap-2 min-w-0">
          <button
            @click="sidebarOpen = true"
            class="md:hidden p-2 -ml-1 rounded-lg hover:bg-accent text-foreground transition-colors flex-shrink-0"
            aria-label="Open menu"
          >
            <MenuIcon class="w-5 h-5" />
          </button>
          <h1 class="text-base sm:text-lg font-semibold text-foreground capitalize truncate">
            {{ pageTitle }}
          </h1>
        </div>
        <div class="flex items-center gap-2 sm:gap-3 flex-shrink-0">
          <div class="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-muted ring-1 ring-border">
            <div class="w-7 h-7 rounded-full bg-primary text-primary-foreground text-xs font-semibold flex items-center justify-center">
              {{ userInitials }}
            </div>
            <span class="text-sm text-foreground truncate max-w-[10rem]">{{ auth.user?.fullName }}</span>
          </div>
          <button
            @click="logout"
            class="inline-flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg bg-destructive/10 text-destructive hover:bg-destructive/20 ring-1 ring-destructive/20 transition-colors"
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
