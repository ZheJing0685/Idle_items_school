<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">
              <Package :size="32" stroke-width="1.5" color="var(--primary-color)" />
              <span class="footer-brand-name">闲置好物</span>
            </div>
            <p class="footer-desc">校园绿色交易平台</p>
          </div>

          <div class="footer-links-group">
            <h4 class="footer-heading">帮助与支持</h4>
            <nav class="footer-links">
              <a href="#" @click.prevent="showComingSoon('常见问题')">常见问题</a>
              <a href="#" @click.prevent="showComingSoon('交易指南')">交易指南</a>
              <a href="#" @click.prevent="showComingSoon('联系客服')">联系客服</a>
              <a href="#" @click.prevent="showComingSoon('意见反馈')">意见反馈</a>
            </nav>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="container">
        <div class="footer-bottom-content">
          <p class="copyright">
            © 2026 闲置好物 · 校园绿色交易平台
          </p>
          <div class="footer-contact">
            <span class="contact-item">
              <Mail :size="14" />
              contact@xianhaowu.com
            </span>
            <span class="contact-item">
              <Phone :size="14" />
              400-888-6666
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { Package, Mail, Phone } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';

const showComingSoon = (name: string) => {
  ElMessage.info(`「${name}」功能即将开放`);
};
</script>

<style scoped>
.footer {
  margin-top: auto;
  background: var(--bg-surface);
  border-top: 1px solid var(--border-subtle);
}

.footer-main {
  padding: var(--space-6) 0;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-8);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.footer-brand-name {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--text-primary);
}

.footer-desc {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin: 0;
}

.footer-links-group {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.footer-heading {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.footer-links {
  display: flex;
  gap: var(--space-4);
}

.footer-links a {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--primary-color);
}

.footer-bottom {
  background: var(--bg-muted);
  padding: var(--space-3) 0;
  border-top: 1px solid var(--border-subtle);
}

.footer-bottom-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.copyright {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin: 0;
}

.footer-contact {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.contact-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.contact-item svg {
  color: var(--primary-color);
}

/* 平板端 */
@media (max-width: 1024px) {
  .footer-content {
    flex-direction: column;
    gap: var(--space-4);
  }

  .footer-links-group {
    flex-direction: column;
    gap: var(--space-2);
  }

  .footer-bottom-content {
    flex-direction: column;
    text-align: center;
  }

  .footer-contact {
    justify-content: center;
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .footer-main {
    padding: var(--space-4) 0;
  }

  .footer-brand {
    flex-direction: column;
    gap: var(--space-1);
    text-align: center;
  }

  .footer-links {
    gap: var(--space-3);
  }

  .footer-links a {
    font-size: var(--text-xs);
  }

  .footer-contact {
    flex-direction: column;
    gap: var(--space-2);
  }
}
</style>
