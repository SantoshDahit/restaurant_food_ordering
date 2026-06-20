<script setup lang="ts">
import { cn } from '@/utils/cn'

defineProps<{ class?: string }>()
</script>

<template>
  <div
    :class="
      cn(
        'relative overflow-hidden rounded-lg bg-muted',
        'after:absolute after:inset-0 after:-translate-x-full after:animate-shimmer',
        'after:bg-gradient-to-r after:from-transparent after:via-card/60 after:to-transparent',
        $props.class,
      )
    "
  />
</template>
