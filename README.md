
# TodoAIAssistant（M1 原型）

**说明：中文内容，英文文件名** —— 按你的偏好设置。此仓库为 Android 首版，已内置系统日历写入与桌面小插件骨架。

## 本地构建
- Android Studio 打开后直接运行。
- CI（无 Gradle Wrapper）：GitHub Actions 中使用预装 Gradle 运行 `gradle :app:assembleDebug`。

## 权限
- 通知、读取/写入日历。首次启动会弹窗请求。
