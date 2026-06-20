<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next'

defineProps<{ open: boolean; title: string; message: string; loading?: boolean }>()
defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-foreground/40 backdrop-blur-sm" @click="$emit('cancel')" />
      <div class="relative bg-card rounded-2xl shadow-lifted ring-1 ring-border p-6 w-full max-w-sm mx-4">
        <div class="flex gap-3.5">
          <div class="w-10 h-10 rounded-xl bg-destructive/10 flex items-center justify-center flex-shrink-0">
            <AlertTriangle class="w-5 h-5 text-destructive" />
          </div>
          <div class="min-w-0">
            <h3 class="text-base font-semibold text-foreground">{{ title }}</h3>
            <p class="mt-1 text-sm text-muted-foreground">{{ message }}</p>
          </div>
        </div>
        <div class="mt-5 flex justify-end gap-2">
          <button
            @click="$emit('cancel')"
            class="px-4 py-2 text-sm rounded-xl bg-card ring-1 ring-border text-foreground hover:bg-accent transition-colors"
          >
            Cancel
          </button>
          <button
            @click="$emit('confirm')"
            :disabled="loading"
            class="px-4 py-2 text-sm rounded-xl bg-destructive hover:bg-destructive/90 text-destructive-foreground shadow-soft disabled:opacity-60 transition-all"
          >
            {{ loading ? 'Deleting…' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
