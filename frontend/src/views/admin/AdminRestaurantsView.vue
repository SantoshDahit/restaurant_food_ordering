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
  <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-3 sm:p-4 mb-4">
    <div class="relative">
      <Search class="w-4 h-4 text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
      <input
        v-model="query"
        placeholder="Search by name, kiosk code, email or address…"
        class="w-full pl-9 pr-3 py-2 bg-muted border border-border rounded-xl text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 focus:bg-card transition-all"
      />
    </div>
  </div>

  <div v-if="loading" class="text-center py-16 text-muted-foreground">Loading restaurants…</div>

  <div v-else-if="!filtered.length" class="bg-card rounded-2xl ring-1 ring-border shadow-soft">
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
      class="group text-left bg-card rounded-2xl ring-1 ring-border shadow-soft hover:shadow-card hover:ring-warning/30 transition-all p-4"
    >
      <div class="flex items-start gap-3">
        <div class="w-11 h-11 rounded-xl bg-warning flex items-center justify-center text-warning-foreground flex-shrink-0 shadow-soft">
          <Store class="w-5 h-5" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="flex items-center justify-between gap-2">
            <h3 class="font-semibold text-foreground truncate">{{ r.name }}</h3>
            <ChevronRight class="w-4 h-4 text-muted-foreground group-hover:text-warning transition-colors flex-shrink-0" />
          </div>
          <p class="text-xs text-muted-foreground truncate">{{ r.address || '—' }}</p>
          <div class="flex items-center gap-2 mt-2">
            <span class="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded-md bg-info/10 text-info ring-1 ring-info/20">
              {{ r.kioskCode }}
            </span>
            <span :class="[
              'inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-md ring-1',
              r.isActive
                ? 'bg-success/10 text-success ring-success/20'
                : 'bg-muted text-muted-foreground ring-border'
            ]">
              <Circle :class="['w-2 h-2', r.isActive ? 'fill-success text-success' : 'fill-muted-foreground text-muted-foreground']" />
              {{ r.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
        </div>
      </div>
    </button>
  </div>
</template>
