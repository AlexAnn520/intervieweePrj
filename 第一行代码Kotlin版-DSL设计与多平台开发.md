# 第一行代码 Kotlin版 - DSL设计与多平台开发篇

> 构建优雅的DSL，掌握多平台开发，成为Kotlin全栈专家

## 目录

- [第9章 DSL设计与实现](#第9章-dsl设计与实现)
- [第10章 Kotlin多平台开发](#第10章-kotlin多平台开发)
- [第11章 测试驱动开发](#第11章-测试驱动开发)
- [第12章 性能优化与最佳实践](#第12章-性能优化与最佳实践)

---

## 第9章 DSL设计与实现

DSL（Domain-Specific Language，领域特定语言）是Kotlin最强大的特性之一。本章将深入探讨如何设计和实现优雅的DSL。

### 9.1 DSL基础理论

#### 9.1.1 DSL的核心概念

```kotlin
// DSL的本质：让代码更接近自然语言
// 传统方式构建HTML
fun traditionalHtml(): String {
    val html = StringBuilder()
    html.append("<html>")
    html.append("<head>")
    html.append("<title>我的网页</title>")
    html.append("</head>")
    html.append("<body>")
    html.append("<h1>欢迎</h1>")
    html.append("<p>这是内容</p>")
    html.append("</body>")
    html.append("</html>")
    return html.toString()
}

// DSL方式构建HTML
@DslMarker
annotation class HtmlDsl

fun html(init: HTML.() -> Unit): String {
    val html = HTML()
    html.init()
    return html.render()
}

@HtmlDsl
class HTML {
    private val elements = mutableListOf<String>()
    
    fun head(init: Head.() -> Unit) {
        val head = Head()
        head.init()
        elements.add(head.render())
    }
    
    fun body(init: Body.() -> Unit) {
        val body = Body()
        body.init()
        elements.add(body.render())
    }
    
    fun render(): String = "<html>${elements.joinToString("")}</html>"
}

@HtmlDsl
class Head {
    private val elements = mutableListOf<String>()
    
    fun title(text: String) {
        elements.add("<title>$text</title>")
    }
    
    fun meta(name: String, content: String) {
        elements.add("<meta name=\"$name\" content=\"$content\">")
    }
    
    fun render(): String = "<head>${elements.joinToString("")}</head>"
}

@HtmlDsl
class Body {
    private val elements = mutableListOf<String>()
    
    fun h1(text: String) {
        elements.add("<h1>$text</h1>")
    }
    
    fun p(text: String) {
        elements.add("<p>$text</p>")
    }
    
    fun div(cssClass: String? = null, init: Div.() -> Unit) {
        val div = Div(cssClass)
        div.init()
        elements.add(div.render())
    }
    
    fun render(): String = "<body>${elements.joinToString("")}</body>"
}

@HtmlDsl
class Div(private val cssClass: String?) {
    private val elements = mutableListOf<String>()
    
    fun p(text: String) {
        elements.add("<p>$text</p>")
    }
    
    fun span(text: String) {
        elements.add("<span>$text</span>")
    }
    
    fun render(): String {
        val classAttr = if (cssClass != null) " class=\"$cssClass\"" else ""
        return "<div$classAttr>${elements.joinToString("")}</div>"
    }
}

// 使用DSL
fun demonstrateHtmlDsl(): String {
    return html {
        head {
            title("我的网页")
            meta("charset", "UTF-8")
            meta("viewport", "width=device-width, initial-scale=1.0")
        }
        body {
            h1("欢迎来到我的网站")
            p("这是一个使用Kotlin DSL构建的网页")
            div("container") {
                p("这是容器内的内容")
                span("重要提示")
            }
        }
    }
}
```

#### 9.1.2 DSL设计原则

```kotlin
// 设计原则1：类型安全
@DslMarker
annotation class SqlDsl

@SqlDsl
class SelectBuilder {
    private val columns = mutableListOf<String>()
    private var tableName: String = ""
    private val conditions = mutableListOf<String>()
    private val joins = mutableListOf<String>()
    private var orderBy: String? = null
    private var limit: Int? = null
    
    fun select(vararg columns: String) {
        this.columns.addAll(columns)
    }
    
    fun from(table: String) {
        this.tableName = table
    }
    
    fun where(condition: String) {
        conditions.add(condition)
    }
    
    fun join(table: String, on: String) {
        joins.add("JOIN $table ON $on")
    }
    
    fun leftJoin(table: String, on: String) {
        joins.add("LEFT JOIN $table ON $on")
    }
    
    fun orderBy(column: String, direction: OrderDirection = OrderDirection.ASC) {
        this.orderBy = "$column ${direction.name}"
    }
    
    fun limit(count: Int) {
        this.limit = count
    }
    
    fun build(): String {
        require(columns.isNotEmpty()) { "必须指定查询字段" }
        require(tableName.isNotEmpty()) { "必须指定表名" }
        
        val query = StringBuilder()
        query.append("SELECT ${columns.joinToString(", ")}")
        query.append(" FROM $tableName")
        
        if (joins.isNotEmpty()) {
            query.append(" ${joins.joinToString(" ")}")
        }
        
        if (conditions.isNotEmpty()) {
            query.append(" WHERE ${conditions.joinToString(" AND ")}")
        }
        
        orderBy?.let { query.append(" ORDER BY $it") }
        limit?.let { query.append(" LIMIT $it") }
        
        return query.toString()
    }
}

enum class OrderDirection { ASC, DESC }

fun select(init: SelectBuilder.() -> Unit): String {
    val builder = SelectBuilder()
    builder.init()
    return builder.build()
}

// 使用类型安全的SQL DSL
fun demonstrateSqlDsl() {
    val query1 = select {
        select("id", "name", "email")
        from("users")
        where("age > 18")
        where("status = 'active'")
        orderBy("name")
        limit(10)
    }
    
    val query2 = select {
        select("u.name", "p.title", "p.content")
        from("users u")
        leftJoin("posts p", "u.id = p.user_id")
        where("u.status = 'active'")
        orderBy("p.created_at", OrderDirection.DESC)
    }
    
    println("查询1: $query1")
    println("查询2: $query2")
}
```

### 9.2 高级DSL模式

#### 9.2.1 构建器模式DSL

```kotlin
// 配置DSL - 构建复杂的应用配置
@DslMarker
annotation class ConfigDsl

@ConfigDsl
class AppConfig {
    var appName: String = ""
    var version: String = "1.0.0"
    var debugMode: Boolean = false
    
    private var _database: DatabaseConfig? = null
    private var _server: ServerConfig? = null
    private var _logging: LoggingConfig? = null
    private val _features = mutableMapOf<String, Boolean>()
    
    fun database(init: DatabaseConfig.() -> Unit) {
        _database = DatabaseConfig().apply(init)
    }
    
    fun server(init: ServerConfig.() -> Unit) {
        _server = ServerConfig().apply(init)
    }
    
    fun logging(init: LoggingConfig.() -> Unit) {
        _logging = LoggingConfig().apply(init)
    }
    
    fun feature(name: String, enabled: Boolean = true) {
        _features[name] = enabled
    }
    
    // 验证配置
    fun validate(): ConfigValidationResult {
        val errors = mutableListOf<String>()
        
        if (appName.isBlank()) errors.add("应用名称不能为空")
        if (_database == null) errors.add("必须配置数据库")
        if (_server == null) errors.add("必须配置服务器")
        
        _database?.let { db ->
            if (db.host.isBlank()) errors.add("数据库主机不能为空")
            if (db.port <= 0) errors.add("数据库端口必须大于0")
        }
        
        return if (errors.isEmpty()) {
            ConfigValidationResult.Success(this)
        } else {
            ConfigValidationResult.Error(errors)
        }
    }
    
    fun toMap(): Map<String, Any> = mapOf(
        "app" to mapOf(
            "name" to appName,
            "version" to version,
            "debug" to debugMode
        ),
        "database" to (_database?.toMap() ?: emptyMap()),
        "server" to (_server?.toMap() ?: emptyMap()),
        "logging" to (_logging?.toMap() ?: emptyMap()),
        "features" to _features
    )
}

@ConfigDsl
class DatabaseConfig {
    var host: String = "localhost"
    var port: Int = 5432
    var database: String = ""
    var username: String = ""
    var password: String = ""
    var maxConnections: Int = 10
    var connectionTimeout: Long = 30000
    
    fun toMap(): Map<String, Any> = mapOf(
        "host" to host,
        "port" to port,
        "database" to database,
        "username" to username,
        "password" to password,
        "maxConnections" to maxConnections,
        "connectionTimeout" to connectionTimeout
    )
}

@ConfigDsl
class ServerConfig {
    var host: String = "0.0.0.0"
    var port: Int = 8080
    var ssl: Boolean = false
    var maxRequestSize: Long = 10 * 1024 * 1024 // 10MB
    
    private val _cors = CorsConfig()
    
    fun cors(init: CorsConfig.() -> Unit) {
        _cors.init()
    }
    
    fun toMap(): Map<String, Any> = mapOf(
        "host" to host,
        "port" to port,
        "ssl" to ssl,
        "maxRequestSize" to maxRequestSize,
        "cors" to _cors.toMap()
    )
}

@ConfigDsl
class CorsConfig {
    var enabled: Boolean = false
    val allowedOrigins = mutableSetOf<String>()
    val allowedMethods = mutableSetOf<String>()
    val allowedHeaders = mutableSetOf<String>()
    
    fun allowOrigin(origin: String) {
        allowedOrigins.add(origin)
    }
    
    fun allowMethods(vararg methods: String) {
        allowedMethods.addAll(methods)
    }
    
    fun allowHeaders(vararg headers: String) {
        allowedHeaders.addAll(headers)
    }
    
    fun toMap(): Map<String, Any> = mapOf(
        "enabled" to enabled,
        "allowedOrigins" to allowedOrigins.toList(),
        "allowedMethods" to allowedMethods.toList(),
        "allowedHeaders" to allowedHeaders.toList()
    )
}

@ConfigDsl
class LoggingConfig {
    var level: LogLevel = LogLevel.INFO
    var console: Boolean = true
    var file: Boolean = false
    var filePath: String = "logs/app.log"
    var maxFileSize: String = "10MB"
    var maxFiles: Int = 5
    
    fun toMap(): Map<String, Any> = mapOf(
        "level" to level.name,
        "console" to console,
        "file" to file,
        "filePath" to filePath,
        "maxFileSize" to maxFileSize,
        "maxFiles" to maxFiles
    )
}

enum class LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

sealed class ConfigValidationResult {
    data class Success(val config: AppConfig) : ConfigValidationResult()
    data class Error(val errors: List<String>) : ConfigValidationResult()
}

// 配置DSL的使用
fun config(init: AppConfig.() -> Unit): ConfigValidationResult {
    val config = AppConfig()
    config.init()
    return config.validate()
}

fun demonstrateConfigDsl() {
    val result = config {
        appName = "我的应用"
        version = "2.0.0"
        debugMode = true
        
        database {
            host = "db.example.com"
            port = 5432
            database = "myapp"
            username = "admin"
            password = "secret"
            maxConnections = 20
            connectionTimeout = 45000
        }
        
        server {
            host = "0.0.0.0"
            port = 8080
            ssl = true
            maxRequestSize = 50 * 1024 * 1024
            
            cors {
                enabled = true
                allowOrigin("https://example.com")
                allowOrigin("https://app.example.com")
                allowMethods("GET", "POST", "PUT", "DELETE")
                allowHeaders("Content-Type", "Authorization")
            }
        }
        
        logging {
            level = LogLevel.DEBUG
            console = true
            file = true
            filePath = "logs/myapp.log"
            maxFileSize = "50MB"
            maxFiles = 10
        }
        
        feature("userRegistration", true)
        feature("socialLogin", false)
        feature("analytics", true)
    }
    
    when (result) {
        is ConfigValidationResult.Success -> {
            println("配置验证成功:")
            println(result.config.toMap())
        }
        is ConfigValidationResult.Error -> {
            println("配置验证失败:")
            result.errors.forEach { println("  - $it") }
        }
    }
}
```

#### 9.2.2 路由DSL

```kotlin
// Web路由DSL
@DslMarker
annotation class RouteDsl

@RouteDsl
class Router {
    private val routes = mutableListOf<Route>()
    private val middlewares = mutableListOf<Middleware>()
    
    fun get(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.GET, path, handler))
    }
    
    fun post(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.POST, path, handler))
    }
    
    fun put(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.PUT, path, handler))
    }
    
    fun delete(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.DELETE, path, handler))
    }
    
    fun group(prefix: String, init: RouteGroup.() -> Unit) {
        val group = RouteGroup(prefix)
        group.init()
        routes.addAll(group.getRoutes())
    }
    
    fun middleware(middleware: Middleware) {
        middlewares.add(middleware)
    }
    
    fun getRoutes(): List<Route> = routes.toList()
    fun getMiddlewares(): List<Middleware> = middlewares.toList()
}

@RouteDsl
class RouteGroup(private val prefix: String) {
    private val routes = mutableListOf<Route>()
    
    fun get(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.GET, "$prefix$path", handler))
    }
    
    fun post(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.POST, "$prefix$path", handler))
    }
    
    fun put(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.PUT, "$prefix$path", handler))
    }
    
    fun delete(path: String, handler: RequestHandler) {
        routes.add(Route(HttpMethod.DELETE, "$prefix$path", handler))
    }
    
    fun getRoutes(): List<Route> = routes.toList()
}

data class Route(
    val method: HttpMethod,
    val path: String,
    val handler: RequestHandler
)

enum class HttpMethod { GET, POST, PUT, DELETE, PATCH }

typealias RequestHandler = (Request) -> Response
typealias Middleware = (Request, () -> Response) -> Response

data class Request(
    val method: HttpMethod,
    val path: String,
    val params: Map<String, String> = emptyMap(),
    val query: Map<String, String> = emptyMap(),
    val body: String? = null,
    val headers: Map<String, String> = emptyMap()
)

data class Response(
    val status: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
)

// 创建路由DSL
fun router(init: Router.() -> Unit): Router {
    val router = Router()
    router.init()
    return router
}

// 使用路由DSL
fun demonstrateRouteDsl() {
    val appRouter = router {
        // 中间件
        middleware { request, next ->
            println("日志: ${request.method} ${request.path}")
            val response = next()
            println("响应: ${response.status}")
            response
        }
        
        middleware { request, next ->
            if (request.headers["Authorization"] != null) {
                next()
            } else {
                Response(401, "未授权")
            }
        }
        
        // 根路由
        get("/") { request ->
            Response(200, "欢迎访问API")
        }
        
        get("/health") { request ->
            Response(200, """{"status": "ok", "timestamp": ${System.currentTimeMillis()}}""")
        }
        
        // API路由组
        group("/api/v1") {
            get("/users") { request ->
                val users = """[{"id": 1, "name": "张三"}, {"id": 2, "name": "李四"}]"""
                Response(200, users)
            }
            
            get("/users/{id}") { request ->
                val userId = request.params["id"]
                Response(200, """{"id": $userId, "name": "用户$userId"}""")
            }
            
            post("/users") { request ->
                val body = request.body ?: ""
                Response(201, """{"message": "用户创建成功", "data": $body}""")
            }
            
            put("/users/{id}") { request ->
                val userId = request.params["id"]
                val body = request.body ?: ""
                Response(200, """{"message": "用户$userId 更新成功", "data": $body}""")
            }
            
            delete("/users/{id}") { request ->
                val userId = request.params["id"]
                Response(200, """{"message": "用户$userId 删除成功"}""")
            }
        }
        
        // 管理员路由组
        group("/admin") {
            get("/dashboard") { request ->
                Response(200, "管理员仪表板")
            }
            
            get("/stats") { request ->
                Response(200, """{"users": 100, "orders": 50}""")
            }
        }
    }
    
    // 打印所有路由
    println("注册的路由:")
    appRouter.getRoutes().forEach { route ->
        println("  ${route.method} ${route.path}")
    }
    
    println("注册的中间件数量: ${appRouter.getMiddlewares().size}")
}
```

### 9.3 测试DSL

#### 9.3.1 测试场景DSL

```kotlin
// 测试DSL - 让测试更具表达力
@DslMarker
annotation class TestDsl

@TestDsl
class TestSuite(val name: String) {
    private val testCases = mutableListOf<TestCase>()
    private var setupBlock: (() -> Unit)? = null
    private var teardownBlock: (() -> Unit)? = null
    
    fun setup(block: () -> Unit) {
        setupBlock = block
    }
    
    fun teardown(block: () -> Unit) {
        teardownBlock = block
    }
    
    fun test(description: String, block: TestContext.() -> Unit) {
        testCases.add(TestCase(description, block))
    }
    
    fun run(): TestResult {
        val results = mutableListOf<TestCaseResult>()
        
        testCases.forEach { testCase ->
            try {
                setupBlock?.invoke()
                
                val context = TestContext()
                testCase.block(context)
                
                results.add(TestCaseResult.Success(testCase.description))
                teardownBlock?.invoke()
            } catch (e: AssertionError) {
                results.add(TestCaseResult.Failure(testCase.description, e.message ?: "断言失败"))
            } catch (e: Exception) {
                results.add(TestCaseResult.Error(testCase.description, e.message ?: "测试错误"))
            }
        }
        
        return TestResult(name, results)
    }
}

@TestDsl
class TestContext {
    
    infix fun <T> T.shouldBe(expected: T) {
        if (this != expected) {
            throw AssertionError("期望 $expected 但得到 $this")
        }
    }
    
    infix fun <T> T.shouldNotBe(unexpected: T) {
        if (this == unexpected) {
            throw AssertionError("不期望得到 $unexpected")
        }
    }
    
    infix fun String.shouldContain(substring: String) {
        if (!this.contains(substring)) {
            throw AssertionError("字符串 '$this' 应该包含 '$substring'")
        }
    }
    
    infix fun <T> Collection<T>.shouldContain(element: T) {
        if (!this.contains(element)) {
            throw AssertionError("集合 $this 应该包含 $element")
        }
    }
    
    infix fun Collection<*>.shouldHaveSize(size: Int) {
        if (this.size != size) {
            throw AssertionError("集合大小应该是 $size 但实际是 ${this.size}")
        }
    }
    
    fun <T> T?.shouldNotBeNull(): T {
        return this ?: throw AssertionError("值不应该为null")
    }
    
    fun Any?.shouldBeNull() {
        if (this != null) {
            throw AssertionError("值应该为null但实际是 $this")
        }
    }
    
    inline fun <reified T : Throwable> shouldThrow(block: () -> Unit): T {
        try {
            block()
            throw AssertionError("应该抛出 ${T::class.simpleName} 异常")
        } catch (e: Throwable) {
            if (e is T) {
                return e
            } else {
                throw AssertionError("期望 ${T::class.simpleName} 但得到 ${e::class.simpleName}: ${e.message}")
            }
        }
    }
}

data class TestCase(val description: String, val block: TestContext.() -> Unit)

sealed class TestCaseResult {
    data class Success(val description: String) : TestCaseResult()
    data class Failure(val description: String, val message: String) : TestCaseResult()
    data class Error(val description: String, val message: String) : TestCaseResult()
}

data class TestResult(val suiteName: String, val results: List<TestCaseResult>) {
    val totalTests = results.size
    val passedTests = results.count { it is TestCaseResult.Success }
    val failedTests = results.count { it is TestCaseResult.Failure }
    val errorTests = results.count { it is TestCaseResult.Error }
    
    fun printSummary() {
        println("测试套件: $suiteName")
        println("总测试: $totalTests, 通过: $passedTests, 失败: $failedTests, 错误: $errorTests")
        
        results.forEach { result ->
            when (result) {
                is TestCaseResult.Success -> println("  ✓ ${result.description}")
                is TestCaseResult.Failure -> println("  ✗ ${result.description} - ${result.message}")
                is TestCaseResult.Error -> println("  ⚠ ${result.description} - ${result.message}")
            }
        }
    }
}

// 创建测试套件DSL
fun testSuite(name: String, init: TestSuite.() -> Unit): TestResult {
    val suite = TestSuite(name)
    suite.init()
    return suite.run()
}

// 被测试的类
class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun subtract(a: Int, b: Int): Int = a - b
    fun multiply(a: Int, b: Int): Int = a * b
    fun divide(a: Int, b: Int): Int {
        if (b == 0) throw IllegalArgumentException("除数不能为0")
        return a / b
    }
}

class UserService {
    private val users = mutableMapOf<String, String>()
    
    fun addUser(id: String, name: String) {
        if (id.isBlank()) throw IllegalArgumentException("用户ID不能为空")
        if (name.isBlank()) throw IllegalArgumentException("用户名不能为空")
        users[id] = name
    }
    
    fun getUser(id: String): String? = users[id]
    fun getAllUsers(): Map<String, String> = users.toMap()
    fun removeUser(id: String): Boolean = users.remove(id) != null
}

// 使用测试DSL
fun demonstrateTestDsl() {
    val calculatorResult = testSuite("计算器测试") {
        val calculator = Calculator()
        
        setup {
            println("准备计算器测试")
        }
        
        teardown {
            println("清理计算器测试")
        }
        
        test("加法应该正确计算两个正数的和") {
            val result = calculator.add(2, 3)
            result shouldBe 5
        }
        
        test("减法应该正确计算两个数的差") {
            val result = calculator.subtract(10, 4)
            result shouldBe 6
        }
        
        test("乘法应该正确计算两个数的积") {
            val result = calculator.multiply(3, 4)
            result shouldBe 12
        }
        
        test("除法应该正确计算两个数的商") {
            val result = calculator.divide(12, 3)
            result shouldBe 4
        }
        
        test("除以0应该抛出异常") {
            shouldThrow<IllegalArgumentException> {
                calculator.divide(10, 0)
            }
        }
    }
    
    val userServiceResult = testSuite("用户服务测试") {
        val userService = UserService()
        
        test("应该能够添加用户") {
            userService.addUser("1", "张三")
            val user = userService.getUser("1")
            user shouldNotBeNull()
            user shouldBe "张三"
        }
        
        test("应该能够获取所有用户") {
            userService.addUser("1", "张三")
            userService.addUser("2", "李四")
            
            val allUsers = userService.getAllUsers()
            allUsers shouldHaveSize 2
            allUsers shouldContain ("1" to "张三")
            allUsers shouldContain ("2" to "李四")
        }
        
        test("添加空ID用户应该抛出异常") {
            shouldThrow<IllegalArgumentException> {
                userService.addUser("", "张三")
            }
        }
        
        test("获取不存在的用户应该返回null") {
            val user = userService.getUser("999")
            user.shouldBeNull()
        }
        
        test("移除用户应该成功") {
            userService.addUser("1", "张三")
            val removed = userService.removeUser("1")
            removed shouldBe true
            
            val user = userService.getUser("1")
            user.shouldBeNull()
        }
    }
    
    calculatorResult.printSummary()
    println()
    userServiceResult.printSummary()
}
```

### 9.4 Android DSL应用

#### 9.4.1 布局DSL

```kotlin
// Android布局DSL
@DslMarker
annotation class LayoutDsl

@LayoutDsl
class LinearLayoutDsl(private val context: Context) {
    val view = LinearLayout(context)
    
    init {
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
    
    var orientation: Int
        get() = view.orientation
        set(value) { view.orientation = value }
    
    var padding: Int = 0
        set(value) {
            field = value
            view.setPadding(value, value, value, value)
        }
    
    var gravity: Int
        get() = view.gravity
        set(value) { view.gravity = value }
    
    fun textView(text: String, init: TextViewDsl.() -> Unit = {}) {
        val textViewDsl = TextViewDsl(context, text)
        textViewDsl.init()
        view.addView(textViewDsl.view)
    }
    
    fun button(text: String, init: ButtonDsl.() -> Unit = {}) {
        val buttonDsl = ButtonDsl(context, text)
        buttonDsl.init()
        view.addView(buttonDsl.view)
    }
    
    fun editText(hint: String = "", init: EditTextDsl.() -> Unit = {}) {
        val editTextDsl = EditTextDsl(context, hint)
        editTextDsl.init()
        view.addView(editTextDsl.view)
    }
    
    fun linearLayout(init: LinearLayoutDsl.() -> Unit) {
        val childLayout = LinearLayoutDsl(context)
        childLayout.init()
        view.addView(childLayout.view)
    }
}

@LayoutDsl
class TextViewDsl(context: Context, text: String) {
    val view = TextView(context)
    
    init {
        view.text = text
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
    
    var textSize: Float
        get() = view.textSize
        set(value) { view.textSize = value }
    
    var textColor: Int
        get() = view.currentTextColor
        set(value) { view.setTextColor(value) }
    
    var gravity: Int
        get() = view.gravity
        set(value) { view.gravity = value }
    
    var padding: Int = 0
        set(value) {
            field = value
            view.setPadding(value, value, value, value)
        }
    
    fun onClick(listener: () -> Unit) {
        view.setOnClickListener { listener() }
    }
}

@LayoutDsl
class ButtonDsl(context: Context, text: String) {
    val view = Button(context)
    
    init {
        view.text = text
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
    
    var textSize: Float
        get() = view.textSize
        set(value) { view.textSize = value }
    
    var textColor: Int
        get() = view.currentTextColor
        set(value) { view.setTextColor(value) }
    
    var backgroundColor: Int = 0
        set(value) {
            field = value
            view.setBackgroundColor(value)
        }
    
    var padding: Int = 0
        set(value) {
            field = value
            view.setPadding(value, value, value, value)
        }
    
    fun onClick(listener: () -> Unit) {
        view.setOnClickListener { listener() }
    }
}

@LayoutDsl
class EditTextDsl(context: Context, hint: String) {
    val view = EditText(context)
    
    init {
        view.hint = hint
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
    
    var textSize: Float
        get() = view.textSize
        set(value) { view.textSize = value }
    
    var inputType: Int
        get() = view.inputType
        set(value) { view.inputType = value }
    
    var padding: Int = 0
        set(value) {
            field = value
            view.setPadding(value, value, value, value)
        }
    
    fun onTextChanged(listener: (String) -> Unit) {
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

// DSL构建函数
fun Context.linearLayout(init: LinearLayoutDsl.() -> Unit): LinearLayout {
    val dsl = LinearLayoutDsl(this)
    dsl.init()
    return dsl.view
}

// 使用布局DSL的Activity
class DslActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = linearLayout {
            orientation = LinearLayout.VERTICAL
            padding = 16.dpToPx()
            gravity = Gravity.CENTER
            
            textView("欢迎使用DSL布局") {
                textSize = 24f
                textColor = Color.BLUE
                gravity = Gravity.CENTER
                padding = 16.dpToPx()
            }
            
            editText("请输入用户名") {
                textSize = 16f
                padding = 12.dpToPx()
                inputType = InputType.TYPE_CLASS_TEXT
                
                onTextChanged { text ->
                    Log.d("DSL", "用户名输入: $text")
                }
            }
            
            editText("请输入密码") {
                textSize = 16f
                padding = 12.dpToPx()
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                padding = 16.dpToPx()
                
                button("登录") {
                    textSize = 18f
                    textColor = Color.WHITE
                    backgroundColor = Color.BLUE
                    padding = 16.dpToPx()
                    
                    onClick {
                        Toast.makeText(this@DslActivity, "登录按钮被点击", Toast.LENGTH_SHORT).show()
                    }
                }
                
                button("取消") {
                    textSize = 18f
                    textColor = Color.BLACK
                    backgroundColor = Color.GRAY
                    padding = 16.dpToPx()
                    
                    onClick {
                        finish()
                    }
                }
            }
        }
        
        setContentView(layout)
    }
    
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
```

### 9.5 DSL面试常考题

#### 题目1：DSL的实现原理

**问题：** Kotlin DSL是如何实现的？@DslMarker的作用是什么？

**答案：**
```kotlin
// DSL实现的核心技术
class DslImplementationExample {
    
    // 1. 扩展函数 + Lambda with Receiver
    fun String.processText(block: StringBuilder.() -> Unit): String {
        val builder = StringBuilder(this)
        builder.block()  // Lambda with Receiver
        return builder.toString()
    }
    
    // 2. @DslMarker防止意外嵌套
    @DslMarker
    annotation class MyDsl
    
    @MyDsl
    class OuterScope {
        fun outer() = "外部"
        
        fun inner(block: InnerScope.() -> Unit) {
            val innerScope = InnerScope()
            innerScope.block()
        }
    }
    
    @MyDsl
    class InnerScope {
        fun inner() = "内部"
        
        fun test() {
            // outer()  // 编译错误！@DslMarker防止访问外部作用域
        }
    }
    
    // 3. 操作符重载增强DSL
    @MyDsl
    class MathDsl {
        private var result = 0
        
        operator fun Int.unaryPlus(): Int {
            result += this
            return result
        }
        
        operator fun Int.unaryMinus(): Int {
            result -= this
            return result
        }
        
        operator fun Int.times(other: Int): Int {
            result = this * other
            return result
        }
        
        fun getResult() = result
    }
    
    fun math(block: MathDsl.() -> Unit): Int {
        val dsl = MathDsl()
        dsl.block()
        return dsl.getResult()
    }
    
    fun demonstrateDslFeatures() {
        // 扩展函数 + Lambda with Receiver
        val processed = "Hello".processText {
            append(" World")
            append("!")
            reverse()
        }
        println("处理结果: $processed")
        
        // 操作符重载
        val mathResult = math {
            +10
            +5
            -3
        }
        println("数学计算结果: $mathResult")
    }
}
```

**核心技术：**
- **Lambda with Receiver：** 让Lambda可以直接调用接收者的方法
- **扩展函数：** 为现有类型添加DSL方法
- **@DslMarker：** 防止不同作用域间的意外嵌套
- **操作符重载：** 让DSL更接近自然语言

#### 题目2：DSL的性能影响

**问题：** 使用DSL会不会影响性能？如何优化DSL性能？

**答案：**
```kotlin
// DSL性能分析和优化
class DslPerformanceAnalysis {
    
    // 问题1：对象创建开销
    fun inefficientDsl(block: StringBuilder.() -> Unit): String {
        val builder = StringBuilder()  // 每次调用都创建新对象
        builder.block()
        return builder.toString()
    }
    
    // 优化：对象池
    private val builderPool = mutableListOf<StringBuilder>()
    
    fun efficientDsl(block: StringBuilder.() -> Unit): String {
        val builder = builderPool.removeFirstOrNull() ?: StringBuilder()
        builder.clear()  // 重置状态
        
        try {
            builder.block()
            return builder.toString()
        } finally {
            if (builderPool.size < 10) {  // 限制池大小
                builderPool.add(builder)
            }
        }
    }
    
    // 问题2：深度嵌套的Lambda调用
    class NestedDsl {
        fun level1(block: Level1.() -> Unit) = Level1().apply(block)
    }
    
    class Level1 {
        fun level2(block: Level2.() -> Unit) = Level2().apply(block)
    }
    
    class Level2 {
        fun level3(block: Level3.() -> Unit) = Level3().apply(block)
    }
    
    class Level3 {
        fun action() = "深度嵌套动作"
    }
    
    // 优化：内联函数减少调用开销
    inline fun inlineLevel1(block: InlineLevel1.() -> Unit): String {
        return InlineLevel1().block()
    }
    
    class InlineLevel1 {
        inline fun inlineLevel2(block: InlineLevel2.() -> Unit): String {
            return InlineLevel2().block()
        }
    }
    
    class InlineLevel2 {
        inline fun inlineAction(): String = "内联动作"
    }
    
    // 性能测试
    fun performanceTest() {
        val iterations = 100000
        
        // 测试普通DSL
        val time1 = measureTimeMillis {
            repeat(iterations) {
                inefficientDsl {
                    append("test")
                    append(it)
                }
            }
        }
        
        // 测试优化DSL
        val time2 = measureTimeMillis {
            repeat(iterations) {
                efficientDsl {
                    append("test")
                    append(it)
                }
            }
        }
        
        println("普通DSL: ${time1}ms")
        println("优化DSL: ${time2}ms")
        println("性能提升: ${time1.toFloat() / time2}x")
    }
}
```

**优化策略：**
- **对象池：** 重用DSL构建对象
- **内联函数：** 减少函数调用开销
- **延迟求值：** 只在需要时才执行昂贵操作
- **缓存结果：** 避免重复计算

#### 题目3：DSL的测试策略

**问题：** 如何测试复杂的DSL？确保DSL的正确性？

**答案：**
```kotlin
// DSL测试策略
class DslTestingStrategies {
    
    // 被测试的DSL
    @DslMarker
    annotation class QueryDsl
    
    @QueryDsl
    class SqlQuery {
        private val parts = mutableMapOf<String, String>()
        
        fun select(columns: String) {
            parts["SELECT"] = columns
        }
        
        fun from(table: String) {
            parts["FROM"] = table
        }
        
        fun where(condition: String) {
            parts["WHERE"] = condition
        }
        
        fun orderBy(column: String) {
            parts["ORDER BY"] = column
        }
        
        fun build(): String {
            val query = StringBuilder()
            query.append("SELECT ${parts["SELECT"] ?: "*"}")
            query.append(" FROM ${parts["FROM"] ?: "table"}")
            parts["WHERE"]?.let { query.append(" WHERE $it") }
            parts["ORDER BY"]?.let { query.append(" ORDER BY $it") }
            return query.toString()
        }
    }
    
    fun sql(block: SqlQuery.() -> Unit): String {
        val query = SqlQuery()
        query.block()
        return query.build()
    }
    
    // 测试策略1：基础功能测试
    @Test
    fun testBasicDslFunctionality() {
        val query = sql {
            select("name, age")
            from("users")
            where("age > 18")
            orderBy("name")
        }
        
        val expected = "SELECT name, age FROM users WHERE age > 18 ORDER BY name"
        assertEquals(expected, query)
    }
    
    // 测试策略2：边界条件测试
    @Test
    fun testDslEdgeCases() {
        // 测试最小DSL
        val minimalQuery = sql {
            from("users")
        }
        assertEquals("SELECT * FROM users", minimalQuery)
        
        // 测试空值处理
        val emptyQuery = sql { }
        assertEquals("SELECT * FROM table", emptyQuery)
    }
    
    // 测试策略3：DSL语法验证
    @Test
    fun testDslSyntaxValidation() {
        // 测试DSL的语法约束
        assertThrows<IllegalStateException> {
            sql {
                select("*")
                // 缺少from子句应该抛出异常
            }.let { query ->
                if (!query.contains("FROM")) {
                    throw IllegalStateException("缺少FROM子句")
                }
            }
        }
    }
    
    // 测试策略4：复杂场景测试
    @Test
    fun testComplexDslScenarios() {
        val complexQuery = sql {
            select("u.name, COUNT(o.id) as order_count")
            from("users u LEFT JOIN orders o ON u.id = o.user_id")
            where("u.status = 'active' AND u.created_at > '2024-01-01'")
            orderBy("order_count DESC")
        }
        
        assertTrue(complexQuery.contains("LEFT JOIN"))
        assertTrue(complexQuery.contains("COUNT"))
        assertTrue(complexQuery.contains("DESC"))
    }
    
    // 测试策略5：DSL构建器状态测试
    class DslStateTest {
        
        @Test
        fun testDslBuilderState() {
            val builder = SqlQuery()
            
            // 测试初始状态
            assertEquals("SELECT * FROM table", builder.build())
            
            // 测试状态变化
            builder.select("name")
            assertEquals("SELECT name FROM table", builder.build())
            
            builder.from("users")
            assertEquals("SELECT name FROM users", builder.build())
        }
    }
    
    // 测试策略6：DSL性能测试
    @Test
    fun testDslPerformance() {
        val iterations = 10000
        
        val startTime = System.currentTimeMillis()
        
        repeat(iterations) {
            sql {
                select("id, name, email")
                from("users")
                where("status = 'active'")
                orderBy("created_at")
            }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // 性能基准：应该在合理时间内完成
        assertTrue("DSL构建太慢: ${duration}ms", duration < 1000)
    }
}
```

**测试策略：**
- **单元测试：** 测试DSL各个组件的基本功能
- **集成测试：** 测试DSL组件间的协作
- **语法测试：** 验证DSL的语法约束和规则
- **性能测试：** 确保DSL的性能满足要求
- **边界测试：** 测试极端情况和错误处理

#### 题目4：DSL vs Builder模式

**问题：** DSL和Builder模式有什么区别？什么时候选择DSL？

**答案：**
```kotlin
// DSL vs Builder模式对比
class DslVsBuilderComparison {
    
    // Builder模式实现
    class HttpRequestBuilder {
        private var url: String = ""
        private var method: String = "GET"
        private val headers = mutableMapOf<String, String>()
        private val params = mutableMapOf<String, String>()
        private var body: String? = null
        
        fun url(url: String): HttpRequestBuilder {
            this.url = url
            return this
        }
        
        fun method(method: String): HttpRequestBuilder {
            this.method = method
            return this
        }
        
        fun header(key: String, value: String): HttpRequestBuilder {
            headers[key] = value
            return this
        }
        
        fun param(key: String, value: String): HttpRequestBuilder {
            params[key] = value
            return this
        }
        
        fun body(body: String): HttpRequestBuilder {
            this.body = body
            return this
        }
        
        fun build(): HttpRequest {
            return HttpRequest(url, method, headers.toMap(), params.toMap(), body)
        }
    }
    
    // DSL实现
    @DslMarker
    annotation class HttpDsl
    
    @HttpDsl
    class HttpRequestDsl {
        var url: String = ""
        var method: String = "GET"
        var body: String? = null
        
        private val headers = mutableMapOf<String, String>()
        private val params = mutableMapOf<String, String>()
        
        fun headers(block: MutableMap<String, String>.() -> Unit) {
            headers.block()
        }
        
        fun params(block: MutableMap<String, String>.() -> Unit) {
            params.block()
        }
        
        fun build(): HttpRequest {
            return HttpRequest(url, method, headers.toMap(), params.toMap(), body)
        }
    }
    
    fun httpRequest(block: HttpRequestDsl.() -> Unit): HttpRequest {
        val dsl = HttpRequestDsl()
        dsl.block()
        return dsl.build()
    }
    
    data class HttpRequest(
        val url: String,
        val method: String,
        val headers: Map<String, String>,
        val params: Map<String, String>,
        val body: String?
    )
    
    fun compareApproaches() {
        // Builder模式使用
        val builderRequest = HttpRequestBuilder()
            .url("https://api.example.com/users")
            .method("POST")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer token")
            .param("page", "1")
            .param("size", "10")
            .body("""{"name": "张三", "email": "zhang@example.com"}""")
            .build()
        
        // DSL使用
        val dslRequest = httpRequest {
            url = "https://api.example.com/users"
            method = "POST"
            body = """{"name": "张三", "email": "zhang@example.com"}"""
            
            headers {
                put("Content-Type", "application/json")
                put("Authorization", "Bearer token")
            }
            
            params {
                put("page", "1")
                put("size", "10")
            }
        }
        
        println("Builder结果: $builderRequest")
        println("DSL结果: $dslRequest")
    }
}
```

**选择原则：**

| 特性 | Builder模式 | DSL |
|------|-------------|-----|
| **学习成本** | 低 | 中等 |
| **可读性** | 良好 | 优秀 |
| **类型安全** | 一般 | 优秀 |
| **嵌套支持** | 困难 | 自然 |
| **IDE支持** | 好 | 很好 |
| **适用场景** | 简单对象构建 | 复杂结构、配置 |

**选择DSL的时机：**
- 需要嵌套结构（如HTML、XML、JSON）
- 配置文件或设置
- 测试场景描述
- 领域特定的表达需求

#### 题目5：DSL的设计模式

**问题：** 设计DSL时有哪些常见模式？如何确保DSL的易用性？

**答案：**
```kotlin
// DSL设计模式总结
class DslDesignPatterns {
    
    // 模式1：流畅接口模式
    @DslMarker
    annotation class FluentDsl
    
    @FluentDsl
    class FluentBuilder {
        private val steps = mutableListOf<String>()
        
        fun step1(value: String): FluentBuilder {
            steps.add("步骤1: $value")
            return this
        }
        
        fun step2(value: String): FluentBuilder {
            steps.add("步骤2: $value")
            return this
        }
        
        fun step3(value: String): FluentBuilder {
            steps.add("步骤3: $value")
            return this
        }
        
        fun build(): List<String> = steps.toList()
    }
    
    // 模式2：嵌套作用域模式
    @DslMarker
    annotation class NestedDsl
    
    @NestedDsl
    class DocumentBuilder {
        private val sections = mutableListOf<Section>()
        
        fun section(title: String, block: SectionBuilder.() -> Unit) {
            val sectionBuilder = SectionBuilder(title)
            sectionBuilder.block()
            sections.add(sectionBuilder.build())
        }
        
        fun build(): Document = Document(sections)
    }
    
    @NestedDsl
    class SectionBuilder(private val title: String) {
        private val paragraphs = mutableListOf<String>()
        
        fun paragraph(text: String) {
            paragraphs.add(text)
        }
        
        fun build(): Section = Section(title, paragraphs)
    }
    
    data class Document(val sections: List<Section>)
    data class Section(val title: String, val paragraphs: List<String>)
    
    // 模式3：状态机模式
    @DslMarker
    annotation class StateMachineDsl
    
    @StateMachineDsl
    class StateMachine<T> {
        private val states = mutableMapOf<T, State<T>>()
        private var currentState: T? = null
        
        fun state(name: T, block: StateBuilder<T>.() -> Unit) {
            val builder = StateBuilder<T>(name)
            builder.block()
            states[name] = builder.build()
        }
        
        fun start(initialState: T) {
            currentState = initialState
        }
        
        fun trigger(event: String): T? {
            val current = currentState ?: return null
            val state = states[current] ?: return null
            val nextState = state.transitions[event]
            if (nextState != null) {
                currentState = nextState
                state.onExit?.invoke()
                states[nextState]?.onEnter?.invoke()
            }
            return currentState
        }
        
        fun getCurrentState(): T? = currentState
    }
    
    @StateMachineDsl
    class StateBuilder<T>(private val name: T) {
        val transitions = mutableMapOf<String, T>()
        var onEnter: (() -> Unit)? = null
        var onExit: (() -> Unit)? = null
        
        fun on(event: String, goTo: T) {
            transitions[event] = goTo
        }
        
        fun onEnter(action: () -> Unit) {
            onEnter = action
        }
        
        fun onExit(action: () -> Unit) {
            onExit = action
        }
        
        fun build(): State<T> = State(name, transitions, onEnter, onExit)
    }
    
    data class State<T>(
        val name: T,
        val transitions: Map<String, T>,
        val onEnter: (() -> Unit)?,
        val onExit: (() -> Unit)?
    )
    
    fun <T> stateMachine(block: StateMachine<T>.() -> Unit): StateMachine<T> {
        val machine = StateMachine<T>()
        machine.block()
        return machine
    }
    
    enum class DoorState { CLOSED, OPEN, LOCKED }
    
    fun demonstrateDesignPatterns() {
        // 流畅接口模式
        val fluentResult = FluentBuilder()
            .step1("初始化")
            .step2("处理数据")
            .step3("完成")
            .build()
        
        println("流畅接口结果: $fluentResult")
        
        // 嵌套作用域模式
        val document = DocumentBuilder().apply {
            section("引言") {
                paragraph("这是文档的开始")
                paragraph("介绍主要内容")
            }
            
            section("正文") {
                paragraph("详细说明")
                paragraph("具体实现")
            }
            
            section("结论") {
                paragraph("总结要点")
            }
        }.build()
        
        println("文档结构: ${document.sections.size}个章节")
        
        // 状态机模式
        val doorMachine = stateMachine<DoorState> {
            state(DoorState.CLOSED) {
                on("open", DoorState.OPEN)
                on("lock", DoorState.LOCKED)
                onEnter { println("门已关闭") }
            }
            
            state(DoorState.OPEN) {
                on("close", DoorState.CLOSED)
                onEnter { println("门已打开") }
            }
            
            state(DoorState.LOCKED) {
                on("unlock", DoorState.CLOSED)
                onEnter { println("门已锁定") }
            }
        }
        
        doorMachine.start(DoorState.CLOSED)
        println("当前状态: ${doorMachine.getCurrentState()}")
        
        doorMachine.trigger("open")
        println("触发开门后: ${doorMachine.getCurrentState()}")
        
        doorMachine.trigger("close")
        println("触发关门后: ${doorMachine.getCurrentState()}")
    }
}
```

**DSL易用性设计原则：**
- **自然语言风格：** 让代码读起来像自然语言
- **类型安全：** 编译时捕获错误
- **IDE友好：** 提供良好的代码补全和语法高亮
- **渐进式：** 支持从简单到复杂的使用场景
- **一致性：** 保持API风格的一致性
- **错误友好：** 提供清晰的错误信息

---

## 第10章 Kotlin多平台开发

Kotlin多平台（Kotlin Multiplatform）是Kotlin最具前景的特性之一，允许在不同平台间共享代码逻辑。

### 10.1 Kotlin多平台架构原理

#### 10.1.1 多平台项目结构

```kotlin
// 共同代码模块 (commonMain)
// 定义通用的业务逻辑和数据模型

/**
 * 用户数据模型 - 在所有平台共享
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val createdAt: Long,
    val isActive: Boolean = true
) {
    // 通用的业务逻辑方法
    fun getDisplayName(): String {
        return if (name.isNotBlank()) name else email.substringBefore("@")
    }
    
    fun isValidEmail(): Boolean {
        return email.contains("@") && email.contains(".")
    }
    
    fun getProfileInitials(): String {
        return name.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercase() ?: "" }
            .joinToString("")
            .take(2)
    }
}

/**
 * 网络响应的通用封装
 */
@Serializable
sealed class ApiResponse<out T> {
    @Serializable
    data class Success<T>(val data: T, val message: String? = null) : ApiResponse<T>()
    
    @Serializable
    data class Error(
        val code: Int,
        val message: String,
        val details: Map<String, String>? = null
    ) : ApiResponse<Nothing>()
    
    @Serializable
    object Loading : ApiResponse<Nothing>()
}

/**
 * 用户仓库接口 - 定义平台无关的数据访问接口
 */
interface UserRepository {
    suspend fun getAllUsers(): ApiResponse<List<User>>
    suspend fun getUserById(id: String): ApiResponse<User>
    suspend fun createUser(user: User): ApiResponse<User>
    suspend fun updateUser(user: User): ApiResponse<User>
    suspend fun deleteUser(id: String): ApiResponse<Unit>
    suspend fun searchUsers(query: String): ApiResponse<List<User>>
}

/**
 * 网络接口定义
 */
interface NetworkClient {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): String
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): String
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): String
}

/**
 * 本地存储接口
 */
interface LocalStorage {
    suspend fun saveUser(user: User)
    suspend fun getUser(id: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun deleteUser(id: String)
    suspend fun clear()
}

/**
 * 平台特定功能的expect声明
 */
expect class PlatformContext

expect fun getPlatformName(): String

expect fun getCurrentTimestamp(): Long

expect fun generateUUID(): String

expect class Logger() {
    fun log(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable?)
}

expect suspend fun delay(timeMillis: Long)
```

#### 10.1.2 平台特定实现

**Android实现 (androidMain)**

```kotlin
// Android平台的具体实现

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay as kotlinDelay

actual typealias PlatformContext = Context

actual fun getPlatformName(): String = "Android ${android.os.Build.VERSION.RELEASE}"

actual fun getCurrentTimestamp(): Long = System.currentTimeMillis()

actual fun generateUUID(): String = java.util.UUID.randomUUID().toString()

actual class Logger {
    actual fun log(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    actual fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}

actual suspend fun delay(timeMillis: Long) = kotlinDelay(timeMillis)

/**
 * Android平台的网络客户端实现
 */
class AndroidNetworkClient(private val context: Context) : NetworkClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(LoggingInterceptor())
        .build()
    
    override suspend fun get(url: String, headers: Map<String, String>): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpException(response.code, response.message)
                }
                response.body?.string() ?: ""
            }
        }
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): String {
        return withContext(Dispatchers.IO) {
            val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpException(response.code, response.message)
                }
                response.body?.string() ?: ""
            }
        }
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): String {
        return withContext(Dispatchers.IO) {
            val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpException(response.code, response.message)
                }
                response.body?.string() ?: ""
            }
        }
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .delete()
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpException(response.code, response.message)
                }
                response.body?.string() ?: ""
            }
        }
    }
}

/**
 * Android平台的本地存储实现
 */
class AndroidLocalStorage(private val context: Context) : LocalStorage {
    private val database: UserDatabase by lazy {
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_database"
        ).build()
    }
    
    override suspend fun saveUser(user: User) {
        database.userDao().insertUser(user.toEntity())
    }
    
    override suspend fun getUser(id: String): User? {
        return database.userDao().getUserById(id)?.toModel()
    }
    
    override suspend fun getAllUsers(): List<User> {
        return database.userDao().getAllUsers().map { it.toModel() }
    }
    
    override suspend fun deleteUser(id: String) {
        database.userDao().deleteUser(id)
    }
    
    override suspend fun clear() {
        database.userDao().deleteAll()
    }
}

// Room数据库相关代码
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val createdAt: Long,
    val isActive: Boolean
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Database(entities = [UserEntity::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// 扩展函数用于数据转换
fun User.toEntity(): UserEntity {
    return UserEntity(id, name, email, avatarUrl, createdAt, isActive)
}

fun UserEntity.toModel(): User {
    return User(id, name, email, avatarUrl, createdAt, isActive)
}
```

**iOS实现 (iosMain)**

```kotlin
// iOS平台的具体实现

import platform.Foundation.*
import platform.UIKit.*
import kotlinx.cinterop.*
import kotlinx.coroutines.delay as kotlinDelay

actual typealias PlatformContext = NSObject

actual fun getPlatformName(): String {
    return "iOS ${UIDevice.currentDevice.systemVersion}"
}

actual fun getCurrentTimestamp(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun generateUUID(): String {
    return NSUUID().UUIDString
}

actual class Logger {
    actual fun log(tag: String, message: String) {
        println("[$tag] $message")
    }
    
    actual fun error(tag: String, message: String, throwable: Throwable?) {
        println("ERROR [$tag] $message")
        throwable?.printStackTrace()
    }
}

actual suspend fun delay(timeMillis: Long) = kotlinDelay(timeMillis)

/**
 * iOS平台的网络客户端实现
 */
class IosNetworkClient : NetworkClient {
    
    override suspend fun get(url: String, headers: Map<String, String>): String {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL(string = url)
            val request = NSMutableURLRequest(uRL = nsUrl)
            request.HTTPMethod = "GET"
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, forHTTPHeaderField = key)
            }
            
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request = request,
                completionHandler = { data, response, error ->
                    if (error != null) {
                        continuation.resumeWithException(NetworkException(error.localizedDescription))
                        return@dataTaskWithRequest
                    }
                    
                    val httpResponse = response as? NSHTTPURLResponse
                    if (httpResponse?.statusCode !in 200..299) {
                        continuation.resumeWithException(
                            HttpException(
                                httpResponse?.statusCode?.toInt() ?: -1,
                                "HTTP Error"
                            )
                        )
                        return@dataTaskWithRequest
                    }
                    
                    val responseString = data?.let {
                        NSString.create(it, NSUTF8StringEncoding)?.toString()
                    } ?: ""
                    
                    continuation.resume(responseString)
                }
            )
            
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): String {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL(string = url)
            val request = NSMutableURLRequest(uRL = nsUrl)
            request.HTTPMethod = "POST"
            
            // 设置请求体
            request.HTTPBody = body.toNSData()
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, forHTTPHeaderField = key)
            }
            
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request = request,
                completionHandler = { data, response, error ->
                    if (error != null) {
                        continuation.resumeWithException(NetworkException(error.localizedDescription))
                        return@dataTaskWithRequest
                    }
                    
                    val httpResponse = response as? NSHTTPURLResponse
                    if (httpResponse?.statusCode !in 200..299) {
                        continuation.resumeWithException(
                            HttpException(
                                httpResponse?.statusCode?.toInt() ?: -1,
                                "HTTP Error"
                            )
                        )
                        return@dataTaskWithRequest
                    }
                    
                    val responseString = data?.let {
                        NSString.create(it, NSUTF8StringEncoding)?.toString()
                    } ?: ""
                    
                    continuation.resume(responseString)
                }
            )
            
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): String {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL(string = url)
            val request = NSMutableURLRequest(uRL = nsUrl)
            request.HTTPMethod = "PUT"
            
            // 设置请求体
            request.HTTPBody = body.toNSData()
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, forHTTPHeaderField = key)
            }
            
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request = request,
                completionHandler = { data, response, error ->
                    if (error != null) {
                        continuation.resumeWithException(NetworkException(error.localizedDescription))
                        return@dataTaskWithRequest
                    }
                    
                    val httpResponse = response as? NSHTTPURLResponse
                    if (httpResponse?.statusCode !in 200..299) {
                        continuation.resumeWithException(
                            HttpException(
                                httpResponse?.statusCode?.toInt() ?: -1,
                                "HTTP Error"
                            )
                        )
                        return@dataTaskWithRequest
                    }
                    
                    val responseString = data?.let {
                        NSString.create(it, NSUTF8StringEncoding)?.toString()
                    } ?: ""
                    
                    continuation.resume(responseString)
                }
            )
            
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): String {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL(string = url)
            val request = NSMutableURLRequest(uRL = nsUrl)
            request.HTTPMethod = "DELETE"
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, forHTTPHeaderField = key)
            }
            
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request = request,
                completionHandler = { data, response, error ->
                    if (error != null) {
                        continuation.resumeWithException(NetworkException(error.localizedDescription))
                        return@dataTaskWithRequest
                    }
                    
                    val httpResponse = response as? NSHTTPURLResponse
                    if (httpResponse?.statusCode !in 200..299) {
                        continuation.resumeWithException(
                            HttpException(
                                httpResponse?.statusCode?.toInt() ?: -1,
                                "HTTP Error"
                            )
                        )
                        return@dataTaskWithRequest
                    }
                    
                    val responseString = data?.let {
                        NSString.create(it, NSUTF8StringEncoding)?.toString()
                    } ?: ""
                    
                    continuation.resume(responseString)
                }
            )
            
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
}

/**
 * iOS平台的本地存储实现
 */
class IosLocalStorage : LocalStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val usersKey = "stored_users"
    
    override suspend fun saveUser(user: User) {
        val users = getAllUsers().toMutableList()
        val existingIndex = users.indexOfFirst { it.id == user.id }
        
        if (existingIndex >= 0) {
            users[existingIndex] = user
        } else {
            users.add(user)
        }
        
        saveAllUsers(users)
    }
    
    override suspend fun getUser(id: String): User? {
        return getAllUsers().find { it.id == id }
    }
    
    override suspend fun getAllUsers(): List<User> {
        val data = userDefaults.dataForKey(usersKey)
        return if (data != null) {
            try {
                val jsonString = NSString.create(data, NSUTF8StringEncoding)?.toString() ?: "[]"
                Json.decodeFromString<List<User>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    override suspend fun deleteUser(id: String) {
        val users = getAllUsers().toMutableList()
        users.removeAll { it.id == id }
        saveAllUsers(users)
    }
    
    override suspend fun clear() {
        userDefaults.removeObjectForKey(usersKey)
    }
    
    private fun saveAllUsers(users: List<User>) {
        val jsonString = Json.encodeToString(users)
        val data = jsonString.toNSData()
        userDefaults.setObject(data, forKey = usersKey)
    }
}

// 扩展函数用于字符串转换
fun String.toNSData(): NSData {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
}
```

### 10.2 共享业务逻辑实现

#### 10.2.1 用户管理业务逻辑

```kotlin
// commonMain中的业务逻辑实现

/**
 * 用户服务的通用实现
 * 包含所有平台共享的业务逻辑
 */
class UserService(
    private val networkClient: NetworkClient,
    private val localStorage: LocalStorage,
    private val logger: Logger
) {
    private val baseUrl = "https://jsonplaceholder.typicode.com"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 获取所有用户 - 网络优先，本地备份策略
     */
    suspend fun getAllUsers(forceRefresh: Boolean = false): ApiResponse<List<User>> {
        return try {
            if (!forceRefresh) {
                // 首先尝试从本地缓存加载
                val cachedUsers = localStorage.getAllUsers()
                if (cachedUsers.isNotEmpty()) {
                    logger.log("UserService", "从本地缓存加载了${cachedUsers.size}个用户")
                    return ApiResponse.Success(cachedUsers)
                }
            }
            
            // 从网络获取最新数据
            val response = networkClient.get("$baseUrl/users")
            val users = json.decodeFromString<List<User>>(response)
            
            // 缓存到本地
            localStorage.clear()
            users.forEach { user ->
                localStorage.saveUser(user)
            }
            
            logger.log("UserService", "从网络加载了${users.size}个用户")
            ApiResponse.Success(users, "数据已更新")
            
        } catch (e: Exception) {
            logger.error("UserService", "获取用户列表失败", e)
            
            // 网络失败时尝试返回本地数据
            val cachedUsers = localStorage.getAllUsers()
            if (cachedUsers.isNotEmpty()) {
                ApiResponse.Success(cachedUsers, "网络连接失败，显示缓存数据")
            } else {
                ApiResponse.Error(
                    code = getErrorCode(e),
                    message = "获取用户列表失败: ${e.message}",
                    details = mapOf("exception" to e::class.simpleName.orEmpty())
                )
            }
        }
    }
    
    /**
     * 根据ID获取用户详情
     */
    suspend fun getUserById(id: String): ApiResponse<User> {
        return try {
            // 首先尝试从本地获取
            val cachedUser = localStorage.getUser(id)
            if (cachedUser != null) {
                logger.log("UserService", "从本地缓存获取用户: $id")
            }
            
            // 从网络获取最新数据
            val response = networkClient.get("$baseUrl/users/$id")
            val user = json.decodeFromString<User>(response)
            
            // 更新本地缓存
            localStorage.saveUser(user)
            
            logger.log("UserService", "从网络获取用户详情: $id")
            ApiResponse.Success(user)
            
        } catch (e: Exception) {
            logger.error("UserService", "获取用户详情失败: $id", e)
            
            // 网络失败时返回本地数据
            val cachedUser = localStorage.getUser(id)
            if (cachedUser != null) {
                ApiResponse.Success(cachedUser, "网络连接失败，显示缓存数据")
            } else {
                ApiResponse.Error(
                    code = getErrorCode(e),
                    message = "获取用户详情失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 创建新用户
     */
    suspend fun createUser(user: User): ApiResponse<User> {
        return try {
            // 验证用户数据
            val validationResult = validateUser(user)
            if (validationResult != null) {
                return validationResult
            }
            
            val userWithId = user.copy(
                id = generateUUID(),
                createdAt = getCurrentTimestamp()
            )
            
            val jsonBody = json.encodeToString(userWithId)
            val response = networkClient.post("$baseUrl/users", jsonBody)
            val createdUser = json.decodeFromString<User>(response)
            
            // 保存到本地
            localStorage.saveUser(createdUser)
            
            logger.log("UserService", "成功创建用户: ${createdUser.id}")
            ApiResponse.Success(createdUser, "用户创建成功")
            
        } catch (e: Exception) {
            logger.error("UserService", "创建用户失败", e)
            ApiResponse.Error(
                code = getErrorCode(e),
                message = "创建用户失败: ${e.message}"
            )
        }
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUser(user: User): ApiResponse<User> {
        return try {
            // 验证用户数据
            val validationResult = validateUser(user)
            if (validationResult != null) {
                return validationResult
            }
            
            val jsonBody = json.encodeToString(user)
            val response = networkClient.put("$baseUrl/users/${user.id}", jsonBody)
            val updatedUser = json.decodeFromString<User>(response)
            
            // 更新本地缓存
            localStorage.saveUser(updatedUser)
            
            logger.log("UserService", "成功更新用户: ${updatedUser.id}")
            ApiResponse.Success(updatedUser, "用户信息已更新")
            
        } catch (e: Exception) {
            logger.error("UserService", "更新用户失败: ${user.id}", e)
            ApiResponse.Error(
                code = getErrorCode(e),
                message = "更新用户失败: ${e.message}"
            )
        }
    }
    
    /**
     * 删除用户
     */
    suspend fun deleteUser(id: String): ApiResponse<Unit> {
        return try {
            networkClient.delete("$baseUrl/users/$id")
            
            // 从本地删除
            localStorage.deleteUser(id)
            
            logger.log("UserService", "成功删除用户: $id")
            ApiResponse.Success(Unit, "用户已删除")
            
        } catch (e: Exception) {
            logger.error("UserService", "删除用户失败: $id", e)
            ApiResponse.Error(
                code = getErrorCode(e),
                message = "删除用户失败: ${e.message}"
            )
        }
    }
    
    /**
     * 搜索用户
     */
    suspend fun searchUsers(query: String): ApiResponse<List<User>> {
        return try {
            if (query.isBlank()) {
                return getAllUsers()
            }
            
            // 从本地搜索
            val allUsers = localStorage.getAllUsers()
            val filteredUsers = allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                        user.email.contains(query, ignoreCase = true)
            }
            
            logger.log("UserService", "本地搜索找到${filteredUsers.size}个匹配用户")
            ApiResponse.Success(filteredUsers)
            
        } catch (e: Exception) {
            logger.error("UserService", "搜索用户失败: $query", e)
            ApiResponse.Error(
                code = getErrorCode(e),
                message = "搜索用户失败: ${e.message}"
            )
        }
    }
    
    /**
     * 批量操作用户
     */
    suspend fun batchUpdateUsers(users: List<User>): ApiResponse<List<User>> {
        return try {
            val results = mutableListOf<User>()
            val errors = mutableListOf<String>()
            
            users.forEach { user ->
                when (val result = updateUser(user)) {
                    is ApiResponse.Success -> results.add(result.data)
                    is ApiResponse.Error -> errors.add("用户${user.id}: ${result.message}")
                    else -> errors.add("用户${user.id}: 未知错误")
                }
            }
            
            if (errors.isEmpty()) {
                ApiResponse.Success(results, "批量更新成功")
            } else {
                ApiResponse.Error(
                    code = 400,
                    message = "批量更新部分失败",
                    details = mapOf("errors" to errors.joinToString("; "))
                )
            }
            
        } catch (e: Exception) {
            logger.error("UserService", "批量更新用户失败", e)
            ApiResponse.Error(
                code = getErrorCode(e),
                message = "批量更新用户失败: ${e.message}"
            )
        }
    }
    
    /**
     * 用户数据验证
     */
    private fun validateUser(user: User): ApiResponse.Error? {
        val errors = mutableMapOf<String, String>()
        
        if (user.name.isBlank()) {
            errors["name"] = "用户名不能为空"
        }
        
        if (!user.isValidEmail()) {
            errors["email"] = "邮箱格式不正确"
        }
        
        if (user.name.length > 100) {
            errors["name"] = "用户名长度不能超过100个字符"
        }
        
        if (user.email.length > 200) {
            errors["email"] = "邮箱长度不能超过200个字符"
        }
        
        return if (errors.isNotEmpty()) {
            ApiResponse.Error(
                code = 400,
                message = "用户数据验证失败",
                details = errors
            )
        } else {
            null
        }
    }
    
    /**
     * 获取错误码
     */
    private fun getErrorCode(exception: Exception): Int {
        return when (exception) {
            is HttpException -> exception.code
            is NetworkException -> 0
            else -> -1
        }
    }
}

/**
 * 自定义异常类
 */
class HttpException(val code: Int, message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)

/**
 * 用户统计服务
 */
class UserAnalyticsService(
    private val userService: UserService,
    private val logger: Logger
) {
    /**
     * 获取用户统计数据
     */
    suspend fun getUserStatistics(): ApiResponse<UserStatistics> {
        return try {
            when (val result = userService.getAllUsers()) {
                is ApiResponse.Success -> {
                    val users = result.data
                    val statistics = UserStatistics(
                        totalUsers = users.size,
                        activeUsers = users.count { it.isActive },
                        inactiveUsers = users.count { !it.isActive },
                        averageUserAge = calculateAverageAge(users),
                        topDomains = getTopEmailDomains(users),
                        recentRegistrations = getRecentRegistrations(users),
                        userGrowthTrend = getUserGrowthTrend(users)
                    )
                    
                    logger.log("UserAnalytics", "生成用户统计数据: ${statistics.totalUsers}个用户")
                    ApiResponse.Success(statistics)
                }
                is ApiResponse.Error -> result
                else -> ApiResponse.Error(0, "获取用户数据失败")
            }
            
        } catch (e: Exception) {
            logger.error("UserAnalytics", "生成统计数据失败", e)
            ApiResponse.Error(-1, "生成统计数据失败: ${e.message}")
        }
    }
    
    private fun calculateAverageAge(users: List<User>): Double {
        if (users.isEmpty()) return 0.0
        
        val currentTime = getCurrentTimestamp()
        val totalAge = users.sumOf { user ->
            val ageInMillis = currentTime - user.createdAt
            val ageInDays = ageInMillis / (1000 * 60 * 60 * 24)
            ageInDays.toDouble()
        }
        
        return totalAge / users.size
    }
    
    private fun getTopEmailDomains(users: List<User>): List<Pair<String, Int>> {
        return users
            .mapNotNull { user ->
                user.email.substringAfterLast("@", "").takeIf { it.isNotEmpty() }
            }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }
    
    private fun getRecentRegistrations(users: List<User>): List<User> {
        val sevenDaysAgo = getCurrentTimestamp() - (7 * 24 * 60 * 60 * 1000)
        return users
            .filter { it.createdAt >= sevenDaysAgo }
            .sortedByDescending { it.createdAt }
    }
    
    private fun getUserGrowthTrend(users: List<User>): List<Pair<String, Int>> {
        val thirtyDaysAgo = getCurrentTimestamp() - (30 * 24 * 60 * 60 * 1000)
        val recentUsers = users.filter { it.createdAt >= thirtyDaysAgo }
        
        return recentUsers
            .groupBy { user ->
                val daysSinceRegistration = (getCurrentTimestamp() - user.createdAt) / (24 * 60 * 60 * 1000)
                (daysSinceRegistration / 7).toInt() // 按周分组
            }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.first }
            .map { (week, count) -> "第${week + 1}周" to count }
    }
}

/**
 * 用户统计数据模型
 */
@Serializable
data class UserStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val inactiveUsers: Int,
    val averageUserAge: Double,
    val topDomains: List<Pair<String, Int>>,
    val recentRegistrations: List<User>,
    val userGrowthTrend: List<Pair<String, Int>>
)
```

### 10.3 多平台项目配置与构建

#### 10.3.1 Gradle配置文件

```kotlin
// build.gradle.kts (Project level)
plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("com.android.library") version "8.1.0"
    id("org.jetbrains.compose") version "1.5.4"
}

kotlin {
    // 目标平台配置
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // 源码集配置
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("io.ktor:ktor-client-core:2.3.5")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
                
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.5")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
                implementation("androidx.room:room-runtime:2.6.0")
                implementation("androidx.room:room-ktx:2.6.0")
                
                // Android Compose
                implementation("androidx.compose.ui:ui:1.5.4")
                implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
                implementation("androidx.compose.material3:material3:1.1.2")
                implementation("androidx.activity:activity-compose:1.8.1")
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.5")
            }
        }
    }
}

android {
    namespace = "com.example.kmm"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

#### 10.3.2 共享UI组件（Compose Multiplatform）

```kotlin
// commonMain中的共享UI组件

@Composable
fun UserListScreen(
    userService: UserService,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // 使用LaunchedEffect执行异步操作
    LaunchedEffect(Unit) {
        when (val result = userService.getAllUsers()) {
            is ApiResponse.Success -> {
                users = result.data
                isLoading = false
            }
            is ApiResponse.Error -> {
                error = result.message
                isLoading = false
            }
            ApiResponse.Loading -> {
                // 保持加载状态
            }
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 搜索框
        SearchBar(
            query = searchQuery,
            onQueryChange = { query ->
                searchQuery = query
                // 实时搜索
                if (query.isNotEmpty()) {
                    // 这里可以添加防抖逻辑
                    LaunchedEffect(query) {
                        delay(300) // 防抖
                        when (val result = userService.searchUsers(query)) {
                            is ApiResponse.Success -> users = result.data
                            is ApiResponse.Error -> error = result.message
                            ApiResponse.Loading -> { }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        when {
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )
            }
            error != null -> {
                ErrorMessage(
                    message = error ?: "未知错误",
                    onRetry = {
                        isLoading = true
                        error = null
                        // 重新加载数据
                        LaunchedEffect(Unit) {
                            when (val result = userService.getAllUsers(forceRefresh = true)) {
                                is ApiResponse.Success -> {
                                    users = result.data
                                    isLoading = false
                                }
                                is ApiResponse.Error -> {
                                    error = result.message
                                    isLoading = false
                                }
                                ApiResponse.Loading -> { }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                UserList(
                    users = users,
                    onUserClick = onUserClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun UserList(
    users: List<User>,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserCard(
                user = user,
                onClick = { onUserClick(user) }
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            AsyncImage(
                model = user.avatarUrl ?: "",
                contentDescription = "用户头像",
                placeholder = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.getProfileInitials(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 状态指示器
            StatusIndicator(
                isActive = user.isActive,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun StatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isActive) {
                    Color.Green
                } else {
                    Color.Gray
                },
                shape = CircleShape
            )
    )
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索用户...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "搜索")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

// 跨平台的图片加载组件
@Composable
expect fun AsyncImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
)
```

### 10.4 多平台面试常考题

#### 题目1：KMM的架构原理

**问题：** Kotlin Multiplatform Mobile是如何实现代码共享的？有什么限制？

**答案：**
```kotlin
// KMM架构的核心机制
class KMMArchitectureExplanation {
    
    /**
     * 1. expect/actual机制 - 平台特定实现
     */
    
    // commonMain中的声明
    expect fun getCurrentPlatform(): String
    expect class HttpClient()
    expect suspend fun makeNetworkCall(url: String): String
    
    // androidMain中的实现
    actual fun getCurrentPlatform(): String = "Android"
    actual class HttpClient {
        private val okHttpClient = OkHttpClient()
        // Android specific implementation
    }
    
    // iosMain中的实现  
    actual fun getCurrentPlatform(): String = "iOS"
    actual class HttpClient {
        // iOS specific implementation using NSURLSession
    }
    
    /**
     * 2. 代码共享的层次
     */
    
    // 可以共享的：
    // - 业务逻辑代码
    // - 数据模型
    // - 网络请求逻辑
    // - 数据库操作
    // - 工具类和算法
    
    // 不能直接共享的：
    // - UI代码（需要Compose Multiplatform）
    // - 平台特定的API调用
    // - 文件系统操作
    // - 权限请求
    // - 推送通知处理
}

/**
 * 架构最佳实践
 */
class KMMBestPractices {
    
    /**
     * 推荐的项目结构
     */
    /*
    shared/
    ├── src/
    │   ├── commonMain/kotlin/
    │   │   ├── domain/          // 业务逻辑
    │   │   ├── data/           // 数据层
    │   │   └── presentation/   // 表示层逻辑
    │   ├── androidMain/kotlin/  // Android特定实现
    │   ├── iosMain/kotlin/     // iOS特定实现
    │   └── commonTest/kotlin/  // 共享测试
    androidApp/                 // Android应用
    iosApp/                    // iOS应用
    */
    
    /**
     * 依赖注入的处理方式
     */
    interface DependencyContainer {
        fun getUserService(): UserService
        fun getNetworkClient(): NetworkClient
        fun getLocalStorage(): LocalStorage
    }
    
    // Android实现
    class AndroidDependencyContainer(private val context: Context) : DependencyContainer {
        override fun getUserService(): UserService {
            return UserService(
                networkClient = AndroidNetworkClient(context),
                localStorage = AndroidLocalStorage(context),
                logger = Logger()
            )
        }
        // ... 其他依赖
    }
    
    // iOS实现  
    class IosDependencyContainer : DependencyContainer {
        override fun getUserService(): UserService {
            return UserService(
                networkClient = IosNetworkClient(),
                localStorage = IosLocalStorage(),
                logger = Logger()
            )
        }
        // ... 其他依赖
    }
}
```

**KMM的优势和限制：**

**优势：**
- 业务逻辑代码100%共享
- 减少重复开发工作
- 统一的API设计
- 更好的代码一致性

**限制：**
- UI需要平台特定实现（除非使用Compose Multiplatform）
- 平台特定功能需要expect/actual
- 学习成本较高
- 生态系统相对较小

#### 题目2：多平台项目的测试策略

**问题：** 如何为KMM项目编写有效的测试？如何处理平台特定的测试？

**答案：**
```kotlin
// commonTest - 共享的测试代码
class UserServiceTest {
    
    private lateinit var userService: UserService
    private lateinit var mockNetworkClient: MockNetworkClient
    private lateinit var mockLocalStorage: MockLocalStorage
    
    @BeforeEach
    fun setup() {
        mockNetworkClient = MockNetworkClient()
        mockLocalStorage = MockLocalStorage()
        userService = UserService(
            networkClient = mockNetworkClient,
            localStorage = mockLocalStorage,
            logger = TestLogger()
        )
    }
    
    @Test
    fun `getAllUsers should return cached data when available`() = runTest {
        // Given
        val cachedUsers = listOf(
            User("1", "张三", "zhang@test.com", null, 123456789L, true),
            User("2", "李四", "li@test.com", null, 123456790L, true)
        )
        mockLocalStorage.users = cachedUsers.toMutableList()
        
        // When
        val result = userService.getAllUsers(forceRefresh = false)
        
        // Then
        assertTrue(result is ApiResponse.Success)
        assertEquals(2, (result as ApiResponse.Success).data.size)
        assertEquals("张三", result.data[0].name)
    }
    
    @Test
    fun `getAllUsers should fetch from network when cache is empty`() = runTest {
        // Given
        mockLocalStorage.users = mutableListOf()
        mockNetworkClient.mockResponse = """
            [
                {"id":"1","name":"网络用户","email":"network@test.com","avatarUrl":null,"createdAt":123456789,"isActive":true}
            ]
        """.trimIndent()
        
        // When
        val result = userService.getAllUsers(forceRefresh = false)
        
        // Then
        assertTrue(result is ApiResponse.Success)
        assertEquals(1, (result as ApiResponse.Success).data.size)
        assertEquals("网络用户", result.data[0].name)
        
        // 验证数据被缓存
        assertEquals(1, mockLocalStorage.users.size)
    }
    
    @Test
    fun `createUser should validate user data`() = runTest {
        // Given
        val invalidUser = User("", "", "invalid-email", null, 0L, true)
        
        // When
        val result = userService.createUser(invalidUser)
        
        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(400, (result as ApiResponse.Error).code)
        assertTrue(result.message.contains("验证失败"))
    }
    
    @Test
    fun `searchUsers should filter by name and email`() = runTest {
        // Given
        val users = listOf(
            User("1", "张三", "zhang@test.com", null, 123456789L, true),
            User("2", "李四", "li@test.com", null, 123456790L, true),
            User("3", "王五", "wang@example.com", null, 123456791L, true)
        )
        mockLocalStorage.users = users.toMutableList()
        
        // When
        val result = userService.searchUsers("test")
        
        // Then
        assertTrue(result is ApiResponse.Success)
        assertEquals(2, (result as ApiResponse.Success).data.size)
        assertTrue(result.data.any { it.name == "张三" })
        assertTrue(result.data.any { it.name == "李四" })
    }
}

// Mock实现
class MockNetworkClient : NetworkClient {
    var mockResponse: String = ""
    var shouldThrowException: Boolean = false
    var exceptionToThrow: Exception = NetworkException("Mock network error")
    
    override suspend fun get(url: String, headers: Map<String, String>): String {
        if (shouldThrowException) throw exceptionToThrow
        return mockResponse
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): String {
        if (shouldThrowException) throw exceptionToThrow
        return mockResponse
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): String {
        if (shouldThrowException) throw exceptionToThrow
        return mockResponse
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): String {
        if (shouldThrowException) throw exceptionToThrow
        return mockResponse
    }
}

class MockLocalStorage : LocalStorage {
    var users = mutableListOf<User>()
    
    override suspend fun saveUser(user: User) {
        val existingIndex = users.indexOfFirst { it.id == user.id }
        if (existingIndex >= 0) {
            users[existingIndex] = user
        } else {
            users.add(user)
        }
    }
    
    override suspend fun getUser(id: String): User? {
        return users.find { it.id == id }
    }
    
    override suspend fun getAllUsers(): List<User> {
        return users.toList()
    }
    
    override suspend fun deleteUser(id: String) {
        users.removeAll { it.id == id }
    }
    
    override suspend fun clear() {
        users.clear()
    }
}

class TestLogger : Logger {
    override fun log(tag: String, message: String) {
        println("[$tag] $message")
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("ERROR [$tag] $message")
        throwable?.printStackTrace()
    }
}

// 平台特定的测试
// androidTest
class AndroidSpecificTest {
    
    @Test
    fun `AndroidNetworkClient should use OkHttp correctly`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val client = AndroidNetworkClient(context)
        
        // 测试Android特定的网络实现
        // ...
    }
    
    @Test
    fun `AndroidLocalStorage should use Room database`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val storage = AndroidLocalStorage(context)
        
        // 测试Room数据库操作
        val user = User("1", "测试用户", "test@example.com", null, getCurrentTimestamp(), true)
        storage.saveUser(user)
        
        val retrievedUser = storage.getUser("1")
        assertEquals(user.name, retrievedUser?.name)
    }
}

// iosTest - iOS平台特定测试
class IosSpecificTest {
    
    @Test
    fun `IosNetworkClient should use NSURLSession correctly`() = runTest {
        val client = IosNetworkClient()
        
        // 测试iOS特定的网络实现
        // ...
    }
    
    @Test
    fun `IosLocalStorage should use NSUserDefaults`() = runTest {
        val storage = IosLocalStorage()
        
        // 测试NSUserDefaults存储
        val user = User("1", "测试用户", "test@example.com", null, getCurrentTimestamp(), true)
        storage.saveUser(user)
        
        val retrievedUser = storage.getUser("1")
        assertEquals(user.name, retrievedUser?.name)
    }
}
```

**测试策略要点：**
- **共享测试：** 在commonTest中测试业务逻辑
- **Mock对象：** 使用Mock实现测试依赖
- **平台特定测试：** 在对应平台的测试模块中测试平台实现
- **集成测试：** 测试平台间的数据传递和兼容性

#### 题目3：性能优化策略

**问题：** KMM项目有哪些性能优化的考虑点？如何优化跨平台调用？

**答案：**
```kotlin
// 性能优化的关键策略
class KMMPerformanceOptimization {
    
    /**
     * 1. 减少跨平台调用开销
     */
    
    // 不好的实现：频繁的小调用
    class InefficientUserService {
        private val storage: LocalStorage = getLocalStorage()
        
        suspend fun processUsers(): List<String> {
            val result = mutableListOf<String>()
            val userCount = storage.getUserCount() // 调用1
            
            for (i in 0 until userCount) {
                val user = storage.getUserByIndex(i) // 调用N次
                result.add(user.name)
            }
            
            return result
        }
    }
    
    // 优化的实现：批量操作
    class OptimizedUserService {
        private val storage: LocalStorage = getLocalStorage()
        
        suspend fun processUsers(): List<String> {
            // 一次调用获取所有数据
            val allUsers = storage.getAllUsers()
            return allUsers.map { it.name }
        }
    }
    
    /**
     * 2. 内存优化 - 使用Sequence避免中间集合
     */
    
    class MemoryOptimizedOperations {
        
        fun processLargeUserList(users: List<User>): List<String> {
            // 使用序列避免创建中间集合
            return users.asSequence()
                .filter { it.isActive }
                .map { it.getDisplayName() }
                .filter { it.isNotEmpty() }
                .take(100)
                .toList()
        }
        
        // 流式处理大数据集
        suspend fun processUserDataStream(): Flow<ProcessedUser> = flow {
            val userService = getUserService()
            
            when (val result = userService.getAllUsers()) {
                is ApiResponse.Success -> {
                    result.data.asFlow()
                        .map { user -> processUser(user) }
                        .collect { processedUser -> emit(processedUser) }
                }
                else -> { /* 处理错误 */ }
            }
        }
        
        private suspend fun processUser(user: User): ProcessedUser {
            // 模拟处理逻辑
            delay(10)
            return ProcessedUser(
                id = user.id,
                displayName = user.getDisplayName(),
                domain = user.email.substringAfter("@")
            )
        }
    }
    
    /**
     * 3. 缓存策略优化
     */
    
    class SmartCacheManager {
        private val memoryCache = mutableMapOf<String, CacheEntry<Any>>()
        private val maxCacheSize = 100
        private val cacheExpireTime = 5 * 60 * 1000L // 5分钟
        
        inline fun <reified T> getOrPut(
            key: String,
            crossinline producer: suspend () -> T
        ): T {
            // 检查内存缓存
            val cached = memoryCache[key]
            if (cached != null && !cached.isExpired() && cached.data is T) {
                return cached.data
            }
            
            // 缓存未命中，获取新数据
            val newData = runBlocking { producer() }
            
            // 限制缓存大小
            if (memoryCache.size >= maxCacheSize) {
                // 移除最旧的条目
                val oldestKey = memoryCache.minByOrNull { it.value.timestamp }?.key
                oldestKey?.let { memoryCache.remove(it) }
            }
            
            memoryCache[key] = CacheEntry(newData, getCurrentTimestamp())
            return newData
        }
        
        fun invalidate(key: String) {
            memoryCache.remove(key)
        }
        
        fun clear() {
            memoryCache.clear()
        }
    }
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    ) {
        fun isExpired(): Boolean {
            return getCurrentTimestamp() - timestamp > 5 * 60 * 1000L
        }
    }
    
    /**
     * 4. 异步操作优化
     */
    
    class OptimizedAsyncOperations {
        
        // 并发执行多个独立操作
        suspend fun loadUserDashboard(userId: String): UserDashboard {
            return coroutineScope {
                val userDeferred = async { userService.getUserById(userId) }
                val statsDeferred = async { analyticsService.getUserStatistics() }
                val recentActivityDeferred = async { activityService.getRecentActivity(userId) }
                
                val user = userDeferred.await()
                val stats = statsDeferred.await()
                val recentActivity = recentActivityDeferred.await()
                
                UserDashboard(user, stats, recentActivity)
            }
        }
        
        // 使用Flow处理实时数据流
        fun observeUserUpdates(): Flow<User> = channelFlow {
            // 模拟实时数据源
            while (!isClosedForSend) {
                val users = userService.getAllUsers()
                if (users is ApiResponse.Success) {
                    users.data.forEach { user ->
                        send(user)
                        delay(100) // 控制发射频率
                    }
                }
                delay(5000) // 每5秒检查一次更新
            }
        }
    }
    
    /**
     * 5. 启动时间优化
     */
    
    class StartupOptimization {
        
        // 延迟初始化非关键组件
        val userService by lazy { createUserService() }
        val analyticsService by lazy { createAnalyticsService() }
        
        // 异步预热
        suspend fun warmUpServices() {
            coroutineScope {
                launch { preloadCriticalData() }
                launch { initializeBackgroundServices() }
            }
        }
        
        private suspend fun preloadCriticalData() {
            // 预加载关键数据
            userService.getAllUsers()
        }
        
        private suspend fun initializeBackgroundServices() {
            // 初始化后台服务
            analyticsService.initialize()
        }
    }
    
    /**
     * 6. 内存泄漏防护
     */
    
    class MemoryLeakPrevention {
        private val coroutineScope = CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )
        
        fun startPeriodicTask() {
            coroutineScope.launch {
                while (isActive) {
                    performPeriodicWork()
                    delay(60000) // 每分钟执行一次
                }
            }
        }
        
        fun cleanup() {
            coroutineScope.cancel() // 清理所有协程
        }
        
        private suspend fun performPeriodicWork() {
            // 定期清理缓存
            cacheManager.clearExpired()
            
            // 垃圾回收提示
            System.gc()
        }
    }
}

data class ProcessedUser(
    val id: String,
    val displayName: String,
    val domain: String
)

data class UserDashboard(
    val user: ApiResponse<User>,
    val stats: ApiResponse<UserStatistics>,
    val recentActivity: ApiResponse<List<Activity>>
)

data class Activity(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: Long
)
```

**性能优化要点：**
- **减少跨平台调用：** 使用批量操作，避免频繁的小调用
- **内存优化：** 使用序列和Flow处理大数据集
- **缓存策略：** 实现智能缓存，避免重复网络请求
- **异步优化：** 合理使用并发，避免阻塞操作
- **启动优化：** 延迟初始化，异步预热
- **资源管理：** 及时清理资源，避免内存泄漏

---

## 本章小结

第10章我们深入学习了Kotlin多平台开发：

### 主要内容：
1. **架构原理：** expect/actual机制、项目结构、平台特定实现
2. **代码共享：** 业务逻辑、数据模型、网络和存储抽象
3. **UI组件：** Compose Multiplatform的跨平台UI实现
4. **项目配置：** Gradle配置、依赖管理、构建优化

### 面试重点：
- **架构理解：** KMM的工作原理和代码共享机制
- **实际应用：** 如何设计跨平台的业务逻辑层
- **测试策略：** 共享测试和平台特定测试的编写
- **性能优化：** 跨平台调用优化和内存管理

Kotlin多平台开发是移动开发的未来趋势，掌握KMM能让你在跨平台开发领域具备强大的竞争力。

---