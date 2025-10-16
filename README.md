
# TodoAIAssistant（M1 原型）

**说明：中文内容，英文文件名** —— 按你的偏好设置。此仓库为 Android 首版，可后续扩展 Windows 端（Tauri）在后续里程碑。

## 功能概览（M1）
- 代办（Room）：新增/勾选完成
- 桌面小插件（AppWidget）：显示标题、点击进入应用
- 一键汇总（OpenAI）：示例调用封装，支持网关与代理（默认关闭）
- 日历写入：写入系统日历（需授权）与后续内置日历（DB表）
- 通知（预留）：后续汇总完成与到期提醒

## 配置
- `app/src/main/assets/config.example.yaml` 提供网关、代理、模型示例。
- 实际运行时，请在应用内设置页导入 YAML（M2 将提供 UI），或在代码中设置 `OpenAIConfig`。

## 构建
- Android Studio Ladybug / AGP 8.5+ / Kotlin 2.0+ / Compose Material3。
- 首次构建：`./gradlew :app:assembleDebug`

## 权限
- 通知、读取/写入日历。首次启动会弹窗请求。

## 后续计划（与需求对齐）
- M2：系统日历选择器 + 内置日历表；OpenAI 汇总结果入库 + 通知；Widget 勾选/新增快捷。
- M3：代理/网关测试面板；失败重试与日志；导入导出。
