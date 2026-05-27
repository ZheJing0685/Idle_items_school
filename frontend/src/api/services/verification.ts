import { get, post } from '../config/http';
import type { VerificationRecord, VerificationRequest } from '../../types/api';

const verification = {
  getStatus: () => get<VerificationRecord>('/verification/status'),
  submit: (data: VerificationRequest) => post<VerificationRecord>('/verification/submit', data),
  upload: (data: FormData) => post<{ url: string }>('/verification/upload', data),
  resubmit: (data: VerificationRequest) => post<VerificationRecord>('/verification/resubmit', data),
};

export default verification;
