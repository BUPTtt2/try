# Tilo - 文艺生活记录 APP

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.0-pink.svg)

**一款用 Jetpack Compose 和 Material Design 3 开发的文艺风生活记录应用**

</div>

---

## ✨ 功能特点

| 功能 | 说明 |
|------|------|
| 🎨 瀑布流展示 | 图片优先的文艺风格卡片布局 |
| ✨ AI 智能生成 | 输入关键词，ModelScope 千问大模型生成优美内容 |
| 📍 位置记录 | 支持 GPS 定位，记录生活足迹 |
| 🏷️ 标签分类 | 便捷的多标签分类管理 |
| 💬 评论互动 | 每条记录下方实时评论 |
| 📷 多图上传 | 支持一次选择并上传多张图片 |
| 🔍 搜索筛选 | 按内容、标签快速查找 |

---

## 🛠️ 技术栈

| 分类 | 技术 |
|------|------|
| **语言** | Kotlin |
| **UI** | Jetpack Compose + Material Design 3 |
| **数据库** | Supabase (PostgreSQL) |
| **文件存储** | Supabase Storage |
| **AI** | ModelScope Qwen 大模型 |
| **定位** | Google Play Services Location |
| **图片加载** | Coil |
| **网络** | OkHttp + Retrofit + Gson |

---

## 📱 界面预览

| 主界面 | AI 生成 | 记录详情 |
|:------:|:-------:|:--------:|
| 瀑布流卡片 | 关键词生成 | 大图展示 |
| 标签筛选 | 智能续写 | 评论互动 |

---

## 📂 项目结构

```
tilo/
├── app/
│   └── src/main/
│       ├── java/com/example/try3/
│       │   ├── data/
│       │   │   ├── ai/              # AI 生成
│       │   │   ├── location/        # GPS 定位
│       │   │   ├── model/           # 数据模型
│       │   │   ├── repository/      # 数据仓库
│       │   │   └── supabase/        # 云端 API
│       │   ├── ui/theme/            # 主题配色
│       │   └── MainActivity.kt      # 主界面
│       └── res/                     # 资源文件
├── portfolio/                       # 作品集（面试用）
├── gradle/                          # Gradle 配置
└── build.gradle.kts                 # 项目构建配置
```

---

## 🚀 本地运行

### 前置条件

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 24+
- JDK 11+

### 步骤

1. **克隆项目**

```bash
git clone https://github.com/yourusername/tilo.git
cd tilo
```

2. **创建 Supabase 项目**

- 注册 [Supabase](https://supabase.com)
- 创建新项目
- 在 SQL Editor 执行 `supabase_setup.sql`

3. **创建 Storage 存储桶**

- 进入 Storage -> New bucket
- 名称: `images`
- 勾选 Public bucket
- 添加 RLS 策略允许公开访问

4. **配置环境变量**

在 `gradle.properties` 中添加：

```properties
# Supabase 配置
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key

# ModelScope AI 配置
AI_API_KEY=your-model-scope-api-key
AI_BASE_URL=https://api-inference.modelscope.cn/v1
```

5. **运行项目**

在 Android Studio 中打开项目，点击 Run 按钮即可。

---

## 🎯 项目亮点

### 1. 现代化 UI 开发

- 100% Jetpack Compose，无 XML 布局
- Material Design 3 动态配色
- 梦幻粉紫渐变主题

### 2. 云端架构

- Supabase REST API 封装
- 异步数据流处理
- 完善的错误处理

### 3. AI 集成

- ModelScope 千问大模型调用
- 智能内容生成
- 本地备用方案

### 4. 用户体验

- 瀑布流布局展示
- 流畅的动画效果
- 完善的加载状态

---

## 📋 作品集交付

作品集相关文件位于 `portfolio/` 目录：

```
portfolio/
├── README.md              # 作品集入口
├── 产品设计文档.pdf        # 完整设计文档
├── 开发路径.md            # 学习路径
└── screenshots/          # 功能截图
    ├── 1-主界面瀑布流.png
    ├── 2-AI生成功能.png
    ├── 3-记录详情.png
    └── 4-标签筛选.png
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

本项目仅供学习交流使用。

---

<p align="center">
  <sub>Built with ❤️ by 叶粟臻</sub>
</sub>
</p>