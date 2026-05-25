<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { toast } from 'vue-sonner'
import PageHeader from '@/components/shared/PageHeader.vue'
import {
  Store, Users, ShoppingBag, TrendingUp, ShieldCheck,
  CheckCircle2, ChefHat, UserCircle,
} from 'lucide-vue-next'
import type { Component } from 'vue'
import type { PlatformStats } from '@/types'

const stats = ref<PlatformStats | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    stats.value = await adminApi.stats()
  } catch {
    toast.error('Failed to load platform stats')
  } finally {
    loading.value = false
  }
})

function fmt(n: number | undefined): string {
  return (n ?? 0).toLocaleString()
}

function fmtMoney(n: number | undefined): string {
  return 'NPR ' + (n ?? 0).toLocaleString(undefined, { maximumFractionDigits: 0 })
}

interface Tile {
  label: string
  value: string
  sub?: string
  icon: Component
  gradient: string
}

const tiles = (): Tile[] => stats.value ? [
  { label: 'Restaurants',     value: fmt(stats.value.activeRestaurants), sub: `${fmt(stats.value.totalRestaurants)} total`,           icon: Store,        gradient: 'from-amber-500 to-orange-500' },
  { label: 'Managers',        value: fmt(stats.value.totalManagers),     sub: 'restaurant owners',                                   icon: UserCircle,   gradient: 'from-violet-500 to-fuchsia-500' },
  { label: 'Staff accounts',  value: fmt(stats.value.totalStaff),        sub: 'cross-platform',                                      icon: ChefHat,      gradient: 'from-blue-500 to-indigo-500' },
  { label: 'Total users',     value: fmt(stats.value.totalUsers),        sub: 'including admins',                                    icon: Users,        gradient: 'from-slate-500 to-slate-700' },
  { label: 'Orders today',    value: fmt(stats.value.ordersToday),       sub: `${fmt(stats.value.totalOrders)} all-time`,            icon: ShoppingBag,  gradient: 'from-emerald-500 to-teal-500' },
  { label: 'Revenue today',   value: fmtMoney(stats.value.revenueToday), sub: `${fmtMoney(stats.value.totalRevenue)} all-time`,      icon: TrendingUp,   gradient: 'from-rose-500 to-pink-500' },
] : []
</script>

<template>
  <PageHeader title="Platform overview" description="Live across every restaurant on RestaurantOS." />

  <div v-if="loading" class="text-center py-16 text-slate-400">Loading platform stats…</div>

  <template v-else-if="stats">
    <!-- Welcome card -->
    <div class="bg-gradient-to-br from-amber-500 via-orange-500 to-rose-500 rounded-3xl p-6 sm:p-8 mb-6 text-white shadow-xl shadow-amber-500/30 relative overflow-hidden">
      <div class="absolute -right-12 -top-12 w-48 h-48 bg-white/10 rounded-full blur-3xl" aria-hidden="true" />
      <div class="absolute -right-6 -bottom-16 w-40 h-40 bg-white/10 rounded-full blur-3xl" aria-hidden="true" />
      <div class="relative flex items-start gap-4">
        <div class="w-12 h-12 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center flex-shrink-0">
          <ShieldCheck class="w-6 h-6 text-white" />
        </div>
        <div class="min-w-0">
          <h2 class="text-xl sm:text-2xl font-bold">You're running the platform.</h2>
          <p class="text-white/85 text-sm mt-1 max-w-lg">
            Monitor every restaurant, manage owners, audit users — all from here. Drill into a restaurant to see how it's doing.
          </p>
        </div>
      </div>
    </div>

    <!-- Stat tiles -->
    <div class="grid grid-cols-2 md:grid-cols-3 gap-3 sm:gap-4 mb-6">
      <div v-for="t in tiles()" :key="t.label"
        class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-4 sm:p-5 hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between mb-3">
          <span class="text-[11px] font-semibold text-slate-500 uppercase tracking-wide">{{ t.label }}</span>
          <div :class="['w-9 h-9 rounded-xl flex items-center justify-center text-white bg-gradient-to-br shadow-sm', t.gradient]">
            <component :is="t.icon" class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-slate-900 tabular-nums">{{ t.value }}</div>
        <p v-if="t.sub" class="text-xs text-slate-500 mt-0.5">{{ t.sub }}</p>
      </div>
    </div>

    <!-- Active rate -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-emerald-100 flex items-center justify-center">
            <CheckCircle2 class="w-5 h-5 text-emerald-600" />
          </div>
          <div>
            <h3 class="text-sm font-semibold text-slate-900">Restaurant activity</h3>
            <p class="text-xs text-slate-500">Share of restaurants currently active.</p>
          </div>
        </div>
        <span class="text-sm font-bold tabular-nums text-emerald-600">
          {{ stats.totalRestaurants === 0 ? '0' : Math.round((stats.activeRestaurants / stats.totalRestaurants) * 100) }}%
        </span>
      </div>
      <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
        <div class="h-full bg-gradient-to-r from-emerald-400 to-teal-500"
          :style="{ width: stats.totalRestaurants === 0 ? '0%' : `${(stats.activeRestaurants / stats.totalRestaurants) * 100}%` }" />
      </div>
      <p class="mt-2 text-xs text-slate-500 tabular-nums">
        {{ fmt(stats.activeRestaurants) }} active · {{ fmt(stats.totalRestaurants - stats.activeRestaurants) }} inactive
      </p>
    </div>
  </template>
</template>
