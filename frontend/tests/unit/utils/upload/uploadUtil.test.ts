import { describe, it, expect } from 'vitest';
import UploadUtil from '@/utils/upload/uploadUtil';

describe('UploadUtil工具', () => {
  describe('getFileExtension', () => {
    it('应该能获取文件扩展名', () => {
      expect(UploadUtil.getFileExtension('test.jpg')).toBe('jpg');
      expect(UploadUtil.getFileExtension('image.png')).toBe('png');
      expect(UploadUtil.getFileExtension('document.pdf')).toBe('pdf');
    });

    it('应该处理没有扩展名的文件', () => {
      expect(UploadUtil.getFileExtension('filename')).toBe('');
    });

    it('应该处理多个点的文件名', () => {
      expect(UploadUtil.getFileExtension('file.name.jpg')).toBe('jpg');
    });
  });

  describe('getFileSize', () => {
    it('应该能格式化文件大小', () => {
      expect(UploadUtil.getFileSize(0)).toBe('0 Bytes');
      expect(UploadUtil.getFileSize(1024)).toBe('1 KB');
      expect(UploadUtil.getFileSize(1024 * 1024)).toBe('1 MB');
      expect(UploadUtil.getFileSize(1024 * 1024 * 1024)).toBe('1 GB');
    });

    it('应该能格式化小数文件大小', () => {
      expect(UploadUtil.getFileSize(1023)).toBe('1023 Bytes');
      expect(UploadUtil.getFileSize(1536)).toBe('1.5 KB');
      expect(UploadUtil.getFileSize(1.5 * 1024 * 1024)).toBe('1.5 MB');
    });
  });

  describe('validateImage', () => {
    it('应该验证有效的图片文件', () => {
      const file = new File([''], 'test.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 1024 * 100 });

      const result = UploadUtil.validateImage(file);
      expect(result.valid).toBe(true);
      expect(result.message).toBe('');
    });

    it('应该拒绝空文件', () => {
      const result = UploadUtil.validateImage(null);
      expect(result.valid).toBe(false);
      expect(result.message).toBe('请选择文件');
    });

    it('应该拒绝过大的文件', () => {
      const file = new File([''], 'large.jpg', { type: 'image/jpeg' });
      Object.defineProperty(file, 'size', { value: 6 * 1024 * 1024 });

      const result = UploadUtil.validateImage(file);
      expect(result.valid).toBe(false);
      expect(result.message).toContain('文件大小不能超过');
    });

    it('应该拒绝不支持的文件类型', () => {
      const file = new File([''], 'test.gif', { type: 'image/gif' });
      Object.defineProperty(file, 'size', { value: 1024 });

      const result = UploadUtil.validateImage(file);
      expect(result.valid).toBe(false);
      expect(result.message).toContain('只支持 JPG、PNG、WebP');
    });

    it('应该接受PNG格式', () => {
      const file = new File([''], 'test.png', { type: 'image/png' });
      Object.defineProperty(file, 'size', { value: 1024 });

      const result = UploadUtil.validateImage(file);
      expect(result.valid).toBe(true);
    });

    it('应该接受WebP格式', () => {
      const file = new File([''], 'test.webp', { type: 'image/webp' });
      Object.defineProperty(file, 'size', { value: 1024 });

      const result = UploadUtil.validateImage(file);
      expect(result.valid).toBe(true);
    });
  });

  describe('calculateHash', () => {
    it('应该能计算哈希值', () => {
      const buffer = new ArrayBuffer(8);
      const view = new Uint8Array(buffer);
      view[0] = 1;
      view[1] = 2;
      view[2] = 3;

      const hash = UploadUtil.calculateHash(buffer);
      expect(typeof hash).toBe('string');
      expect(hash.length).toBeGreaterThan(0);
    });
  });

  describe('sliceFile', () => {
    it('应该能将文件分割成块', () => {
      const content = new ArrayBuffer(3 * 1024 * 1024);
      const file = new File([content], 'test.bin', { type: 'application/octet-stream' });

      const chunks = UploadUtil.sliceFile(file, 1024 * 1024);
      expect(chunks.length).toBe(3);
    });

    it('应该处理小于块大小的文件', () => {
      const content = new ArrayBuffer(512);
      const file = new File([content], 'test.bin', { type: 'application/octet-stream' });

      const chunks = UploadUtil.sliceFile(file, 1024 * 1024);
      expect(chunks.length).toBe(1);
    });
  });

  describe('getUploadProgress', () => {
    it('应该能计算上传进度', () => {
      expect(UploadUtil.getUploadProgress(0, 100)).toBe(0);
      expect(UploadUtil.getUploadProgress(50, 100)).toBe(50);
      expect(UploadUtil.getUploadProgress(100, 100)).toBe(100);
    });

    it('应该处理小数进度', () => {
      expect(UploadUtil.getUploadProgress(33, 100)).toBe(33);
      expect(UploadUtil.getUploadProgress(1, 3)).toBe(33);
    });
  });

  describe('formatFileSize', () => {
    it('应该能格式化文件大小', () => {
      expect(UploadUtil.formatFileSize(0)).toBe('0 Bytes');
      expect(UploadUtil.formatFileSize(1024)).toBe('1 KB');
      expect(UploadUtil.formatFileSize(1024 * 1024)).toBe('1 MB');
    });
  });

  describe('generateUniqueFilename', () => {
    it('应该能生成唯一文件名', () => {
      const filename1 = UploadUtil.generateUniqueFilename('test.jpg');
      const filename2 = UploadUtil.generateUniqueFilename('test.jpg');

      expect(filename1).not.toBe(filename2);
      expect(filename1).toContain('.jpg');
      expect(filename2).toContain('.jpg');
    });

    it('应该保留原始文件扩展名', () => {
      const filename = UploadUtil.generateUniqueFilename('document.pdf');
      expect(filename).toContain('.pdf');
    });
  });
});
