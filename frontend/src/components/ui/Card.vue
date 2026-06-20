<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'

const props = withDefaults(
  defineProps<{
    /** Elevation: flat hairline card vs. softly lifted. */
    elevation?: 'flat' | 'soft' | 'card' | 'lifted'
    /** Built-in padding scale; pass 'none' to control padding yourself. */
    padding?: 'none' | 'sm' | 'md' | 'lg'
    interactive?: boolean
    as?: string
  }>(),
  { elevation: 'soft', padding: 'md', as: 'div' },
)

const classes = computed(() =>
  cn(
    'bg-card text-card-foreground rounded-xl border border-border/70',
    {
      flat: 'shadow-none',
      soft: 'shadow-soft',
      card: 'shadow-card',
      lifted: 'shadow-lifted',
    }[props.elevation],
    { none: '', sm: 'p-3', md: 'p-5', lg: 'p-7' }[props.padding],
    props.interactive &&
      'transition-all duration-200 ease-premium hover:shadow-card hover:border-primary/30 cursor-pointer active:scale-[0.99]',
  ),
)
</script>

<template>
  <component :is="as" :class="classes">
    <slot />
  </component>
</template>
