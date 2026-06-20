<script setup lang="ts">
import { computed } from 'vue'
import Badge from '@/components/ui/Badge.vue'

const props = defineProps<{ status: string }>()

type Variant = 'neutral' | 'success' | 'warning' | 'info' | 'destructive'

// Every status maps to one of the muted-earthy semantic colors.
// success = good/done · warning = in-progress/busy · info = scheduled/neutral-positive
// destructive = failed/cancelled/absent · neutral = idle/hidden
const variantMap: Record<string, Variant> = {
  // Order lifecycle
  PENDING:      'neutral',
  CONFIRMED:    'info',
  PREPARING:    'warning',
  READY:        'success',
  SERVED:       'success',
  COMPLETED:    'success',
  CANCELLED:    'destructive',
  // Tables
  AVAILABLE:    'success',
  OCCUPIED:     'warning',
  RESERVED:     'info',
  CLEANING:     'neutral',
  // Payments
  PAID:         'success',
  ON_HOLD:      'warning',
  FAILED:       'destructive',
  REFUNDED:     'info',
  // Attendance
  PRESENT:      'success',
  ABSENT:       'destructive',
  HALF_DAY:     'warning',
  LEAVE:        'info',
  HOLIDAY:      'neutral',
  // Item availability
  OUT_OF_STOCK: 'warning',
  HIDDEN:       'neutral',
}

const variant = computed<Variant>(() => variantMap[props.status] ?? 'neutral')
</script>

<template>
  <Badge :variant="variant" dot>{{ status.replace(/_/g, ' ') }}</Badge>
</template>
