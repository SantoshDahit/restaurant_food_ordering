<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'
import { formatNpr } from '@/utils/cn'

const props = withDefaults(
  defineProps<{
    amount: number
    size?: 'sm' | 'md' | 'lg' | 'xl'
    muted?: boolean
  }>(),
  { size: 'md' },
)

const formatted = computed(() => formatNpr(props.amount))

const classes = computed(() =>
  cn(
    'font-semibold tabular-nums tracking-tight',
    props.muted ? 'text-muted-foreground' : 'text-foreground',
    { sm: 'text-sm', md: 'text-base', lg: 'text-lg', xl: 'text-2xl' }[props.size],
  ),
)
</script>

<template>
  <span :class="classes">{{ formatted }}</span>
</template>
