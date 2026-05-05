# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: user-flows.spec.js >> 首页流程测试 >> 首页正常加载
- Location: tests\e2e\user-flows.spec.js:9:3

# Error details

```
Error: expect(page).toHaveTitle(expected) failed

Expected pattern: /闲置物品交易平台/
Received string:  "首页"
Timeout: 5000ms

Call log:
  - Expect "toHaveTitle" with timeout 5000ms
    9 × unexpected value "首页"

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - banner [ref=e4]:
    - generic [ref=e7]:
      - link "闲置好物" [ref=e8] [cursor=pointer]:
        - /url: /
        - img [ref=e10]
        - generic [ref=e15]: 闲置好物
      - generic [ref=e18]:
        - generic [ref=e19]:
          - img [ref=e21]
          - textbox "搜索你想要的闲置好物..." [ref=e24]
        - button "搜索" [ref=e26] [cursor=pointer]:
          - generic [ref=e27]: 搜索
      - navigation [ref=e28]:
        - link [ref=e29] [cursor=pointer]:
          - /url: /items
          - img [ref=e30]
        - link "登录" [ref=e35] [cursor=pointer]:
          - /url: /login
        - link "注册" [ref=e36] [cursor=pointer]:
          - /url: /register
    - navigation [ref=e37]:
      - generic [ref=e39]:
        - link [ref=e40] [cursor=pointer]:
          - /url: /
          - img [ref=e41]
        - link [ref=e44] [cursor=pointer]:
          - /url: /items
          - img [ref=e45]
        - link [ref=e50] [cursor=pointer]:
          - /url: /items?category=1
        - link [ref=e51] [cursor=pointer]:
          - /url: /items?category=2
        - link [ref=e52] [cursor=pointer]:
          - /url: /items?category=3
        - link [ref=e53] [cursor=pointer]:
          - /url: /items?category=4
        - link [ref=e54] [cursor=pointer]:
          - /url: /items?category=5
        - link [ref=e55] [cursor=pointer]:
          - /url: /items?category=6
        - link [ref=e56] [cursor=pointer]:
          - /url: /items?category=7
  - main [ref=e57]:
    - generic [ref=e58]:
      - generic [ref=e61]:
        - generic [ref=e62]:
          - generic [ref=e63]:
            - img [ref=e64]
            - generic [ref=e67]: 校园绿色交易平台
          - heading "闲置不闲置 变废为宝" [level=1] [ref=e68]:
            - text: 闲置不闲置
            - text: 变废为宝
          - paragraph [ref=e69]:
            - text: 让闲置物品找到新主人，让资源得到充分利用。
            - text: 绿色校园，你我共创。
          - generic [ref=e70]:
            - link "探索好物" [ref=e71] [cursor=pointer]:
              - /url: /items
              - img [ref=e72]
              - text: 探索好物
            - link "发布闲置" [ref=e75] [cursor=pointer]:
              - /url: /publish
              - img [ref=e76]
              - text: 发布闲置
        - generic [ref=e78]:
          - generic [ref=e79]:
            - img "二手笔记本" [ref=e81]
            - img "二手教材" [ref=e83]
            - img "运动手环" [ref=e85]
          - generic [ref=e86]:
            - img [ref=e87]
            - generic [ref=e90]: 已节省 128.5 吨碳排放
      - generic [ref=e93]:
        - generic [ref=e94]:
          - generic [ref=e95]: 12,847
          - text: 成功交易
        - generic [ref=e96]:
          - generic [ref=e97]: 8,234
          - text: 注册用户
        - generic [ref=e98]:
          - generic [ref=e99]: 45,231
          - text: 发布物品
        - generic [ref=e100]:
          - generic [ref=e101]: "128.5"
          - text: 吨碳减排
      - generic [ref=e103]:
        - generic [ref=e105]:
          - heading "分类浏览" [level=2] [ref=e106]
          - paragraph [ref=e107]: 找到你需要的闲置好物
        - generic [ref=e108]:
          - link "📱 数码产品 7 件物品" [ref=e109] [cursor=pointer]:
            - /url: /items?category=1
            - generic [ref=e110]: 📱
            - generic [ref=e111]:
              - heading "数码产品" [level=3] [ref=e112]
              - text: 7 件物品
            - img [ref=e113]
          - link "📚 书籍教材 5 件物品" [ref=e115] [cursor=pointer]:
            - /url: /items?category=2
            - generic [ref=e116]: 📚
            - generic [ref=e117]:
              - heading "书籍教材" [level=3] [ref=e118]
              - text: 5 件物品
            - img [ref=e119]
          - link "🏠 服饰鞋包 3 件物品" [ref=e121] [cursor=pointer]:
            - /url: /items?category=3
            - generic [ref=e122]: 🏠
            - generic [ref=e123]:
              - heading "服饰鞋包" [level=3] [ref=e124]
              - text: 3 件物品
            - img [ref=e125]
          - link "⚽ 生活用品 3 件物品" [ref=e127] [cursor=pointer]:
            - /url: /items?category=4
            - generic [ref=e128]: ⚽
            - generic [ref=e129]:
              - heading "生活用品" [level=3] [ref=e130]
              - text: 3 件物品
            - img [ref=e131]
          - link "👔 运动户外 1 件物品" [ref=e133] [cursor=pointer]:
            - /url: /items?category=5
            - generic [ref=e134]: 👔
            - generic [ref=e135]:
              - heading "运动户外" [level=3] [ref=e136]
              - text: 1 件物品
            - img [ref=e137]
          - link "📦 虚拟物品 0 件物品" [ref=e139] [cursor=pointer]:
            - /url: /items?category=6
            - generic [ref=e140]: 📦
            - generic [ref=e141]:
              - heading "虚拟物品" [level=3] [ref=e142]
              - text: 0 件物品
            - img [ref=e143]
          - link "📱 其他 0 件物品" [ref=e145] [cursor=pointer]:
            - /url: /items?category=7
            - generic [ref=e146]: 📱
            - generic [ref=e147]:
              - heading "其他" [level=3] [ref=e148]
              - text: 0 件物品
            - img [ref=e149]
      - generic [ref=e152]:
        - generic [ref=e153]:
          - generic [ref=e154]:
            - heading "热门好物" [level=2] [ref=e155]
            - paragraph [ref=e156]: 精选优质闲置，抢手好货
          - link "查看更多" [ref=e157] [cursor=pointer]:
            - /url: /items
            - text: 查看更多
            - img [ref=e158]
        - generic [ref=e160]:
          - article [ref=e161] [cursor=pointer]:
            - generic [ref=e162]:
              - img "iPhone 14 Pro Max 256G 暗紫色" [ref=e163]
              - button "查看详情" [ref=e165]:
                - img [ref=e166]
                - text: 查看详情
            - generic [ref=e169]:
              - heading "iPhone 14 Pro Max 256G 暗紫色" [level=3] [ref=e170]
              - generic [ref=e171]:
                - generic [ref=e172]: ¥5999
                - generic [ref=e173]: ¥8999
              - generic [ref=e174]:
                - generic [ref=e175]:
                  - generic [ref=e176]: 未
                  - generic [ref=e177]: 未知卖家
                - generic [ref=e179]:
                  - img [ref=e180]
                  - text: "1270"
          - article [ref=e183] [cursor=pointer]:
            - generic [ref=e184]:
              - img "小米笔记本Pro 15 2022款" [ref=e185]
              - button "查看详情" [ref=e187]:
                - img [ref=e188]
                - text: 查看详情
            - generic [ref=e191]:
              - heading "小米笔记本Pro 15 2022款" [level=3] [ref=e192]
              - generic [ref=e193]:
                - generic [ref=e194]: ¥4599
                - generic [ref=e195]: ¥6499
              - generic [ref=e196]:
                - generic [ref=e197]:
                  - generic [ref=e198]: 未
                  - generic [ref=e199]: 未知卖家
                - generic [ref=e201]:
                  - img [ref=e202]
                  - text: "897"
          - article [ref=e205] [cursor=pointer]:
            - generic [ref=e206]:
              - img "iPad Air 5 64G WiFi版 星光色" [ref=e207]
              - button "查看详情" [ref=e209]:
                - img [ref=e210]
                - text: 查看详情
            - generic [ref=e213]:
              - heading "iPad Air 5 64G WiFi版 星光色" [level=3] [ref=e214]
              - generic [ref=e215]:
                - generic [ref=e216]: ¥3299
                - generic [ref=e217]: ¥4799
              - generic [ref=e218]:
                - generic [ref=e219]:
                  - generic [ref=e220]: 未
                  - generic [ref=e221]: 未知卖家
                - generic [ref=e223]:
                  - img [ref=e224]
                  - text: "789"
          - article [ref=e227] [cursor=pointer]:
            - generic [ref=e228]:
              - img "索尼WH-1000XM4降噪耳机" [ref=e229]
              - button "查看详情" [ref=e231]:
                - img [ref=e232]
                - text: 查看详情
            - generic [ref=e235]:
              - heading "索尼WH-1000XM4降噪耳机" [level=3] [ref=e236]
              - generic [ref=e237]:
                - generic [ref=e238]: ¥1299
                - generic [ref=e239]: ¥2299
              - generic [ref=e240]:
                - generic [ref=e241]:
                  - generic [ref=e242]: 未
                  - generic [ref=e243]: 未知卖家
                - generic [ref=e245]:
                  - img [ref=e246]
                  - text: "678"
          - article [ref=e249] [cursor=pointer]:
            - generic [ref=e250]:
              - img "AirPods Pro 2代 带充电盒" [ref=e251]
              - button "查看详情" [ref=e253]:
                - img [ref=e254]
                - text: 查看详情
            - generic [ref=e257]:
              - heading "AirPods Pro 2代 带充电盒" [level=3] [ref=e258]
              - generic [ref=e259]:
                - generic [ref=e260]: ¥1299
                - generic [ref=e261]: ¥1899
              - generic [ref=e262]:
                - generic [ref=e263]:
                  - generic [ref=e264]: 未
                  - generic [ref=e265]: 未知卖家
                - generic [ref=e267]:
                  - img [ref=e268]
                  - text: "570"
          - article [ref=e271] [cursor=pointer]:
            - generic [ref=e272]:
              - img "芙丽芳丝洗面奶100g" [ref=e273]
              - button "查看详情" [ref=e275]:
                - img [ref=e276]
                - text: 查看详情
            - generic [ref=e279]:
              - heading "芙丽芳丝洗面奶100g" [level=3] [ref=e280]
              - generic [ref=e281]:
                - generic [ref=e282]: ¥89
                - generic [ref=e283]: ¥150
              - generic [ref=e284]:
                - generic [ref=e285]:
                  - generic [ref=e286]: 未
                  - generic [ref=e287]: 未知卖家
                - generic [ref=e289]:
                  - img [ref=e290]
                  - text: "567"
          - article [ref=e293] [cursor=pointer]:
            - generic [ref=e294]:
              - img "高等数学同济第七版上下册" [ref=e295]
              - button "查看详情" [ref=e297]:
                - img [ref=e298]
                - text: 查看详情
            - generic [ref=e301]:
              - heading "高等数学同济第七版上下册" [level=3] [ref=e302]
              - generic [ref=e303]:
                - generic [ref=e304]: ¥35
                - generic [ref=e305]: ¥68
              - generic [ref=e306]:
                - generic [ref=e307]:
                  - generic [ref=e308]: 未
                  - generic [ref=e309]: 未知卖家
                - generic [ref=e311]:
                  - img [ref=e312]
                  - text: "456"
          - article [ref=e315] [cursor=pointer]:
            - generic [ref=e316]:
              - img "Nike Air Force 1 白色 42码" [ref=e317]
              - button "查看详情" [ref=e319]:
                - img [ref=e320]
                - text: 查看详情
            - generic [ref=e323]:
              - heading "Nike Air Force 1 白色 42码" [level=3] [ref=e324]
              - generic [ref=e325]:
                - generic [ref=e326]: ¥399
                - generic [ref=e327]: ¥799
              - generic [ref=e328]:
                - generic [ref=e329]:
                  - generic [ref=e330]: 未
                  - generic [ref=e331]: 未知卖家
                - generic [ref=e333]:
                  - img [ref=e334]
                  - text: "446"
      - generic [ref=e339]:
        - generic [ref=e340]:
          - generic [ref=e341]: 新用户专享
          - heading "注册即送100积分" [level=2] [ref=e342]
          - paragraph [ref=e343]: 首单立减10元，让交易更划算
          - link "立即注册" [ref=e344] [cursor=pointer]:
            - /url: /register
            - text: 立即注册
            - img [ref=e345]
        - img "新用户优惠" [ref=e348]
      - generic [ref=e351]:
        - generic [ref=e352]:
          - img [ref=e354]
          - heading "实名认证" [level=3] [ref=e357]
          - paragraph [ref=e358]: 所有用户经过学生身份认证，交易更放心
        - generic [ref=e359]:
          - img [ref=e361]
          - heading "快捷发布" [level=3] [ref=e363]
          - paragraph [ref=e364]: 拍照上传，简单几步即可发布你的闲置
        - generic [ref=e365]:
          - img [ref=e367]
          - heading "环保交易" [level=3] [ref=e371]
          - paragraph [ref=e372]: 减少资源浪费，为绿色校园贡献力量
  - contentinfo [ref=e373]:
    - generic [ref=e376]:
      - generic [ref=e377]:
        - generic [ref=e378]:
          - img [ref=e379]
          - generic [ref=e383]:
            - generic [ref=e384]: 闲置好物
            - generic [ref=e385]: 校园绿色交易平台
        - paragraph [ref=e386]: 让闲置物品找到新主人，让资源得到充分利用。绿色校园，你我共创。
        - generic [ref=e387]:
          - img [ref=e388]
          - generic [ref=e392]: 环保交易 · 减少浪费
      - generic [ref=e393]:
        - heading "快速链接" [level=4] [ref=e394]
        - navigation [ref=e395]:
          - link "首页" [ref=e396] [cursor=pointer]:
            - /url: /
          - link "发现好物" [ref=e397] [cursor=pointer]:
            - /url: /items
          - link "发布闲置" [ref=e398] [cursor=pointer]:
            - /url: /publish
          - link "我的订单" [ref=e399] [cursor=pointer]:
            - /url: /orders
      - generic [ref=e400]:
        - heading "分类浏览" [level=4] [ref=e401]
        - navigation [ref=e402]:
          - link "数码产品" [ref=e403] [cursor=pointer]:
            - /url: /items?category=1
          - link "书籍教材" [ref=e404] [cursor=pointer]:
            - /url: /items?category=2
          - link "生活用品" [ref=e405] [cursor=pointer]:
            - /url: /items?category=3
          - link "运动器材" [ref=e406] [cursor=pointer]:
            - /url: /items?category=4
          - link "服装鞋帽" [ref=e407] [cursor=pointer]:
            - /url: /items?category=5
      - generic [ref=e408]:
        - heading "帮助与支持" [level=4] [ref=e409]
        - navigation [ref=e410]:
          - link "常见问题" [ref=e411] [cursor=pointer]:
            - /url: "#"
          - link "交易指南" [ref=e412] [cursor=pointer]:
            - /url: "#"
          - link "联系客服" [ref=e413] [cursor=pointer]:
            - /url: "#"
          - link "意见反馈" [ref=e414] [cursor=pointer]:
            - /url: "#"
      - generic [ref=e415]:
        - heading "联系我们" [level=4] [ref=e416]
        - generic [ref=e417]:
          - img [ref=e418]
          - generic [ref=e421]: contact@xianhaowu.com
        - generic [ref=e422]:
          - img [ref=e423]
          - generic [ref=e425]: 400-888-6666
        - generic [ref=e426]:
          - img [ref=e427]
          - generic [ref=e430]: 校园大学生活动中心
    - generic [ref=e433]:
      - paragraph [ref=e434]: © 2026 闲置好物 · 校园绿色交易平台 · 变废为宝，绿色校园
      - generic [ref=e436]:
        - img [ref=e437]
        - text: 已帮助 12,847 件物品找到新主人
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test'
  2   | 
  3   | /**
  4   |  * 用户交互流程测试套件
  5   |  * 覆盖主要功能模块的端到端流程
  6   |  */
  7   | 
  8   | test.describe('首页流程测试', () => {
  9   |   test('首页正常加载', async ({ page }) => {
  10  |     await page.goto('/')
  11  |     await page.waitForLoadState('networkidle')
  12  |     
  13  |     // 检查页面标题
> 14  |     await expect(page).toHaveTitle(/闲置物品交易平台/)
      |                        ^ Error: expect(page).toHaveTitle(expected) failed
  15  |     
  16  |     // 检查 Header
  17  |     await expect(page.locator('header, .header').first()).toBeVisible()
  18  |     
  19  |     // 检查 Footer
  20  |     const footer = page.locator('footer, .footer').first()
  21  |     if (await footer.count() > 0) {
  22  |       await expect(footer).toBeVisible()
  23  |     }
  24  |   })
  25  | 
  26  |   test('首页搜索功能', async ({ page }) => {
  27  |     await page.goto('/')
  28  |     await page.waitForLoadState('networkidle')
  29  | 
  30  |     // 找到搜索框并输入关键词
  31  |     const searchInput = page.locator('input[placeholder*="搜索"]').first()
  32  |     if (await searchInput.isVisible()) {
  33  |       await searchInput.fill('手机')
  34  |       await searchInput.press('Enter')
  35  |       await page.waitForTimeout(1000)
  36  | 
  37  |       // 应该跳转到搜索结果页或显示结果
  38  |       const body = await page.textContent('body')
  39  |       expect(body).toBeTruthy()
  40  |     }
  41  |   })
  42  | 
  43  |   test('首页导航到登录页', async ({ page }) => {
  44  |     await page.goto('/')
  45  |     await page.waitForLoadState('networkidle')
  46  | 
  47  |     const loginBtn = page.getByRole('button', { name: /登录/ }).first()
  48  |     if (await loginBtn.isVisible()) {
  49  |       await loginBtn.click()
  50  |       await expect(page).toHaveURL(/\/login/)
  51  |     }
  52  |   })
  53  | })
  54  | 
  55  | test.describe('浏览物品流程测试', () => {
  56  |   test('物品列表页正常加载', async ({ page }) => {
  57  |     await page.goto('/items')
  58  |     await page.waitForLoadState('networkidle')
  59  | 
  60  |     // 检查页面主要元素
  61  |     await expect(page.locator('body')).toBeVisible()
  62  |   })
  63  | 
  64  |   test('物品分类筛选功能', async ({ page }) => {
  65  |     await page.goto('/items')
  66  |     await page.waitForLoadState('networkidle')
  67  |     await page.waitForTimeout(1000)
  68  | 
  69  |     // 检查页面不崩溃即可
  70  |     await expect(page.locator('body')).toBeVisible()
  71  |   })
  72  | 
  73  |   test('物品详情页可访问', async ({ page }) => {
  74  |     // 先访问物品列表
  75  |     await page.goto('/items')
  76  |     await page.waitForLoadState('networkidle')
  77  | 
  78  |     // 尝试找一个物品链接
  79  |     const itemLinks = page.locator('a[href*="/item/"]')
  80  |     const itemCount = await itemLinks.count()
  81  | 
  82  |     if (itemCount > 0) {
  83  |       await itemLinks.first().click()
  84  |       await page.waitForLoadState('networkidle')
  85  |       await expect(page.locator('body')).toBeVisible()
  86  |     } else {
  87  |       // 直接测试详情页 URL（如果有测试数据 ID）
  88  |       await page.goto('/item/1')
  89  |       await page.waitForTimeout(1000)
  90  |       // 页面不应崩溃
  91  |       expect(page.locator('body')).toBeVisible()
  92  |     }
  93  |   })
  94  | })
  95  | 
  96  | test.describe('个人中心流程测试', () => {
  97  |   test('未登录访问个人中心应跳转到登录页', async ({ page }) => {
  98  |     // 清除登录状态
  99  |     await page.goto('/')
  100 |     await page.evaluate(() => localStorage.clear())
  101 | 
  102 |     // 尝试访问个人中心
  103 |     await page.goto('/user')
  104 |     await page.waitForTimeout(1000)
  105 | 
  106 |     // 应该跳转到登录页
  107 |     await expect(page).toHaveURL(/\/login/)
  108 |   })
  109 | 
  110 |   test('个人中心页面正常加载（需登录）', async ({ page }) => {
  111 |     // 模拟登录状态
  112 |     await page.goto('/login')
  113 |     await page.evaluate(() => {
  114 |       localStorage.setItem('idle_items_token', 'mock-token')
```