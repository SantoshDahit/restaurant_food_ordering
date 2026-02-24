import api from './axios'
import type { AttendanceResponse, AttendanceCreateRequest, AttendancePatchRequest, PageResponse, AttendanceStatus } from '@/types'

export const attendanceApi = {
  search: (params: { restaurantCode?: string; employeeCode?: string; dateFrom?: string; dateTo?: string; status?: AttendanceStatus }) =>
    api.get<PageResponse<AttendanceResponse>>('/attendance/search', { params }).then(r => r.data),

  create: (data: AttendanceCreateRequest) =>
    api.post<AttendanceResponse>('/attendance', data).then(r => r.data),

  update: (code: string, data: AttendancePatchRequest) =>
    api.patch<AttendanceResponse>(`/attendance/${code}`, data).then(r => r.data),
}
