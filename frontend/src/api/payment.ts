import api from './axios'
import type { PaymentResponse, PaymentCreateRequest, PaymentStatusUpdateRequest, PageResponse, PaymentStatus, PaymentMethod } from '@/types'

export const paymentApi = {
  search: (params: { restaurantCode?: string; status?: PaymentStatus; paymentMethod?: PaymentMethod }) =>
    api.get<PageResponse<PaymentResponse>>('/payments/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<PaymentResponse>(`/payments/${code}`).then(r => r.data),

  getByOrder: (orderCode: string) =>
    api.get<PaymentResponse | null>(`/payments/by-order/${orderCode}`).then(r => r.data),

  create: (data: PaymentCreateRequest) =>
    api.post<PaymentResponse>('/payments', data).then(r => r.data),

  updateStatus: (code: string, data: PaymentStatusUpdateRequest) =>
    api.patch<PaymentResponse>(`/payments/${code}/status`, data).then(r => r.data),
}
