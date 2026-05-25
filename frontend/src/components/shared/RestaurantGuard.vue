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
    <div class="max-w-md w-full bg-white rounded-3xl shadow-sm ring-1 ring-slate-200/60 p-8 text-center">
      <div class="mx-auto w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-400 to-fuchsia-500 flex items-center justify-center mb-5 shadow-lg shadow-violet-500/30">
        <Store class="w-7 h-7 text-white" />
      </div>
      <h2 class="text-xl font-bold text-slate-900 mb-2">Restaurant required</h2>
      <p class="text-sm text-slate-500 mb-6 leading-relaxed">
        You need to create your restaurant before you can {{ resource ? `manage ${resource}` : 'access this page' }}.
        Each manager works inside their own restaurant — nothing is shared.
      </p>
      <button
        @click="goToRestaurant"
        class="inline-flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm font-medium rounded-xl shadow-md shadow-violet-500/30 transition-all"
      >
        <Plus class="w-4 h-4" />
        Create your restaurant
      </button>
    </div>
  </div>

  <slot v-else />
</template>
