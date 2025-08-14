# 第一行代码 Kotlin版 - 函数式编程与DSL实战篇

> 掌握函数式编程思维，构建优雅的DSL，成为Kotlin架构师

## 目录

- [第8章 函数式编程深入](#第8章-函数式编程深入)
- [第9章 DSL设计与实现](#第9章-dsl设计与实现)
- [第10章 Kotlin多平台开发](#第10章-kotlin多平台开发)
- [第11章 测试驱动开发](#第11章-测试驱动开发)
- [第12章 架构模式与设计原则](#第12章-架构模式与设计原则)

---

## 第8章 函数式编程深入

函数式编程不仅仅是使用Lambda表达式，而是一种编程思维模式。本章将深入探讨如何在Kotlin中践行函数式编程理念。

### 8.1 函数式编程核心概念

#### 8.1.1 纯函数与副作用

```kotlin
// 纯函数：相同输入总是产生相同输出，且无副作用
fun add(a: Int, b: Int): Int = a + b

fun multiply(a: Int, b: Int): Int = a * b

// 非纯函数：有副作用
var counter = 0
fun impureIncrement(): Int {
    counter++  // 副作用：修改外部状态
    return counter
}

fun impurePrint(message: String): String {
    println(message)  // 副作用：I/O操作
    return message
}

// 将非纯函数转换为纯函数
data class CounterState(val value: Int)

fun pureIncrement(state: CounterState): CounterState {
    return state.copy(value = state.value + 1)
}

// 函数式的日志记录
data class LogEntry(val level: String, val message: String, val timestamp: Long)
data class Logger(val entries: List<LogEntry>)

fun log(logger: Logger, level: String, message: String): Logger {
    val entry = LogEntry(level, message, System.currentTimeMillis())
    return logger.copy(entries = logger.entries + entry)
}

fun demonstratePurity() {
    // 纯函数的好处：可预测、可测试、可组合
    val result1 = add(2, 3)
    val result2 = add(2, 3)
    assert(result1 == result2)  // 总是相等
    
    // 函数式状态管理
    var state = CounterState(0)
    state = pureIncrement(state)
    state = pureIncrement(state)
    println("计数器值: ${state.value}")  // 2
    
    // 函数式日志
    var logger = Logger(emptyList())
    logger = log(logger, "INFO", "应用启动")
    logger = log(logger, "ERROR", "发生错误")
    logger.entries.forEach { println("${it.level}: ${it.message}") }
}
```

#### 8.1.2 不可变性与数据结构

```kotlin
// 不可变数据结构
data class ImmutableStack<T>(private val elements: List<T> = emptyList()) {
    
    fun push(element: T): ImmutableStack<T> {
        return ImmutableStack(elements + element)
    }
    
    fun pop(): Pair<T?, ImmutableStack<T>> {
        return if (elements.isEmpty()) {
            null to this
        } else {
            elements.last() to ImmutableStack(elements.dropLast(1))
        }
    }
    
    fun peek(): T? = elements.lastOrNull()
    
    fun isEmpty(): Boolean = elements.isEmpty()
    
    fun size(): Int = elements.size
    
    override fun toString(): String = "Stack${elements.reversed()}"
}

// 不可变二叉树
sealed class ImmutableTree<T : Comparable<T>> {
    object Empty : ImmutableTree<Nothing>()
    data class Node<T : Comparable<T>>(
        val value: T,
        val left: ImmutableTree<T> = Empty,
        val right: ImmutableTree<T> = Empty
    ) : ImmutableTree<T>()
    
    fun insert(newValue: T): ImmutableTree<T> = when (this) {
        is Empty -> Node(newValue)
        is Node -> when {
            newValue < value -> copy(left = left.insert(newValue))
            newValue > value -> copy(right = right.insert(newValue))
            else -> this  // 值已存在，不插入
        }
    }
    
    fun contains(searchValue: T): Boolean = when (this) {
        is Empty -> false
        is Node -> when {
            searchValue < value -> left.contains(searchValue)
            searchValue > value -> right.contains(searchValue)
            else -> true
        }
    }
    
    fun toList(): List<T> = when (this) {
        is Empty -> emptyList()
        is Node -> left.toList() + value + right.toList()
    }
}

fun testImmutableDataStructures() {
    // 测试不可变栈
    var stack = ImmutableStack<Int>()
    stack = stack.push(1).push(2).push(3)
    println("栈: $stack")
    
    val (top, newStack) = stack.pop()
    println("弹出: $top, 新栈: $newStack")
    
    // 测试不可变树
    var tree: ImmutableTree<Int> = ImmutableTree.Empty
    tree = tree.insert(5).insert(3).insert(7).insert(1).insert(9)
    
    println("树包含5: ${tree.contains(5)}")
    println("树包含6: ${tree.contains(6)}")
    println("中序遍历: ${tree.toList()}")
}
```

### 8.2 高阶函数模式

#### 8.2.1 函数组合

```kotlin
// 函数组合操作符
infix fun <A, B, C> ((B) -> C).compose(f: (A) -> B): (A) -> C {
    return { a -> this(f(a)) }
}

infix fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C {
    return { a -> f(this(a)) }
}

// 管道操作符
infix fun <T, R> T.pipe(f: (T) -> R): R = f(this)

// 实际应用：数据处理管道
class DataProcessor {
    
    // 基础处理函数
    val trimWhitespace: (String) -> String = { it.trim() }
    val toUpperCase: (String) -> String = { it.uppercase() }
    val removeNumbers: (String) -> String = { it.filter { char -> !char.isDigit() } }
    val addPrefix: (String) -> (String) -> String = { prefix -> { text -> "$prefix$text" } }
    
    // 组合处理函数
    val cleanAndFormat = trimWhitespace andThen 
                        removeNumbers andThen 
                        toUpperCase andThen 
                        addPrefix("PROCESSED: ")
    
    // 使用管道风格
    fun processWithPipe(input: String): String {
        return input pipe trimWhitespace pipe 
               removeNumbers pipe 
               toUpperCase pipe 
               addPrefix("PIPED: ")
    }
    
    // 批量处理
    fun processData(data: List<String>): List<String> {
        return data.map(cleanAndFormat)
    }
}

fun testFunctionComposition() {
    val processor = DataProcessor()
    
    val testData = listOf(
        "  hello world 123  ",
        "kotlin programming 456",
        "  functional style 789  "
    )
    
    // 测试组合函数
    val processed = processor.processData(testData)
    processed.forEach { println(it) }
    
    // 测试管道风格
    val pipeResult = processor.processWithPipe("  test pipe 999  ")
    println("管道结果: $pipeResult")
}
```

#### 8.2.2 柯里化与部分应用

```kotlin
// 柯里化：将多参数函数转换为单参数函数链
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
    return { a -> { b -> f(a, b) } }
}

fun <A, B, C, D> curry(f: (A, B, C) -> D): (A) -> (B) -> (C) -> D {
    return { a -> { b -> { c -> f(a, b, c) } } }
}

// 反柯里化
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C {
    return { a, b -> f(a)(b) }
}

// 部分应用
fun <A, B, C> partial(f: (A, B) -> C, a: A): (B) -> C {
    return { b -> f(a, b) }
}

fun <A, B, C, D> partial(f: (A, B, C) -> D, a: A, b: B): (C) -> D {
    return { c -> f(a, b, c) }
}

// 实际应用示例
class MathOperations {
    
    // 基础数学函数
    val add: (Int, Int) -> Int = { a, b -> a + b }
    val multiply: (Int, Int, Int) -> Int = { a, b, c -> a * b * c }
    val power: (Double, Double) -> Double = { base, exp -> Math.pow(base, exp) }
    
    // 柯里化版本
    val curriedAdd = curry(add)
    val curriedMultiply = curry(multiply)
    
    // 部分应用创建专用函数
    val add10 = partial(add, 10)  // 加10的函数
    val square = partial(power, 2.0)  // 平方函数
    val cube = partial(power, 3.0)   // 立方函数
    
    // 复合函数示例
    fun createCalculator(baseValue: Int): Calculator {
        return Calculator(
            addBase = partial(add, baseValue),
            multiplyByBase = partial({ a, b -> a * b }, baseValue)
        )
    }
}

data class Calculator(
    val addBase: (Int) -> Int,
    val multiplyByBase: (Int) -> Int
)

fun testCurryingAndPartialApplication() {
    val math = MathOperations()
    
    // 测试柯里化
    val add5 = math.curriedAdd(5)
    println("5 + 3 = ${add5(3)}")
    
    val multiply2 = math.curriedMultiply(2)
    val multiply2And3 = multiply2(3)
    println("2 * 3 * 4 = ${multiply2And3(4)}")
    
    // 测试部分应用
    println("10 + 7 = ${math.add10(7)}")
    println("2^4 = ${math.square(4.0)}")
    println("3^3 = ${math.cube(3.0)}")
    
    // 测试计算器
    val calc10 = math.createCalculator(10)
    println("10 + 5 = ${calc10.addBase(5)}")
    println("10 * 3 = ${calc10.multiplyByBase(3)}")
}
```

### 8.3 Monad模式

#### 8.3.1 Maybe Monad

```kotlin
// Maybe Monad实现
sealed class Maybe<out T> {
    object None : Maybe<Nothing>()
    data class Some<T>(val value: T) : Maybe<T>()
    
    // Functor: map
    fun <R> map(transform: (T) -> R): Maybe<R> = when (this) {
        is None -> None
        is Some -> Some(transform(value))
    }
    
    // Monad: flatMap
    fun <R> flatMap(transform: (T) -> Maybe<R>): Maybe<R> = when (this) {
        is None -> None
        is Some -> transform(value)
    }
    
    // Applicative: apply
    fun <R> apply(fn: Maybe<(T) -> R>): Maybe<R> = when (fn) {
        is None -> None
        is Some -> map(fn.value)
    }
    
    // 工具方法
    fun getOrElse(default: T): T = when (this) {
        is None -> default
        is Some -> value
    }
    
    fun filter(predicate: (T) -> Boolean): Maybe<T> = when (this) {
        is None -> None
        is Some -> if (predicate(value)) this else None
    }
    
    fun isEmpty(): Boolean = this is None
    fun isNotEmpty(): Boolean = this is Some
    
    companion object {
        fun <T> just(value: T): Maybe<T> = Some(value)
        fun <T> none(): Maybe<T> = None
        
        fun <T> fromNullable(value: T?): Maybe<T> = 
            if (value != null) Some(value) else None
    }
}

// Maybe的实际应用
class UserService {
    private val users = mapOf(
        "1" to User("1", "张三", "zhang@example.com"),
        "2" to User("2", "李四", "li@example.com")
    )
    
    fun findUser(id: String): Maybe<User> {
        return Maybe.fromNullable(users[id])
    }
    
    fun getUserEmail(id: String): Maybe<String> {
        return findUser(id).map { it.email }
    }
    
    fun getUserDomain(id: String): Maybe<String> {
        return getUserEmail(id)
            .map { it.substringAfter("@") }
            .filter { it.isNotEmpty() }
    }
}

fun testMaybeMonad() {
    val userService = UserService()
    
    // 成功案例
    val user1Email = userService.getUserEmail("1")
    println("用户1邮箱: ${user1Email.getOrElse("未找到")}")
    
    val user1Domain = userService.getUserDomain("1")
    println("用户1域名: ${user1Domain.getOrElse("未找到")}")
    
    // 失败案例
    val user3Email = userService.getUserEmail("3")
    println("用户3邮箱: ${user3Email.getOrElse("未找到")}")
    
    // 链式操作
    val result = userService.findUser("1")
        .map { "用户名: ${it.name}" }
        .map { it.uppercase() }
        .getOrElse("用户不存在")
    
    println("处理结果: $result")
}
```

#### 8.3.2 Either Monad

```kotlin
// Either Monad: 表示成功或失败
sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
    
    // Functor
    fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }
    
    // Monad
    fun <T> flatMap(transform: (R) -> Either<L, T>): Either<L, T> = when (this) {
        is Left -> this
        is Right -> transform(value)
    }
    
    // 错误映射
    fun <T> mapLeft(transform: (L) -> T): Either<T, R> = when (this) {
        is Left -> Left(transform(value))
        is Right -> this
    }
    
    // 工具方法
    fun isLeft(): Boolean = this is Left
    fun isRight(): Boolean = this is Right
    
    fun getOrElse(default: R): R = when (this) {
        is Left -> default
        is Right -> value
    }
    
    fun fold(leftTransform: (L) -> R, rightTransform: (R) -> R): R = when (this) {
        is Left -> leftTransform(value)
        is Right -> rightTransform(value)
    }
    
    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
        
        // 异常捕获
        fun <R> catch(action: () -> R): Either<Exception, R> {
            return try {
                Right(action())
            } catch (e: Exception) {
                Left(e)
            }
        }
    }
}

// 错误类型定义
sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class NotFoundError(val resource: String) : AppError()
}

// 使用Either进行错误处理
class ValidationService {
    
    fun validateEmail(email: String): Either<AppError, String> {
        return if (email.contains("@") && email.contains(".")) {
            Either.right(email)
        } else {
            Either.left(AppError.ValidationError("email", "邮箱格式不正确"))
        }
    }
    
    fun validateAge(age: Int): Either<AppError, Int> {
        return if (age in 1..120) {
            Either.right(age)
        } else {
            Either.left(AppError.ValidationError("age", "年龄必须在1-120之间"))
        }
    }
    
    fun validateUser(name: String, email: String, age: Int): Either<AppError, User> {
        return validateEmail(email).flatMap { validEmail ->
            validateAge(age).map { validAge ->
                User("generated_id", name, validEmail)
            }
        }
    }
}

class NetworkService {
    
    fun fetchData(url: String): Either<AppError, String> {
        return Either.catch {
            // 模拟网络请求
            if (url.startsWith("https://")) {
                "数据从 $url 获取成功"
            } else {
                throw IllegalArgumentException("无效的URL")
            }
        }.mapLeft { AppError.NetworkError(it.message ?: "网络错误") }
    }
    
    fun processData(data: String): Either<AppError, String> {
        return if (data.length > 10) {
            Either.right(data.uppercase())
        } else {
            Either.left(AppError.ValidationError("data", "数据长度不足"))
        }
    }
    
    fun pipeline(url: String): Either<AppError, String> {
        return fetchData(url).flatMap { data ->
            processData(data)
        }
    }
}

fun testEitherMonad() {
    val validator = ValidationService()
    val network = NetworkService()
    
    // 测试验证
    val validUser = validator.validateUser("张三", "zhang@example.com", 25)
    println("验证用户: ${validUser.fold({ "错误: $it" }, { "成功: $it" })}")
    
    val invalidUser = validator.validateUser("李四", "invalid-email", 150)
    println("验证用户: ${invalidUser.fold({ "错误: $it" }, { "成功: $it" })}")
    
    // 测试网络请求
    val successResult = network.pipeline("https://api.example.com/data")
    println("网络请求: ${successResult.getOrElse("请求失败")}")
    
    val failResult = network.pipeline("http://invalid-url")
    println("网络请求: ${failResult.fold({ "错误: $it" }, { it })}")
}
```

### 8.4 函数式集合操作

#### 8.4.1 高级集合变换

```kotlin
// 函数式集合处理工具
object FunctionalCollections {
    
    // 分组变换
    fun <T, K, V> List<T>.groupByTransform(
        keySelector: (T) -> K,
        valueTransform: (T) -> V
    ): Map<K, List<V>> {
        return this.groupBy(keySelector, valueTransform)
    }
    
    // 窗口操作
    fun <T> List<T>.windowed(size: Int, step: Int = 1): List<List<T>> {
        return this.windowed(size, step)
    }
    
    // 累积扫描
    fun <T, R> List<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R> {
        val result = mutableListOf(initial)
        var accumulator = initial
        for (element in this) {
            accumulator = operation(accumulator, element)
            result.add(accumulator)
        }
        return result
    }
    
    // 索引变换
    fun <T, R> List<T>.mapIndexed(transform: (index: Int, T) -> R): List<R> {
        return this.mapIndexed(transform)
    }
    
    // 条件映射
    fun <T, R> List<T>.mapNotNull(transform: (T) -> R?): List<R> {
        return this.mapNotNull(transform)
    }
    
    // 交错操作
    fun <T> List<T>.interleave(other: List<T>): List<T> {
        val result = mutableListOf<T>()
        val maxSize = maxOf(this.size, other.size)
        for (i in 0 until maxSize) {
            if (i < this.size) result.add(this[i])
            if (i < other.size) result.add(other[i])
        }
        return result
    }
    
    // 分批处理
    fun <T, R> List<T>.batchProcess(
        batchSize: Int,
        processor: (List<T>) -> List<R>
    ): List<R> {
        return this.chunked(batchSize).flatMap(processor)
    }
}

// 实际应用：数据分析
class DataAnalyzer {
    
    data class SalesRecord(
        val date: String,
        val product: String,
        val amount: Double,
        val quantity: Int
    )
    
    fun analyzeSalesData(records: List<SalesRecord>): SalesAnalysis {
        // 按产品分组统计
        val productSales = records.groupByTransform(
            keySelector = { it.product },
            valueTransform = { it.amount }
        ).mapValues { (_, amounts) -> amounts.sum() }
        
        // 按日期统计趋势
        val dailySales = records.groupBy { it.date }
            .mapValues { (_, dayRecords) -> dayRecords.sumOf { it.amount } }
            .toList()
            .sortedBy { it.first }
        
        // 计算移动平均
        val movingAverage = dailySales.map { it.second }
            .windowed(3) { window -> window.average() }
        
        // 累积销售额
        val cumulativeSales = dailySales.map { it.second }
            .scan(0.0) { acc, daily -> acc + daily }
            .drop(1) // 移除初始值
        
        return SalesAnalysis(
            productSales = productSales,
            dailyTrend = dailySales.map { it.second },
            movingAverage = movingAverage,
            cumulativeSales = cumulativeSales
        )
    }
    
    // 异常值检测
    fun detectOutliers(values: List<Double>): List<Int> {
        val mean = values.average()
        val stdDev = Math.sqrt(values.map { (it - mean).pow(2) }.average())
        val threshold = 2.0 * stdDev
        
        return values.mapIndexedNotNull { index, value ->
            if (Math.abs(value - mean) > threshold) index else null
        }
    }
}

data class SalesAnalysis(
    val productSales: Map<String, Double>,
    val dailyTrend: List<Double>,
    val movingAverage: List<Double>,
    val cumulativeSales: List<Double>
)

fun testFunctionalCollections() {
    val analyzer = DataAnalyzer()
    
    val salesData = listOf(
        DataAnalyzer.SalesRecord("2024-01", "产品A", 1000.0, 10),
        DataAnalyzer.SalesRecord("2024-01", "产品B", 1500.0, 15),
        DataAnalyzer.SalesRecord("2024-02", "产品A", 1200.0, 12),
        DataAnalyzer.SalesRecord("2024-02", "产品B", 1800.0, 18),
        DataAnalyzer.SalesRecord("2024-03", "产品A", 900.0, 9),
        DataAnalyzer.SalesRecord("2024-03", "产品B", 2000.0, 20)
    )
    
    val analysis = analyzer.analyzeSalesData(salesData)
    
    println("产品销售额:")
    analysis.productSales.forEach { (product, sales) ->
        println("  $product: $sales")
    }
    
    println("日销售趋势: ${analysis.dailyTrend}")
    println("移动平均: ${analysis.movingAverage}")
    println("累积销售: ${analysis.cumulativeSales}")
    
    // 测试异常值检测
    val testValues = listOf(1.0, 2.0, 3.0, 100.0, 4.0, 5.0, 6.0)
    val outliers = analyzer.detectOutliers(testValues)
    println("异常值索引: $outliers")
}
```

### 8.5 函数式编程面试常考题

#### 题目1：函数式vs面向对象

**问题：** 函数式编程和面向对象编程的主要区别是什么？什么时候选择函数式？

**答案：**
```kotlin
// 面向对象方式
class BankAccountOOP(private var balance: Double) {
    fun deposit(amount: Double) {
        balance += amount  // 可变状态
    }
    
    fun withdraw(amount: Double): Boolean {
        return if (balance >= amount) {
            balance -= amount
            true
        } else {
            false
        }
    }
    
    fun getBalance(): Double = balance
}

// 函数式方式
data class BankAccountFP(val balance: Double) {
    fun deposit(amount: Double): BankAccountFP {
        return copy(balance = balance + amount)  // 不可变
    }
    
    fun withdraw(amount: Double): Either<String, BankAccountFP> {
        return if (balance >= amount) {
            Either.right(copy(balance = balance - amount))
        } else {
            Either.left("余额不足")
        }
    }
}

// 使用对比
fun compareParadigms() {
    // OOP方式
    val oopAccount = BankAccountOOP(1000.0)
    oopAccount.deposit(500.0)
    println("OOP余额: ${oopAccount.getBalance()}")
    
    // FP方式
    var fpAccount = BankAccountFP(1000.0)
    fpAccount = fpAccount.deposit(500.0)
    
    val withdrawResult = fpAccount.withdraw(200.0)
    withdrawResult.fold(
        leftTransform = { error -> println("错误: $error") },
        rightTransform = { newAccount -> 
            fpAccount = newAccount
            println("FP余额: ${fpAccount.balance}")
        }
    )
}
```

**选择原则：**
- **函数式：** 并发安全、数据处理、无副作用场景
- **面向对象：** 复杂状态管理、UI组件、业务建模

#### 题目2：柯里化的实际应用

**问题：** 柯里化在实际开发中有什么用途？如何提高代码复用性？

**答案：**
```kotlin
// 配置函数的柯里化应用
class ApiClientBuilder {
    
    // 柯里化的HTTP请求函数
    val makeRequest = curry4 { method: String, baseUrl: String, path: String, params: Map<String, String> ->
        "$method $baseUrl$path?${params.entries.joinToString("&") { "${it.key}=${it.value}" }}"
    }
    
    // 创建专用的API函数
    val postToApi = makeRequest("POST")("https://api.example.com")
    val getFromApi = makeRequest("GET")("https://api.example.com")
    
    // 更具体的函数
    val getUserData = getFromApi("/users")
    val createUser = postToApi("/users")
    
    fun demonstrateReuse() {
        val userParams = mapOf("id" to "123")
        val userData = getUserData(userParams)
        println("获取用户: $userData")
        
        val createParams = mapOf("name" to "张三", "email" to "zhang@example.com")
        val createResult = createUser(createParams)
        println("创建用户: $createResult")
    }
}

// 4参数柯里化
fun <A, B, C, D, E> curry4(f: (A, B, C, D) -> E): (A) -> (B) -> (C) -> (D) -> E {
    return { a -> { b -> { c -> { d -> f(a, b, c, d) } } } }
}

// 验证函数的柯里化
class ValidationBuilder {
    
    val validate = curry3 { field: String, rule: (String) -> Boolean, value: String ->
        if (rule(value)) {
            Either.right(value)
        } else {
            Either.left("字段 $field 验证失败")
        }
    }
    
    // 预定义验证规则
    val emailRule: (String) -> Boolean = { it.contains("@") && it.contains(".") }
    val notEmptyRule: (String) -> Boolean = { it.isNotEmpty() }
    val lengthRule: (Int) -> (String) -> Boolean = { minLength -> { it.length >= minLength } }
    
    // 专用验证函数
    val validateEmail = validate("email")(emailRule)
    val validateName = validate("name")(notEmptyRule)
    val validatePassword = validate("password")(lengthRule(8))
    
    fun validateUser(name: String, email: String, password: String): Either<String, User> {
        return validateName(name).flatMap { validName ->
            validateEmail(email).flatMap { validEmail ->
                validatePassword(password).map { validPassword ->
                    User("generated", validName, validEmail)
                }
            }
        }
    }
}

fun <A, B, C, D> curry3(f: (A, B, C) -> D): (A) -> (B) -> (C) -> D {
    return { a -> { b -> { c -> f(a, b, c) } } }
}
```

#### 题目3：不可变数据结构的性能

**问题：** 不可变数据结构会不会影响性能？如何优化？

**答案：**
```kotlin
// 性能优化的不可变数据结构
class OptimizedImmutableList<T> private constructor(
    private val elements: Array<Any?>,
    private val size: Int
) {
    
    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        return elements[index] as T
    }
    
    // 结构共享优化
    fun add(element: T): OptimizedImmutableList<T> {
        val newElements = elements.copyOf(size + 1)
        newElements[size] = element
        return OptimizedImmutableList(newElements, size + 1)
    }
    
    // 批量操作优化
    fun addAll(newElements: Collection<T>): OptimizedImmutableList<T> {
        val totalSize = size + newElements.size
        val combined = elements.copyOf(totalSize)
        var index = size
        for (element in newElements) {
            combined[index++] = element
        }
        return OptimizedImmutableList(combined, totalSize)
    }
    
    // 懒加载转换
    fun <R> map(transform: (T) -> R): Sequence<R> {
        return (0 until size).asSequence().map { transform(get(it)) }
    }
    
    companion object {
        fun <T> empty(): OptimizedImmutableList<T> {
            return OptimizedImmutableList(emptyArray(), 0)
        }
        
        fun <T> of(vararg elements: T): OptimizedImmutableList<T> {
            return OptimizedImmutableList(elements.copyOf(), elements.size)
        }
    }
}

// 性能测试
class PerformanceComparison {
    
    fun compareListPerformance() {
        val size = 100000
        
        // 可变列表性能
        val mutableTime = measureTimeMillis {
            val mutableList = mutableListOf<Int>()
            repeat(size) { mutableList.add(it) }
        }
        
        // 不可变列表性能（朴素实现）
        val immutableTime = measureTimeMillis {
            var immutableList = listOf<Int>()
            repeat(size) { immutableList = immutableList + it }
        }
        
        // 优化的不可变列表
        val optimizedTime = measureTimeMillis {
            var optimizedList = OptimizedImmutableList.empty<Int>()
            repeat(size) { optimizedList = optimizedList.add(it) }
        }
        
        println("可变列表: ${mutableTime}ms")
        println("不可变列表: ${immutableTime}ms")
        println("优化不可变列表: ${optimizedTime}ms")
    }
    
    // 内存使用对比
    fun compareMemoryUsage() {
        val runtime = Runtime.getRuntime()
        
        // 测试数据共享
        val baseList = OptimizedImmutableList.of(*(1..1000).toList().toTypedArray())
        
        System.gc()
        val before = runtime.totalMemory() - runtime.freeMemory()
        
        // 创建多个派生列表
        val derivedLists = (1..100).map { baseList.add(it) }
        
        System.gc()
        val after = runtime.totalMemory() - runtime.freeMemory()
        
        println("创建100个派生列表的内存开销: ${(after - before) / 1024}KB")
    }
}
```

**优化策略：**
- **结构共享：** 新版本共享未修改的部分
- **持久化数据结构：** 使用Trie等高效结构
- **写时复制：** 延迟到真正修改时才复制
- **批量操作：** 减少中间对象创建

#### 题目4：Monad的实际应用

**问题：** Monad在Android开发中如何应用？解决了什么问题？

**答案：**
```kotlin
// Android中的Monad应用：异步操作链
sealed class AsyncResult<out T> {
    object Loading : AsyncResult<Nothing>()
    data class Success<T>(val data: T) : AsyncResult<T>()
    data class Error(val exception: Throwable) : AsyncResult<Nothing>()
    
    // Monad操作
    fun <R> map(transform: (T) -> R): AsyncResult<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    fun <R> flatMap(transform: (T) -> AsyncResult<R>): AsyncResult<R> = when (this) {
        is Loading -> Loading
        is Success -> transform(data)
        is Error -> this
    }
    
    // Android特定的工具方法
    fun onSuccess(action: (T) -> Unit): AsyncResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    fun onError(action: (Throwable) -> Unit): AsyncResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    fun onLoading(action: () -> Unit): AsyncResult<T> {
        if (this is Loading) action()
        return this
    }
}

// Repository模式中的应用
class UserRepository {
    
    suspend fun getUser(id: String): AsyncResult<User> {
        return try {
            AsyncResult.Loading
            // 模拟网络请求
            delay(1000)
            AsyncResult.Success(User(id, "用户$id", "user$id@example.com"))
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }
    
    suspend fun getUserPosts(userId: String): AsyncResult<List<Post>> {
        return try {
            delay(500)
            AsyncResult.Success(
                listOf(
                    Post("1", "帖子1", userId),
                    Post("2", "帖子2", userId)
                )
            )
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }
    }
    
    // 链式异步操作
    suspend fun getUserWithPosts(userId: String): AsyncResult<UserWithPosts> {
        return getUser(userId).flatMap { user ->
            getUserPosts(userId).map { posts ->
                UserWithPosts(user, posts)
            }
        }
    }
}

data class Post(val id: String, val title: String, val userId: String)
data class UserWithPosts(val user: User, val posts: List<Post>)

// ViewModel中的使用
class UserViewModel(private val repository: UserRepository) : ViewModel() {
    
    private val _uiState = MutableLiveData<AsyncResult<UserWithPosts>>()
    val uiState: LiveData<AsyncResult<UserWithPosts>> = _uiState
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = AsyncResult.Loading
            
            val result = repository.getUserWithPosts(userId)
            _uiState.value = result
            
            // 链式处理结果
            result
                .onSuccess { data ->
                    Log.d("UserViewModel", "加载成功: ${data.user.name}")
                }
                .onError { error ->
                    Log.e("UserViewModel", "加载失败", error)
                }
        }
    }
}

// UI中的使用
fun observeUserData(viewModel: UserViewModel) {
    viewModel.uiState.observe(lifecycleOwner) { result ->
        result
            .onLoading {
                showLoading()
            }
            .onSuccess { data ->
                hideLoading()
                displayUser(data.user)
                displayPosts(data.posts)
            }
            .onError { error ->
                hideLoading()
                showError(error.message ?: "未知错误")
            }
    }
}
```

**Monad解决的问题：**
- **错误传播：** 自动处理错误，避免嵌套的try-catch
- **空值处理：** 链式操作中的空值安全
- **异步组合：** 优雅地组合异步操作
- **状态管理：** 统一的成功/失败/加载状态

#### 题目5：函数式编程的性能陷阱

**问题：** 函数式编程有哪些性能陷阱？如何避免？

**答案：**
```kotlin
// 性能陷阱示例
class FunctionalPerformanceTraps {
    
    // 陷阱1：过度使用链式操作
    fun inefficientChaining(numbers: List<Int>): List<Int> {
        return numbers
            .filter { it > 0 }      // 创建中间List
            .map { it * 2 }         // 创建中间List
            .filter { it < 100 }    // 创建中间List
            .sorted()               // 创建最终List
    }
    
    // 优化：使用序列
    fun efficientChaining(numbers: List<Int>): List<Int> {
        return numbers.asSequence()
            .filter { it > 0 }
            .map { it * 2 }
            .filter { it < 100 }
            .sorted()
            .toList()  // 只在最后创建一个List
    }
    
    // 陷阱2：递归导致栈溢出
    fun naiveRecursiveSum(numbers: List<Int>): Int {
        return if (numbers.isEmpty()) {
            0
        } else {
            numbers.first() + naiveRecursiveSum(numbers.drop(1))
        }
    }
    
    // 优化：尾递归
    tailrec fun tailRecursiveSum(numbers: List<Int>, acc: Int = 0): Int {
        return if (numbers.isEmpty()) {
            acc
        } else {
            tailRecursiveSum(numbers.drop(1), acc + numbers.first())
        }
    }
    
    // 陷阱3：不必要的对象创建
    fun inefficientObjectCreation(): List<String> {
        return (1..1000).map { i ->
            "Item $i".let { str ->
                str.uppercase().let { upper ->
                    "[$upper]"
                }
            }
        }
    }
    
    // 优化：减少中间对象
    fun efficientObjectCreation(): List<String> {
        return (1..1000).map { i ->
            "[ITEM $i]"  // 直接构造最终结果
        }
    }
    
    // 陷阱4：重复计算
    fun expensiveCalculation(x: Int): Int {
        Thread.sleep(100)  // 模拟昂贵计算
        return x * x
    }
    
    fun inefficientMapping(numbers: List<Int>): List<Int> {
        return numbers.map { expensiveCalculation(it) }
            .filter { it > 100 }
            .map { expensiveCalculation(it) }  // 重复计算！
    }
    
    // 优化：缓存结果
    private val calculationCache = mutableMapOf<Int, Int>()
    
    fun cachedCalculation(x: Int): Int {
        return calculationCache.getOrPut(x) { expensiveCalculation(x) }
    }
    
    fun efficientMapping(numbers: List<Int>): List<Int> {
        return numbers.map { cachedCalculation(it) }
            .filter { it > 100 }
            .map { it }  // 直接使用已计算的值
    }
    
    // 性能测试
    fun performanceTest() {
        val largeList = (1..100000).toList()
        
        // 测试链式操作
        val time1 = measureTimeMillis {
            inefficientChaining(largeList)
        }
        
        val time2 = measureTimeMillis {
            efficientChaining(largeList)
        }
        
        println("低效链式操作: ${time1}ms")
        println("高效链式操作: ${time2}ms")
        println("性能提升: ${time1.toFloat() / time2}x")
        
        // 测试递归
        val mediumList = (1..10000).toList()
        
        val time3 = measureTimeMillis {
            tailRecursiveSum(mediumList)
        }
        
        println("尾递归求和: ${time3}ms")
    }
}
```

**避免性能陷阱的策略：**
- **使用序列：** 大数据集的链式操作
- **尾递归优化：** 避免栈溢出
- **结果缓存：** 避免重复计算
- **延迟计算：** 只在需要时执行
- **批量操作：** 减少中间对象创建

---

## 本章小结

第8章我们深入探讨了函数式编程：

### 主要内容：
1. **核心概念：** 纯函数、不可变性、无副作用
2. **高阶函数：** 函数组合、柯里化、部分应用
3. **Monad模式：** Maybe、Either的实际应用
4. **集合操作：** 高级变换、数据分析
5. **性能优化：** 避免常见陷阱，提升效率

### 面试重点：
- **函数式vs面向对象：** 适用场景和选择原则
- **柯里化应用：** 提高代码复用性的实际案例
- **不可变数据结构：** 性能优化和内存管理
- **Monad模式：** 错误处理和异步操作组合
- **性能陷阱：** 识别和避免常见性能问题

函数式编程不是银弹，但在正确的场景下能极大提升代码的可读性、可测试性和可维护性。

---