interface FileValidationResult {
  valid: boolean
  message: string
}

class UploadUtil {
  static getFileExtension(filename: string): string {
    return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2);
  }

  static getFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  static validateImage(file: File | null): FileValidationResult {
    if (!file) {
      return { valid: false, message: '请选择文件' };
    }

    const maxSize = 5 * 1024 * 1024;
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

  static async generateFileHash(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = async (e) => {
        const arrayBuffer = e.target?.result as ArrayBuffer;
        if (arrayBuffer) {
          const hash = await UploadUtil.calculateHashAsync(arrayBuffer);
          resolve(hash);
        } else {
          reject(new Error('无法读取文件'));
        }
      };
      reader.onerror = () => reject(new Error('文件读取失败'));
      reader.readAsArrayBuffer(file);
    });
  }

  static calculateHash(arrayBuffer: ArrayBuffer): string {
    const uint8Array = new Uint8Array(arrayBuffer);
    let hash = 0;
    for (let i = 0; i < uint8Array.length; i++) {
      hash = (hash << 5) - hash + uint8Array[i];
      hash |= 0;
    }
    return Math.abs(hash).toString(16);
  }

  static async calculateHashAsync(arrayBuffer: ArrayBuffer): Promise<string> {
    const chunkSize = 1024 * 1024;
    const chunks = Math.ceil(arrayBuffer.byteLength / chunkSize);
    let hash = 0;

    for (let i = 0; i < chunks; i++) {
      const start = i * chunkSize;
      const end = Math.min(start + chunkSize, arrayBuffer.byteLength);
      const chunk = arrayBuffer.slice(start, end);

      await new Promise<void>(resolve => {
        if (typeof requestIdleCallback !== 'undefined') {
          requestIdleCallback(() => resolve(), { timeout: 100 });
        } else {
          setTimeout(() => resolve(), 0);
        }
      });

      const uint8Array = new Uint8Array(chunk);
      for (let j = 0; j < uint8Array.length; j++) {
        hash = (hash << 5) - hash + uint8Array[j];
        hash |= 0;
      }
    }

    return Math.abs(hash).toString(16);
  }

  static async calculateHashWithTimeout(
    arrayBuffer: ArrayBuffer,
    timeoutMs: number = 5000,
  ): Promise<string> {
    return Promise.race([
      UploadUtil.calculateHashAsync(arrayBuffer),
      new Promise<string>((_, reject) =>
        setTimeout(() => reject(new Error('哈希计算超时')), timeoutMs),
      ),
    ]);
  }

  static sliceFile(file: File, chunkSize: number = 1024 * 1024): Blob[] {
    const chunks: Blob[] = [];
    let offset = 0;
    while (offset < file.size) {
      const end = Math.min(offset + chunkSize, file.size);
      chunks.push(file.slice(offset, end));
      offset = end;
    }
    return chunks;
  }

  static getUploadProgress(uploaded: number, total: number): number {
    return Math.round((uploaded / total) * 100);
  }

  static formatFileSize(bytes: number): string {
    return this.getFileSize(bytes);
  }

  static generateUniqueFilename(originalFilename: string): string {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 10000);
    const extension = this.getFileExtension(originalFilename);
    return `${timestamp}_${random}.${extension}`;
  }
}

export default UploadUtil;
