import { describe, it, expect } from 'vitest'
import UploadUtil from '../../../src/utils/upload/uploadUtil'

describe('UploadUtil', () => {
  describe('getFileExtension', () => {
    it('应该返回文件扩展名', () => {
      expect(UploadUtil.getFileExtension('test.jpg')).toBe('jpg')
      expect(UploadUtil.getFileExtension('image.png')).toBe('png')
      expect(UploadUtil.getFileExtension('doc.pdf')).toBe('pdf')
    })

    it('应该处理没有扩展名的文件', () => {
      expect(UploadUtil.getFileExtension('noext')).toBe('')
    })

    it('应该处理多个点的文件名', () => {
      expect(UploadUtil.getFileExtension('file.name.txt')).toBe('txt')
    })
  })

  describe('getFileSize', () => {
    it('应该返回 0 Bytes', () => {
      expect(UploadUtil.getFileSize(0)).toBe('0 Bytes')
    })

    it('应该返回 KB', () => {
      expect(UploadUtil.getFileSize(1024)).toBe('1 KB')
    })

    it('应该返回 MB', () => {
      expect(UploadUtil.getFileSize(1024 * 1024)).toBe('1 MB')
    })

    it('应该返回 GB', () => {
      expect(UploadUtil.getFileSize(1024 * 1024 * 1024)).toBe('1 GB')
    })

    it('应该返回小数值', () => {
      expect(UploadUtil.getFileSize(1536)).toBe('1.5 KB')
    })
  })

  describe('validateImage', () => {
    it('应该拒绝 null 文件', () => {
      const result = UploadUtil.validateImage(null)
      expect(result.valid).toBe(false)
      expect(result.message).toContain('请选择文件')
    })

    it('应该拒绝超大文件', () => {
      const bigFile = new File(['x'.repeat(6 * 1024 * 1024)], 'big.jpg', { type: 'image/jpeg' })
      const result = UploadUtil.validateImage(bigFile)
      expect(result.valid).toBe(false)
      expect(result.message).toContain('不能超过')
    })

    it('应该拒绝不支持的格式', () => {
      const file = new File(['test'], 'test.gif', { type: 'image/gif' })
      const result = UploadUtil.validateImage(file)
      expect(result.valid).toBe(false)
      expect(result.message).toContain('JPG、PNG、WebP')
    })

    it('应该接受有效的 JPG', () => {
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const result = UploadUtil.validateImage(file)
      expect(result.valid).toBe(true)
    })

    it('应该接受有效的 PNG', () => {
      const file = new File(['test'], 'test.png', { type: 'image/png' })
      const result = UploadUtil.validateImage(file)
      expect(result.valid).toBe(true)
    })

    it('应该接受有效的 WebP', () => {
      const file = new File(['test'], 'test.webp', { type: 'image/webp' })
      const result = UploadUtil.validateImage(file)
      expect(result.valid).toBe(true)
    })
  })

  describe('calculateHash', () => {
    it('应该计算哈希值', () => {
      const buffer = new ArrayBuffer(4)
      const view = new Uint8Array(buffer)
      view[0] = 1; view[1] = 2; view[2] = 3; view[3] = 4
      const hash = UploadUtil.calculateHash(buffer)
      expect(typeof hash).toBe('string')
      expect(hash.length).toBeGreaterThan(0)
    })

    it('应该返回十六进制字符串', () => {
      const buffer = new ArrayBuffer(1)
      const hash = UploadUtil.calculateHash(buffer)
      expect(hash).toMatch(/^[0-9a-f]+$/)
    })

    it('相同内容应返回相同哈希', () => {
      const buffer1 = new ArrayBuffer(4)
      const buffer2 = new ArrayBuffer(4)
      new Uint8Array(buffer1).fill(42)
      new Uint8Array(buffer2).fill(42)
      expect(UploadUtil.calculateHash(buffer1)).toBe(UploadUtil.calculateHash(buffer2))
    })
  })

  describe('sliceFile', () => {
    it('应该将文件切片', () => {
      const file = new File(['x'.repeat(3000)], 'test.txt', { type: 'text/plain' })
      const chunks = UploadUtil.sliceFile(file, 1000)
      expect(chunks.length).toBe(3)
    })

    it('小文件应该只有一个切片', () => {
      const file = new File(['small'], 'test.txt', { type: 'text/plain' })
      const chunks = UploadUtil.sliceFile(file, 1024 * 1024)
      expect(chunks.length).toBe(1)
    })
  })

  describe('getUploadProgress', () => {
    it('应该计算上传进度', () => {
      expect(UploadUtil.getUploadProgress(50, 100)).toBe(50)
      expect(UploadUtil.getUploadProgress(0, 100)).toBe(0)
      expect(UploadUtil.getUploadProgress(100, 100)).toBe(100)
    })

    it('应该四舍五入', () => {
      expect(UploadUtil.getUploadProgress(33, 100)).toBe(33)
    })
  })

  describe('formatFileSize', () => {
    it('应该等于 getFileSize', () => {
      expect(UploadUtil.formatFileSize(1024)).toBe(UploadUtil.getFileSize(1024))
    })
  })

  describe('generateUniqueFilename', () => {
    it('应该生成带时间戳的文件名', () => {
      const name = UploadUtil.generateUniqueFilename('photo.jpg')
      expect(name).toContain('.jpg')
      expect(name).toContain('_')
    })

    it('应该保留扩展名', () => {
      const name = UploadUtil.generateUniqueFilename('doc.pdf')
      expect(name.endsWith('.pdf')).toBe(true)
    })

    it('每次应该生成不同的文件名', () => {
      const name1 = UploadUtil.generateUniqueFilename('test.jpg')
      const name2 = UploadUtil.generateUniqueFilename('test.jpg')
      // 极小概率相同，但大多数情况不同
      expect(name1).not.toBe(name2)
    })
  })

  describe('generateFileHash', () => {
    it('应该返回 Promise<string>', async () => {
      const file = new File(['hello'], 'test.txt', { type: 'text/plain' })
      const hash = await UploadUtil.generateFileHash(file)
      expect(typeof hash).toBe('string')
      expect(hash.length).toBeGreaterThan(0)
    })
  })
})
