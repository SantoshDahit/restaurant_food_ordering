<script setup lang="ts">
import { computed } from 'vue'
import { cva, type VariantProps } from 'class-variance-authority'
import { Loader2 } from 'lucide-vue-next'
import { cn } from '@/utils/cn'

/**
 * The single button primitive for the whole app.
 * Premium-minimal: quiet fills, sage primary, soft focus ring.
 */
const button = cva(
  'inline-flex items-center justify-center gap-2 whitespace-nowrap font-medium ' +
    'transition-all duration-200 ease-premium select-none ' +
    'focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-ring/20 ' +
    'disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]',
  {
    variants: {
      variant: {
        primary: 'bg-primary text-primary-foreground shadow-soft hover:bg-primary/90 hover:shadow-glow',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/70',
        outline: 'border border-border bg-card text-foreground hover:bg-accent hover:border-primary/30',
        ghost: 'text-foreground hover:bg-accent',
        subtle: 'bg-accent text-accent-foreground hover:bg-accent/70',
        destructive: 'bg-destructive text-destructive-foreground shadow-soft hover:bg-destructive/90',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      size: {
        sm: 'h-9 px-3.5 text-sm rounded-md',
        md: 'h-11 px-5 text-sm rounded-lg',
        lg: 'h-12 px-7 text-base rounded-xl',
        icon: 'h-10 w-10 rounded-lg',
      },
      block: { true: 'w-full' },
    },
    defaultVariants: { variant: 'primary', size: 'md' },
  },
)

type ButtonVariants = VariantProps<typeof button>

const props = withDefaults(
  defineProps<{
    variant?: ButtonVariants['variant']
    size?: ButtonVariants['size']
    block?: boolean
    type?: 'button' | 'submit' | 'reset'
    loading?: boolean
    disabled?: boolean
  }>(),
  { type: 'button', loading: false, disabled: false },
)

const classes = computed(() =>
  cn(button({ variant: props.variant, size: props.size, block: props.block })),
)
</script>

<template>
  <button :type="type" :class="classes" :disabled="disabled || loading">
    <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
    <slot v-else name="icon" />
    <slot />
  </button>
</template>
