import api from './axios'
import type { EmployeeResponse, EmployeeCreateRequest, EmployeePatchRequest, PageResponse } from '@/types'

export const employeeApi = {
  search: (params: { restaurantCode?: string; fullName?: string; isActive?: boolean; size?: number; page?: number }) =>
    api.get<PageResponse<EmployeeResponse>>('/employees/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<EmployeeResponse>(`/employees/${code}`).then(r => r.data),

  create: (data: EmployeeCreateRequest) =>
    api.post<EmployeeResponse>('/employees', data).then(r => r.data),

  update: (code: string, data: EmployeePatchRequest) =>
    api.patch<EmployeeResponse>(`/employees/${code}`, data).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/employees/${code}`),
}
