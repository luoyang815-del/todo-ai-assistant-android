
# TodoAIAssistant（M1 全量修复版）

- 开启 AndroidX / Jetifier（gradle.properties）
- Kotlin 2.0 + Compose：显式启用 Compose 编译器插件（kotlin-compose）
- Compose 编译器版本：1.6.10（与 Kotlin 2.0 兼容）
- CI：生成 Gradle Wrapper(8.7) 后用 `./gradlew` 构建

## 用法
- 本地：Android Studio 打开 → 直接运行。
- CI：推到 GitHub，Actions 自动出 Debug APK。
