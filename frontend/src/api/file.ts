import api from './axios'
import type { FileResponse } from '@/types'

interface PreSignedUrlRequest {
  fileName: string
  folderName: string
  type: 'IMAGE' | 'PDF' | 'DOCUMENT'
}

interface PreSignedUrlResponse {
  id: string
  url: string
  preSignedUrl: string
}

export const fileApi = {
  /**
   * Upload a file via S3 presigned URL.
   * 1. Request a presigned URL from the backend
   * 2. PUT the file directly to S3
   * 3. Return the file code and public URL
   */
  upload: async (file: File, folderName = 'menu-items'): Promise<{ code: string; url: string }> => {
    // Step 1: Get presigned URL
    const requests: PreSignedUrlRequest[] = [{
      fileName: file.name,
      folderName,
      type: 'IMAGE',
    }]
    const responses = await api.post<PreSignedUrlResponse[]>('/files/pre-signed-url', requests)
      .then(r => r.data)

    const { id, url, preSignedUrl } = responses[0]

    // Step 2: Upload file directly to S3
    await fetch(preSignedUrl, {
      method: 'PUT',
      body: file,
      headers: { 'Content-Type': file.type },
    })

    // Step 3: Return file code and public URL
    return { code: id, url }
  },

  get: (code: string): Promise<FileResponse> =>
    api.get<FileResponse>(`/files/${code}`).then(r => r.data),
}
