async function globalTeardown() {
  // 清理测试数据
  console.log('Running global teardown...');

  // 可选：清理截屏和视频
  // const fs = require('fs')
  // const path = require('path')
  // const reportsDir = path.join(__dirname, '..', 'reports')
  // if (fs.existsSync(reportsDir)) {
  //   // 保留最近的报告，清理旧报告
  // }

  console.log('✅ Global teardown completed');
}

export default globalTeardown;
