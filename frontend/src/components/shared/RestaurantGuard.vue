<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Store, Plus } from 'lucide-vue-next'

defineProps<{
  /** What the user is being blocked from. Shown in the empty state. */
  resource?: string
}>()

const router = useRouter()
const auth = useAuthStore()

function goToRestaurant() {
  router.push('/dashboard/restaurant')
}
</script>

<template>
  <div v-if="!auth.restaurantCode" class="flex items-center justify-center py-12 sm:py-20">
    <div class="max-w-md w-full bg-card rounded-3xl shadow-soft ring-1 ring-border p-8 text-center">
      <div class="mx-auto w-16 h-16 rounded-2xl bg-primary flex items-center justify-center mb-5 shadow-soft">
        <Store class="w-7 h-7 text-primary-foreground" />
      </div>
      <h2 class="text-xl font-bold text-foreground mb-2">Restaurant required</h2>
      <p class="text-sm text-muted-foreground mb-6 leading-relaxed">
        You need to create your restaurant before you can {{ resource ? `manage ${resource}` : 'access this page' }}.
        Each manager works inside their own restaurant — nothing is shared.
      </p>
      <button
        @click="goToRestaurant"
        class="inline-flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary/90 text-primary-foreground text-sm font-medium rounded-xl shadow-soft transition-all"
      >
        <Plus class="w-4 h-4" />
        Create your restaurant
      </button>
    </div>
  </div>

  <slot v-else />
</template>
