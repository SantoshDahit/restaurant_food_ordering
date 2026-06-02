<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

// Each status gets a soft chip color + a saturated dot color.
const styleMap: Record<string, { chip: string; dot: string }> = {
  PENDING:      { chip: 'bg-slate-100 text-slate-700 ring-slate-200',     dot: 'bg-slate-400' },
  CONFIRMED:    { chip: 'bg-sky-50 text-sky-700 ring-sky-200',            dot: 'bg-sky-500' },
  PREPARING:    { chip: 'bg-amber-50 text-amber-700 ring-amber-200',      dot: 'bg-amber-500' },
  READY:        { chip: 'bg-green-50 text-green-700 ring-green-200',      dot: 'bg-green-500' },
  SERVED:       { chip: 'bg-teal-50 text-teal-700 ring-teal-200',        dot: 'bg-teal-500' },
  COMPLETED:    { chip: 'bg-emerald-50 text-emerald-700 ring-emerald-200', dot: 'bg-emerald-500' },
  CANCELLED:    { chip: 'bg-rose-50 text-rose-700 ring-rose-200',         dot: 'bg-rose-500' },
  AVAILABLE:    { chip: 'bg-emerald-50 text-emerald-700 ring-emerald-200', dot: 'bg-emerald-500' },
  OCCUPIED:     { chip: 'bg-rose-50 text-rose-700 ring-rose-200',         dot: 'bg-rose-500' },
  RESERVED:     { chip: 'bg-amber-50 text-amber-700 ring-amber-200',      dot: 'bg-amber-500' },
  CLEANING:     { chip: 'bg-slate-100 text-slate-600 ring-slate-200',     dot: 'bg-slate-400' },
  PAID:         { chip: 'bg-emerald-50 text-emerald-700 ring-emerald-200', dot: 'bg-emerald-500' },
  ON_HOLD:      { chip: 'bg-amber-50 text-amber-700 ring-amber-200',      dot: 'bg-amber-500' },
  PRESENT:      { chip: 'bg-emerald-50 text-emerald-700 ring-emerald-200', dot: 'bg-emerald-500' },
  ABSENT:       { chip: 'bg-rose-50 text-rose-700 ring-rose-200',         dot: 'bg-rose-500' },
  HALF_DAY:     { chip: 'bg-amber-50 text-amber-700 ring-amber-200',      dot: 'bg-amber-500' },
  LEAVE:        { chip: 'bg-sky-50 text-sky-700 ring-sky-200',            dot: 'bg-sky-500' },
  HOLIDAY:      { chip: 'bg-purple-50 text-purple-700 ring-purple-200',   dot: 'bg-purple-500' },
  FAILED:       { chip: 'bg-rose-50 text-rose-700 ring-rose-200',         dot: 'bg-rose-500' },
  REFUNDED:     { chip: 'bg-purple-50 text-purple-700 ring-purple-200',   dot: 'bg-purple-500' },
  OUT_OF_STOCK: { chip: 'bg-amber-50 text-amber-700 ring-amber-200',      dot: 'bg-amber-500' },
  HIDDEN:       { chip: 'bg-slate-100 text-slate-500 ring-slate-200',     dot: 'bg-slate-400' },
}

const style = computed(() =>
  styleMap[props.status] || { chip: 'bg-slate-100 text-slate-600 ring-slate-200', dot: 'bg-slate-400' }
)
</script>

<template>
  <span
    :class="[
      'inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[11px] font-medium ring-1 ring-inset',
      style.chip,
    ]"
  >
    <span :class="['w-1.5 h-1.5 rounded-full', style.dot]" />
    {{ status.replace(/_/g, ' ') }}
  </span>
</template>
