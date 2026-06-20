<script setup lang="ts">
import { computed } from 'vue'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/utils/cn'

const badge = cva(
  'inline-flex items-center gap-1.5 font-medium rounded-full ring-1 ring-inset whitespace-nowrap',
  {
    variants: {
      variant: {
        neutral: 'bg-muted text-muted-foreground ring-border',
        primary: 'bg-accent text-accent-foreground ring-primary/20',
        success: 'bg-success/10 text-success ring-success/20',
        warning: 'bg-warning/10 text-warning ring-warning/25',
        info: 'bg-info/10 text-info ring-info/20',
        destructive: 'bg-destructive/10 text-destructive ring-destructive/20',
        outline: 'bg-transparent text-foreground ring-border',
      },
      size: {
        sm: 'text-[11px] px-2 py-0.5',
        md: 'text-xs px-2.5 py-1',
      },
    },
    defaultVariants: { variant: 'neutral', size: 'sm' },
  },
)

type BadgeVariants = VariantProps<typeof badge>

const props = defineProps<{
  variant?: BadgeVariants['variant']
  size?: BadgeVariants['size']
  dot?: boolean
}>()

const classes = computed(() => cn(badge({ variant: props.variant, size: props.size })))
</script>

<template>
  <span :class="classes">
    <span v-if="dot" class="w-1.5 h-1.5 rounded-full bg-current opacity-70" />
    <slot />
  </span>
</template>
