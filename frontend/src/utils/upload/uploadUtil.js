class UploadUtil {
  static getFileExtension(filename) {
    return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2);
  }

  static getFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  static validateImage(file) {
    if (!file) {
      return { valid: false, message: '请选择文件' };
    }

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      return {
        valid: false,
        message: `文件大小不能超过 ${this.getFileSize(maxSize)}`,
      };
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return { valid: false, message: '只支持 JPG、PNG、WebP 格式的图片' };
    }

    return { valid: true, message: '' };
  }

  static generateFileHash(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = function (event) {
        const arrayBuffer = event.target.result;
        const hash = UploadUtil.calculateHash(arrayBuffer);
        resolve(hash);
      };
      reader.onerror = function () {
        reject(new Error('文件读取失败'));
      };
      reader.readAsArrayBuffer(file);
    });
  }

  static calculateHash(arrayBuffer) {
    const uint8Array = new Uint8Array(arrayBuffer);
    let hash = 0;
    for (let i = 0; i < uint8Array.length; i++) {
      hash = (hash << 5) - hash + uint8Array[i];
      hash |= 0;
    }
    return Math.abs(hash).toString(16);
  }

  static sliceFile(file, chunkSize = 1024 * 1024) {
    const chunks = [];
    let offset = 0;
    while (offset < file.size) {
      const end = Math.min(offset + chunkSize, file.size);
      chunks.push(file.slice(offset, end));
      offset = end;
    }
    return chunks;
  }

  static getUploadProgress(uploaded, total) {
    return Math.round((uploaded / total) * 100);
  }

  static formatFileSize(bytes) {
    return this.getFileSize(bytes);
  }

  static generateUniqueFilename(originalFilename) {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 10000);
    const extension = this.getFileExtension(originalFilename);
    return `${timestamp}_${random}.${extension}`;
  }
}

export default UploadUtil;
