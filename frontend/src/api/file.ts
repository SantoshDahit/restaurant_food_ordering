import axios from 'axios'
import api from './axios'
import type { FileResponse } from '@/types'

interface PreSignedUrlResponse {
  id: string
  url: string
  preSignedUrl: string
}

export const fileApi = {
  /**
   * Upload a file to S3 via pre-signed URL:
   *   1. Ask backend for a pre-signed PUT URL + public URL + file code
   *   2. PUT the raw file body directly to S3
   * Returns the file code (DB id) and the public S3 URL.
   */
  upload: async (file: File, folderName = 'menu-items'): Promise<{ code: string; url: string }> => {
    const [presigned] = await api.post<PreSignedUrlResponse[]>('/files/pre-signed-url', [
      { fileName: file.name, folderName, type: 'IMAGE' },
    ]).then(r => r.data)

    await axios.put(presigned.preSignedUrl, file, {
      headers: { 'Content-Type': file.type || 'application/octet-stream' },
      transformRequest: [(d) => d],
    })

    return { code: presigned.id, url: presigned.url }
  },

  get: (code: string): Promise<FileResponse> =>
    api.get<FileResponse>(`/files/${code}`).then(r => r.data),
}
