import api from './axios'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types'

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', data).then(r => r.data),

  register: (data: RegisterRequest) =>
    api.post<LoginResponse>('/auth/register', data).then(r => r.data),
}
