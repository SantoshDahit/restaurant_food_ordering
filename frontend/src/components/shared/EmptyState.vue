<script setup lang="ts">
import type { Component } from 'vue'
import { Inbox } from 'lucide-vue-next'

defineProps<{
  /** Lucide icon component. Defaults to Inbox. */
  icon?: Component
  title: string
  description?: string
  /** Optional CTA label; when provided, click event fires. */
  ctaLabel?: string
  /** Visual tone: orange (default), slate (muted), purple (special). */
  tone?: 'orange' | 'slate' | 'purple'
}>()

const emit = defineEmits<{ cta: [] }>()

function onCta() {
  emit('cta')
}
</script>

<template>
  <div class="flex flex-col items-center justify-center py-12 sm:py-16 px-6 text-center">
    <div
      :class="[
        'w-16 h-16 sm:w-20 sm:h-20 rounded-2xl flex items-center justify-center mb-4 shadow-sm',
        tone === 'slate'
          ? 'bg-slate-100 text-slate-400 ring-1 ring-slate-200/60'
          : tone === 'purple'
          ? 'bg-gradient-to-br from-purple-100 to-fuchsia-100 text-purple-500 ring-1 ring-purple-200/60'
          : 'bg-gradient-to-br from-violet-100 to-fuchsia-100 text-violet-500 ring-1 ring-violet-200/60',
      ]"
    >
      <component :is="icon || Inbox" class="w-7 h-7 sm:w-8 sm:h-8" />
    </div>
    <h3 class="text-base sm:text-lg font-semibold text-slate-900">{{ title }}</h3>
    <p v-if="description" class="mt-1 text-sm text-slate-500 max-w-sm leading-relaxed">{{ description }}</p>
    <button
      v-if="ctaLabel"
      @click="onCta"
      class="mt-5 inline-flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm font-medium rounded-xl shadow-md shadow-violet-500/30 transition-all"
    >
      <slot name="cta-icon" />
      {{ ctaLabel }}
    </button>
  </div>
</template>
