import instance from '../config/axios';

const user = {
  getItems: (status, page, size) =>
    instance.get('/items/user', { params: { status, page, size } }),
  getProfile: () => instance.get('/users/profile'),
  updateProfile: (data) => instance.put('/users/profile', data),
};

export default user;
