<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo">
              <svg width="40" height="40" viewBox="0 0 36 36" fill="none">
                <circle cx="18" cy="18" r="16" fill="var(--primary-color)" />
                <path
                  d="M12 18C12 14.6863 14.6863 12 18 12C21.3137 12 24 14.6863 24 18"
                  stroke="white"
                  stroke-width="2.5"
                  stroke-linecap="round"
                />
                <path
                  d="M18 18V24"
                  stroke="white"
                  stroke-width="2.5"
                  stroke-linecap="round"
                />
                <circle cx="18" cy="14" r="2" fill="white" />
              </svg>
              <div class="footer-logo-text">
                <span class="footer-brand-name">闲置好物</span>
                <span class="footer-brand-tagline">校园绿色交易平台</span>
              </div>
            </div>
            <p class="footer-desc">
              让闲置物品找到新主人，让资源得到充分利用。绿色校园，你我共创。
            </p>
            <div class="footer-eco-badge">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="var(--secondary-color)"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path
                  d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <path
                  d="M12 9C12 9 9 10 9 12"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              <span>环保交易 · 减少浪费</span>
            </div>
          </div>

          <div class="footer-links-group">
            <h4 class="footer-heading">快速链接</h4>
            <nav class="footer-links">
              <router-link to="/">首页</router-link>
              <router-link to="/items">发现好物</router-link>
              <router-link to="/publish">发布闲置</router-link>
              <router-link to="/orders">我的订单</router-link>
            </nav>
          </div>

          <div class="footer-links-group">
            <h4 class="footer-heading">分类浏览</h4>
            <nav class="footer-links">
              <router-link to="/items?category=1">数码产品</router-link>
              <router-link to="/items?category=2">书籍教材</router-link>
              <router-link to="/items?category=3">生活用品</router-link>
              <router-link to="/items?category=4">运动器材</router-link>
              <router-link to="/items?category=5">服装鞋帽</router-link>
            </nav>
          </div>

          <div class="footer-links-group">
            <h4 class="footer-heading">帮助与支持</h4>
            <nav class="footer-links">
              <a href="#">常见问题</a>
              <a href="#">交易指南</a>
              <a href="#">联系客服</a>
              <a href="#">意见反馈</a>
            </nav>
          </div>

          <div class="footer-contact">
            <h4 class="footer-heading">联系我们</h4>
            <div class="contact-item">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z"
                />
                <path d="M22 6L12 13L2 6" />
              </svg>
              <span>contact@xianhaowu.com</span>
            </div>
            <div class="contact-item">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M22 16.92V19.92C22 20.48 21.56 20.96 21 20.99C20.62 21 20.23 21 19.84 21C10.83 21 3.00003 13.17 3.00003 4.16C3.00003 3.77 3.00003 3.38 3.01003 3C3.04003 2.44 3.52003 2 4.08003 2H7.08003C7.56003 2 7.98003 2.34 8.07003 2.81C8.23003 3.67 8.49003 4.5 8.85003 5.29L7.03003 7.11C8.59003 10.39 11.61 13.41 14.89 14.97L16.71 13.15C17.5 13.51 18.33 13.77 19.19 13.93C19.66 14.02 20 14.44 20 14.92V16.92"
                />
              </svg>
              <span>400-888-6666</span>
            </div>
            <div class="contact-item">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z"
                />
                <circle cx="12" cy="10" r="3" />
              </svg>
              <span>校园大学生活动中心</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="container">
        <div class="footer-bottom-content">
          <p class="copyright">
            © 2026 闲置好物 · 校园绿色交易平台 · 变废为宝，绿色校园
          </p>
          <div class="footer-stat">
            <span class="stat-item">
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="var(--secondary-color)"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path
                  d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              已帮助 12,847 件物品找到新主人
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup></script>

<style scoped src="../styles/components/footer.css"></style>
