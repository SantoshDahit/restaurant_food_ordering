<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { restaurantApi } from '@/api/restaurant'
import { toast } from 'vue-sonner'
import PageHeader from '@/components/shared/PageHeader.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { Store, Search, ChevronRight, Circle } from 'lucide-vue-next'
import type { RestaurantResponse } from '@/types'

const router = useRouter()
const restaurants = ref<RestaurantResponse[]>([])
const loading = ref(true)
const query = ref('')

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await restaurantApi.search()
    restaurants.value = data.content
  } catch {
    toast.error('Failed to load restaurants')
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return restaurants.value
  return restaurants.value.filter(r =>
    r.name.toLowerCase().includes(q) ||
    r.kioskCode.toLowerCase().includes(q) ||
    (r.email ?? '').toLowerCase().includes(q) ||
    (r.address ?? '').toLowerCase().includes(q)
  )
})

function open(code: string) {
  router.push({ name: 'admin-restaurant-detail', params: { code } })
}
</script>

<template>
  <PageHeader title="Restaurants" :description="`${restaurants.length} on the platform`" />

  <!-- Search -->
  <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-3 sm:p-4 mb-4">
    <div class="relative">
      <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
      <input
        v-model="query"
        placeholder="Search by name, kiosk code, email or address…"
        class="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-amber-500/40 focus:border-amber-400 focus:bg-white transition-all"
      />
    </div>
  </div>

  <div v-if="loading" class="text-center py-16 text-slate-400">Loading restaurants…</div>

  <div v-else-if="!filtered.length" class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm">
    <EmptyState
      :icon="Store"
      title="No restaurants found"
      :description="query ? 'Try a different search term.' : 'No restaurants have been onboarded yet.'"
    />
  </div>

  <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
    <button
      v-for="r in filtered"
      :key="r.code"
      @click="open(r.code)"
      class="group text-left bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm hover:shadow-md hover:ring-amber-200 transition-all p-4"
    >
      <div class="flex items-start gap-3">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white flex-shrink-0 shadow-sm shadow-amber-500/30">
          <Store class="w-5 h-5" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="flex items-center justify-between gap-2">
            <h3 class="font-semibold text-slate-900 truncate">{{ r.name }}</h3>
            <ChevronRight class="w-4 h-4 text-slate-300 group-hover:text-amber-500 transition-colors flex-shrink-0" />
          </div>
          <p class="text-xs text-slate-500 truncate">{{ r.address || '—' }}</p>
          <div class="flex items-center gap-2 mt-2">
            <span class="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded-md bg-teal-50 text-teal-700 ring-1 ring-teal-200/60">
              {{ r.kioskCode }}
            </span>
            <span :class="[
              'inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-md ring-1',
              r.isActive
                ? 'bg-emerald-50 text-emerald-700 ring-emerald-200/60'
                : 'bg-slate-100 text-slate-500 ring-slate-200/60'
            ]">
              <Circle :class="['w-2 h-2', r.isActive ? 'fill-emerald-500 text-emerald-500' : 'fill-slate-400 text-slate-400']" />
              {{ r.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
        </div>
      </div>
    </button>
  </div>
</template>
