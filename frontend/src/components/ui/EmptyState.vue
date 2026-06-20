<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon?: Component
  title: string
  description?: string
  /** Visual weight of the wrapper. */
  compact?: boolean
}>()
</script>

<template>
  <div
    class="flex flex-col items-center justify-center text-center px-6"
    :class="compact ? 'py-10' : 'py-16'"
  >
    <div
      v-if="icon"
      class="w-14 h-14 rounded-2xl bg-accent flex items-center justify-center mb-4"
    >
      <component :is="icon" class="w-6 h-6 text-primary/60" />
    </div>
    <p class="font-medium text-foreground">{{ title }}</p>
    <p v-if="description" class="text-sm text-muted-foreground mt-1 max-w-xs">{{ description }}</p>
    <div v-if="$slots.action" class="mt-5">
      <slot name="action" />
    </div>
  </div>
</template>
