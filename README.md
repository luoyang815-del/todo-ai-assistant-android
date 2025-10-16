
# TodoAIAssistant（M2 配置页 + 汇总链路）
- Kotlin 2.0 + Compose 编译器插件
- AndroidX / Jetifier 已启用
- 设置页：Base URL（网关可填）、API Key、Model、Proxy JSON、Gateway Basic、YAML 导入
- 汇总：将 To-Do 列表发到 OpenAI（通过你的网关/代理），把返回内容入库并展示
- 日历：系统日历写入通路
- CI：先生成 Wrapper(8.7)，再 `./gradlew` 构建

进入 App → 底部导航 "设置" 配置完成后 → 回到 "待办" 点击 “汇总到 OpenAI 并入库”。
