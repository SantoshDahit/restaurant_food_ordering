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
          ? 'bg-muted text-muted-foreground ring-1 ring-border'
          : tone === 'purple'
          ? 'bg-info/10 text-info ring-1 ring-info/20'
          : 'bg-accent text-primary ring-1 ring-primary/30',
      ]"
    >
      <component :is="icon || Inbox" class="w-7 h-7 sm:w-8 sm:h-8" />
    </div>
    <h3 class="text-base sm:text-lg font-semibold text-foreground">{{ title }}</h3>
    <p v-if="description" class="mt-1 text-sm text-muted-foreground max-w-sm leading-relaxed">{{ description }}</p>
    <button
      v-if="ctaLabel"
      @click="onCta"
      class="mt-5 inline-flex items-center gap-1.5 px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground text-sm font-medium rounded-xl shadow-soft transition-all"
    >
      <slot name="cta-icon" />
      {{ ctaLabel }}
    </button>
  </div>
</template>
