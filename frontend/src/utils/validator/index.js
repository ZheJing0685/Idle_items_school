export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(phone);
};

export const validatePassword = (password) => {
  if (!password) return false;
  if (password.length < 8 || password.length > 32) return false;
  const hasUpper = /[A-Z]/.test(password);
  const hasLower = /[a-z]/.test(password);
  const hasNumber = /\d/.test(password);
  const hasSpecial = /[@$!%*?&]/.test(password);
  return hasUpper && hasLower && hasNumber && hasSpecial;
};

export const validateUsername = (username) => {
  if (!username) return false;
  return username.length >= 3 && username.length <= 20;
};

export const validateNickname = (nickname) => {
  if (!nickname) return false;
  return nickname.length >= 2 && nickname.length <= 20;
};

export const validatePrice = (price) => {
  if (price === null || price === undefined) return false;
  const priceNum = parseFloat(price);
  return !isNaN(priceNum) && priceNum > 0 && priceNum <= 999999;
};

export const validateTitle = (title) => {
  if (!title) return false;
  return title.length >= 3 && title.length <= 50;
};

export const validateDescription = (description) => {
  if (!description) return false;
  return description.length >= 10 && description.length <= 1000;
};

export const validateImage = (file) => {
  if (!file) return false;
  const maxSize = 5 * 1024 * 1024; // 5MB
  if (file.size > maxSize) return false;
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  return allowedTypes.includes(file.type);
};

export const validateArray = (array, minLength = 0, maxLength = Infinity) => {
  if (!Array.isArray(array)) return false;
  return array.length >= minLength && array.length <= maxLength;
};

export const validateRequired = (value) => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim() !== '';
  if (Array.isArray(value)) return value.length > 0;
  return true;
};

export const validateMinLength = (value, min) => {
  if (!value) return false;
  if (typeof value === 'string') return value.length >= min;
  if (Array.isArray(value)) return value.length >= min;
  return false;
};

export const validateMaxLength = (value, max) => {
  if (!value) return true;
  if (typeof value === 'string') return value.length <= max;
  if (Array.isArray(value)) return value.length <= max;
  return true;
};

export const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度8-32个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validatePassword(value)) {
          callback(new Error('密码必须包含大小写字母、数字和特殊字符'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validateEmail(value)) {
          callback(new Error('邮箱格式不正确'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validatePhone(value)) {
          callback(new Error('手机号格式不正确'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' }
  ]
};

export const itemRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 3, max: 50, message: '标题长度3-50个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!validatePrice(value)) {
          callback(new Error('价格必须大于0且不超过999999'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度10-1000个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  images: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少上传一张图片'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
};

export default {
  validateEmail,
  validatePhone,
  validatePassword,
  validateUsername,
  validateNickname,
  validatePrice,
  validateTitle,
  validateDescription,
  validateImage,
  validateArray,
  validateRequired,
  validateMinLength,
  validateMaxLength,
  formRules,
  itemRules
};
