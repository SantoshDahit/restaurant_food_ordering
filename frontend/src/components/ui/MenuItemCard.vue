<script setup lang="ts">
import { computed } from 'vue'
import { UtensilsCrossed, Plus, Leaf, Clock } from 'lucide-vue-next'
import { cn, formatNpr } from '@/utils/cn'
import type { MenuItemResponse } from '@/types'

const props = withDefaults(
  defineProps<{
    item: MenuItemResponse
    imageUrl?: string
    /** Quantity currently in the cart (0 = not added). */
    quantity?: number
    showPrepTime?: boolean
  }>(),
  { quantity: 0 },
)

defineEmits<{ (e: 'add'): void }>()

const inCart = computed(() => props.quantity > 0)

const hasDiscount = computed(() => (props.item.discountPercent ?? 0) > 0)
const finalPrice = computed(() =>
  hasDiscount.value
    ? props.item.price * (1 - props.item.discountPercent / 100)
    : props.item.price,
)
</script>

<template>
  <button
    type="button"
    :class="
      cn(
        'group relative flex flex-col text-left bg-card rounded-2xl overflow-hidden border transition-all duration-300 ease-premium',
        'active:scale-[0.98] hover:-translate-y-1',
        inCart
          ? 'border-primary shadow-card ring-1 ring-primary/30'
          : 'border-border/70 shadow-soft hover:shadow-lifted hover:border-primary/30',
      )
    "
    @click="$emit('add')"
  >
    <!-- Quantity pill -->
    <Transition
      enter-active-class="transition duration-200 ease-premium"
      enter-from-class="scale-0 opacity-0"
    >
      <div
        v-if="inCart"
        class="absolute top-2.5 right-2.5 z-10 min-w-7 h-7 px-1.5 rounded-full bg-primary text-primary-foreground text-sm font-semibold flex items-center justify-center shadow-card tabular-nums"
      >
        {{ quantity }}
      </div>
    </Transition>

    <!-- Image -->
    <div class="relative aspect-[4/3] w-full overflow-hidden bg-accent">
      <img
        v-if="imageUrl"
        :src="imageUrl"
        :alt="item.name"
        loading="lazy"
        class="w-full h-full object-cover transition-transform duration-500 ease-premium group-hover:scale-105"
      />
      <div v-else class="w-full h-full flex items-center justify-center">
        <UtensilsCrossed class="w-9 h-9 text-primary/25" />
      </div>

      <!-- Badges -->
      <div class="absolute top-2.5 left-2.5 flex flex-col gap-1.5">
        <span
          v-if="item.isVeg"
          class="inline-flex items-center gap-1 rounded-full bg-success/90 backdrop-blur px-2 py-0.5 text-[10px] font-semibold text-success-foreground"
        >
          <Leaf class="w-3 h-3" /> Veg
        </span>
        <span
          v-if="hasDiscount"
          class="inline-flex items-center rounded-full bg-destructive/90 backdrop-blur px-2 py-0.5 text-[10px] font-semibold text-destructive-foreground"
        >
          -{{ item.discountPercent }}%
        </span>
      </div>
    </div>

    <!-- Body -->
    <div class="flex flex-1 flex-col p-3.5">
      <h3 class="font-medium text-foreground text-sm leading-snug line-clamp-1">{{ item.name }}</h3>
      <p
        v-if="item.description"
        class="text-xs text-muted-foreground mt-1 line-clamp-2 leading-relaxed"
      >
        {{ item.description }}
      </p>

      <div
        v-if="showPrepTime && item.prepTimeMinutes"
        class="mt-1.5 inline-flex items-center gap-1 text-[11px] text-muted-foreground"
      >
        <Clock class="w-3 h-3" /> {{ item.prepTimeMinutes }} min
      </div>

      <div class="mt-auto pt-3 flex items-end justify-between gap-2">
        <div class="min-w-0">
          <span class="font-serif text-lg font-semibold text-foreground tabular-nums">
            {{ formatNpr(finalPrice) }}
          </span>
          <span
            v-if="hasDiscount"
            class="ml-1.5 text-xs text-muted-foreground line-through tabular-nums"
          >
            {{ formatNpr(item.price) }}
          </span>
        </div>
        <span
          :class="
            cn(
              'w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 transition-colors',
              inCart ? 'bg-primary text-primary-foreground' : 'bg-accent text-primary group-hover:bg-primary group-hover:text-primary-foreground',
            )
          "
        >
          <Plus class="w-4 h-4" />
        </span>
      </div>
    </div>
  </button>
</template>
