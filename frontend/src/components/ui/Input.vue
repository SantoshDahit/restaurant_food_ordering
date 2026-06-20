<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'

const props = withDefaults(
  defineProps<{
    modelValue?: string | number
    type?: string
    placeholder?: string
    disabled?: boolean
    size?: 'sm' | 'md' | 'lg'
    invalid?: boolean
  }>(),
  { type: 'text', size: 'md' },
)

defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const classes = computed(() =>
  cn(
    'w-full bg-card text-foreground rounded-lg border border-input',
    'placeholder:text-muted-foreground/70 transition-all duration-200',
    'focus:outline-none focus:border-primary/50 focus:ring-4 focus:ring-ring/15',
    'disabled:opacity-50 disabled:cursor-not-allowed',
    { sm: 'h-9 text-sm', md: 'h-11 text-sm', lg: 'h-12 text-base' }[props.size],
    props.invalid && 'border-destructive/60 focus:border-destructive focus:ring-destructive/15',
  ),
)
</script>

<template>
  <div class="relative">
    <span
      v-if="$slots.leading"
      class="absolute inset-y-0 left-0 pl-3.5 flex items-center text-muted-foreground pointer-events-none"
    >
      <slot name="leading" />
    </span>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :class="cn(classes, $slots.leading ? 'pl-10 pr-4' : 'px-4')"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
  </div>
</template>
