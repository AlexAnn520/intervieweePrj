# 第一行代码 Kotlin版 - 高级特性与实战篇

> 深入掌握Kotlin高级特性，成为真正的Kotlin专家

## 目录

- [第7章 Kotlin高级特性](#第7章-kotlin高级特性)
- [第8章 函数式编程深入](#第8章-函数式编程深入)
- [第9章 DSL设计与实现](#第9章-dsl设计与实现)
- [第10章 Kotlin多平台开发](#第10章-kotlin多平台开发)
- [第11章 性能优化与最佳实践](#第11章-性能优化与最佳实践)
- [第12章 实战项目：完整Android应用](#第12章-实战项目完整android应用)

---

## 第7章 Kotlin高级特性

本章将探索Kotlin的高级特性，这些特性能让你的代码更加优雅、高效和安全。

### 7.1 内联函数（Inline Functions）

#### 7.1.1 内联函数的原理

内联函数是Kotlin性能优化的重要特性，可以消除Lambda表达式的开销。

```kotlin
// 普通高阶函数
fun normalHigherOrder(block: () -> Unit) {
    println("执行前")
    block()
    println("执行后")
}

// 内联函数
inline fun inlineHigherOrder(block: () -> Unit) {
    println("执行前")
    block()
    println("执行后")
}

fun demonstrateInline() {
    // 普通函数调用 - 会创建Function对象
    normalHigherOrder {
        println("普通函数体")
    }
    
    // 内联函数调用 - 代码直接展开
    inlineHigherOrder {
        println("内联函数体")
    }
    
    // 编译后的效果类似于：
    // println("执行前")
    // println("内联函数体")
    // println("执行后")
}
```

#### 7.1.2 内联函数的高级用法

```kotlin
// 多个Lambda参数的内联函数
inline fun <T> measure(
    setup: () -> Unit = {},
    noinline teardown: () -> Unit = {},  // noinline: 不内联这个参数
    crossinline action: () -> T          // crossinline: 防止非局部返回
): T {
    setup()
    try {
        return action()
    } finally {
        teardown()
    }
}

// 泛型内联函数 - 实化类型参数
inline fun <reified T> isInstanceOf(value: Any): Boolean {
    return value is T  // 在运行时可以访问T的具体类型
}

// 实际应用：类型安全的JSON解析
inline fun <reified T> String.parseJson(): T? {
    return try {
        // 使用具体类型T进行解析
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}

fun testAdvancedInline() {
    // 使用measure函数
    val result = measure(
        setup = { println("准备工作") },
        teardown = { println("清理工作") }
    ) {
        (1..1000).sum()
    }
    println("计算结果: $result")
    
    // 使用实化类型参数
    val obj: Any = "Hello Kotlin"
    if (isInstanceOf<String>(obj)) {
        println("obj是String类型")
    }
    
    // JSON解析示例
    val jsonString = """{"name":"张三","age":25}"""
    val user = jsonString.parseJson<User>()
    println("解析用户: $user")
}

data class User(val name: String, val age: Int)
```

#### 7.1.3 内联属性

```kotlin
class Rectangle(val width: Double, val height: Double) {
    // 内联属性
    val area: Double
        inline get() = width * height
    
    var perimeter: Double
        inline get() = 2 * (width + height)
        inline set(value) {
            // 设置周长时调整宽度
            val newWidth = value / 2 - height
            if (newWidth > 0) {
                // 这里实际上不能修改width，这只是示例
                println("设置周长为: $value")
            }
        }
}

// 顶层内联属性
val currentTimeMillis: Long
    inline get() = System.currentTimeMillis()

fun testInlineProperties() {
    val rect = Rectangle(5.0, 3.0)
    println("面积: ${rect.area}")      // 内联展开，无函数调用开销
    println("周长: ${rect.perimeter}") // 内联展开
    
    println("当前时间: $currentTimeMillis")
}
```

### 7.2 委托（Delegation）

#### 7.2.1 类委托

Kotlin支持委托模式的语言级实现，使用`by`关键字。

```kotlin
// 接口定义
interface Printer {
    fun print(message: String)
    fun printError(error: String)
}

// 具体实现
class ConsolePrinter : Printer {
    override fun print(message: String) {
        println("[INFO] $message")
    }
    
    override fun printError(error: String) {
        println("[ERROR] $error")
    }
}

class FilePrinter(private val filename: String) : Printer {
    override fun print(message: String) {
        // 模拟写入文件
        println("写入文件 $filename: $message")
    }
    
    override fun printError(error: String) {
        println("写入错误到文件 $filename: $error")
    }
}

// 使用委托
class Logger(printer: Printer) : Printer by printer {
    private val timestamp get() = System.currentTimeMillis()
    
    // 可以重写委托的方法
    override fun print(message: String) {
        (this as Printer).print("[$timestamp] $message")
    }
    
    // 添加新的方法
    fun debug(message: String) {
        print("[DEBUG] $message")
    }
}

fun testClassDelegation() {
    val consoleLogger = Logger(ConsolePrinter())
    val fileLogger = Logger(FilePrinter("app.log"))
    
    consoleLogger.print("应用启动")
    consoleLogger.debug("调试信息")
    consoleLogger.printError("发生错误")
    
    fileLogger.print("文件操作")
    fileLogger.printError("文件错误")
}
```

#### 7.2.2 属性委托

```kotlin
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

// 自定义委托
class LoggingDelegate<T>(private var value: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        println("获取属性 ${property.name} = $value")
        return value
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        println("设置属性 ${property.name}: $value -> $newValue")
        value = newValue
    }
}

// 延迟委托
class ExpensiveResource {
    init {
        println("创建昂贵的资源")
    }
    
    fun doSomething() {
        println("使用资源")
    }
}

class ResourceManager {
    // 使用自定义委托
    var name: String by LoggingDelegate("初始名称")
    
    // 延迟初始化
    val expensiveResource: ExpensiveResource by lazy {
        println("首次访问，初始化资源")
        ExpensiveResource()
    }
    
    // 可观察属性
    var score: Int by Delegates.observable(0) { property, oldValue, newValue ->
        println("分数变化: $oldValue -> $newValue")
    }
    
    // 可否决属性
    var level: Int by Delegates.vetoable(1) { property, oldValue, newValue ->
        newValue > oldValue  // 只允许提升等级
    }
    
    // 映射委托
    private val map = mutableMapOf<String, Any>()
    var email: String by map
    var age: Int by map
}

fun testPropertyDelegation() {
    val manager = ResourceManager()
    
    // 测试日志委托
    println("当前名称: ${manager.name}")
    manager.name = "新名称"
    
    // 测试延迟初始化
    println("第一次访问资源:")
    manager.expensiveResource.doSomething()
    println("第二次访问资源:")
    manager.expensiveResource.doSomething()
    
    // 测试可观察属性
    manager.score = 100
    manager.score = 150
    
    // 测试可否决属性
    manager.level = 2  // 成功
    manager.level = 1  // 失败，不会改变
    println("当前等级: ${manager.level}")
    
    // 测试映射委托
    manager.email = "test@example.com"
    manager.age = 25
    println("邮箱: ${manager.email}, 年龄: ${manager.age}")
}
```

#### 7.2.3 实际应用：SharedPreferences委托

```kotlin
// Android SharedPreferences委托
class PreferenceDelegate<T>(
    private val key: String,
    private val defaultValue: T,
    private val preferences: SharedPreferences
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is String -> preferences.getString(key, defaultValue) as T
            is Int -> preferences.getInt(key, defaultValue) as T
            is Boolean -> preferences.getBoolean(key, defaultValue) as T
            is Float -> preferences.getFloat(key, defaultValue) as T
            is Long -> preferences.getLong(key, defaultValue) as T
            else -> throw IllegalArgumentException("不支持的类型")
        }
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        with(preferences.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("不支持的类型")
            }
            apply()
        }
    }
}

// 扩展函数简化使用
fun <T> SharedPreferences.delegate(key: String, defaultValue: T) =
    PreferenceDelegate(key, defaultValue, this)

// 使用示例
class AppSettings(preferences: SharedPreferences) {
    var username: String by preferences.delegate("username", "")
    var isFirstLaunch: Boolean by preferences.delegate("first_launch", true)
    var loginCount: Int by preferences.delegate("login_count", 0)
    var lastLoginTime: Long by preferences.delegate("last_login", 0L)
    
    fun recordLogin() {
        loginCount++
        lastLoginTime = System.currentTimeMillis()
        if (isFirstLaunch) {
            isFirstLaunch = false
        }
    }
}
```

### 7.3 注解与反射

#### 7.3.1 自定义注解

```kotlin
// 定义注解
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiEndpoint(
    val path: String,
    val method: HttpMethod = HttpMethod.GET,
    val requiresAuth: Boolean = false
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PathParam(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class QueryParam(val name: String)

enum class HttpMethod { GET, POST, PUT, DELETE }

// 使用注解
@ApiEndpoint("/api/users", HttpMethod.GET)
class UserController {
    
    @ApiEndpoint("/api/users/{id}", HttpMethod.GET, requiresAuth = true)
    fun getUser(@PathParam("id") userId: String): User? {
        return User(userId, "用户$userId", 25)
    }
    
    @ApiEndpoint("/api/users/search", HttpMethod.GET)
    fun searchUsers(
        @QueryParam("q") query: String,
        @QueryParam("limit") limit: Int = 10
    ): List<User> {
        return listOf(User("1", "搜索结果", 30))
    }
    
    @ApiEndpoint("/api/users", HttpMethod.POST, requiresAuth = true)
    fun createUser(user: User): User {
        return user.copy(id = "new_id")
    }
}
```

#### 7.3.2 反射的使用

```kotlin
import kotlin.reflect.*
import kotlin.reflect.full.*

// 反射工具类
object ReflectionUtils {
    
    // 获取类的所有注解
    fun getClassAnnotations(clazz: KClass<*>): List<Annotation> {
        return clazz.annotations
    }
    
    // 获取方法的注解
    fun getFunctionAnnotations(clazz: KClass<*>): Map<String, List<Annotation>> {
        return clazz.memberFunctions.associate { function ->
            function.name to function.annotations
        }
    }
    
    // 创建类的实例
    inline fun <reified T : Any> createInstance(vararg args: Any?): T? {
        return try {
            val constructor = T::class.constructors.first()
            constructor.call(*args)
        } catch (e: Exception) {
            null
        }
    }
    
    // 调用方法
    fun callFunction(instance: Any, functionName: String, vararg args: Any?): Any? {
        return try {
            val function = instance::class.memberFunctions
                .find { it.name == functionName }
            function?.call(instance, *args)
        } catch (e: Exception) {
            null
        }
    }
}

// API路由生成器
class ApiRouteGenerator {
    fun generateRoutes(controllerClass: KClass<*>): List<RouteInfo> {
        val routes = mutableListOf<RouteInfo>()
        val classAnnotation = controllerClass.findAnnotation<ApiEndpoint>()
        
        for (function in controllerClass.memberFunctions) {
            val functionAnnotation = function.findAnnotation<ApiEndpoint>()
            if (functionAnnotation != null) {
                val pathParams = function.parameters
                    .filter { it.findAnnotation<PathParam>() != null }
                    .map { it.findAnnotation<PathParam>()!!.name }
                
                val queryParams = function.parameters
                    .filter { it.findAnnotation<QueryParam>() != null }
                    .map { it.findAnnotation<QueryParam>()!!.name }
                
                routes.add(
                    RouteInfo(
                        path = functionAnnotation.path,
                        method = functionAnnotation.method,
                        requiresAuth = functionAnnotation.requiresAuth,
                        pathParams = pathParams,
                        queryParams = queryParams,
                        functionName = function.name
                    )
                )
            }
        }
        
        return routes
    }
}

data class RouteInfo(
    val path: String,
    val method: HttpMethod,
    val requiresAuth: Boolean,
    val pathParams: List<String>,
    val queryParams: List<String>,
    val functionName: String
)

fun testReflection() {
    // 测试反射工具
    val userInstance = ReflectionUtils.createInstance<User>("1", "测试用户", 25)
    println("创建实例: $userInstance")
    
    // 生成API路由
    val generator = ApiRouteGenerator()
    val routes = generator.generateRoutes(UserController::class)
    
    println("生成的路由:")
    routes.forEach { route ->
        println("${route.method} ${route.path}")
        println("  认证要求: ${route.requiresAuth}")
        println("  路径参数: ${route.pathParams}")
        println("  查询参数: ${route.queryParams}")
        println("  函数名: ${route.functionName}")
        println()
    }
}
```

### 7.4 协程进阶

#### 7.4.1 自定义协程调度器

```kotlin
import kotlinx.coroutines.*
import java.util.concurrent.*

// 自定义线程池调度器
class CustomDispatcher(
    private val threadPoolSize: Int = 4,
    private val queueCapacity: Int = 100
) : CoroutineDispatcher() {
    
    private val executor = ThreadPoolExecutor(
        threadPoolSize,
        threadPoolSize,
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(queueCapacity),
        ThreadFactory { runnable ->
            Thread(runnable, "CustomDispatcher-${Thread.currentThread().id}")
        }
    )
    
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            executor.execute(block)
        } catch (e: RejectedExecutionException) {
            // 队列满时的处理策略
            Dispatchers.Default.dispatch(context, block)
        }
    }
    
    override fun close() {
        executor.shutdown()
    }
    
    fun getActiveThreadCount(): Int = executor.activeCount
    fun getQueueSize(): Int = executor.queue.size
}

// 协程监控
class CoroutineMonitor {
    private val activeCoroutines = mutableMapOf<String, Long>()
    
    suspend fun <T> monitor(name: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        activeCoroutines[name] = startTime
        
        return try {
            withContext(CoroutineName(name)) {
                block()
            }
        } finally {
            val endTime = System.currentTimeMillis()
            activeCoroutines.remove(name)
            println("协程 $name 执行时间: ${endTime - startTime}ms")
        }
    }
    
    fun getActiveCoroutines(): Map<String, Long> = activeCoroutines.toMap()
}

fun testCustomDispatcher() = runBlocking {
    val customDispatcher = CustomDispatcher(2, 10)
    val monitor = CoroutineMonitor()
    
    // 使用自定义调度器
    val jobs = (1..5).map { id ->
        launch(customDispatcher) {
            monitor.monitor("Task-$id") {
                delay(1000)
                println("任务 $id 完成，线程: ${Thread.currentThread().name}")
            }
        }
    }
    
    // 监控状态
    launch {
        repeat(3) {
            delay(500)
            println("活跃线程数: ${customDispatcher.getActiveThreadCount()}")
            println("队列大小: ${customDispatcher.getQueueSize()}")
            println("活跃协程: ${monitor.getActiveCoroutines().keys}")
            println("---")
        }
    }
    
    jobs.joinAll()
    customDispatcher.close()
}
```

#### 7.4.2 协程异常处理策略

```kotlin
// 协程异常处理器
class GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
    
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val coroutineName = context[CoroutineName]?.name ?: "Unknown"
        println("全局异常处理器捕获异常 [$coroutineName]: ${exception.message}")
        
        // 根据异常类型进行不同处理
        when (exception) {
            is CancellationException -> {
                println("协程被取消")
            }
            is TimeoutCancellationException -> {
                println("协程超时")
            }
            else -> {
                println("未知异常，记录日志")
                // 发送错误报告
            }
        }
    }
}

// 协程错误恢复策略
class CoroutineRetryPolicy {
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    throw e
                }
                
                println("第${attempt + 1}次尝试失败: ${e.message}")
                delay(currentDelay)
                currentDelay = minOf(currentDelay * factor.toLong(), maxDelay)
            }
        }
        
        // 不会到达这里
        throw IllegalStateException("重试逻辑错误")
    }
}

// 协程生命周期管理
class CoroutineLifecycleManager {
    private val jobs = mutableListOf<Job>()
    private val scope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Default + 
        GlobalCoroutineExceptionHandler()
    )
    
    fun launchManagedCoroutine(
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val job = scope.launch(CoroutineName(name)) {
            try {
                block()
            } catch (e: CancellationException) {
                println("协程 $name 被取消")
                throw e
            } catch (e: Exception) {
                println("协程 $name 发生异常: ${e.message}")
                throw e
            }
        }
        
        jobs.add(job)
        
        // 清理已完成的任务
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        
        return job
    }
    
    suspend fun cancelAll() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        scope.cancel()
        scope.coroutineContext[Job]?.join()
    }
    
    fun getActiveJobCount(): Int = jobs.count { it.isActive }
}

fun testAdvancedCoroutines() = runBlocking {
    val retryPolicy = CoroutineRetryPolicy()
    val lifecycleManager = CoroutineLifecycleManager()
    
    // 测试重试策略
    try {
        val result = retryPolicy.retryWithBackoff(maxRetries = 3) {
            if (Random.nextBoolean()) {
                throw RuntimeException("模拟失败")
            }
            "成功结果"
        }
        println("重试结果: $result")
    } catch (e: Exception) {
        println("重试最终失败: ${e.message}")
    }
    
    // 测试生命周期管理
    repeat(3) { id ->
        lifecycleManager.launchManagedCoroutine("Task-$id") {
            delay(2000)
            if (id == 1) {
                throw RuntimeException("任务1异常")
            }
            println("任务$id完成")
        }
    }
    
    delay(1000)
    println("活跃协程数: ${lifecycleManager.getActiveJobCount()}")
    
    delay(2000)
    lifecycleManager.cancelAll()
}
```

### 7.5 面试常考题

#### 题目1：内联函数的性能影响

**问题：** 内联函数什么时候会提升性能？什么时候反而会降低性能？

**答案：**
```kotlin
// 内联函数的性能分析
class InlinePerformanceAnalysis {
    
    // 提升性能的场景
    inline fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
        // 内联后避免了Function对象的创建
        val result = mutableListOf<T>()
        for (element in this) {
            if (predicate(element)) {  // 这里的Lambda会被内联
                result.add(element)
            }
        }
        return result
    }
    
    // 可能降低性能的场景
    inline fun largeInlineFunction(block: () -> Unit) {
        // 大量的代码会被复制到每个调用点
        println("大量的代码...")
        println("重复的逻辑...")
        println("复杂的计算...")
        // ... 更多代码
        block()
        println("更多代码...")
        // 如果这个函数在很多地方被调用，会导致代码膨胀
    }
    
    // 性能测试
    fun performanceTest() {
        val largeList = (1..1000000).toList()
        
        // 测试内联版本
        val time1 = measureTimeMillis {
            val result = largeList.customFilter { it % 2 == 0 }
        }
        
        // 测试标准版本
        val time2 = measureTimeMillis {
            val result = largeList.filter { it % 2 == 0 }
        }
        
        println("内联版本: ${time1}ms")
        println("标准版本: ${time2}ms")
    }
}
```

**要点：**
- **提升性能：** 消除Lambda对象创建开销，减少函数调用栈
- **降低性能：** 代码膨胀，增加编译后的字节码大小
- **使用原则：** 小函数且频繁调用时使用内联

#### 题目2：委托模式的实现原理

**问题：** Kotlin的属性委托是如何实现的？编译器做了什么？

**答案：**
```kotlin
// 编译前的代码
class Example {
    var property: String by CustomDelegate()
}

// 编译器生成的等价代码
class Example {
    private val property$delegate = CustomDelegate()
    
    var property: String
        get() = property$delegate.getValue(this, ::property)
        set(value) = property$delegate.setValue(this, ::property, value)
}

// 委托接口
interface ReadWriteProperty<in T, V> {
    operator fun getValue(thisRef: T, property: KProperty<*>): V
    operator fun setValue(thisRef: T, property: KProperty<*>, value: V)
}

// 自定义委托实现
class CustomDelegate : ReadWriteProperty<Any?, String> {
    private var value: String = ""
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        println("获取属性 ${property.name}")
        return value
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("设置属性 ${property.name} = $value")
        this.value = value
    }
}
```

#### 题目3：反射的性能开销

**问题：** 反射操作有哪些性能开销？如何优化？

**答案：**
```kotlin
// 反射性能优化
class ReflectionOptimization {
    
    // 缓存反射结果
    companion object {
        private val functionCache = mutableMapOf<String, KFunction<*>>()
        private val propertyCache = mutableMapOf<String, KProperty1<*, *>>()
    }
    
    // 优化前：每次都进行反射查找
    fun slowReflection(obj: Any, functionName: String): Any? {
        val function = obj::class.memberFunctions.find { it.name == functionName }
        return function?.call(obj)
    }
    
    // 优化后：缓存反射结果
    fun fastReflection(obj: Any, functionName: String): Any? {
        val key = "${obj::class.qualifiedName}.$functionName"
        val function = functionCache.getOrPut(key) {
            obj::class.memberFunctions.find { it.name == functionName }
                ?: throw NoSuchMethodException(functionName)
        }
        return function.call(obj)
    }
    
    // 性能测试
    fun performanceComparison() {
        val testObj = TestClass()
        val iterations = 100000
        
        // 测试慢速反射
        val slowTime = measureTimeMillis {
            repeat(iterations) {
                slowReflection(testObj, "testMethod")
            }
        }
        
        // 测试快速反射
        val fastTime = measureTimeMillis {
            repeat(iterations) {
                fastReflection(testObj, "testMethod")
            }
        }
        
        println("慢速反射: ${slowTime}ms")
        println("快速反射: ${fastTime}ms")
        println("性能提升: ${slowTime.toFloat() / fastTime}x")
    }
}

class TestClass {
    fun testMethod(): String = "test result"
}
```

**优化策略：**
- 缓存反射结果避免重复查找
- 使用编译时注解处理器生成代码
- 考虑使用代码生成替代运行时反射

#### 题目4：协程vs线程的内存开销

**问题：** 协程相比线程有什么内存优势？如何证明？

**答案：**
```kotlin
// 内存使用对比
class MemoryUsageComparison {
    
    fun compareThreadVsCoroutine() {
        println("=== 线程 vs 协程内存对比 ===")
        
        // 测试线程内存使用
        val threadMemory = measureMemoryUsage {
            val threads = (1..1000).map {
                Thread {
                    Thread.sleep(5000)
                    println("线程 $it 完成")
                }.apply { start() }
            }
            threads.forEach { it.join() }
        }
        
        // 测试协程内存使用
        val coroutineMemory = measureMemoryUsage {
            runBlocking {
                val jobs = (1..1000).map {
                    launch {
                        delay(5000)
                        println("协程 $it 完成")
                    }
                }
                jobs.joinAll()
            }
        }
        
        println("线程内存使用: ${threadMemory}MB")
        println("协程内存使用: ${coroutineMemory}MB")
        println("内存节省: ${((threadMemory - coroutineMemory) / threadMemory * 100).toInt()}%")
    }
    
    private fun measureMemoryUsage(block: () -> Unit): Long {
        val runtime = Runtime.getRuntime()
        System.gc()
        val beforeMemory = runtime.totalMemory() - runtime.freeMemory()
        
        block()
        
        System.gc()
        val afterMemory = runtime.totalMemory() - runtime.freeMemory()
        
        return (afterMemory - beforeMemory) / (1024 * 1024) // 转换为MB
    }
    
    // 协程轻量级特性演示
    fun demonstrateCoroutineLightweight() = runBlocking {
        println("=== 协程轻量级特性 ===")
        
        // 创建大量协程
        val startTime = System.currentTimeMillis()
        val jobs = (1..100000).map { id ->
            launch {
                delay(100)
                if (id % 10000 == 0) {
                    println("协程 $id 运行")
                }
            }
        }
        
        jobs.joinAll()
        val endTime = System.currentTimeMillis()
        
        println("成功运行100,000个协程，耗时: ${endTime - startTime}ms")
        println("平均每个协程耗时: ${(endTime - startTime) / 100000.0}ms")
    }
}
```

**内存优势：**
- 协程栈很小（约几KB），线程栈很大（通常1-8MB）
- 协程在用户态调度，无内核态切换开销
- 协程对象比Thread对象轻量得多

#### 题目5：DSL设计原则

**问题：** 如何设计一个好用的Kotlin DSL？有什么最佳实践？

**答案：**
```kotlin
// DSL设计示例：HTML构建器
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class HtmlBuilder {
    private val elements = mutableListOf<String>()
    
    fun build(): String = elements.joinToString("")
    
    fun element(tag: String, init: ElementBuilder.() -> Unit) {
        val builder = ElementBuilder(tag)
        builder.init()
        elements.add(builder.build())
    }
}

@HtmlDsl
class ElementBuilder(private val tag: String) {
    private var content = ""
    private val attributes = mutableMapOf<String, String>()
    private val children = mutableListOf<String>()
    
    fun text(content: String) {
        this.content += content
    }
    
    fun attribute(name: String, value: String) {
        attributes[name] = value
    }
    
    fun child(tag: String, init: ElementBuilder.() -> Unit) {
        val builder = ElementBuilder(tag)
        builder.init()
        children.add(builder.build())
    }
    
    fun build(): String {
        val attrs = if (attributes.isEmpty()) "" else 
            " " + attributes.entries.joinToString(" ") { "${it.key}=\"${it.value}\"" }
        
        val childrenStr = children.joinToString("")
        return "<$tag$attrs>$content$childrenStr</$tag>"
    }
}

// DSL扩展函数
fun html(init: HtmlBuilder.() -> Unit): String {
    val builder = HtmlBuilder()
    builder.init()
    return builder.build()
}

fun HtmlBuilder.head(init: ElementBuilder.() -> Unit) = element("head", init)
fun HtmlBuilder.body(init: ElementBuilder.() -> Unit) = element("body", init)
fun ElementBuilder.div(init: ElementBuilder.() -> Unit) = child("div", init)
fun ElementBuilder.p(init: ElementBuilder.() -> Unit) = child("p", init)
fun ElementBuilder.h1(init: ElementBuilder.() -> Unit) = child("h1", init)

// 使用DSL
fun testHtmlDsl() {
    val htmlContent = html {
        head {
            child("title") {
                text("我的网页")
            }
        }
        body {
            h1 {
                text("欢迎来到Kotlin")
            }
            div {
                attribute("class", "container")
                p {
                    text("这是一个段落")
                }
                p {
                    text("这是另一个段落")
                }
            }
        }
    }
    
    println(htmlContent)
}
```

**DSL设计原则：**
- 使用`@DslMarker`防止意外的嵌套
- 利用扩展函数提供流畅的API
- 使用lambda参数实现嵌套结构
- 保持API的一致性和直观性
- 提供类型安全的构建过程

---

## 本章小结

第7章我们学习了Kotlin的高级特性：

### 主要内容：
1. **内联函数：** 性能优化、实化类型参数、使用场景
2. **委托模式：** 类委托、属性委托、自定义委托
3. **注解与反射：** 自定义注解、反射API、性能优化
4. **协程进阶：** 自定义调度器、异常处理、生命周期管理

### 面试重点：
- **内联函数原理：** 代码展开、性能权衡、使用时机
- **委托实现机制：** 编译器生成的代码、操作符重载
- **反射性能优化：** 结果缓存、编译时处理
- **协程内存优势：** 与线程对比、轻量级特性
- **DSL设计模式：** 类型安全、流畅API、最佳实践

接下来我们将深入学习函数式编程在Kotlin中的应用。

---