<script setup lang="ts">
import { cn } from '@/utils/cn'

defineProps<{
  modelValue: string
  options: { value: string; label: string }[]
}>()

defineEmits<{ (e: 'update:modelValue', value: string): void }>()
</script>

<template>
  <div class="relative">
    <div class="flex gap-2 overflow-x-auto no-scrollbar py-0.5">
      <button
        v-for="opt in options"
        :key="opt.value"
        type="button"
        :class="
          cn(
            'px-4 h-9 rounded-full text-sm font-medium whitespace-nowrap transition-all duration-200 ease-premium',
            modelValue === opt.value
              ? 'bg-primary text-primary-foreground shadow-soft'
              : 'bg-card text-muted-foreground border border-border hover:text-foreground hover:border-primary/30',
          )
        "
        @click="$emit('update:modelValue', opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
    <!-- Right-edge fade hinting more chips -->
    <div
      aria-hidden="true"
      class="pointer-events-none absolute inset-y-0 right-0 w-8 bg-gradient-to-l from-background to-transparent"
    />
  </div>
</template>
