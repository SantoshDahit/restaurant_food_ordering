import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

/** Merge conditional class names, de-duplicating conflicting Tailwind utilities. */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs))
}

/**
 * Format an amount as Nepali Rupees, e.g. 1250 -> "NPR 1,250".
 * Grouped, no decimals — matches the system's currency convention.
 */
export function formatNpr(amount: number | null | undefined): string {
  const value = Math.round(Number(amount ?? 0))
  return `NPR ${value.toLocaleString('en-IN')}`
}
