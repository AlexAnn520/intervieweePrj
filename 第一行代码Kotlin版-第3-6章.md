# 第一行代码 Kotlin版 - 第3-6章

> 接续前面章节，深入学习Kotlin的高级特性和Android实践

---

## 第3章 Kotlin特有特性 - 空安全与函数式编程

第3章将带你领略Kotlin最具魅力的特性。如果说前两章让你感受到了Kotlin的简洁和强大，那么这一章将让你体验到Kotlin的革命性创新——彻底解决空指针异常，以及优雅的函数式编程支持。

### 3.1 空安全（Null Safety）

还记得Java中那些令人头疼的`NullPointerException`吗？在Kotlin中，这个问题得到了彻底的解决！

#### 3.1.1 可空类型与非空类型

```kotlin
fun demonstrateNullSafety() {
    // 非空类型 - 不能为null
    var nonNullString: String = "Hello"
    // nonNullString = null  // 编译错误！
    
    // 可空类型 - 可以为null
    var nullableString: String? = "World"
    nullableString = null  // OK
    
    // 编译器强制检查
    // println(nullableString.length)  // 编译错误！需要空检查
    
    // 安全访问
    println(nullableString?.length)  // 安全调用操作符
    
    // Elvis操作符
    val length = nullableString?.length ?: 0
    println("字符串长度：$length")
    
    // 非空断言（慎用！）
    // val forceLength = nullableString!!.length  // 可能抛出KotlinNullPointerException
}
```

#### 3.1.2 空安全操作符详解

```kotlin
data class Person(val name: String, val address: Address?)
data class Address(val city: String, val street: String?)

fun exploreNullSafetyOperators() {
    val person: Person? = Person("张三", Address("北京", null))
    
    // 1. 安全调用操作符 ?.
    println("城市：${person?.address?.city}")
    println("街道：${person?.address?.street}")
    
    // 2. Elvis操作符 ?:
    val city = person?.address?.city ?: "未知城市"
    val street = person?.address?.street ?: "未知街道"
    println("地址：$city $street")
    
    // 3. 安全转换 as?
    val personAsString = person as? String  // 转换失败返回null
    println("转换结果：$personAsString")
    
    // 4. let函数配合空安全
    person?.address?.let { address ->
        println("处理地址：${address.city}")
        // 只有address不为null时才执行这里的代码
    }
    
    // 5. 链式调用的空安全
    val result = person
        ?.address
        ?.city
        ?.uppercase()
        ?.take(2)
    println("处理结果：$result")
}
```

#### 3.1.3 空检查的最佳实践

```kotlin
class UserService {
    private val users = mutableMapOf<String, User>()
    
    // 返回可空类型
    fun findUser(id: String): User? {
        return users[id]
    }
    
    // 处理可空参数
    fun updateUser(id: String, name: String?, email: String?) {
        val user = findUser(id) ?: return  // 早返回模式
        
        // 使用作用域函数处理可空值
        name?.let { user.name = it }
        email?.let { user.email = it }
        
        users[id] = user
    }
    
    // 集合的空安全处理
    fun getUsersByCity(city: String?): List<User> {
        return if (city == null) {
            emptyList()
        } else {
            users.values.filter { it.address?.city == city }
        }
    }
    
    // 复杂的空安全逻辑
    fun getFullAddress(user: User?): String {
        return user?.address?.let { address ->
            buildString {
                append(address.city)
                address.street?.let { 
                    append(", $it")
                }
            }
        } ?: "地址未知"
    }
}

// 平台类型处理
fun handlePlatformTypes() {
    // Java方法返回的类型是平台类型 String!
    // 你需要决定如何处理
    val javaString = getJavaString()  // 来自Java的方法
    
    // 选择1：当作非空处理
    val nonNull: String = javaString
    
    // 选择2：当作可空处理
    val nullable: String? = javaString
    
    // 最佳实践：根据Java文档和实际情况决定
}
```

### 3.2 扩展函数（Extension Functions）

扩展函数是Kotlin的另一个杀手级特性，可以为现有类添加新功能，而无需修改原类或继承。

#### 3.2.1 基础扩展函数

```kotlin
// 为String类添加扩展函数
fun String.isEmailValid(): Boolean {
    return this.contains("@") && this.contains(".")
}

// 为Int类添加扩展函数
fun Int.isEven(): Boolean = this % 2 == 0
fun Int.isOdd(): Boolean = !this.isEven()

// 为List添加扩展函数
fun <T> List<T>.secondOrNull(): T? {
    return if (this.size >= 2) this[1] else null
}

// 使用扩展函数
fun testBasicExtensions() {
    val email = "user@example.com"
    println("邮箱有效：${email.isEmailValid()}")
    
    val number = 42
    println("$number 是偶数：${number.isEven()}")
    
    val list = listOf("A", "B", "C")
    println("第二个元素：${list.secondOrNull()}")
}
```

#### 3.2.2 实用的扩展函数示例

```kotlin
// 日期格式化扩展
fun Date.formatToString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

// View扩展（Android开发常用）
fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.showIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.GONE
}

// Context扩展
fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.dp2px(dp: Float): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

// 集合扩展
fun <T> List<T>.random(): T? {
    return if (isEmpty()) null else this[(0 until size).random()]
}

fun <K, V> Map<K, V>.getOrDefault(key: K, defaultValue: V): V {
    return this[key] ?: defaultValue
}

// 字符串处理扩展
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) this else take(maxLength) + suffix
}

fun String.removeSpaces(): String = replace(" ", "")

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.capitalize() }
}

// 使用示例
fun testPracticalExtensions() {
    // 日期扩展
    val now = Date()
    println("当前时间：${now.formatToString()}")
    println("日期：${now.formatToString("yyyy-MM-dd")}")
    
    // 字符串扩展
    val text = "hello kotlin world"
    println("标题化：${text.capitalizeWords()}")
    println("截断：${text.truncate(10)}")
    
    // 集合扩展
    val numbers = listOf(1, 2, 3, 4, 5)
    println("随机数：${numbers.random()}")
}
```

#### 3.2.3 扩展属性

```kotlin
// 扩展属性
val String.lastChar: Char?
    get() = if (isEmpty()) null else this[length - 1]

val <T> List<T>.lastIndex: Int
    get() = size - 1

val Int.kb: Long
    get() = this * 1024L

val Int.mb: Long
    get() = this.kb * 1024L

val Int.gb: Long
    get() = this.mb * 1024L

// 可变扩展属性
var StringBuilder.firstChar: Char
    get() = if (isEmpty()) '\u0000' else this[0]
    set(value) {
        if (isNotEmpty()) {
            this[0] = value
        }
    }

fun testExtensionProperties() {
    val text = "Kotlin"
    println("最后一个字符：${text.lastChar}")
    
    val list = listOf(1, 2, 3, 4, 5)
    println("最后索引：${list.lastIndex}")
    
    val fileSize = 256.mb
    println("文件大小：$fileSize 字节")
    
    val sb = StringBuilder("Hello")
    sb.firstChar = 'h'
    println("修改后：$sb")
}
```

### 3.3 高阶函数与Lambda表达式

高阶函数是指接受函数作为参数或返回函数的函数，这是函数式编程的核心特性。

#### 3.3.1 Lambda表达式基础

```kotlin
fun demonstrateLambdas() {
    // Lambda表达式语法
    val sum = { x: Int, y: Int -> x + y }
    println("求和：${sum(3, 5)}")
    
    // 类型推断
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map { it * 2 }  // it是隐式参数
    println("翻倍：$doubled")
    
    // 多行Lambda
    val complexOperation = { x: Int ->
        val temp = x * 2
        val result = temp + 10
        result  // 最后一个表达式是返回值
    }
    println("复杂操作：${complexOperation(5)}")
    
    // 函数引用
    fun isEven(x: Int): Boolean = x % 2 == 0
    val evenNumbers = numbers.filter(::isEven)  // 函数引用
    println("偶数：$evenNumbers")
    
    // 成员函数引用
    val strings = listOf("hello", "world", "kotlin")
    val lengths = strings.map(String::length)  // 成员函数引用
    println("长度：$lengths")
}
```

#### 3.3.2 常用的高阶函数

```kotlin
fun exploreHigherOrderFunctions() {
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    
    // filter - 过滤
    val evenNumbers = numbers.filter { it % 2 == 0 }
    println("偶数：$evenNumbers")
    
    // map - 转换
    val squares = numbers.map { it * it }
    println("平方：$squares")
    
    // forEach - 遍历
    numbers.forEach { print("$it ") }
    println()
    
    // find - 查找第一个匹配的元素
    val firstEven = numbers.find { it % 2 == 0 }
    println("第一个偶数：$firstEven")
    
    // any - 是否存在匹配的元素
    val hasEven = numbers.any { it % 2 == 0 }
    println("包含偶数：$hasEven")
    
    // all - 是否所有元素都匹配
    val allPositive = numbers.all { it > 0 }
    println("都是正数：$allPositive")
    
    // reduce - 累积操作
    val sum = numbers.reduce { acc, n -> acc + n }
    println("总和：$sum")
    
    // fold - 带初始值的累积操作
    val product = numbers.fold(1) { acc, n -> acc * n }
    println("乘积：$product")
    
    // groupBy - 分组
    val groupedByRemainder = numbers.groupBy { it % 3 }
    println("按余数分组：$groupedByRemainder")
    
    // partition - 分区
    val (evens, odds) = numbers.partition { it % 2 == 0 }
    println("偶数：$evens，奇数：$odds")
}
```

#### 3.3.3 自定义高阶函数

```kotlin
// 定义高阶函数
fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

// 函数类型作为参数
fun calculate(x: Int, y: Int, operation: (Int, Int) -> Int): Int {
    return operation(x, y)
}

// 返回函数的函数
fun getOperation(type: String): (Int, Int) -> Int {
    return when (type) {
        "add" -> { x, y -> x + y }
        "multiply" -> { x, y -> x * y }
        "subtract" -> { x, y -> x - y }
        else -> { _, _ -> 0 }
    }
}

// 函数类型的变量
fun testFunctionTypes() {
    // 函数类型变量
    var operation: (Int, Int) -> Int = { x, y -> x + y }
    println("加法：${operation(3, 5)}")
    
    operation = { x, y -> x * y }
    println("乘法：${operation(3, 5)}")
    
    // 使用自定义高阶函数
    val numbers = listOf(1, 2, 3, 4, 5, 6)
    val evenNumbers = numbers.customFilter { it % 2 == 0 }
    println("自定义过滤：$evenNumbers")
    
    // 使用calculate函数
    val addResult = calculate(10, 20) { x, y -> x + y }
    val multiplyResult = calculate(10, 20) { x, y -> x * y }
    println("计算结果：加法=$addResult，乘法=$multiplyResult")
    
    // 使用返回函数的函数
    val adder = getOperation("add")
    println("获取的加法函数：${adder(15, 25)}")
}

// 实际应用：网络请求回调
typealias NetworkCallback<T> = (T?, String?) -> Unit

class NetworkManager {
    fun <T> request(
        url: String,
        success: (T) -> Unit,
        error: (String) -> Unit
    ) {
        // 模拟网络请求
        try {
            // 假设请求成功
            @Suppress("UNCHECKED_CAST")
            success("请求成功的数据" as T)
        } catch (e: Exception) {
            error("网络请求失败：${e.message}")
        }
    }
    
    // 简化版本
    fun <T> requestSimple(url: String, callback: NetworkCallback<T>) {
        try {
            @Suppress("UNCHECKED_CAST")
            callback("数据" as T, null)
        } catch (e: Exception) {
            callback(null, e.message)
        }
    }
}

fun testNetworkExample() {
    val networkManager = NetworkManager()
    
    // 使用分离的成功和失败回调
    networkManager.request<String>(
        url = "https://api.example.com/data",
        success = { data ->
            println("请求成功：$data")
        },
        error = { errorMsg ->
            println("请求失败：$errorMsg")
        }
    )
    
    // 使用单一回调
    networkManager.requestSimple<String>("https://api.example.com/data") { data, error ->
        if (data != null) {
            println("成功：$data")
        } else {
            println("失败：$error")
        }
    }
}
```

### 3.4 作用域函数

Kotlin提供了几个作用域函数，让代码更加简洁和表达性更强。

#### 3.4.1 let函数

```kotlin
fun demonstrateLet() {
    // let - 处理可空对象
    val nullableString: String? = "Hello Kotlin"
    
    nullableString?.let { str ->
        println("字符串长度：${str.length}")
        println("大写：${str.uppercase()}")
    }
    
    // 链式调用
    val result = "   hello world   "
        .let { it.trim() }
        .let { it.split(" ") }
        .let { it.map { word -> word.capitalize() } }
        .let { it.joinToString(" ") }
    
    println("处理结果：$result")
    
    // 作为表达式使用
    val number = "42".let { str ->
        try {
            str.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
    println("转换数字：$number")
}
```

#### 3.4.2 apply函数

```kotlin
data class User(var name: String = "", var email: String = "", var age: Int = 0)

fun demonstrateApply() {
    // apply - 配置对象
    val user = User().apply {
        name = "张三"
        email = "zhang@example.com"
        age = 25
    }
    println("用户：$user")
    
    // TextView配置示例（Android开发）
    /*
    val textView = TextView(context).apply {
        text = "Hello Kotlin"
        textSize = 16f
        setTextColor(Color.BLUE)
        gravity = Gravity.CENTER
    }
    */
    
    // 集合操作
    val list = mutableListOf<String>().apply {
        add("Kotlin")
        add("Java")
        add("Python")
        sort()
    }
    println("语言列表：$list")
}
```

#### 3.4.3 run函数

```kotlin
fun demonstrateRun() {
    // run - 执行代码块并返回结果
    val result = run {
        val x = 10
        val y = 20
        x * y  // 返回值
    }
    println("计算结果：$result")
    
    // 对象的run
    val user = User("李四", "li@example.com", 30)
    val info = user.run {
        "姓名：$name，年龄：$age，邮箱：$email"
    }
    println("用户信息：$info")
    
    // 可空对象的run
    val nullableUser: User? = User("王五", "wang@example.com", 28)
    nullableUser?.run {
        println("处理用户：$name")
        println("发送邮件到：$email")
    }
}
```

#### 3.4.4 with函数

```kotlin
fun demonstrateWith() {
    val user = User("赵六", "zhao@example.com", 35)
    
    // with - 对象作为参数
    val description = with(user) {
        "用户名：$name\n邮箱：$email\n年龄：$age"
    }
    println(description)
    
    // 配置多个属性
    val numbers = mutableListOf<Int>()
    with(numbers) {
        add(1)
        add(2)
        add(3)
        println("列表大小：$size")
        println("内容：$this")
    }
}
```

#### 3.4.5 also函数

```kotlin
fun demonstrateAlso() {
    val user = User("孙七", "sun@example.com", 40)
        .also { println("创建用户：${it.name}") }
        .also { user ->
            // 记录日志
            println("记录用户创建日志：${user.email}")
        }
        .also {
            // 发送通知
            println("发送欢迎邮件给：${it.name}")
        }
    
    println("最终用户：$user")
    
    // 链式调用中的调试
    val result = listOf(1, 2, 3, 4, 5)
        .filter { it > 2 }
        .also { println("过滤后：$it") }
        .map { it * 2 }
        .also { println("映射后：$it") }
        .sum()
    
    println("最终结果：$result")
}
```

#### 3.4.6 作用域函数总结

```kotlin
fun scopeFunctionsSummary() {
    val user: User? = User("总结", "summary@example.com", 25)
    
    // let - 处理可空对象，返回lambda结果
    user?.let { 
        println("let: 处理非空用户 ${it.name}")
        it.age
    }
    
    // apply - 配置对象，返回对象本身
    user?.apply {
        name = "修改后的名字"
        age = 26
    }
    
    // run - 执行代码块，返回lambda结果
    user?.run {
        println("run: 用户年龄是 $age")
        age > 18
    }
    
    // also - 执行附加操作，返回对象本身
    user?.also {
        println("also: 记录用户 ${it.name}")
    }
    
    // with - 对象作为参数，返回lambda结果
    user?.let { nonNullUser ->
        with(nonNullUser) {
            println("with: 用户信息 $name, $email")
        }
    }
}
```

### 3.5 面试常考题

#### 题目1：空安全的实现原理

**问题：** Kotlin是如何实现空安全的？编译器做了哪些检查？

**答案：**
```kotlin
// Kotlin的空安全是编译时检查
fun nullSafetyInternals() {
    var nonNull: String = "Hello"
    var nullable: String? = "World"
    
    // 编译器在编译时检查：
    // 1. 非空类型不能赋值为null
    // nonNull = null  // 编译错误
    
    // 2. 可空类型访问成员需要检查
    // println(nullable.length)  // 编译错误
    
    // 3. 编译器生成的字节码包含检查
    if (nullable != null) {
        println(nullable.length)  // 智能转换，这里是安全的
    }
    
    // 4. 平台类型的处理
    // Java方法返回String!（平台类型）
    // 程序员决定如何处理：
    val javaResult = getJavaString()  // String!
    val asNonNull: String = javaResult      // 当作非空
    val asNullable: String? = javaResult    // 当作可空
}

// 字节码层面的null检查
fun compiledNullCheck(value: String?) {
    // Kotlin编译后会生成类似的检查：
    // if (value == null) throw KotlinNullPointerException()
    println(value!!.length)  // !!操作符会生成运行时检查
}
```

**要点：**
- 空安全是编译时特性，运行时有少量开销
- 智能转换通过控制流分析实现
- `!!`操作符会生成运行时null检查
- 平台类型需要开发者明确处理

#### 题目2：扩展函数的限制和原理

**问题：** 扩展函数有什么限制？它们是如何工作的？

**答案：**
```kotlin
// 扩展函数的限制
class MyClass {
    private val secret = "私有数据"
    fun publicMethod() = "公开方法"
}

// 扩展函数的限制：
fun MyClass.cannotAccessPrivate(): String {
    // return secret  // 编译错误！不能访问私有成员
    return publicMethod()  // 只能访问公开成员
}

// 扩展函数是静态解析的
open class Base
class Derived : Base()

fun Base.foo() = "Base扩展"
fun Derived.foo() = "Derived扩展"

fun testStaticResolution() {
    val base: Base = Derived()  // 运行时类型是Derived
    println(base.foo())  // 输出"Base扩展"！因为是静态解析
}

// 成员函数优先级高于扩展函数
class Example {
    fun memberFunction() = "成员函数"
}

fun Example.memberFunction() = "扩展函数"  // 被忽略

fun testMemberPriority() {
    val example = Example()
    println(example.memberFunction())  // 输出"成员函数"
}

// 扩展函数的字节码实现
fun String.reverse(): String {
    // 编译后变成静态方法：
    // public static String reverse(String $this) {
    //     return new StringBuilder($this).reverse().toString();
    // }
    return StringBuilder(this).reverse().toString()
}
```

**要点：**
- 扩展函数不能访问私有成员
- 扩展函数是静态解析的，不支持多态
- 成员函数优先级高于扩展函数
- 编译后变成静态方法，第一个参数是接收者

#### 题目3：高阶函数的性能

**问题：** 高阶函数对性能有什么影响？如何优化？

**答案：**
```kotlin
// Lambda表达式的性能影响
fun performanceComparison() {
    val numbers = (1..1000000).toList()
    
    // 1. 普通循环（最快）
    var sum1 = 0
    val time1 = measureTimeMillis {
        for (number in numbers) {
            if (number % 2 == 0) {
                sum1 += number * number
            }
        }
    }
    
    // 2. 高阶函数链（较慢，创建中间集合）
    val time2 = measureTimeMillis {
        val sum2 = numbers
            .filter { it % 2 == 0 }  // 创建中间List
            .map { it * it }         // 创建另一个中间List
            .sum()
    }
    
    // 3. 序列优化（推荐）
    val time3 = measureTimeMillis {
        val sum3 = numbers.asSequence()
            .filter { it % 2 == 0 }
            .map { it * it }
            .sum()  // 惰性求值，不创建中间集合
    }
    
    println("普通循环: ${time1}ms")
    println("高阶函数: ${time2}ms") 
    println("序列优化: ${time3}ms")
}

// inline函数优化
inline fun <T> myFilter(list: List<T>, predicate: (T) -> Boolean): List<T> {
    // inline关键字让编译器将函数体内联到调用点
    // 避免了函数对象的创建开销
    val result = mutableListOf<T>()
    for (item in list) {
        if (predicate(item)) {  // 这里会被内联
            result.add(item)
        }
    }
    return result
}

// noinline参数
inline fun <T> processItems(
    items: List<T>,
    inline processor: (T) -> Unit,    // 会被内联
    noinline logger: (String) -> Unit // 不会被内联
) {
    logger("开始处理 ${items.size} 个项目")
    for (item in items) {
        processor(item)
    }
    logger("处理完成")
}
```

**要点：**
- Lambda表达式会创建函数对象，有性能开销
- 高阶函数链会创建中间集合，消耗内存
- 使用`sequence`进行惰性求值优化
- `inline`函数可以消除Lambda的开销
- `noinline`标记不需要内联的参数

#### 题目4：作用域函数的选择

**问题：** 什么时候使用哪个作用域函数？它们的区别是什么？

**答案：**
```kotlin
// 作用域函数选择指南
fun scopeFunctionGuide() {
    val user: User? = getUser()
    
    // 1. let - 处理可空对象，进行转换
    val userInfo = user?.let { 
        "${it.name} (${it.age}岁)"  // 返回转换结果
    }
    
    // 2. apply - 配置对象属性
    val configuredUser = User().apply {
        name = "配置的用户"  // 配置属性
        age = 25
    }  // 返回User对象本身
    
    // 3. run - 计算并返回结果
    val isAdult = user?.run {
        age >= 18  // 返回Boolean结果
    }
    
    // 4. also - 执行附加操作（如日志、验证）
    val validatedUser = user?.also { 
        println("验证用户：${it.name}")  // 副作用操作
        validateUser(it)
    }  // 返回原始User对象
    
    // 5. with - 对非空对象执行多个操作
    user?.let { nonNullUser ->
        with(nonNullUser) {
            println("姓名：$name")     // 不需要it或this
            println("年龄：$age")
            updateLastLogin()
        }
    }
}

// 实际应用场景
fun practicalScenarios() {
    // 场景1：配置Builder
    val request = HttpRequest.Builder().apply {
        url("https://api.example.com")
        method("POST")
        header("Content-Type", "application/json")
    }.build()
    
    // 场景2：空安全转换
    val phoneNumber: String? = getPhoneNumber()
    val formattedPhone = phoneNumber?.let { phone ->
        if (phone.startsWith("+86")) phone else "+86$phone"
    }
    
    // 场景3：条件执行
    val result = calculateSomething()
    result.takeIf { it > 0 }?.also { 
        println("计算成功：$it")
        saveResult(it)
    }
    
    // 场景4：资源管理
    FileInputStream("file.txt").use { input ->
        input.readBytes()  // use确保文件被关闭
    }
}

// 返回值总结
fun returnValueSummary() {
    val obj = User("test", "test@example.com", 25)
    
    val letResult: String = obj.let { "let返回lambda结果: ${it.name}" }
    val applyResult: User = obj.apply { name = "modified" }  // 返回对象本身
    val runResult: Int = obj.run { name.length }  // 返回lambda结果
    val alsoResult: User = obj.also { println("also: ${it.name}") }  // 返回对象本身
    val withResult: String = with(obj) { "with: $name" }  // 返回lambda结果
}
```

**选择指南：**
- `let`: 可空对象转换，链式调用
- `apply`: 对象初始化和配置  
- `run`: 计算并返回结果
- `also`: 附加操作（日志、验证等）
- `with`: 对非空对象的多个操作

#### 题目5：函数式编程的优缺点

**问题：** 在Android开发中使用函数式编程有什么优缺点？

**答案：**
```kotlin
// 函数式编程的优点
fun functionalAdvantages() {
    val users = listOf(
        User("张三", "zhang@example.com", 25),
        User("李四", "li@example.com", 30),
        User("王五", "wang@example.com", 22)
    )
    
    // 优点1：代码简洁，表达性强
    val adultEmails = users
        .filter { it.age >= 18 }
        .map { it.email }
        .sorted()
    
    // vs 命令式写法（更冗长）
    val adultEmailsImperative = mutableListOf<String>()
    for (user in users) {
        if (user.age >= 18) {
            adultEmailsImperative.add(user.email)
        }
    }
    adultEmailsImperative.sort()
    
    // 优点2：不可变性，减少bug
    val processedUsers = users.map { it.copy(age = it.age + 1) }
    // 原始users未被修改
    
    // 优点3：易于测试和组合
    val emailValidator: (String) -> Boolean = { it.contains("@") }
    val ageValidator: (Int) -> Boolean = { it >= 0 }
    
    fun validateUser(user: User): Boolean {
        return emailValidator(user.email) && ageValidator(user.age)
    }
}

// 函数式编程的缺点和注意事项
fun functionalDisadvantages() {
    val largeList = (1..1000000).toList()
    
    // 缺点1：性能开销（中间集合）
    val inefficient = largeList
        .filter { it % 2 == 0 }      // 创建中间List
        .map { it * it }             // 创建另一个中间List
        .filter { it > 1000 }        // 再创建一个中间List
        .take(10)
    
    // 解决方案：使用Sequence
    val efficient = largeList.asSequence()
        .filter { it % 2 == 0 }
        .map { it * it }
        .filter { it > 1000 }
        .take(10)
        .toList()  // 只在最后创建一个List
    
    // 缺点2：调试困难
    val result = listOf(1, 2, 3, 4, 5)
        .map { it * 2 }
        .filter { it > 5 }
        .fold(0) { acc, n -> acc + n }
    // 在链式调用中设置断点比较困难
    
    // 解决方案：使用also进行调试
    val debugResult = listOf(1, 2, 3, 4, 5)
        .map { it * 2 }
        .also { println("映射后: $it") }
        .filter { it > 5 }
        .also { println("过滤后: $it") }
        .fold(0) { acc, n -> acc + n }
    
    // 缺点3：学习曲线陡峭
    // 复杂的函数式代码可能难以理解
}

// Android开发中的最佳实践
fun androidBestPractices() {
    // 1. ViewModel中的数据转换
    /*
    class UserViewModel : ViewModel() {
        private val _users = MutableLiveData<List<User>>()
        
        val displayUsers: LiveData<List<UserDisplayItem>> = _users.map { users ->
            users.map { user ->
                UserDisplayItem(
                    name = user.name.uppercase(),
                    ageGroup = if (user.age < 30) "年轻" else "成熟"
                )
            }
        }
    }
    */
    
    // 2. 网络响应处理
    /*
    suspend fun loadUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            Result.success(
                response.data
                    .filter { it.isActive }
                    .map { it.toUser() }
                    .sortedBy { it.name }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
    
    // 3. UI状态管理
    /*
    val uiState = combine(
        usersFlow,
        searchQueryFlow,
        sortOptionFlow
    ) { users, query, sortOption ->
        users
            .filter { it.name.contains(query, ignoreCase = true) }
            .let { filteredUsers ->
                when (sortOption) {
                    SortOption.NAME -> filteredUsers.sortedBy { it.name }
                    SortOption.AGE -> filteredUsers.sortedBy { it.age }
                }
            }
            .let { UserListState.Success(it) }
    }
    */
}
```

**总结：**
- **优点：** 代码简洁、表达性强、易于测试、减少副作用
- **缺点：** 性能开销、调试困难、学习曲线陡峭
- **最佳实践：** 合理使用序列优化、结合命令式编程、注意性能监控

---

## 本章小结

第3章我们深入学习了Kotlin的特有特性：

### 主要内容：
1. **空安全：** 可空类型、安全调用操作符、Elvis操作符
2. **扩展函数：** 为现有类添加功能，提高代码复用性
3. **高阶函数：** Lambda表达式、函数类型、内联函数
4. **作用域函数：** let、apply、run、also、with的使用场景

### 面试重点：
- **空安全原理：** 编译时检查、智能转换、平台类型处理
- **扩展函数限制：** 静态解析、不能访问私有成员、成员优先
- **高阶函数性能：** 对象创建开销、序列优化、内联函数
- **作用域函数选择：** 根据返回值和使用场景选择合适的函数

下一章我们将学习Kotlin的集合框架和泛型机制。

---

## 第4章 集合与泛型 - 数据容器的使用

Kotlin的集合框架在Java集合的基础上做了很多改进，提供了丰富的API和更好的类型安全。本章将详细介绍各种集合类型的使用方法以及强大的泛型机制。

### 4.1 集合概述

#### 4.1.1 可变与不可变集合

```kotlin
fun demonstrateCollectionTypes() {
    // 不可变集合（只读）
    val readOnlyList = listOf("A", "B", "C")
    val readOnlySet = setOf(1, 2, 3, 2)  // 重复元素会被去除
    val readOnlyMap = mapOf("key1" to "value1", "key2" to "value2")
    
    println("只读列表: $readOnlyList")
    println("只读集合: $readOnlySet")
    println("只读映射: $readOnlyMap")
    
    // 可变集合
    val mutableList = mutableListOf("X", "Y", "Z")
    val mutableSet = mutableSetOf(10, 20, 30)
    val mutableMap = mutableMapOf("name" to "张三", "age" to "25")
    
    // 修改可变集合
    mutableList.add("W")
    mutableSet.remove(10)
    mutableMap["city"] = "北京"
    
    println("可变列表: $mutableList")
    println("可变集合: $mutableSet") 
    println("可变映射: $mutableMap")
    
    // 类型关系
    val list: List<String> = mutableList  // MutableList是List的子类型
    // list.add("V")  // 编译错误！List接口没有add方法
}
```

#### 4.1.2 集合的创建方式

```kotlin
fun collectionCreationMethods() {
    // List创建方式
    val list1 = listOf(1, 2, 3)                    // 不可变
    val list2 = mutableListOf(1, 2, 3)             // 可变
    val list3 = arrayListOf(1, 2, 3)               // ArrayList
    val list4 = List(5) { it * it }                // 工厂函数：[0, 1, 4, 9, 16]
    val list5 = (1..10).toList()                   // 范围转换
    
    // Set创建方式
    val set1 = setOf("a", "b", "c")
    val set2 = mutableSetOf("a", "b", "c")
    val set3 = hashSetOf("a", "b", "c")
    val set4 = linkedSetOf("a", "b", "c")          // 保持插入顺序
    val set5 = sortedSetOf("c", "a", "b")          // 自动排序：[a, b, c]
    
    // Map创建方式
    val map1 = mapOf("key1" to "value1", "key2" to "value2")
    val map2 = mutableMapOf("key1" to "value1")
    val map3 = hashMapOf("key1" to "value1")
    val map4 = linkedMapOf("key1" to "value1")     // 保持插入顺序
    val map5 = sortedMapOf("key2" to "value2", "key1" to "value1")
    
    // 空集合
    val emptyList = emptyList<String>()
    val emptySet = emptySet<Int>()
    val emptyMap = emptyMap<String, String>()
    
    println("各种集合创建方式演示完成")
}
```

### 4.2 List操作详解

#### 4.2.1 List的基本操作

```kotlin
fun listBasicOperations() {
    val fruits = listOf("苹果", "香蕉", "橘子", "葡萄", "草莓")
    
    // 访问元素
    println("第一个水果: ${fruits[0]}")
    println("最后一个水果: ${fruits.last()}")
    println("第二个水果: ${fruits.getOrNull(1)}")  // 安全访问
    println("不存在的索引: ${fruits.getOrNull(10)}")  // 返回null
    
    // 查找操作
    println("香蕉的索引: ${fruits.indexOf("香蕉")}")
    println("包含苹果: ${fruits.contains("苹果")}")
    println("包含西瓜: ${"西瓜" in fruits}")
    
    // 子列表操作
    println("前3个水果: ${fruits.take(3)}")
    println("跳过2个后的水果: ${fruits.drop(2)}")
    println("子列表(1-3): ${fruits.subList(1, 4)}")
    
    // 可变列表操作
    val mutableFruits = fruits.toMutableList()
    mutableFruits.add("西瓜")
    mutableFruits.add(1, "樱桃")           // 在指定位置插入
    mutableFruits.remove("香蕉")
    mutableFruits.removeAt(0)             // 移除指定位置元素
    
    println("修改后的水果列表: $mutableFruits")
}
```

#### 4.2.2 List的高级操作

```kotlin
fun listAdvancedOperations() {
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    
    // 过滤操作
    val evenNumbers = numbers.filter { it % 2 == 0 }
    val oddNumbers = numbers.filterNot { it % 2 == 0 }
    val numbersAsStrings = numbers.filterIsInstance<Int>()  // 类型过滤
    
    println("偶数: $evenNumbers")
    println("奇数: $oddNumbers")
    
    // 转换操作
    val squares = numbers.map { it * it }
    val doubledEvens = numbers.filter { it % 2 == 0 }.map { it * 2 }
    
    // 扁平化操作
    val nestedLists = listOf(listOf(1, 2), listOf(3, 4), listOf(5))
    val flattened = nestedLists.flatten()  // [1, 2, 3, 4, 5]
    val flatMapped = nestedLists.flatMap { it.map { num -> num * 2 } }
    
    println("平方: $squares")
    println("扁平化: $flattened")
    println("扁平映射: $flatMapped")
    
    // 分组和分区
    val grouped = numbers.groupBy { it % 3 }  // 按余数分组
    val (small, large) = numbers.partition { it <= 5 }  // 分区
    
    println("按3的余数分组: $grouped")
    println("小于等于5: $small, 大于5: $large")
    
    // 聚合操作
    val sum = numbers.sum()
    val average = numbers.average()
    val max = numbers.maxOrNull()
    val min = numbers.minOrNull()
    
    println("总和: $sum, 平均值: $average, 最大值: $max, 最小值: $min")
    
    // 自定义聚合
    val product = numbers.reduce { acc, n -> acc * n }
    val factorial = numbers.fold(1) { acc, n -> acc * n }
    
    println("乘积: $product")
    println("阶乘: $factorial")
}
```

### 4.3 Set和Map操作

#### 4.3.1 Set的特性和操作

```kotlin
fun setOperations() {
    val set1 = setOf(1, 2, 3, 4, 5)
    val set2 = setOf(4, 5, 6, 7, 8)
    
    // 集合运算
    val union = set1 union set2         // 并集: [1, 2, 3, 4, 5, 6, 7, 8]
    val intersect = set1 intersect set2 // 交集: [4, 5]
    val subtract = set1 subtract set2   // 差集: [1, 2, 3]
    
    println("集合1: $set1")
    println("集合2: $set2")
    println("并集: $union")
    println("交集: $intersect")
    println("差集: $subtract")
    
    // 可变Set操作
    val mutableSet = mutableSetOf("A", "B", "C")
    mutableSet.add("D")
    mutableSet.addAll(listOf("E", "F"))
    mutableSet.remove("A")
    mutableSet.removeAll(listOf("B", "C"))
    
    println("可变集合操作后: $mutableSet")
    
    // 不同类型的Set
    val hashSet = hashSetOf(3, 1, 4, 1, 5, 9)     // 无序，去重
    val linkedSet = linkedSetOf(3, 1, 4, 1, 5, 9) // 保持插入顺序
    val sortedSet = sortedSetOf(3, 1, 4, 1, 5, 9) // 自动排序
    
    println("HashSet: $hashSet")
    println("LinkedHashSet: $linkedSet")
    println("TreeSet: $sortedSet")
}
```

#### 4.3.2 Map的操作详解

```kotlin
data class Student(val name: String, val age: Int, val grade: String)

fun mapOperations() {
    // 创建Map
    val studentGrades = mapOf(
        "张三" to "A",
        "李四" to "B", 
        "王五" to "A",
        "赵六" to "C"
    )
    
    // 基本操作
    println("张三的成绩: ${studentGrades["张三"]}")
    println("孙七的成绩: ${studentGrades.getOrDefault("孙七", "未知")}")
    println("包含李四: ${"李四" in studentGrades}")
    
    // 遍历Map
    for ((name, grade) in studentGrades) {
        println("学生: $name, 成绩: $grade")
    }
    
    // 使用forEach
    studentGrades.forEach { (name, grade) ->
        println("$name -> $grade")
    }
    
    // 键和值的操作
    val students = studentGrades.keys
    val grades = studentGrades.values
    val entries = studentGrades.entries
    
    println("所有学生: $students")
    println("所有成绩: $grades")
    
    // 过滤和转换
    val excellentStudents = studentGrades.filter { (_, grade) -> grade == "A" }
    val studentAges = studentGrades.mapValues { (name, _) -> 
        // 根据姓名查找年龄的逻辑
        when (name) {
            "张三" -> 20
            "李四" -> 21
            "王五" -> 19
            "赵六" -> 22
            else -> 18
        }
    }
    
    println("优秀学生: $excellentStudents")
    println("学生年龄: $studentAges")
    
    // 可变Map操作
    val mutableMap = mutableMapOf("A" to 1, "B" to 2)
    mutableMap["C"] = 3                    // 添加或更新
    mutableMap.put("D", 4)                 // put方法
    mutableMap.putAll(mapOf("E" to 5, "F" to 6))
    mutableMap.remove("A")
    
    println("可变Map: $mutableMap")
    
    // 高级Map操作
    val groupedByGrade = listOf(
        Student("张三", 20, "A"),
        Student("李四", 21, "B"),
        Student("王五", 19, "A"),
        Student("赵六", 22, "C")
    ).groupBy { it.grade }
    
    println("按成绩分组: $groupedByGrade")
    
    // associate系列函数
    val nameToAge = listOf("张三:20", "李四:21", "王五:19").associate { 
        val (name, age) = it.split(":")
        name to age.toInt()
    }
    
    println("姓名年龄映射: $nameToAge")
}
```

### 4.4 序列（Sequence）

#### 4.4.1 序列的优势

```kotlin
fun sequenceAdvantages() {
    val largeList = (1..1000000).toList()
    
    // 使用集合操作（创建中间集合）
    val listResult = largeList
        .filter { it % 2 == 0 }     // 创建中间List
        .map { it * it }            // 再创建一个中间List
        .take(10)                   // 创建最终List
    
    // 使用序列（惰性求值）
    val sequenceResult = largeList.asSequence()
        .filter { it % 2 == 0 }     // 不创建中间集合
        .map { it * it }            // 不创建中间集合  
        .take(10)                   // 不创建中间集合
        .toList()                   // 只在这里创建最终List
    
    println("集合操作结果: $listResult")
    println("序列操作结果: $sequenceResult")
    
    // 性能对比
    val listTime = measureTimeMillis {
        repeat(100) {
            largeList.filter { it % 2 == 0 }.map { it * it }.take(10)
        }
    }
    
    val sequenceTime = measureTimeMillis {
        repeat(100) {
            largeList.asSequence().filter { it % 2 == 0 }.map { it * it }.take(10).toList()
        }
    }
    
    println("集合操作时间: ${listTime}ms")
    println("序列操作时间: ${sequenceTime}ms")
}
```

#### 4.2.2 序列的创建和操作

```kotlin
fun sequenceOperations() {
    // 创建序列的方式
    val sequence1 = sequenceOf(1, 2, 3, 4, 5)
    val sequence2 = listOf(1, 2, 3, 4, 5).asSequence()
    val sequence3 = generateSequence(1) { it + 1 }.take(5)  // 无限序列
    
    // 从文件读取序列（实际应用）
    /*
    val fileSequence = File("data.txt").useLines { lines ->
        lines.filter { it.isNotEmpty() }
             .map { it.trim() }
             .toList()
    }
    */
    
    // 复杂的序列操作
    val result = (1..100).asSequence()
        .filter { it % 2 == 0 }
        .map { it * 3 }
        .filter { it > 50 }
        .groupBy { it % 10 }
        .mapValues { (_, values) -> values.sum() }
    
    println("复杂序列操作结果: $result")
    
    // 无限序列示例
    val fibonacciSequence = generateSequence(1 to 1) { (a, b) -> 
        b to (a + b)
    }.map { it.first }
    
    val first10Fibonacci = fibonacciSequence.take(10).toList()
    println("前10个斐波那契数: $first10Fibonacci")
    
    // 序列的终端操作
    val numbers = (1..100).asSequence()
    println("总和: ${numbers.sum()}")
    println("第一个大于50的数: ${numbers.first { it > 50 }}")
    println("是否有偶数: ${numbers.any { it % 2 == 0 }}")
    println("都是正数: ${numbers.all { it > 0 }}")
}
```

### 4.5 泛型基础

#### 4.5.1 泛型类和泛型函数

```kotlin
// 泛型类
class Box<T>(private var content: T) {
    fun get(): T = content
    fun set(value: T) {
        content = value
    }
    
    override fun toString(): String = "Box($content)"
}

// 泛型函数
fun <T> singletonList(item: T): List<T> {
    return listOf(item)
}

fun <T> T.toSingletonList(): List<T> {
    return listOf(this)
}

// 多个类型参数
fun <T, R> transform(input: T, transformer: (T) -> R): R {
    return transformer(input)
}

fun genericBasics() {
    // 使用泛型类
    val stringBox = Box("Hello Kotlin")
    val intBox = Box(42)
    
    println("字符串盒子: $stringBox")
    println("整数盒子: $intBox")
    
    stringBox.set("Hello World")
    intBox.set(100)
    
    println("修改后 - 字符串: ${stringBox.get()}, 整数: ${intBox.get()}")
    
    // 使用泛型函数
    val singleString = singletonList("Kotlin")
    val singleInt = singletonList(42)
    
    println("单元素列表: $singleString, $singleInt")
    
    // 扩展函数
    val listFromExtension = "Hello".toSingletonList()
    println("扩展函数创建的列表: $listFromExtension")
    
    // 多类型参数
    val length = transform("Hello Kotlin") { it.length }
    val doubled = transform(21) { it * 2 }
    
    println("字符串长度: $length, 双倍数字: $doubled")
}
```

#### 4.5.2 类型约束

```kotlin
// 上界约束
fun <T : Number> sum(numbers: List<T>): Double {
    return numbers.sumOf { it.toDouble() }
}

// 多个约束
interface Printable {
    fun print()
}

fun <T> processItem(item: T) where T : Number, T : Printable {
    println("数字值: ${item.toDouble()}")
    item.print()
}

// 实际应用：比较器
fun <T : Comparable<T>> findMax(items: List<T>): T? {
    return items.maxOrNull()
}

// 自定义约束
abstract class Animal(val name: String) {
    abstract fun makeSound()
}

class Dog(name: String) : Animal(name) {
    override fun makeSound() {
        println("$name says: Woof!")
    }
}

class Cat(name: String) : Animal(name) {
    override fun makeSound() {
        println("$name says: Meow!")
    }
}

// 约束为Animal的子类
fun <T : Animal> makeAllSound(animals: List<T>) {
    for (animal in animals) {
        animal.makeSound()
    }
}

fun typeConstraints() {
    // 数字类型约束
    val intList = listOf(1, 2, 3, 4, 5)
    val doubleList = listOf(1.5, 2.5, 3.5)
    
    println("整数列表总和: ${sum(intList)}")
    println("小数列表总和: ${sum(doubleList)}")
    
    // 比较器约束
    val strings = listOf("apple", "banana", "cherry")
    val numbers = listOf(3, 1, 4, 1, 5, 9)
    
    println("最大字符串: ${findMax(strings)}")
    println("最大数字: ${findMax(numbers)}")
    
    // 动物约束
    val animals = listOf(
        Dog("旺财"),
        Cat("咪咪"),
        Dog("小黄")
    )
    
    makeAllSound(animals)
}
```

### 4.6 型变（Variance）

#### 4.6.1 协变（Covariance）

```kotlin
// 协变 - out关键字
interface Producer<out T> {
    fun produce(): T
    // fun consume(item: T)  // 编译错误！out类型不能出现在输入位置
}

class StringProducer : Producer<String> {
    override fun produce(): String = "Hello Kotlin"
}

class NumberProducer : Producer<Number> {
    override fun produce(): Number = 42
}

fun demonstrateCovariance() {
    val stringProducer: Producer<String> = StringProducer()
    val anyProducer: Producer<Any> = stringProducer  // 协变：String是Any的子类型
    
    println("从Any生产者获取: ${anyProducer.produce()}")
    
    // List也是协变的
    val stringList: List<String> = listOf("A", "B", "C")
    val anyList: List<Any> = stringList  // 合法的协变转换
    
    println("Any列表的第一个元素: ${anyList[0]}")
}
```

#### 4.6.2 逆变（Contravariance）

```kotlin
// 逆变 - in关键字
interface Consumer<in T> {
    fun consume(item: T)
    // fun produce(): T  // 编译错误！in类型不能出现在输出位置
}

class AnyConsumer : Consumer<Any> {
    override fun consume(item: Any) {
        println("消费任意类型: $item")
    }
}

class NumberConsumer : Consumer<Number> {
    override fun consume(item: Number) {
        println("消费数字: $item")
    }
}

fun demonstrateContravariance() {
    val anyConsumer: Consumer<Any> = AnyConsumer()
    val stringConsumer: Consumer<String> = anyConsumer  // 逆变：Any是String的父类型
    
    stringConsumer.consume("Hello")  // 传递String给Any消费者
    
    // 比较器是逆变的
    val anyComparator = Comparator<Any> { a, b -> a.hashCode() - b.hashCode() }
    val stringComparator: Comparator<String> = anyComparator  // 逆变转换
    
    val sortedStrings = listOf("c", "a", "b").sortedWith(stringComparator)
    println("排序后的字符串: $sortedStrings")
}
```

#### 4.6.3 星号投影（Star Projection）

```kotlin
fun starProjection() {
    // 星号投影 - 类型未知
    val unknownList: List<*> = listOf("A", "B", "C")  // 可以是任意类型的List
    
    // 只能安全地读取为Any?
    val firstItem: Any? = unknownList[0]  // 类型是Any?
    println("第一个元素: $firstItem")
    
    // 不能写入（除了null）
    // val mutableUnknown: MutableList<*> = mutableListOf("A", "B")
    // mutableUnknown.add("C")  // 编译错误！
    
    // 实际应用：处理未知类型的集合
    fun printListInfo(list: List<*>) {
        println("列表大小: ${list.size}")
        println("是否为空: ${list.isEmpty()}")
        if (list.isNotEmpty()) {
            println("第一个元素: ${list[0]}")
        }
    }
    
    printListInfo(listOf(1, 2, 3))
    printListInfo(listOf("A", "B", "C"))
    printListInfo(listOf(true, false))
}
```

### 4.7 实际应用案例

#### 4.7.1 数据处理管道

```kotlin
data class Order(val id: String, val amount: Double, val status: String, val date: String)
data class Customer(val id: String, val name: String, val email: String)

fun dataProcessingPipeline() {
    val orders = listOf(
        Order("1", 100.0, "completed", "2023-01-15"),
        Order("2", 250.0, "pending", "2023-01-16"),
        Order("3", 75.0, "completed", "2023-01-17"),
        Order("4", 300.0, "cancelled", "2023-01-18"),
        Order("5", 150.0, "completed", "2023-01-19")
    )
    
    // 复杂的数据处理管道
    val result = orders.asSequence()
        .filter { it.status == "completed" }
        .filter { it.amount >= 100.0 }
        .map { it.copy(amount = it.amount * 1.1) }  // 加10%奖励
        .sortedByDescending { it.amount }
        .take(3)
        .groupBy { it.date.substring(0, 7) }  // 按月分组
        .mapValues { (_, orders) -> 
            orders.sumOf { it.amount }
        }
    
    println("处理结果: $result")
    
    // 统计信息
    val statistics = orders.groupingBy { it.status }
        .eachCount()  // 计算每个状态的订单数量
    
    println("订单状态统计: $statistics")
    
    // 累积统计
    val monthlyTotal = orders
        .filter { it.status == "completed" }
        .groupBy { it.date.substring(0, 7) }
        .mapValues { (_, monthOrders) -> 
            monthOrders.sumOf { it.amount }
        }
    
    println("月度完成订单总额: $monthlyTotal")
}
```

#### 4.7.2 缓存实现

```kotlin
// 泛型缓存实现
class LRUCache<K, V>(private val maxSize: Int) {
    private val cache = LinkedHashMap<K, V>(16, 0.75f, true)
    
    fun get(key: K): V? {
        return cache[key]
    }
    
    fun put(key: K, value: V) {
        if (cache.size >= maxSize && !cache.containsKey(key)) {
            val firstKey = cache.keys.iterator().next()
            cache.remove(firstKey)
        }
        cache[key] = value
    }
    
    fun size(): Int = cache.size
    fun clear() = cache.clear()
    
    override fun toString(): String = cache.toString()
}

// 使用缓存
fun cacheExample() {
    val stringCache = LRUCache<String, String>(3)
    
    stringCache.put("1", "First")
    stringCache.put("2", "Second") 
    stringCache.put("3", "Third")
    println("缓存内容: $stringCache")
    
    stringCache.put("4", "Fourth")  // 会移除最老的条目
    println("添加第4个后: $stringCache")
    
    val value = stringCache.get("2")  // 访问"2"，使其成为最近使用
    println("获取值: $value")
    println("访问后缓存: $stringCache")
    
    // 数字缓存示例
    val numberCache = LRUCache<Int, Double>(5)
    repeat(10) { i ->
        numberCache.put(i, i * 1.5)
    }
    println("数字缓存: $numberCache")
}
```

### 4.8 面试常考题

#### 题目1：List、Set、Map的选择

**问题：** 什么时候使用List、Set、Map？它们的底层实现有什么区别？

**答案：**
```kotlin
// 选择原则和底层实现
fun collectionChoice() {
    // List - 有序、可重复、支持索引访问
    // ArrayList: 动态数组，随机访问快，插入删除慢
    // LinkedList: 双向链表，插入删除快，随机访问慢
    val arrayList = ArrayList<String>()  // 适合频繁访问
    val linkedList = LinkedList<String>()  // 适合频繁插入删除
    
    // Set - 不重复、快速查找
    // HashSet: 哈希表，O(1)查找，无序
    // LinkedHashSet: 哈希表+链表，保持插入顺序
    // TreeSet: 红黑树，O(log n)查找，自动排序
    val hashSet = HashSet<Int>()        // 最快的查找
    val linkedHashSet = LinkedHashSet<Int>()  // 保持顺序的查找
    val treeSet = TreeSet<Int>()        // 排序的查找
    
    // Map - 键值对存储
    // HashMap: 哈希表，O(1)访问，无序
    // LinkedHashMap: 保持插入或访问顺序
    // TreeMap: 红黑树，按键排序
    val hashMap = HashMap<String, Int>()
    val linkedHashMap = LinkedHashMap<String, Int>()
    val treeMap = TreeMap<String, Int>()
    
    // 选择建议：
    // 需要索引访问 -> List
    // 需要去重和快速查找 -> Set  
    // 需要键值对映射 -> Map
    // 需要排序 -> TreeSet/TreeMap
    // 需要保持顺序 -> LinkedHashSet/LinkedHashMap
}
```

#### 题目2：可变与不可变集合

**问题：** Kotlin中可变和不可变集合的区别？在多线程环境下如何选择？

**答案：**
```kotlin
// 可变vs不可变集合
fun mutableVsImmutable() {
    // 不可变集合（只读视图）
    val readOnlyList = listOf(1, 2, 3)
    // readOnlyList.add(4)  // 编译错误，没有add方法
    
    // 可变集合
    val mutableList = mutableListOf(1, 2, 3)
    mutableList.add(4)  // OK
    
    // 类型关系
    val list: List<Int> = mutableList  // MutableList是List的子类型
    // list.add(5)  // 编译错误，List接口没有add方法
    
    // 线程安全考虑
    val synchronizedList = Collections.synchronizedList(mutableList)
    val concurrentMap = ConcurrentHashMap<String, String>()
    
    // 不可变集合的优势：
    // 1. 线程安全
    // 2. 防止意外修改
    // 3. 可以安全地作为API返回值
    
    // 实际底层可能是可变的：
    val original = mutableListOf(1, 2, 3)
    val readOnly: List<Int> = original  // 只是只读视图
    original.add(4)  // 影响readOnly
    println("只读视图: $readOnly")  // 输出[1, 2, 3, 4]
}

// 线程安全的集合操作
fun threadSafeCollections() {
    val sharedList = Collections.synchronizedList(mutableListOf<Int>())
    val threads = (1..10).map { threadId ->
        thread {
            repeat(100) { i ->
                sharedList.add(threadId * 100 + i)
            }
        }
    }
    
    threads.forEach { it.join() }
    println("线程安全列表大小: ${sharedList.size}")
}
```

#### 题目3：序列的性能优化

**问题：** 什么时候使用Sequence？它与Stream有什么区别？

**答案：**
```kotlin
// Sequence vs Collection性能对比
fun sequencePerformance() {
    val largeList = (1..1000000).toList()
    
    // 测试不同的数据处理方式
    fun processWithList(): List<Int> {
        return largeList
            .filter { it % 2 == 0 }    // 创建500,000个元素的中间List
            .map { it * it }           // 创建500,000个元素的中间List
            .filter { it > 1000000 }   // 创建最终的List
    }
    
    fun processWithSequence(): List<Int> {
        return largeList.asSequence()
            .filter { it % 2 == 0 }    // 惰性操作，不创建中间集合
            .map { it * it }           // 惰性操作
            .filter { it > 1000000 }   // 惰性操作
            .toList()                  // 只在这里创建最终集合
    }
    
    // 性能测试
    val listTime = measureTimeMillis { processWithList() }
    val sequenceTime = measureTimeMillis { processWithSequence() }
    
    println("Collection处理时间: ${listTime}ms")
    println("Sequence处理时间: ${sequenceTime}ms")
    
    // 使用场景：
    // 1. 大数据集的链式操作
    // 2. 只需要部分结果（如take(10)）
    // 3. 无限数据流
    
    // 无限序列示例
    val infiniteSequence = generateSequence(0) { it + 1 }
    val firstHundredEvens = infiniteSequence
        .filter { it % 2 == 0 }
        .take(100)
        .toList()
    
    println("前100个偶数: ${firstHundredEvens.take(10)}...")
}

// Kotlin Sequence vs Java Stream
fun sequenceVsStream() {
    val numbers = (1..100).toList()
    
    // Kotlin Sequence（冷流）
    val sequence = numbers.asSequence()
        .filter { println("Filter: $it"); it % 2 == 0 }
        .map { println("Map: $it"); it * it }
    
    println("Sequence定义完成，还未执行")
    val result1 = sequence.take(3).toList()  // 只处理需要的元素
    println("Sequence结果: $result1")
    
    // 重用sequence
    val result2 = sequence.take(5).toList()  // 重新执行所有操作
    println("重用Sequence: $result2")
    
    // 区别总结：
    // Sequence: 冷流，每次terminal操作都重新计算
    // Stream: 热流，只能消费一次
}
```

#### 题目4：泛型的型变

**问题：** 解释协变、逆变和不变性？什么时候使用out和in？

**答案：**
```kotlin
// 型变详解
fun varianceExplanation() {
    // 1. 不变性（Invariance）- 默认情况
    class Box<T>(var value: T)
    
    val stringBox = Box("Hello")
    // val anyBox: Box<Any> = stringBox  // 编译错误！Box<String>不是Box<Any>的子类型
    
    // 2. 协变（Covariance）- out关键字
    class Producer<out T>(private val value: T) {
        fun get(): T = value
        // fun set(value: T) {}  // 编译错误！out类型不能作为输入
    }
    
    val stringProducer = Producer("Hello")
    val anyProducer: Producer<Any> = stringProducer  // OK！协变允许
    
    // 3. 逆变（Contravariance）- in关键字
    class Consumer<in T> {
        fun accept(value: T) {
            println("Consuming: $value")
        }
        // fun get(): T {}  // 编译错误！in类型不能作为输出
    }
    
    val anyConsumer = Consumer<Any>()
    val stringConsumer: Consumer<String> = anyConsumer  // OK！逆变允许
    
    // 使用场景：
    // out T: 只产出T，不消费T（协变）
    // in T: 只消费T，不产出T（逆变）
    
    // 实际应用
    fun copyList(from: List<out Any>, to: MutableList<in String>) {
        // from可以是List<String>、List<Int>等（协变）
        // to可以是MutableList<Any>、MutableList<String>等（逆变）
        // to.addAll(from)  // 类型不匹配，需要转换
    }
}

// 型变的实际应用
class AnimalShelter {
    private val animals = mutableListOf<Animal>()
    
    // 协变：可以接受任何Animal的子类型列表
    fun addAnimals(newAnimals: List<out Animal>) {
        animals.addAll(newAnimals)
    }
    
    // 逆变：可以输出到任何Animal的父类型列表
    fun moveAnimalsTo(destination: MutableList<in Animal>) {
        destination.addAll(animals)
        animals.clear()
    }
}

fun shelterExample() {
    val shelter = AnimalShelter()
    val dogs = listOf(Dog("旺财"), Dog("小黄"))
    val cats = listOf(Cat("咪咪"), Cat("小花"))
    
    shelter.addAnimals(dogs)  // List<Dog> -> List<out Animal>
    shelter.addAnimals(cats)  // List<Cat> -> List<out Animal>
    
    val allPets = mutableListOf<Any>()
    shelter.moveAnimalsTo(allPets)  // MutableList<Any> -> MutableList<in Animal>
}
```

#### 题目5：集合的线程安全

**问题：** Kotlin集合在多线程环境下的安全性如何保证？

**答案：**
```kotlin
import java.util.concurrent.*
import kotlin.concurrent.*

// 线程安全的集合操作
fun threadSafetyInCollections() {
    // 1. 不可变集合是线程安全的
    val immutableList = listOf(1, 2, 3, 4, 5)
    // 多线程读取immutableList是安全的
    
    // 2. 可变集合不是线程安全的
    val unsafeList = mutableListOf<Int>()
    val threads = (1..10).map { threadId ->
        thread {
            repeat(1000) { i ->
                unsafeList.add(threadId * 1000 + i)  // 线程不安全！
            }
        }
    }
    threads.forEach { it.join() }
    println("不安全列表大小: ${unsafeList.size}")  // 可能小于10000
    
    // 3. 使用同步包装器
    val synchronizedList = Collections.synchronizedList(mutableListOf<Int>())
    val safeThreads = (1..10).map { threadId ->
        thread {
            repeat(1000) { i ->
                synchronizedList.add(threadId * 1000 + i)  // 线程安全
            }
        }
    }
    safeThreads.forEach { it.join() }
    println("安全列表大小: ${synchronizedList.size}")  // 总是10000
    
    // 4. 使用并发集合
    val concurrentMap = ConcurrentHashMap<String, Int>()
    val copyOnWriteList = CopyOnWriteArrayList<String>()
    
    // 5. 使用原子操作
    val atomicCounter = AtomicInteger(0)
    val atomicList = mutableListOf<Int>()
    val mutex = Mutex()
    
    runBlocking {
        val jobs = (1..1000).map {
            launch {
                mutex.withLock {
                    atomicList.add(atomicCounter.incrementAndGet())
                }
            }
        }
        jobs.joinAll()
        println("原子操作列表大小: ${atomicList.size}")
    }
}

// 集合操作的线程安全模式
class ThreadSafeRepository<T> {
    private val items = ConcurrentHashMap<String, T>()
    private val readWriteLock = ReentrantReadWriteLock()
    
    fun get(key: String): T? {
        readWriteLock.readLock().lock()
        try {
            return items[key]
        } finally {
            readWriteLock.readLock().unlock()
        }
    }
    
    fun put(key: String, value: T) {
        readWriteLock.writeLock().lock()
        try {
            items[key] = value
        } finally {
            readWriteLock.writeLock().unlock()
        }
    }
    
    fun getAll(): Map<String, T> {
        readWriteLock.readLock().lock()
        try {
            return HashMap(items)  // 返回副本以避免并发修改
        } finally {
            readWriteLock.readLock().unlock()
        }
    }
}
```

---

## 第4章小结

第4章我们深入学习了Kotlin的集合框架和泛型系统：

### 主要内容：
1. **集合类型：** List、Set、Map的特性和操作方法
2. **序列优化：** 惰性求值提升性能，避免中间集合创建
3. **泛型基础：** 泛型类、泛型函数、类型约束
4. **型变机制：** 协变（out）、逆变（in）、星号投影

### 面试重点：
- **集合选择：** 根据数据特性选择合适的集合类型
- **性能优化：** 大数据集使用Sequence避免中间集合
- **线程安全：** 不可变集合天然线程安全，可变集合需要同步
- **泛型型变：** out用于输出，in用于输入，提供类型安全的协变和逆变

下一章我们将学习Kotlin最重要的特性之一：协程编程。