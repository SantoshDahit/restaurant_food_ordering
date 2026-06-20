<script setup lang="ts">
import { watch, onBeforeUnmount } from 'vue'
import { X } from 'lucide-vue-next'
import { cn } from '@/utils/cn'

const props = withDefaults(
  defineProps<{
    open: boolean
    /** Where the panel slides in from. */
    side?: 'bottom' | 'right' | 'left'
    title?: string
    description?: string
    /** Hide the default close (X) button. */
    hideClose?: boolean
  }>(),
  { side: 'right' },
)

const emit = defineEmits<{ (e: 'update:open', value: boolean): void }>()

function close() {
  emit('update:open', false)
}

// Lock body scroll while open.
watch(
  () => props.open,
  (open) => {
    if (typeof document === 'undefined') return
    document.body.style.overflow = open ? 'hidden' : ''
  },
)
onBeforeUnmount(() => {
  if (typeof document !== 'undefined') document.body.style.overflow = ''
})

const panelPosition = {
  bottom: 'inset-x-0 bottom-0 max-h-[88vh] rounded-t-2xl',
  right: 'inset-y-0 right-0 w-full max-w-md',
  left: 'inset-y-0 left-0 w-full max-w-md',
}

const enterFrom = {
  bottom: 'translate-y-full',
  right: 'translate-x-full',
  left: '-translate-x-full',
}
</script>

<template>
  <Teleport to="body">
    <!-- Backdrop -->
    <Transition
      enter-active-class="transition-opacity duration-200"
      leave-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-50 bg-foreground/30 backdrop-blur-sm"
        aria-hidden="true"
        @click="close"
      />
    </Transition>

    <!-- Panel -->
    <Transition
      enter-active-class="transition-transform duration-300 ease-premium"
      leave-active-class="transition-transform duration-250 ease-premium"
      :enter-from-class="enterFrom[side]"
      :leave-to-class="enterFrom[side]"
    >
      <div
        v-if="open"
        role="dialog"
        :aria-label="title"
        :class="
          cn('fixed z-50 flex flex-col bg-card text-card-foreground shadow-sheet', panelPosition[side])
        "
      >
        <!-- Grab handle for the bottom sheet -->
        <div v-if="side === 'bottom'" class="flex justify-center pt-2.5 pb-1">
          <div class="h-1.5 w-10 rounded-full bg-border" />
        </div>

        <header
          v-if="title || $slots.header || !hideClose"
          class="flex items-center gap-3 px-5 py-4 border-b border-border/70"
        >
          <slot name="header">
            <div class="min-w-0 flex-1">
              <h3 class="text-lg font-semibold leading-tight truncate">{{ title }}</h3>
              <p v-if="description" class="text-sm text-muted-foreground mt-0.5">{{ description }}</p>
            </div>
          </slot>
          <button
            v-if="!hideClose"
            type="button"
            class="w-9 h-9 -mr-1.5 rounded-full text-muted-foreground hover:bg-accent flex items-center justify-center flex-shrink-0 transition-colors"
            aria-label="Close"
            @click="close"
          >
            <X class="w-5 h-5" />
          </button>
        </header>

        <div class="flex-1 overflow-y-auto scrollbar-fine min-h-0">
          <slot />
        </div>

        <footer v-if="$slots.footer" class="flex-shrink-0 border-t border-border/70">
          <slot name="footer" />
        </footer>
      </div>
    </Transition>
  </Teleport>
</template>
