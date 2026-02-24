<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

const colorMap: Record<string, string> = {
  PENDING: 'bg-gray-100 text-gray-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PREPARING: 'bg-amber-100 text-amber-700',
  READY: 'bg-green-100 text-green-700',
  COMPLETED: 'bg-emerald-100 text-emerald-700',
  CANCELLED: 'bg-red-100 text-red-700',
  AVAILABLE: 'bg-green-100 text-green-700',
  OCCUPIED: 'bg-red-100 text-red-700',
  RESERVED: 'bg-yellow-100 text-yellow-700',
  CLEANING: 'bg-gray-100 text-gray-700',
  PAID: 'bg-emerald-100 text-emerald-700',
  ON_HOLD: 'bg-orange-100 text-orange-700',
  PRESENT: 'bg-green-100 text-green-700',
  ABSENT: 'bg-red-100 text-red-700',
  HALF_DAY: 'bg-yellow-100 text-yellow-700',
  LEAVE: 'bg-blue-100 text-blue-700',
  HOLIDAY: 'bg-purple-100 text-purple-700',
  FAILED: 'bg-red-100 text-red-700',
  REFUNDED: 'bg-purple-100 text-purple-700',
  OUT_OF_STOCK: 'bg-orange-100 text-orange-700',
  HIDDEN: 'bg-gray-100 text-gray-500',
}

const classes = computed(() => colorMap[props.status] || 'bg-gray-100 text-gray-600')
</script>

<template>
  <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium', classes]">
    {{ status.replace(/_/g, ' ') }}
  </span>
</template>
