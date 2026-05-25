<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next'

defineProps<{ open: boolean; title: string; message: string; loading?: boolean }>()
defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="$emit('cancel')" />
      <div class="relative bg-white rounded-2xl shadow-xl ring-1 ring-slate-200/60 p-6 w-full max-w-sm mx-4">
        <div class="flex gap-3.5">
          <div class="w-10 h-10 rounded-xl bg-rose-50 flex items-center justify-center flex-shrink-0">
            <AlertTriangle class="w-5 h-5 text-rose-600" />
          </div>
          <div class="min-w-0">
            <h3 class="text-base font-semibold text-slate-900">{{ title }}</h3>
            <p class="mt-1 text-sm text-slate-500">{{ message }}</p>
          </div>
        </div>
        <div class="mt-5 flex justify-end gap-2">
          <button
            @click="$emit('cancel')"
            class="px-4 py-2 text-sm rounded-xl bg-white ring-1 ring-slate-200 text-slate-700 hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            @click="$emit('confirm')"
            :disabled="loading"
            class="px-4 py-2 text-sm rounded-xl bg-gradient-to-r from-rose-500 to-red-500 hover:from-rose-600 hover:to-red-600 text-white shadow-md shadow-rose-500/30 disabled:opacity-60 transition-all"
          >
            {{ loading ? 'Deleting…' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
