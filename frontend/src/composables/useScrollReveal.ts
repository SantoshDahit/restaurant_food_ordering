import { onMounted, onBeforeUnmount, type Ref } from 'vue'

/**
 * Adds an `is-visible` class to the element when it scrolls into the viewport.
 * Pair with a CSS transition keyed on `.is-visible` to fade/slide in.
 */
export function useScrollReveal(targets: Ref<HTMLElement[] | HTMLElement | null>, options: IntersectionObserverInit = { threshold: 0.15 }) {
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          observer?.unobserve(entry.target)
        }
      })
    }, options)

    const els = Array.isArray(targets.value) ? targets.value : targets.value ? [targets.value] : []
    els.forEach((el) => el && observer!.observe(el))
  })

  onBeforeUnmount(() => observer?.disconnect())
}

/**
 * Count up to `target` over `durationMs` once the element scrolls into view.
 * Returns a ref containing the current display value.
 */
export function useCountUp(elRef: Ref<HTMLElement | null>, target: number, durationMs = 1500) {
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!elRef.value) return
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && entry.target instanceof HTMLElement) {
          const el = entry.target
          const start = performance.now()
          const step = (now: number) => {
            const progress = Math.min(1, (now - start) / durationMs)
            const eased = 1 - Math.pow(1 - progress, 3)
            el.textContent = Math.floor(eased * target).toString()
            if (progress < 1) requestAnimationFrame(step)
            else el.textContent = target.toString()
          }
          requestAnimationFrame(step)
          observer?.unobserve(entry.target)
        }
      })
    }, { threshold: 0.4 })
    observer.observe(elRef.value)
  })

  onBeforeUnmount(() => observer?.disconnect())
}
