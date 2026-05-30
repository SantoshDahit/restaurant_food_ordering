import type { OrderType } from '@/types'

/** Human label for an OrderType. QR_ORDER renders as "Table order". */
export function orderTypeLabel(t: OrderType | string | undefined | null): string {
  switch (t) {
    case 'DINE_IN':  return 'Dine in'
    case 'TAKEAWAY': return 'Takeaway'
    case 'QR_ORDER': return 'Table order'
    case 'KIOSK':    return 'Kiosk'
    default:         return String(t ?? '').replace(/_/g, ' ')
  }
}
