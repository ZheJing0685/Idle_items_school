import instance from '../config/axios';

const verification = {
  getStatus: () => instance.get('/verification/status'),
  submit: (data) => instance.post('/verification/submit', data),
  upload: (data) => instance.post('/verification/upload', data),
  resubmit: (data) => instance.post('/verification/resubmit', data),
};

export default verification;
