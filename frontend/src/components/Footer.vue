<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo">
              <Package :size="40" stroke-width="1.5" color="var(--primary-color)" />
              <div class="footer-logo-text">
                <span class="footer-brand-name">闲置好物</span>
                <span class="footer-brand-tagline">校园绿色交易平台</span>
              </div>
            </div>
            <p class="footer-desc">
              让闲置物品找到新主人，让资源得到充分利用。绿色校园，你我共创。
            </p>
            <div class="footer-eco-badge">
              <Leaf :size="20" fill="var(--secondary-color)" color="var(--secondary-color)" />
              <span>环保交易 · 减少浪费</span>
            </div>
          </div>

          <div class="footer-links-group">
            <h4 class="footer-heading">快速链接</h4>
            <nav class="footer-links">
              <router-link to="/">首页</router-link>
              <router-link to="/items">发现好物</router-link>
              <router-link to="/publish">发布闲置</router-link>
              <router-link to="/user/orders">我的订单</router-link>
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
              <Mail :size="18" />
              <span>contact@xianhaowu.com</span>
            </div>
            <div class="contact-item">
              <Phone :size="18" />
              <span>400-888-6666</span>
            </div>
            <div class="contact-item">
              <MapPin :size="18" />
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
              <Smile :size="14" fill="var(--secondary-color)" color="var(--secondary-color)" />
              已帮助 12,847 件物品找到新主人
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { Package, Leaf, Mail, Phone, MapPin, Smile } from 'lucide-vue-next';
</script>

<style scoped src="../styles/components/footer.css"></style>
