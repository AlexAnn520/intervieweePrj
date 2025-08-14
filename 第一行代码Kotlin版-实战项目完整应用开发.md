# 第一行代码 Kotlin版 - 实战项目完整应用开发篇

> 从零构建企业级Android应用，融合所有Kotlin知识点

## 目录

- [第12章 实战项目：完整Android应用开发](#第12章-实战项目完整android应用开发)
  - [12.1 项目需求分析与架构设计](#121-项目需求分析与架构设计)
  - [12.2 项目环境搭建与基础配置](#122-项目环境搭建与基础配置)
  - [12.3 数据层实现](#123-数据层实现)
  - [12.4 业务逻辑层构建](#124-业务逻辑层构建)
  - [12.5 UI层开发](#125-ui层开发)
  - [12.6 功能模块集成](#126-功能模块集成)
  - [12.7 性能优化与测试](#127-性能优化与测试)
  - [12.8 项目发布与维护](#128-项目发布与维护)

---

## 第12章 实战项目：完整Android应用开发

在前面的章节中，我们学习了Kotlin的各种特性和Android开发技巧。现在，我们将把所有知识点整合起来，从零开始构建一个完整的企业级Android应用——**智能任务管理器TaskMaster**。

### 12.1 项目需求分析与架构设计

#### 12.1.1 项目概述

**TaskMaster** 是一款现代化的任务管理应用，结合了项目管理、团队协作和个人效率工具的功能。

**核心功能：**
- 任务创建与管理（CRUD操作）
- 项目分组管理
- 团队协作与权限控制
- 实时通知与提醒
- 数据同步与离线支持
- 统计报表与数据可视化
- 用户认证与个人资料管理
- 主题切换与个性化设置

#### 12.1.2 技术栈选择

```kotlin
/**
 * TaskMaster应用技术栈
 * 展示现代Android开发的最佳实践
 */
object TechStack {
    
    // 核心框架
    const val KOTLIN_VERSION = "1.9.20"
    const val ANDROID_GRADLE_PLUGIN = "8.1.0"
    const val COMPILE_SDK = 34
    const val MIN_SDK = 24
    const val TARGET_SDK = 34
    
    // 架构组件
    const val LIFECYCLE_VERSION = "2.7.0"
    const val ROOM_VERSION = "2.6.0"
    const val WORK_MANAGER_VERSION = "2.9.0"
    const val NAVIGATION_VERSION = "2.7.5"
    
    // UI框架
    const val COMPOSE_VERSION = "1.5.4"
    const val COMPOSE_MATERIAL3_VERSION = "1.1.2"
    const val COMPOSE_ANIMATION_VERSION = "1.5.4"
    
    // 网络请求
    const val RETROFIT_VERSION = "2.9.0"
    const val OKHTTP_VERSION = "4.12.0"
    const val KOTLINX_SERIALIZATION_VERSION = "1.6.0"
    
    // 依赖注入
    const val HILT_VERSION = "2.48"
    
    // 异步处理
    const val COROUTINES_VERSION = "1.7.3"
    
    // 图片加载
    const val COIL_VERSION = "2.5.0"
    
    // 测试框架
    const val JUNIT_VERSION = "4.13.2"
    const val ESPRESSO_VERSION = "3.5.1"
    const val MOCKK_VERSION = "1.13.8"
    
    // 其他工具
    const val TIMBER_VERSION = "5.0.1"
    const val LOTTIE_VERSION = "6.1.0"
}

/**
 * 项目架构设计
 */
data class ArchitectureDesign(
    val pattern: String = "MVVM + Clean Architecture",
    val layers: List<String> = listOf(
        "Presentation Layer (UI + ViewModel)",
        "Domain Layer (Use Cases + Repository Interfaces)",
        "Data Layer (Repository Implementation + Data Sources)"
    ),
    val principles: List<String> = listOf(
        "单一职责原则 (SRP)",
        "开闭原则 (OCP)", 
        "里氏替换原则 (LSP)",
        "接口隔离原则 (ISP)",
        "依赖倒置原则 (DIP)"
    ),
    val designPatterns: List<String> = listOf(
        "Repository Pattern - 数据访问抽象",
        "Factory Pattern - 对象创建",
        "Observer Pattern - 数据观察",
        "Strategy Pattern - 算法策略",
        "Builder Pattern - 复杂对象构建"
    )
)
```

#### 12.1.3 模块化架构设计

```kotlin
/**
 * 模块化架构 - 提高代码可维护性和构建效率
 */
sealed class AppModule(
    val name: String,
    val description: String,
    val dependencies: List<String>
) {
    
    object App : AppModule(
        name = "app",
        description = "主应用模块，负责应用启动和模块集成",
        dependencies = listOf("feature-*", "core-*")
    )
    
    object CoreCommon : AppModule(
        name = "core-common",
        description = "通用工具类和扩展函数",
        dependencies = emptyList()
    )
    
    object CoreDatabase : AppModule(
        name = "core-database",
        description = "数据库配置和实体定义",
        dependencies = listOf("core-common")
    )
    
    object CoreNetwork : AppModule(
        name = "core-network", 
        description = "网络请求配置和API定义",
        dependencies = listOf("core-common")
    )
    
    object CoreUi : AppModule(
        name = "core-ui",
        description = "通用UI组件和主题",
        dependencies = listOf("core-common")
    )
    
    object FeatureAuth : AppModule(
        name = "feature-auth",
        description = "用户认证功能模块",
        dependencies = listOf("core-*")
    )
    
    object FeatureTask : AppModule(
        name = "feature-task",
        description = "任务管理功能模块", 
        dependencies = listOf("core-*")
    )
    
    object FeatureProject : AppModule(
        name = "feature-project",
        description = "项目管理功能模块",
        dependencies = listOf("core-*", "feature-task")
    )
    
    object FeatureTeam : AppModule(
        name = "feature-team",
        description = "团队协作功能模块",
        dependencies = listOf("core-*", "feature-task", "feature-project")
    )
    
    object FeatureProfile : AppModule(
        name = "feature-profile",
        description = "个人资料管理模块",
        dependencies = listOf("core-*")
    )
    
    fun getAllModules(): List<AppModule> = listOf(
        App, CoreCommon, CoreDatabase, CoreNetwork, CoreUi,
        FeatureAuth, FeatureTask, FeatureProject, FeatureTeam, FeatureProfile
    )
}
```

#### 12.1.4 数据模型设计

```kotlin
/**
 * 核心数据模型定义
 * 使用Kotlin的数据类和密封类特性
 */

/**
 * 用户数据模型
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.MEMBER,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val preferences: UserPreferences = UserPreferences()
) {
    /**
     * 获取用户显示名称
     */
    fun getDisplayName(): String {
        return fullName.takeIf { it.isNotBlank() } ?: username
    }
    
    /**
     * 获取用户头像首字母
     */
    fun getAvatarInitials(): String {
        return fullName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .takeIf { it.isNotEmpty() } ?: username.take(2).uppercase()
    }
    
    /**
     * 检查用户是否为管理员
     */
    fun isAdmin(): Boolean = role == UserRole.ADMIN
    
    /**
     * 检查用户是否为项目经理
     */
    fun isProjectManager(): Boolean = role == UserRole.PROJECT_MANAGER || isAdmin()
}

/**
 * 用户角色枚举
 */
@Serializable
enum class UserRole(val displayName: String, val level: Int) {
    MEMBER("成员", 1),
    TEAM_LEAD("团队负责人", 2), 
    PROJECT_MANAGER("项目经理", 3),
    ADMIN("管理员", 4);
    
    fun hasPermission(requiredRole: UserRole): Boolean {
        return this.level >= requiredRole.level
    }
}

/**
 * 用户偏好设置
 */
@Serializable
data class UserPreferences(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "zh-CN",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val taskReminderEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val workingHoursStart: String = "09:00",
    val workingHoursEnd: String = "18:00",
    val defaultProjectView: ProjectView = ProjectView.LIST,
    val taskSortBy: TaskSortBy = TaskSortBy.DUE_DATE
)

/**
 * 主题模式
 */
@Serializable
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色主题"),
    DARK("深色主题"), 
    SYSTEM("跟随系统");
}

/**
 * 项目视图模式
 */
@Serializable
enum class ProjectView(val displayName: String) {
    LIST("列表视图"),
    GRID("网格视图"),
    KANBAN("看板视图");
}

/**
 * 任务排序方式
 */
@Serializable
enum class TaskSortBy(val displayName: String) {
    DUE_DATE("截止日期"),
    PRIORITY("优先级"),
    CREATED_DATE("创建时间"),
    UPDATED_DATE("更新时间"),
    ALPHABETICAL("字母顺序");
}

/**
 * 任务数据模型
 */
@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val projectId: String,
    val assigneeId: String? = null,
    val reporterId: String,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val tags: List<String> = emptyList(),
    val dueDate: Long? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val completionPercentage: Int = 0,
    val attachments: List<TaskAttachment> = emptyList(),
    val comments: List<TaskComment> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null
) {
    /**
     * 检查任务是否已过期
     */
    fun isOverdue(): Boolean {
        val currentTime = System.currentTimeMillis()
        return dueDate != null && dueDate < currentTime && status != TaskStatus.COMPLETED
    }
    
    /**
     * 检查任务是否即将到期（24小时内）
     */
    fun isDueSoon(): Boolean {
        val currentTime = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        return dueDate != null && 
               dueDate > currentTime && 
               dueDate <= currentTime + twentyFourHours &&
               status != TaskStatus.COMPLETED
    }
    
    /**
     * 获取任务进度颜色
     */
    fun getProgressColor(): TaskProgressColor {
        return when {
            status == TaskStatus.COMPLETED -> TaskProgressColor.COMPLETED
            isOverdue() -> TaskProgressColor.OVERDUE
            isDueSoon() -> TaskProgressColor.DUE_SOON
            completionPercentage >= 75 -> TaskProgressColor.ALMOST_DONE
            completionPercentage >= 50 -> TaskProgressColor.IN_PROGRESS
            completionPercentage >= 25 -> TaskProgressColor.STARTED
            else -> TaskProgressColor.NOT_STARTED
        }
    }
    
    /**
     * 获取任务估算准确性
     */
    fun getEstimationAccuracy(): Float? {
        return if (estimatedHours != null && actualHours != null && estimatedHours > 0) {
            (estimatedHours / actualHours).coerceIn(0f, 2f)
        } else null
    }
}

/**
 * 任务状态
 */
@Serializable
enum class TaskStatus(val displayName: String, val color: String) {
    TODO("待办", "#6B7280"),
    IN_PROGRESS("进行中", "#3B82F6"),
    REVIEW("待审核", "#F59E0B"), 
    TESTING("测试中", "#8B5CF6"),
    COMPLETED("已完成", "#10B981"),
    CANCELLED("已取消", "#EF4444");
    
    /**
     * 获取下一个可能的状态
     */
    fun getNextStates(): List<TaskStatus> {
        return when (this) {
            TODO -> listOf(IN_PROGRESS, CANCELLED)
            IN_PROGRESS -> listOf(REVIEW, TESTING, COMPLETED, TODO)
            REVIEW -> listOf(IN_PROGRESS, TESTING, COMPLETED)
            TESTING -> listOf(IN_PROGRESS, REVIEW, COMPLETED)
            COMPLETED -> listOf(IN_PROGRESS)
            CANCELLED -> listOf(TODO)
        }
    }
}

/**
 * 任务优先级
 */
@Serializable
enum class TaskPriority(
    val displayName: String, 
    val level: Int,
    val color: String
) {
    LOW("低", 1, "#10B981"),
    MEDIUM("中", 2, "#F59E0B"),
    HIGH("高", 3, "#F97316"),
    URGENT("紧急", 4, "#EF4444");
    
    /**
     * 获取优先级图标
     */
    fun getIconResource(): String {
        return when (this) {
            LOW -> "ic_priority_low"
            MEDIUM -> "ic_priority_medium" 
            HIGH -> "ic_priority_high"
            URGENT -> "ic_priority_urgent"
        }
    }
}

/**
 * 任务进度颜色
 */
enum class TaskProgressColor(val color: String) {
    NOT_STARTED("#6B7280"),
    STARTED("#3B82F6"),
    IN_PROGRESS("#8B5CF6"),
    ALMOST_DONE("#F59E0B"),
    COMPLETED("#10B981"),
    DUE_SOON("#F97316"),
    OVERDUE("#EF4444")
}

/**
 * 任务附件
 */
@Serializable
data class TaskAttachment(
    val id: String,
    val taskId: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val uploadedBy: String,
    val uploadedAt: Long
) {
    /**
     * 获取文件大小显示文本
     */
    fun getFileSizeDisplay(): String {
        return when {
            fileSize >= 1024 * 1024 * 1024 -> "%.1f GB".format(fileSize / (1024.0 * 1024.0 * 1024.0))
            fileSize >= 1024 * 1024 -> "%.1f MB".format(fileSize / (1024.0 * 1024.0))
            fileSize >= 1024 -> "%.1f KB".format(fileSize / 1024.0)
            else -> "$fileSize B"
        }
    }
    
    /**
     * 检查是否为图片文件
     */
    fun isImage(): Boolean {
        return mimeType.startsWith("image/")
    }
    
    /**
     * 检查是否为文档文件
     */
    fun isDocument(): Boolean {
        return mimeType in listOf(
            "application/pdf",
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    }
}

/**
 * 任务评论
 */
@Serializable
data class TaskComment(
    val id: String,
    val taskId: String,
    val authorId: String,
    val content: String,
    val mentions: List<String> = emptyList(), // 提及的用户ID列表
    val createdAt: Long,
    val updatedAt: Long,
    val isEdited: Boolean = false
) {
    /**
     * 检查评论是否提及了指定用户
     */
    fun mentionsUser(userId: String): Boolean {
        return mentions.contains(userId)
    }
    
    /**
     * 获取评论的简短预览
     */
    fun getPreview(maxLength: Int = 100): String {
        return if (content.length <= maxLength) {
            content
        } else {
            content.take(maxLength) + "..."
        }
    }
}
```

### 12.2 项目环境搭建与基础配置

#### 12.2.1 项目创建与依赖配置

```kotlin
// build.gradle.kts (Project level)
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("androidx.room") version "2.6.0" apply false
    id("kotlin-parcelize") apply false
}

// build.gradle.kts (App level)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("androidx.room")
    id("kotlin-parcelize")
    kotlin("kapt")
}

android {
    namespace = "com.taskmaster.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.taskmaster.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 数据库配置
        room {
            schemaDirectory("$projectDir/schemas")
        }
        
        // Build配置字段
        buildConfigField("String", "API_BASE_URL", "\"https://api.taskmaster.com/v1/\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
        buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.taskmaster.com/v1/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "API_BASE_URL", "\"https://api.taskmaster.com/v1/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false") 
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        }
        
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.taskmaster.com/v1/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        }
    }
    
    // 编译配置
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
        )
    }
    
    // Compose配置
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    // 打包配置
    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/gradle/incremental.annotation.processors"
            )
        }
    }
    
    // 测试配置
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // 核心Android库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Compose UI
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation:1.5.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.5")
    
    // Room数据库
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // 依赖注入
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("com.google.dagger:hilt-compiler:2.48")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    
    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // 图片加载
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // 日志
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // 动画
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    
    // 时间处理
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // 权限处理
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.5")
    androidTestImplementation("androidx.work:work-testing:2.9.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

#### 12.2.2 基础配置与工具类

```kotlin
/**
 * 应用程序主类
 * 配置全局初始化
 */
@HiltAndroidApp
class TaskMasterApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }
    
    /**
     * 初始化应用程序
     */
    private fun initializeApp() {
        // 初始化日志系统
        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
        
        Timber.d("TaskMaster应用启动")
        
        // 初始化全局异常处理
        setupGlobalExceptionHandler()
        
        // 初始化性能监控
        if (BuildConfig.ENABLE_ANALYTICS) {
            initializeAnalytics()
        }
    }
    
    /**
     * 配置WorkManager
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) Log.VERBOSE else Log.INFO
            )
            .build()
    }
    
    /**
     * 设置全局异常处理
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "未捕获的异常")
            
            // 记录崩溃日志
            recordCrash(throwable)
            
            // 调用系统默认处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 记录崩溃信息
     */
    private fun recordCrash(throwable: Throwable) {
        try {
            val crashInfo = CrashInfo(
                timestamp = System.currentTimeMillis(),
                throwable = throwable.toString(),
                stackTrace = throwable.stackTraceToString(),
                appVersion = BuildConfig.VERSION_NAME,
                androidVersion = Build.VERSION.SDK_INT.toString(),
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
            )
            
            // 这里可以保存到本地文件或发送到服务器
            saveCrashInfoToFile(crashInfo)
            
        } catch (e: Exception) {
            Timber.e(e, "记录崩溃信息失败")
        }
    }
    
    private fun saveCrashInfoToFile(crashInfo: CrashInfo) {
        // 实现崩溃信息保存逻辑
    }
    
    /**
     * 初始化分析服务
     */
    private fun initializeAnalytics() {
        // 初始化Firebase Analytics或其他分析服务
        Timber.d("分析服务初始化完成")
    }
}

/**
 * 崩溃信息数据类
 */
data class CrashInfo(
    val timestamp: Long,
    val throwable: String,
    val stackTrace: String,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String
)

/**
 * 生产环境日志处理树
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        
        // 发送到崩溃报告服务
        if (priority == Log.ERROR && t != null) {
            // 发送错误报告到Firebase Crashlytics或其他服务
        }
    }
}

/**
 * 全局常量配置
 */
object AppConstants {
    
    // 数据库配置
    const val DATABASE_NAME = "taskmaster_database"
    const val DATABASE_VERSION = 1
    
    // SharedPreferences键名
    const val PREFS_NAME = "taskmaster_prefs"
    const val PREFS_USER_TOKEN = "user_token"
    const val PREFS_USER_ID = "user_id"
    const val PREFS_THEME_MODE = "theme_mode"
    const val PREFS_LANGUAGE = "language"
    const val PREFS_FIRST_LAUNCH = "first_launch"
    
    // 网络配置
    const val NETWORK_TIMEOUT = 30L // 秒
    const val NETWORK_RETRY_COUNT = 3
    const val NETWORK_RETRY_DELAY = 1000L // 毫秒
    
    // 缓存配置
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
    const val CACHE_MAX_AGE = 60 * 60 * 24 * 7 // 7天
    
    // 分页配置
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    
    // 文件上传配置
    const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    const val ALLOWED_IMAGE_TYPES = arrayOf("image/jpeg", "image/png", "image/gif")
    const val ALLOWED_DOCUMENT_TYPES = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    
    // 通知配置
    const val NOTIFICATION_CHANNEL_TASKS = "tasks"
    const val NOTIFICATION_CHANNEL_REMINDERS = "reminders"
    const val NOTIFICATION_CHANNEL_UPDATES = "updates"
    
    // 动画配置
    const val ANIMATION_DURATION_SHORT = 150
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500
    
    // UI配置
    const val BOTTOM_SHEET_PEEK_HEIGHT = 200 // dp
    const val CARD_ELEVATION = 8 // dp
    const val CARD_CORNER_RADIUS = 12 // dp
    
    // 格式化配置
    const val DATE_FORMAT_DISPLAY = "yyyy年MM月dd日"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATETIME_FORMAT_DISPLAY = "yyyy年MM月dd日 HH:mm"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
}

/**
 * 全局工具类
 */
object AppUtils {
    
    /**
     * 获取应用版本信息
     */
    fun getAppVersionInfo(context: Context): AppVersionInfo {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            AppVersionInfo(
                versionName = packageInfo.versionName ?: "未知",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                packageName = packageInfo.packageName
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "获取应用版本信息失败")
            AppVersionInfo("未知", 0, context.packageName)
        }
    }
    
    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            brand = Build.BRAND,
            hardware = Build.HARDWARE,
            board = Build.BOARD
        )
    }
    
    /**
     * 检查网络连接状态
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            bytes >= gb -> "%.1f GB".format(bytes / gb)
            bytes >= mb -> "%.1f MB".format(bytes / mb)
            bytes >= kb -> "%.1f KB".format(bytes / kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * 生成随机颜色
     */
    fun generateRandomColor(): Long {
        val colors = arrayOf(
            0xFF1976D2, 0xFF388E3C, 0xFFF57C00, 0xFFD32F2F,
            0xFF7B1FA2, 0xFF303F9F, 0xFF0097A7, 0xFF689F38,
            0xFFFFA000, 0xFFE64A19, 0xFF5D4037, 0xFF455A64
        )
        return colors.random()
    }
    
    /**
     * 获取安全的颜色对比文本颜色
     */
    fun getContrastTextColor(backgroundColor: Long): Long {
        val red = (backgroundColor shr 16 and 0xFF)
        val green = (backgroundColor shr 8 and 0xFF)  
        val blue = (backgroundColor and 0xFF)
        
        // 计算亮度
        val brightness = (red * 299 + green * 587 + blue * 114) / 1000
        
        return if (brightness > 128) 0xFF000000 else 0xFFFFFFFF
    }
}

/**
 * 应用版本信息
 */
data class AppVersionInfo(
    val versionName: String,
    val versionCode: Long,
    val packageName: String
)

/**
 * 设备信息
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val brand: String,
    val hardware: String,
    val board: String
) {
    fun getDisplayName(): String = "$manufacturer $model"
    fun getFullInfo(): String = "$manufacturer $model (Android $androidVersion, API $sdkVersion)"
}
```

### 12.3 数据层实现

#### 12.3.1 Room数据库设计

```kotlin
/**
 * Room数据库实体定义
 * 使用Kotlin的数据类和注解
 */

/**
 * 用户实体
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "full_name")
    val fullName: String,
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    
    @ColumnInfo(name = "role")
    val role: String,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "preferences_json")
    val preferencesJson: String
)

/**
 * 项目实体
 */
@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["owner_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["owner_id"]),
        Index(value = ["name"]),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "start_date")
    val startDate: Long?,
    
    @ColumnInfo(name = "end_date")
    val endDate: Long?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

/**
 * 任务实体
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignee_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["reporter_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["project_id"]),
        Index(value = ["assignee_id"]),
        Index(value = ["reporter_id"]),
        Index(value = ["status"]),
        Index(value = ["priority"]),
        Index(value = ["due_date"]),
        Index(value = ["created_at"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "project_id")
    val projectId: String,
    
    @ColumnInfo(name = "assignee_id")
    val assigneeId: String?,
    
    @ColumnInfo(name = "reporter_id")
    val reporterId: String,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "priority")
    val priority: String,
    
    @ColumnInfo(name = "tags")
    val tags: String, // JSON格式存储
    
    @ColumnInfo(name = "due_date")
    val dueDate: Long?,
    
    @ColumnInfo(name = "estimated_hours")
    val estimatedHours: Float?,
    
    @ColumnInfo(name = "actual_hours")
    val actualHours: Float?,
    
    @ColumnInfo(name = "completion_percentage")
    val completionPercentage: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?
)

/**
 * 数据访问对象(DAO)定义
 */

/**
 * 用户DAO
 */
@Dao
interface UserDao {
    
    /**
     * 获取所有用户
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY full_name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * 根据ID获取用户
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    /**
     * 根据email获取用户
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    /**
     * 根据用户名获取用户
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    /**
     * 搜索用户
     */
    @Query("""
        SELECT * FROM users 
        WHERE (full_name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%')
        AND is_active = 1
        ORDER BY full_name ASC
    """)
    suspend fun searchUsers(query: String): List<UserEntity>
    
    /**
     * 插入用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * 批量插入用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    /**
     * 更新用户
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * 删除用户
     */
    @Query("UPDATE users SET is_active = 0, updated_at = :timestamp WHERE id = :userId")
    suspend fun deactivateUser(userId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 物理删除用户
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    /**
     * 清空所有用户数据
     */
    @Query("DELETE FROM users")
    suspend fun clearAll()
    
    /**
     * 获取用户数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_active = 1")
    suspend fun getUserCount(): Int
    
    /**
     * 获取最近注册的用户
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentUsers(limit: Int = 10): List<UserEntity>
}

/**
 * 项目DAO
 */
@Dao
interface ProjectDao {
    
    /**
     * 获取所有项目
     */
    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    /**
     * 获取用户的项目
     */
    @Query("SELECT * FROM projects WHERE owner_id = :userId ORDER BY updated_at DESC")
    fun getProjectsByUserId(userId: String): Flow<List<ProjectEntity>>
    
    /**
     * 根据状态获取项目
     */
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY updated_at DESC")
    suspend fun getProjectsByStatus(status: String): List<ProjectEntity>
    
    /**
     * 根据ID获取项目
     */
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): ProjectEntity?
    
    /**
     * 搜索项目
     */
    @Query("""
        SELECT * FROM projects 
        WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchProjects(query: String): List<ProjectEntity>
    
    /**
     * 插入项目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)
    
    /**
     * 批量插入项目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) 
    suspend fun insertProjects(projects: List<ProjectEntity>)
    
    /**
     * 更新项目
     */
    @Update
    suspend fun updateProject(project: ProjectEntity)
    
    /**
     * 删除项目
     */
    @Delete
    suspend fun deleteProject(project: ProjectEntity)
    
    /**
     * 根据ID删除项目
     */
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)
    
    /**
     * 获取项目统计信息
     */
    @Query("""
        SELECT 
            status,
            COUNT(*) as count
        FROM projects 
        GROUP BY status
    """)
    suspend fun getProjectStatsByStatus(): List<ProjectStatusCount>
    
    /**
     * 获取即将到期的项目
     */
    @Query("""
        SELECT * FROM projects 
        WHERE end_date IS NOT NULL 
        AND end_date <= :deadline 
        AND status != 'COMPLETED'
        ORDER BY end_date ASC
    """)
    suspend fun getProjectsDueSoon(deadline: Long): List<ProjectEntity>
}

/**
 * 任务DAO
 */
@Dao
interface TaskDao {
    
    /**
     * 获取所有任务
     */
    @Query("SELECT * FROM tasks ORDER BY updated_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    /**
     * 获取项目的任务
     */
    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY created_at DESC")
    fun getTasksByProjectId(projectId: String): Flow<List<TaskEntity>>
    
    /**
     * 获取用户的任务
     */
    @Query("SELECT * FROM tasks WHERE assignee_id = :userId ORDER BY due_date ASC, priority DESC")
    fun getTasksByUserId(userId: String): Flow<List<TaskEntity>>
    
    /**
     * 获取用户创建的任务
     */
    @Query("SELECT * FROM tasks WHERE reporter_id = :userId ORDER BY created_at DESC")
    fun getTasksCreatedByUser(userId: String): Flow<List<TaskEntity>>
    
    /**
     * 根据状态获取任务
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY updated_at DESC")
    suspend fun getTasksByStatus(status: String): List<TaskEntity>
    
    /**
     * 根据优先级获取任务
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY due_date ASC")
    suspend fun getTasksByPriority(priority: String): List<TaskEntity>
    
    /**
     * 根据ID获取任务
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    /**
     * 搜索任务
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    suspend fun searchTasks(query: String): List<TaskEntity>
    
    /**
     * 获取过期任务
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE due_date IS NOT NULL 
        AND due_date < :currentTime 
        AND status != 'COMPLETED'
        ORDER BY due_date ASC
    """)
    suspend fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): List<TaskEntity>
    
    /**
     * 获取即将到期的任务
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE due_date IS NOT NULL 
        AND due_date > :currentTime 
        AND due_date <= :deadline 
        AND status != 'COMPLETED'
        ORDER BY due_date ASC
    """)
    suspend fun getTasksDueSoon(
        currentTime: Long = System.currentTimeMillis(),
        deadline: Long = currentTime + 24 * 60 * 60 * 1000L // 24小时后
    ): List<TaskEntity>
    
    /**
     * 插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    /**
     * 批量插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    /**
     * 更新任务
     */
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    /**
     * 更新任务状态
     */
    @Query("""
        UPDATE tasks 
        SET status = :status, 
            updated_at = :timestamp,
            completed_at = CASE WHEN :status = 'COMPLETED' THEN :timestamp ELSE NULL END
        WHERE id = :taskId
    """)
    suspend fun updateTaskStatus(
        taskId: String, 
        status: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 更新任务进度
     */
    @Query("""
        UPDATE tasks 
        SET completion_percentage = :percentage, 
            updated_at = :timestamp 
        WHERE id = :taskId
    """)
    suspend fun updateTaskProgress(
        taskId: String, 
        percentage: Int, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * 删除任务
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    /**
     * 根据ID删除任务
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    /**
     * 删除项目的所有任务
     */
    @Query("DELETE FROM tasks WHERE project_id = :projectId")
    suspend fun deleteTasksByProjectId(projectId: String)
    
    /**
     * 获取任务统计信息
     */
    @Query("""
        SELECT 
            status,
            COUNT(*) as count,
            AVG(completion_percentage) as avgProgress
        FROM tasks 
        WHERE project_id = :projectId
        GROUP BY status
    """)
    suspend fun getTaskStatsByProject(projectId: String): List<TaskStatusStats>
    
    /**
     * 获取用户任务统计
     */
    @Query("""
        SELECT 
            COUNT(*) as totalTasks,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completedTasks,
            COUNT(CASE WHEN due_date IS NOT NULL AND due_date < :currentTime AND status != 'COMPLETED' THEN 1 END) as overdueTasks
        FROM tasks 
        WHERE assignee_id = :userId
    """)
    suspend fun getUserTaskStats(
        userId: String, 
        currentTime: Long = System.currentTimeMillis()
    ): UserTaskStats
}

/**
 * 统计数据类
 */
data class ProjectStatusCount(
    val status: String,
    val count: Int
)

data class TaskStatusStats(
    val status: String,
    val count: Int,
    val avgProgress: Float
)

data class UserTaskStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int
) {
    val completionRate: Float get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
}

/**
 * Room数据库主类
 */
@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        TaskEntity::class
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = true,
    autoMigrations = [
        // 这里可以定义自动迁移规则
    ]
)
@TypeConverters(Converters::class)
abstract class TaskMasterDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao 
    abstract fun taskDao(): TaskDao
    
    companion object {
        const val DATABASE_NAME = AppConstants.DATABASE_NAME
    }
}

/**
 * Room类型转换器
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            gson.fromJson(value, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromUserPreferences(preferences: UserPreferences): String {
        return gson.toJson(preferences)
    }
    
    @TypeConverter
    fun toUserPreferences(value: String): UserPreferences {
        return try {
            gson.fromJson(value, UserPreferences::class.java)
        } catch (e: Exception) {
            UserPreferences()
        }
    }
}

/**
 * 数据库模块依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供Room数据库实例
     */
    @Provides
    @Singleton
    fun provideTaskMasterDatabase(@ApplicationContext context: Context): TaskMasterDatabase {
        return Room.databaseBuilder(
            context,
            TaskMasterDatabase::class.java,
            TaskMasterDatabase.DATABASE_NAME
        )
        .addMigrations(
            // 这里添加数据库迁移策略
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 数据库首次创建时的初始化操作
                Timber.d("TaskMaster数据库创建完成")
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // 数据库每次打开时的操作
            }
        })
        .fallbackToDestructiveMigration() // 开发阶段使用，生产环境需要移除
        .build()
    }
    
    /**
     * 提供UserDao
     */
    @Provides
    fun provideUserDao(database: TaskMasterDatabase): UserDao {
        return database.userDao()
    }
    
    /**
     * 提供ProjectDao
     */
    @Provides
    fun provideProjectDao(database: TaskMasterDatabase): ProjectDao {
        return database.projectDao()
    }
    
    /**
     * 提供TaskDao
     */
    @Provides
    fun provideTaskDao(database: TaskMasterDatabase): TaskDao {
        return database.taskDao()
    }
}
```

#### 12.3.2 网络层实现

```kotlin
/**
 * API接口定义
 * 使用Retrofit和协程进行网络请求
 */

/**
 * 用户API接口
 */
interface UserApiService {
    
    /**
     * 用户登录
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    /**
     * 用户注册
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>
    
    /**
     * 获取当前用户信息
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): ApiResponse<User>
    
    /**
     * 更新用户信息
     */
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): ApiResponse<User>
    
    /**
     * 获取用户列表
     */
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedResponse<User>>
    
    /**
     * 获取用户详情
     */
    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): ApiResponse<User>
    
    /**
     * 上传用户头像
     */
    @Multipart
    @POST("users/{userId}/avatar")
    suspend fun uploadAvatar(
        @Path("userId") userId: String,
        @Part avatar: MultipartBody.Part
    ): ApiResponse<UploadResponse>
}

/**
 * 项目API接口
 */
interface ProjectApiService {
    
    /**
     * 获取项目列表
     */
    @GET("projects")
    suspend fun getProjects(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedResponse<Project>>
    
    /**
     * 获取用户的项目
     */
    @GET("users/{userId}/projects")
    suspend fun getUserProjects(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PaginatedResponse<Project>>
    
    /**
     * 创建项目
     */
    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): ApiResponse<Project>
    
    /**
     * 获取项目详情
     */
    @GET("projects/{projectId}")
    suspend fun getProjectById(@Path("projectId") projectId: String): ApiResponse<Project>
    
    /**
     * 更新项目
     */
    @PUT("projects/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: String,
        @Body request: UpdateProjectRequest
    ): ApiResponse<Project>
    
    /**
     * 删除项目
     */
    @DELETE("projects/{projectId}")
    suspend fun deleteProject(@Path("projectId") projectId: String): ApiResponse<Unit>
    
    /**
     * 获取项目统计数据
     */
    @GET("projects/{projectId}/stats")
    suspend fun getProjectStats(@Path("projectId") projectId: String): ApiResponse<ProjectStats>
}

/**
 * 任务API接口
 */
interface TaskApiService {
    
    /**
     * 获取任务列表
     */
    @GET("tasks")
    suspend fun getTasks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("project_id") projectId: String? = null,
        @Query("assignee_id") assigneeId: String? = null,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedResponse<Task>>
    
    /**
     * 创建任务
     */
    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): ApiResponse<Task>
    
    /**
     * 获取任务详情
     */
    @GET("tasks/{taskId}")
    suspend fun getTaskById(@Path("taskId") taskId: String): ApiResponse<Task>
    
    /**
     * 更新任务
     */
    @PUT("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskRequest
    ): ApiResponse<Task>
    
    /**
     * 更新任务状态
     */
    @PATCH("tasks/{taskId}/status")
    suspend fun updateTaskStatus(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskStatusRequest
    ): ApiResponse<Task>
    
    /**
     * 删除任务
     */
    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String): ApiResponse<Unit>
    
    /**
     * 上传任务附件
     */
    @Multipart
    @POST("tasks/{taskId}/attachments")
    suspend fun uploadTaskAttachment(
        @Path("taskId") taskId: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody?
    ): ApiResponse<TaskAttachment>
    
    /**
     * 添加任务评论
     */
    @POST("tasks/{taskId}/comments")
    suspend fun addTaskComment(
        @Path("taskId") taskId: String,
        @Body request: AddCommentRequest
    ): ApiResponse<TaskComment>
}

/**
 * 网络请求和响应数据类
 */

// 登录相关
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String? = null
)

@Serializable
data class LoginResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val deviceId: String? = null
)

// 用户相关
@Serializable
data class UpdateUserRequest(
    val fullName: String?,
    val avatarUrl: String?,
    val preferences: UserPreferences?
)

// 项目相关
@Serializable
data class CreateProjectRequest(
    val name: String,
    val description: String,
    val color: String,
    val startDate: Long? = null,
    val endDate: Long? = null
)

@Serializable
data class UpdateProjectRequest(
    val name: String?,
    val description: String?,
    val color: String?,
    val status: String?,
    val startDate: Long?,
    val endDate: Long?
)

// 任务相关
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val projectId: String,
    val assigneeId: String? = null,
    val priority: String = "MEDIUM",
    val tags: List<String> = emptyList(),
    val dueDate: Long? = null,
    val estimatedHours: Float? = null
)

@Serializable
data class UpdateTaskRequest(
    val title: String?,
    val description: String?,
    val assigneeId: String?,
    val status: String?,
    val priority: String?,
    val tags: List<String>?,
    val dueDate: Long?,
    val estimatedHours: Float?,
    val actualHours: Float?,
    val completionPercentage: Int?
)

@Serializable
data class UpdateTaskStatusRequest(
    val status: String,
    val completionPercentage: Int? = null
)

@Serializable
data class AddCommentRequest(
    val content: String,
    val mentions: List<String> = emptyList()
)

// 通用响应
@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

@Serializable
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)

@Serializable
data class UploadResponse(
    val url: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)

@Serializable
data class ProjectStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int,
    val tasksByStatus: Map<String, Int>,
    val tasksByPriority: Map<String, Int>,
    val averageCompletionTime: Float?, // 平均完成时间（小时）
    val productivity: Float // 生产力指数
)

/**
 * 网络模块依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * 提供OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(AppConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(HeaderInterceptor())
            .apply {
                if (BuildConfig.ENABLE_LOGGING) {
                    addNetworkInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .cache(
                Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = AppConstants.CACHE_SIZE
                )
            )
            .build()
    }
    
    /**
     * 提供Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
    
    /**
     * 提供API服务
     */
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideProjectApiService(retrofit: Retrofit): ProjectApiService {
        return retrofit.create(ProjectApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTaskApiService(retrofit: Retrofit): TaskApiService {
        return retrofit.create(TaskApiService::class.java)
    }
}

/**
 * 认证拦截器
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 获取存储的访问令牌
        val token = getAccessToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(newRequest)
        
        // 处理401未授权响应
        if (response.code == 401) {
            response.close()
            
            // 尝试刷新令牌
            val refreshToken = getRefreshToken()
            if (refreshToken != null) {
                val newToken = refreshAccessToken(refreshToken)
                if (newToken != null) {
                    // 使用新令牌重试请求
                    val retryRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(retryRequest)
                }
            }
            
            // 令牌刷新失败，清除本地认证信息并跳转到登录页
            clearAuthTokens()
            // 发送广播通知应用跳转到登录页
            context.sendBroadcast(Intent("ACTION_TOKEN_EXPIRED"))
        }
        
        return response
    }
    
    private fun getAccessToken(): String? {
        val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AppConstants.PREFS_USER_TOKEN, null)
    }
    
    private fun getRefreshToken(): String? {
        val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("refresh_token", null)
    }
    
    private fun refreshAccessToken(refreshToken: String): String? {
        // 这里实现令牌刷新逻辑
        // 注意：这是同步操作，在生产环境中可能需要优化
        return try {
            // 调用刷新令牌API
            null
        } catch (e: Exception) {
            Timber.e(e, "令牌刷新失败")
            null
        }
    }
    
    private fun clearAuthTokens() {
        val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(AppConstants.PREFS_USER_TOKEN)
            .remove("refresh_token")
            .remove(AppConstants.PREFS_USER_ID)
            .apply()
    }
}

/**
 * 请求头拦截器
 */
class HeaderInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "TaskMaster-Android/${BuildConfig.VERSION_NAME}")
            .addHeader("X-Platform", "Android")
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .build()
        
        return chain.proceed(request)
    }
}
```

<function_calls>
<invoke name="TodoWrite">
<parameter name="todos">[{"content": "\u521b\u5efa\u7b2c\u4e00\u7ae0\uff1aKotlin\u8bed\u8a00\u57fa\u7840 - \u53d8\u91cf\u3001\u51fd\u6570\u3001\u63a7\u5236\u6d41\u7a0b", "status": "completed", "id": "1"}, {"content": "\u521b\u5efa\u7b2c\u4e8c\u7ae0\uff1a\u9762\u5411\u5bf9\u8c61\u7f16\u7a0b - \u7c7b\u3001\u7ee7\u627f\u3001\u63a5\u53e3\u3001\u6570\u636e\u7c7b", "status": "completed", "id": "2"}, {"content": "\u521b\u5efa\u7b2c\u4e09\u7ae0\uff1aKotlin\u7279\u6709\u7279\u6027 - \u7a7a\u5b89\u5168\u3001\u6269\u5c55\u51fd\u6570\u3001\u9ad8\u9636\u51fd\u6570", "status": "completed", "id": "3"}, {"content": "\u521b\u5efa\u7b2c\u56db\u7ae0\uff1a\u96c6\u5408\u4e0e\u6cdb\u578b - List\u3001Map\u3001Set\u7684\u4f7f\u7528\u548c\u6cdb\u578b\u673a\u5236", "status": "completed", "id": "4"}, {"content": "\u521b\u5efa\u7b2c\u4e94\u7ae0\uff1a\u534f\u7a0b\u7f16\u7a0b - suspend\u51fd\u6570\u3001Flow\u3001Channel", "status": "completed", "id": "5"}, {"content": "\u521b\u5efa\u7b2c\u516d\u7ae0\uff1aAndroid\u4e2d\u7684Kotlin\u5b9e\u8df5 - Activity\u3001Fragment\u3001ViewModel", "status": "completed", "id": "6"}, {"content": "\u521b\u5efa\u7b2c\u4e03\u7ae0\uff1aKotlin\u9ad8\u7ea7\u7279\u6027 - \u5185\u8054\u51fd\u6570\u3001\u59d4\u6258\u3001\u6ce8\u89e3\u4e0e\u53cd\u5c04", "status": "completed", "id": "7"}, {"content": "\u521b\u5efa\u7b2c\u516b\u7ae0\uff1a\u51fd\u6570\u5f0f\u7f16\u7a0b\u6df1\u5165 - \u9ad8\u9636\u51fd\u6570\u3001\u67ef\u91cc\u5316\u3001\u51fd\u6570\u7ec4\u5408", "status": "completed", "id": "8"}, {"content": "\u521b\u5efa\u7b2c\u4e5d\u7ae0\uff1aDSL\u8bbe\u8ba1\u4e0e\u5b9e\u73b0 - \u9886\u57df\u7279\u5b9a\u8bed\u8a00\u7684\u6784\u5efa\u6280\u5de7", "status": "completed", "id": "9"}, {"content": "\u521b\u5efa\u7b2c\u5341\u7ae0\uff1aKotlin\u591a\u5e73\u53f0\u5f00\u53d1 - KMM\u9879\u76ee\u5b9e\u8df5", "status": "completed", "id": "10"}, {"content": "\u521b\u5efa\u7b2c\u5341\u4e00\u7ae0\uff1a\u6027\u80fd\u4f18\u5316\u4e0e\u6700\u4f73\u5b9e\u8df5 - \u5185\u5b58\u7ba1\u7406\u3001\u7f16\u8bd1\u4f18\u5316", "status": "completed", "id": "11"}, {"content": "\u521b\u5efa\u7b2c\u5341\u4e8c\u7ae0\uff1a\u5b9e\u6218\u9879\u76ee\u7b2c\u4e00\u90e8\u5206 - \u9879\u76ee\u67b6\u6784\u8bbe\u8ba1\u4e0e\u73af\u5883\u642d\u5efa", "status": "completed", "id": "12-1"}, {"content": "\u7ee7\u7eed\u7b2c\u5341\u4e8c\u7ae0\uff1a\u6570\u636e\u5c42\u5b9e\u73b0\u4e0eRepository\u6a21\u5f0f", "status": "completed", "id": "12-2"}, {"content": "\u7ee7\u7eed\u7b2c\u5341\u4e8c\u7ae0\uff1aDataStore\u548cRepository\u5b9e\u73b0", "status": "pending", "id": "12-3"}]