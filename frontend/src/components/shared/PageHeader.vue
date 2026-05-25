<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>

<template>
  <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3 mb-6">
    <div class="min-w-0">
      <h2 class="text-xl sm:text-2xl font-bold text-slate-900 truncate">{{ title }}</h2>
      <p v-if="description" class="text-sm text-slate-500 mt-1">{{ description }}</p>
    </div>
    <div class="flex flex-wrap items-center gap-2 sm:gap-3">
      <slot name="actions" />
    </div>
  </div>
</template>
