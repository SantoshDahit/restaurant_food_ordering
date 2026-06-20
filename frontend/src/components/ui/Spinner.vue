<script setup lang="ts">
import { computed } from 'vue'
import { Loader2 } from 'lucide-vue-next'
import { cn } from '@/utils/cn'

const props = withDefaults(defineProps<{ size?: 'sm' | 'md' | 'lg' }>(), { size: 'md' })

const classes = computed(() =>
  cn('animate-spin text-primary', { sm: 'w-4 h-4', md: 'w-6 h-6', lg: 'w-10 h-10' }[props.size]),
)
</script>

<template>
  <Loader2 :class="classes" />
</template>
