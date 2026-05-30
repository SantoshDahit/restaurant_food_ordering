import api from './axios'
import type { PayrollResponse, PayrollCreateRequest, PayrollStatusUpdateRequest, PageResponse, SalaryStatus } from '@/types'

export const payrollApi = {
  search: (params: { restaurantCode?: string; employeeCode?: string; status?: SalaryStatus; size?: number; page?: number }) =>
    api.get<PageResponse<PayrollResponse>>('/payroll/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<PayrollResponse>(`/payroll/${code}`).then(r => r.data),

  create: (data: PayrollCreateRequest) =>
    api.post<PayrollResponse>('/payroll', data).then(r => r.data),

  updateStatus: (code: string, data: PayrollStatusUpdateRequest) =>
    api.patch<PayrollResponse>(`/payroll/${code}/status`, data).then(r => r.data),
}
