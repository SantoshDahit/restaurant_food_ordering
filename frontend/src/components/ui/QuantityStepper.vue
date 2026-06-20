<script setup lang="ts">
import { computed } from 'vue'
import { Minus, Plus } from 'lucide-vue-next'
import { cn } from '@/utils/cn'

const props = withDefaults(
  defineProps<{
    modelValue: number
    min?: number
    max?: number
    size?: 'sm' | 'md'
  }>(),
  { min: 0, max: 99, size: 'md' },
)

const emit = defineEmits<{ (e: 'update:modelValue', value: number): void }>()

const dims = computed(() =>
  props.size === 'sm'
    ? { btn: 'w-7 h-7', icon: 'w-3.5 h-3.5', label: 'w-7 text-sm' }
    : { btn: 'w-9 h-9', icon: 'w-4 h-4', label: 'w-9 text-base' },
)

const canDecrement = computed(() => props.modelValue > props.min)
const canIncrement = computed(() => props.modelValue < props.max)

function dec() {
  if (canDecrement.value) emit('update:modelValue', props.modelValue - 1)
}
function inc() {
  if (canIncrement.value) emit('update:modelValue', props.modelValue + 1)
}
</script>

<template>
  <div class="inline-flex items-center gap-1 rounded-full border border-border bg-card p-0.5">
    <button
      type="button"
      :disabled="!canDecrement"
      :class="
        cn(
          dims.btn,
          'rounded-full flex items-center justify-center text-foreground transition-colors',
          'hover:bg-accent disabled:opacity-30 disabled:hover:bg-transparent',
        )
      "
      aria-label="Decrease quantity"
      @click="dec"
    >
      <Minus :class="dims.icon" />
    </button>
    <span :class="cn(dims.label, 'text-center font-semibold tabular-nums text-foreground')">
      {{ modelValue }}
    </span>
    <button
      type="button"
      :disabled="!canIncrement"
      :class="
        cn(
          dims.btn,
          'rounded-full flex items-center justify-center bg-primary text-primary-foreground transition-all',
          'hover:bg-primary/90 active:scale-95 disabled:opacity-30',
        )
      "
      aria-label="Increase quantity"
      @click="inc"
    >
      <Plus :class="dims.icon" />
    </button>
  </div>
</template>
