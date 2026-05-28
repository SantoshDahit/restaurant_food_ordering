import api from './axios'
import type { ReceiptResponse } from '@/types'

export const receiptApi = {
  get: (code: string) =>
    api.get<ReceiptResponse>(`/receipts/${code}`).then(r => r.data),

  getByOrder: (orderCode: string) =>
    api.get<ReceiptResponse>(`/receipts/by-order/${orderCode}`).then(r => r.data),
}
