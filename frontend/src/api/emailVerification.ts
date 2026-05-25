import api from './axios'
import type {
  EmailVerificationSendRequest,
  EmailVerificationVerifyRequest,
  EmailVerificationResponse,
} from '@/types'

export const emailVerificationApi = {
  send: (data: EmailVerificationSendRequest) =>
    api.post<EmailVerificationResponse>('/email-verifications', data).then(r => r.data),

  verify: (data: EmailVerificationVerifyRequest) =>
    api.post<EmailVerificationResponse>('/email-verifications/verify', data).then(r => r.data),
}
