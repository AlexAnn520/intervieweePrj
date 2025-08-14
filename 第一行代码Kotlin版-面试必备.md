# 第一行代码 Kotlin版 - Android开发面试必备

> 基于郭霖《第一行代码》风格，专为Android开发者打造的Kotlin学习指南

## 目录

- [第1章 初识Kotlin - 语言基础](#第1章-初识kotlin---语言基础)
- [第2章 面向对象编程 - 类与继承](#第2章-面向对象编程---类与继承)
- [第3章 Kotlin特有特性 - 空安全与函数式编程](#第3章-kotlin特有特性---空安全与函数式编程)
- [第4章 集合与泛型 - 数据容器的使用](#第4章-集合与泛型---数据容器的使用)
- [第5章 协程编程 - 异步编程的艺术](#第5章-协程编程---异步编程的艺术)
- [第6章 Android中的Kotlin实践](#第6章-android中的kotlin实践)

---

## 第1章 初识Kotlin - 语言基础

### 1.1 为什么选择Kotlin

还记得当初学习Java时的繁琐语法吗？需要写大量的模板代码，空指针异常频繁出现，函数式编程支持不佳。Kotlin的诞生就是为了解决这些问题。

```kotlin
// Java代码
public class Person {
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
    
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
}

// Kotlin代码 - 一行搞定！
data class Person(var name: String, var age: Int)
```

这就是Kotlin的威力！同样的功能，Kotlin只需要一行代码。

### 1.2 变量与常量

#### 1.2.1 var和val的区别

在Kotlin中，有两个关键字来声明变量：

```kotlin
fun main() {
    // val - 不可变引用（类似Java的final）
    val name = "张三"        // 类型推导：String
    val age: Int = 25       // 显式指定类型
    
    // name = "李四"         // 编译错误！val不能重新赋值
    
    // var - 可变引用
    var score = 90
    score = 95              // OK！var可以重新赋值
    
    println("姓名：$name，年龄：$age，分数：$score")
}
```

**面试重点：** 
- `val`：值不可变，引用不可变
- `var`：值可变，引用可变
- 优先使用`val`，只有确实需要修改时才用`var`

#### 1.2.2 类型推导

Kotlin具有强大的类型推导能力：

```kotlin
fun demonstrateTypeInference() {
    // 编译器自动推导类型
    val message = "Hello Kotlin"        // String
    val number = 42                     // Int
    val price = 99.9                    // Double
    val isReady = true                  // Boolean
    
    // 集合类型推导
    val numbers = listOf(1, 2, 3)       // List<Int>
    val map = mapOf("key" to "value")   // Map<String, String>
    
    println("message的类型：${message::class.simpleName}")
    println("numbers的类型：${numbers::class.simpleName}")
}
```

### 1.3 函数定义

#### 1.3.1 基础函数语法

```kotlin
// 完整的函数定义
fun calculateSum(a: Int, b: Int): Int {
    return a + b
}

// 单表达式函数（推荐）
fun calculateSum2(a: Int, b: Int) = a + b

// 无返回值的函数
fun printInfo(name: String): Unit {  // Unit可以省略
    println("用户姓名：$name")
}

// 省略Unit
fun printInfo2(name: String) {
    println("用户姓名：$name")
}
```

#### 1.3.2 默认参数和具名参数

```kotlin
// 默认参数 - 减少函数重载
fun createUser(
    name: String,
    age: Int = 18,           // 默认值
    city: String = "北京"
): String {
    return "用户：$name，年龄：$age，城市：$city"
}

fun testDefaultParameters() {
    // 使用默认参数
    println(createUser("小明"))                    // 用户：小明，年龄：18，城市：北京
    println(createUser("小红", 22))               // 用户：小红，年龄：22，城市：北京
    
    // 具名参数 - 提高代码可读性
    println(createUser("小王", city = "上海"))      // 用户：小王，年龄：18，城市：上海
    println(createUser(name = "小李", age = 30, city = "深圳"))
}
```

**面试重点：默认参数可以减少函数重载，具名参数提高代码可读性**

#### 1.3.3 可变参数（vararg）

```kotlin
// 可变参数函数
fun printNumbers(vararg numbers: Int) {
    for (number in numbers) {
        println(number)
    }
}

// 更实用的例子
fun concatenateStrings(separator: String = ", ", vararg strings: String): String {
    return strings.joinToString(separator)
}

fun testVararg() {
    printNumbers(1, 2, 3, 4, 5)
    
    val result = concatenateStrings(" | ", "苹果", "香蕉", "橘子")
    println(result)  // 苹果 | 香蕉 | 橘子
    
    // 展开数组
    val fruits = arrayOf("西瓜", "葡萄", "草莓")
    val result2 = concatenateStrings(" & ", *fruits)  // *展开操作符
    println(result2) // 西瓜 & 葡萄 & 草莓
}
```

### 1.4 控制流程

#### 1.4.1 if表达式

在Kotlin中，`if`是表达式，不是语句！

```kotlin
fun demonstrateIfExpression() {
    val score = 85
    
    // if作为表达式
    val grade = if (score >= 90) {
        "优秀"
    } else if (score >= 80) {
        "良好"
    } else if (score >= 60) {
        "及格"
    } else {
        "不及格"
    }
    
    println("成绩：$grade")
    
    // 简化写法
    val status = if (score >= 60) "通过" else "未通过"
    println("状态：$status")
    
    // 替代三元操作符
    val max = if (10 > 5) 10 else 5  // Java: int max = 10 > 5 ? 10 : 5;
}
```

#### 1.4.2 when表达式（替代switch）

```kotlin
fun demonstrateWhen() {
    val dayOfWeek = 3
    
    // 基础when用法
    val dayName = when (dayOfWeek) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6, 7 -> "周末"        // 多个值
        else -> "无效日期"
    }
    
    println("今天是：$dayName")
    
    // when的高级用法
    val x = 15
    when (x) {
        in 1..10 -> println("1-10范围内")
        in 11..20 -> println("11-20范围内")
        !in 1..20 -> println("不在1-20范围内")
    }
    
    // 不带参数的when
    val temperature = 25
    when {
        temperature < 0 -> println("结冰了")
        temperature < 20 -> println("有点冷")
        temperature < 30 -> println("温度适宜")
        else -> println("太热了")
    }
}

// 实用案例：处理网络响应
fun handleNetworkResponse(code: Int): String = when (code) {
    200 -> "请求成功"
    404 -> "页面未找到"
    500 -> "服务器错误"
    in 400..499 -> "客户端错误"
    in 500..599 -> "服务器错误"
    else -> "未知错误"
}
```

#### 1.4.3 循环语句

```kotlin
fun demonstrateLoops() {
    // for循环 - 遍历范围
    println("=== 数字范围 ===")
    for (i in 1..5) {           // 包含5
        print("$i ")
    }
    println()
    
    for (i in 1 until 5) {      // 不包含5
        print("$i ")
    }
    println()
    
    for (i in 5 downTo 1) {     // 递减
        print("$i ")
    }
    println()
    
    for (i in 1..10 step 2) {   // 步长为2
        print("$i ")
    }
    println()
    
    // 遍历集合
    println("\n=== 遍历集合 ===")
    val fruits = listOf("苹果", "香蕉", "橘子")
    
    for (fruit in fruits) {
        println(fruit)
    }
    
    // 带索引遍历
    for ((index, fruit) in fruits.withIndex()) {
        println("$index: $fruit")
    }
    
    // while循环
    println("\n=== while循环 ===")
    var count = 0
    while (count < 3) {
        println("Count: $count")
        count++
    }
    
    // do-while循环
    do {
        println("至少执行一次")
    } while (false)
}
```

### 1.5 字符串处理

#### 1.5.1 字符串模板

```kotlin
fun demonstrateStringTemplates() {
    val name = "Kotlin"
    val version = 1.8
    
    // 基础字符串模板
    println("语言：$name")
    println("版本：$version")
    
    // 表达式模板
    println("字符串长度：${name.length}")
    println("版本信息：${name} $version")
    
    // 复杂表达式
    val numbers = listOf(1, 2, 3, 4, 5)
    println("数字总和：${numbers.sum()}")
    println("最大值：${numbers.maxOrNull()}")
    
    // 多行字符串
    val multilineString = """
        这是一个多行字符串
        可以包含换行符
        非常方便处理长文本
        当前时间：${System.currentTimeMillis()}
    """.trimIndent()
    
    println(multilineString)
}
```

#### 1.5.2 字符串操作

```kotlin
fun demonstrateStringOperations() {
    val text = "  Hello Kotlin World  "
    
    // 常用字符串操作
    println("原始：'$text'")
    println("去空格：'${text.trim()}'")
    println("大写：'${text.uppercase()}'")
    println("小写：'${text.lowercase()}'")
    println("替换：'${text.replace("Kotlin", "Java")}'")
    
    // 字符串判断
    println("是否包含Kotlin：${text.contains("Kotlin")}")
    println("是否以Hello开头：${text.trim().startsWith("Hello")}")
    println("是否以World结尾：${text.trim().endsWith("World")}")
    
    // 字符串分割
    val csv = "苹果,香蕉,橘子,葡萄"
    val fruits = csv.split(",")
    println("分割结果：$fruits")
    
    // 字符串连接
    val joined = fruits.joinToString(" | ")
    println("连接结果：$joined")
}
```

### 1.6 面试常考题

#### 题目1：val和var的区别

**问题：** 请解释Kotlin中val和var的区别，并说明什么时候使用哪个？

**答案：**
```kotlin
// 示例代码解释区别
fun explainValVar() {
    // val：只读引用，相当于Java的final
    val name = "张三"
    // name = "李四"  // 编译错误
    
    // var：可变引用
    var age = 25
    age = 26  // 正确
    
    // 重要：val只是引用不可变，对象内容可能可变
    val list = mutableListOf(1, 2, 3)
    list.add(4)  // 正确！list引用不变，但内容可变
    
    // 最佳实践：优先使用val
    val user = User("张三", 25)  // 不需要修改引用
}
```

**要点：**
- `val`：只读引用，不能重新赋值
- `var`：可变引用，可以重新赋值  
- 优先使用`val`，提高代码安全性
- `val`不代表对象不可变，只是引用不可变

#### 题目2：when表达式vs Java的switch

**问题：** Kotlin的when表达式相比Java的switch有什么优势？

**答案：**
```kotlin
// Java switch的限制
// switch (value) {
//     case 1:
//     case 2: return "小";
//     case 3: return "中"; 
//     default: return "大";
// }

// Kotlin when的优势
fun demonstrateWhenAdvantages(value: Any): String = when (value) {
    // 1. 可以处理任意类型
    is String -> "字符串：$value"
    is Int -> when {
        value < 10 -> "小于10的整数"
        value < 100 -> "小于100的整数"
        else -> "大整数"
    }
    // 2. 支持范围匹配
    in 1..10 -> "1到10之间"
    
    // 3. 支持多个值
    "A", "B", "C" -> "字母"
    
    // 4. 支持表达式
    else -> "其他类型：${value::class.simpleName}"
}
```

#### 题目3：字符串模板的性能

**问题：** Kotlin字符串模板的性能如何？与StringBuilder相比有什么区别？

**答案：**
```kotlin
fun stringPerformanceComparison() {
    val count = 1000
    val name = "Kotlin"
    
    // 1. 简单字符串模板 - 编译器优化为StringBuilder
    val simple = "Hello $name!"  // 性能好
    
    // 2. 复杂表达式模板 - 可能有额外开销
    val complex = "Length: ${name.length}, Upper: ${name.uppercase()}"
    
    // 3. 循环中的字符串拼接 - 性能问题
    var result = ""
    for (i in 1..count) {
        result += "$i "  // 每次都创建新String对象
    }
    
    // 4. 正确做法 - 使用StringBuilder
    val sb = StringBuilder()
    for (i in 1..count) {
        sb.append("$i ")
    }
    val optimized = sb.toString()
    
    println("推荐在循环中使用StringBuilder")
}
```

#### 题目4：函数默认参数的陷阱

**问题：** 使用函数默认参数时需要注意什么？

**答案：**
```kotlin
// 潜在问题
fun createConnection(
    host: String = "localhost",
    port: Int = 8080,
    timeout: Long = System.currentTimeMillis()  // 危险！只计算一次
) {
    println("连接时间：$timeout")
}

// 正确做法
fun createConnectionCorrect(
    host: String = "localhost", 
    port: Int = 8080,
    timeout: Long = -1  // 使用标记值
) {
    val actualTimeout = if (timeout == -1L) System.currentTimeMillis() else timeout
    println("连接时间：$actualTimeout")
}

// 或者使用函数
fun createConnectionBest(
    host: String = "localhost",
    port: Int = 8080,
    timeoutProvider: () -> Long = { System.currentTimeMillis() }
) {
    println("连接时间：${timeoutProvider()}")
}
```

**要点：**
- 默认参数在函数定义时计算，不是每次调用时
- 避免在默认参数中使用可变的表达式
- 可以使用函数类型参数延迟计算

#### 题目5：类型推导的边界

**问题：** Kotlin的类型推导有什么限制？

**答案：**
```kotlin
fun typeInferenceLimitations() {
    // 1. 可以推导
    val list1 = listOf(1, 2, 3)        // List<Int>
    val map1 = mapOf("a" to 1)         // Map<String, Int>
    
    // 2. 无法推导的情况
    // val emptyList = emptyList()     // 编译错误！无法推导类型
    val emptyList = emptyList<String>() // 需要显式指定
    
    // 3. 混合类型的处理
    val mixed = listOf(1, "hello")      // List<Any>
    
    // 4. null的处理
    val nullValue = null                // Nothing?类型
    // val name = null                  // 编译错误
    val name: String? = null            // 需要显式指定
    
    // 5. 函数返回值推导
    fun getValue() = if (Random.nextBoolean()) 1 else "hello"  // Any类型
    
    println("混合类型：${mixed::class}")
}
```

---

## 本章小结

在第1章中，我们学习了Kotlin的基础语法：

1. **变量声明：** `val`（只读）vs `var`（可变）
2. **函数定义：** 简洁的语法，默认参数，可变参数
3. **控制流程：** `if`表达式，强大的`when`表达式，各种循环
4. **字符串处理：** 字符串模板，丰富的字符串操作API

**面试重点总结：**
- 优先使用`val`，只有需要修改时才用`var`
- `when`表达式比switch更强大，支持类型匹配、范围匹配
- 字符串模板性能良好，但循环中避免字符串拼接
- 类型推导很强大，但某些情况需要显式指定类型

接下来，我们将学习Kotlin的面向对象编程特性。

---

## 第2章 面向对象编程 - 类与继承

如果说第1章让你感受到了Kotlin的简洁，那么第2章将让你体会到Kotlin面向对象编程的强大。Kotlin不仅完全支持面向对象编程，还在Java的基础上做了很多改进和优化。

### 2.1 类的定义与构造函数

#### 2.1.1 基础类定义

在Java中定义一个类需要很多代码，但在Kotlin中却异常简洁：

```kotlin
// 最简单的类定义
class Person

// 带属性的类
class Person(val name: String, var age: Int)

// 等价的Java代码需要20+行，Kotlin只需要1行！
```

让我们来看一个完整的例子：

```kotlin
// 完整的类定义
class Student(
    val name: String,           // 只读属性
    var age: Int,               // 可变属性
    private val id: String      // 私有属性
) {
    // 次构造函数
    constructor(name: String) : this(name, 18, "unknown")
    
    // 初始化块
    init {
        println("创建学生：$name，年龄：$age")
        require(age >= 0) { "年龄不能为负数" }
    }
    
    // 方法
    fun study(subject: String) {
        println("$name 正在学习 $subject")
    }
    
    // 属性的自定义getter和setter
    var grade: String = "A"
        get() = field.uppercase()
        set(value) {
            field = if (value in listOf("A", "B", "C", "D")) value else "D"
        }
}

fun testStudentClass() {
    val student = Student("小明", 20, "S001")
    student.study("Kotlin")
    
    student.grade = "b"
    println("成绩：${student.grade}")  // 输出：B
}
```

#### 2.1.2 主构造函数vs次构造函数

```kotlin
class User {
    val name: String
    val email: String
    val age: Int
    
    // 主构造函数（推荐）
    // constructor(name: String, email: String, age: Int = 18) {
    //     this.name = name
    //     this.email = email  
    //     this.age = age
    // }
    
    // 更简洁的写法 - 直接在主构造函数中定义属性
    // class User(val name: String, val email: String, val age: Int = 18)
    
    // 次构造函数 - 当需要多种初始化方式时使用
    constructor(name: String, email: String, age: Int = 18) {
        this.name = name
        this.email = email
        this.age = age
    }
    
    constructor(name: String, email: String) : this(name, email, 18) {
        println("使用默认年龄创建用户")
    }
    
    constructor(name: String) : this(name, "$name@example.com", 18) {
        println("使用默认邮箱和年龄创建用户")
    }
}
```

**最佳实践：优先使用主构造函数，只有在需要复杂初始化逻辑时才使用次构造函数。**

### 2.2 继承与多态

#### 2.2.1 类的继承

在Kotlin中，所有类默认都是`final`的，这是为了避免脆弱的基类问题。如果要允许继承，需要使用`open`关键字：

```kotlin
// 基类 - 必须用open关键字
open class Animal(val name: String, val species: String) {
    // 可被重写的方法
    open fun makeSound() {
        println("$name makes some sound")
    }
    
    // final方法，不能被重写
    fun eat() {
        println("$name is eating")
    }
    
    // 受保护的方法，子类可访问
    protected fun sleep() {
        println("$name is sleeping")
    }
}

// 子类继承
class Dog(name: String) : Animal(name, "Canine") {
    // 重写父类方法
    override fun makeSound() {
        println("$name says: Woof!")
    }
    
    // 子类特有方法
    fun wagTail() {
        println("$name is wagging tail")
    }
    
    // 可以访问受保护的方法
    fun takeNap() {
        sleep()  // 调用父类的protected方法
    }
}

class Cat(name: String) : Animal(name, "Feline") {
    override fun makeSound() {
        println("$name says: Meow!")
    }
    
    fun climb() {
        println("$name is climbing")
    }
}

// 多态的使用
fun testPolymorphism() {
    val animals: List<Animal> = listOf(
        Dog("旺财"),
        Cat("咪咪"),
        Dog("小黄")
    )
    
    // 多态调用
    for (animal in animals) {
        animal.makeSound()  // 根据实际类型调用相应方法
        animal.eat()        // 调用基类方法
        
        // 类型检查和转换
        when (animal) {
            is Dog -> animal.wagTail()
            is Cat -> animal.climb()
        }
    }
}
```

#### 2.2.2 抽象类

```kotlin
// 抽象类
abstract class Shape(val name: String) {
    // 抽象属性
    abstract val area: Double
    
    // 抽象方法
    abstract fun draw()
    
    // 具体方法
    fun printInfo() {
        println("形状：$name，面积：$area")
    }
}

class Circle(private val radius: Double) : Shape("圆形") {
    // 实现抽象属性
    override val area: Double
        get() = Math.PI * radius * radius
    
    // 实现抽象方法
    override fun draw() {
        println("绘制一个半径为 $radius 的圆")
    }
}

class Rectangle(private val width: Double, private val height: Double) : Shape("矩形") {
    override val area: Double = width * height
    
    override fun draw() {
        println("绘制一个 ${width}x${height} 的矩形")
    }
}

fun testAbstractClass() {
    val shapes: List<Shape> = listOf(
        Circle(5.0),
        Rectangle(4.0, 6.0)
    )
    
    for (shape in shapes) {
        shape.draw()
        shape.printInfo()
        println("---")
    }
}
```

### 2.3 接口

Kotlin的接口比Java的接口更强大，可以包含默认实现：

```kotlin
// 接口定义
interface Drawable {
    // 抽象属性
    val color: String
    
    // 抽象方法
    fun draw()
    
    // 默认实现方法
    fun printDrawInfo() {
        println("绘制颜色为 $color 的图形")
    }
    
    // 带默认实现的方法
    fun animate() {
        println("开始动画效果")
        draw()
        println("动画完成")
    }
}

// 可以实现多个接口
interface Clickable {
    fun click() {
        println("点击事件")  // 默认实现
    }
    
    fun showClickEffect()   // 抽象方法
}

// 实现接口
class Button(override val color: String, val text: String) : Drawable, Clickable {
    override fun draw() {
        println("绘制按钮：$text")
    }
    
    override fun showClickEffect() {
        println("按钮点击效果：按钮变色")
    }
    
    // 可以重写接口的默认方法
    override fun click() {
        super.click()  // 调用接口的默认实现
        showClickEffect()
    }
}

// 接口冲突处理
interface A {
    fun foo() {
        println("A.foo()")
    }
}

interface B {
    fun foo() {
        println("B.foo()")
    }
}

class C : A, B {
    // 必须重写冲突的方法
    override fun foo() {
        super<A>.foo()  // 调用A的实现
        super<B>.foo()  // 调用B的实现
        println("C.foo()")
    }
}
```

### 2.4 数据类（Data Classes）

数据类是Kotlin的杀手级特性之一，自动生成`equals()`、`hashCode()`、`toString()`、`copy()`等方法：

```kotlin
// 数据类定义
data class User(
    val name: String,
    val age: Int,
    val email: String
)

// 编译器自动生成的方法：
// - equals() / hashCode()
// - toString()
// - componentN() functions (用于解构)
// - copy()

fun testDataClass() {
    val user1 = User("张三", 25, "zhang@example.com")
    val user2 = User("张三", 25, "zhang@example.com")
    
    // 自动生成的方法
    println(user1)                      // User(name=张三, age=25, email=zhang@example.com)
    println(user1 == user2)             // true (内容相等)
    println(user1 === user2)            // false (不是同一个对象)
    
    // copy方法 - 复制对象并修改部分属性
    val user3 = user1.copy(age = 26)
    println(user3)                      // User(name=张三, age=26, email=zhang@example.com)
    
    // 解构声明
    val (name, age, email) = user1
    println("姓名：$name，年龄：$age，邮箱：$email")
    
    // 只解构需要的部分
    val (userName, userAge) = user1
    println("用户：$userName，年龄：$userAge")
}

// 数据类的限制和最佳实践
data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String
) {
    // 数据类可以有成员函数
    fun getDisplayName(): String = "$name ($category)"
    
    // 但主构造函数至少需要一个参数
    // 且参数必须是val或var
}

// 嵌套数据类
data class Order(
    val id: String,
    val customer: Customer,
    val items: List<OrderItem>
) {
    data class Customer(val name: String, val phone: String)
    data class OrderItem(val product: Product, val quantity: Int)
}
```

### 2.5 密封类（Sealed Classes）

密封类用于表示受限的类继承结构，类似于枚举，但更强大：

```kotlin
// 密封类定义
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = -1) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// 使用密封类
fun handleResult(result: Result<String>) {
    when (result) {  // when表达式是穷尽的，不需要else分支
        is Result.Success -> {
            println("成功：${result.data}")
        }
        is Result.Error -> {
            println("错误：${result.message}，错误码：${result.code}")
        }
        Result.Loading -> {
            println("加载中...")
        }
        // 编译器保证所有情况都被覆盖
    }
}

// 网络请求的实际应用
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

class UserRepository {
    suspend fun getUser(id: String): NetworkResult<User> {
        return try {
            // 模拟网络请求
            val user = User("用户$id", 25, "$id@example.com")
            NetworkResult.Success(user)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}

// UI层处理
fun displayUser(result: NetworkResult<User>) {
    when (result) {
        is NetworkResult.Success -> {
            val user = result.data
            println("显示用户：${user.name}")
        }
        is NetworkResult.Error -> {
            println("显示错误：${result.exception.message}")
        }
        NetworkResult.Loading -> {
            println("显示加载动画")
        }
    }
}
```

### 2.6 枚举类

Kotlin的枚举类比Java的更强大：

```kotlin
// 基础枚举
enum class Direction {
    NORTH, SOUTH, WEST, EAST
}

// 带属性和方法的枚举
enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF);  // 注意分号
    
    fun containsRed(): Boolean = (this.rgb and 0xFF0000) != 0
}

// 枚举实现接口
interface Expr
enum class BinaryOperator : Expr {
    PLUS {
        override fun apply(x: Int, y: Int) = x + y
    },
    MINUS {
        override fun apply(x: Int, y: Int) = x - y
    },
    MULTIPLY {
        override fun apply(x: Int, y: Int) = x * y
    },
    DIVIDE {
        override fun apply(x: Int, y: Int) = x / y
    };
    
    abstract fun apply(x: Int, y: Int): Int
}

// 使用枚举
fun testEnums() {
    // 基础使用
    val direction = Direction.NORTH
    println(direction)                    // NORTH
    println(direction.name)               // NORTH
    println(direction.ordinal)            // 0
    
    // 遍历所有值
    for (color in Color.values()) {
        println("${color.name}: ${color.rgb.toString(16)}")
    }
    
    // 根据名称获取枚举值
    val red = Color.valueOf("RED")
    println("红色包含红色分量：${red.containsRed()}")
    
    // when表达式中使用
    fun getColorName(color: Color) = when (color) {
        Color.RED -> "红色"
        Color.GREEN -> "绿色"
        Color.BLUE -> "蓝色"
    }
    
    // 枚举方法调用
    val result = BinaryOperator.PLUS.apply(5, 3)
    println("5 + 3 = $result")
}
```

### 2.7 对象声明与单例

#### 2.7.1 object关键字

```kotlin
// 对象声明 - 单例模式
object DatabaseManager {
    private val connections = mutableMapOf<String, String>()
    
    fun connect(url: String): String {
        return connections.getOrPut(url) {
            "连接到数据库：$url"
        }
    }
    
    fun getConnectionCount(): Int = connections.size
}

// 伴生对象
class MathUtils {
    companion object {
        const val PI = 3.14159
        
        fun max(a: Int, b: Int): Int = if (a > b) a else b
        
        // 工厂方法
        fun createCalculator(): Calculator = Calculator()
    }
}

// 对象表达式 - 匿名对象
fun createClickListener(): Clickable {
    return object : Clickable {
        override fun click() {
            println("匿名对象的点击实现")
        }
        
        override fun showClickEffect() {
            println("匿名对象的点击效果")
        }
    }
}

fun testObjects() {
    // 单例对象使用
    val connection1 = DatabaseManager.connect("localhost:5432")
    val connection2 = DatabaseManager.connect("localhost:5432")
    println("连接数：${DatabaseManager.getConnectionCount()}")
    
    // 伴生对象使用
    println("π = ${MathUtils.PI}")
    println("最大值：${MathUtils.max(10, 20)}")
    
    // 匿名对象使用
    val listener = createClickListener()
    listener.click()
}
```

### 2.8 面试常考题

#### 题目1：open、final、abstract的区别

**问题：** 解释Kotlin中open、final、abstract关键字的作用和区别？

**答案：**
```kotlin
// 1. final（默认）- 不能被继承或重写
final class FinalClass {
    final fun finalMethod() {}  // final可以省略
}

// 2. open - 可以被继承或重写
open class OpenClass {
    open fun openMethod() {}    // 可以被重写
    fun normalMethod() {}       // 默认final，不能被重写
}

// 3. abstract - 抽象的，必须被实现
abstract class AbstractClass {
    abstract fun abstractMethod()      // 必须被实现
    open fun openMethod() {}          // 可以被重写
    fun concreteMethod() {}           // 具体实现
}

class ConcreteClass : AbstractClass() {
    override fun abstractMethod() {
        println("实现抽象方法")
    }
}
```

**要点：**
- Kotlin中类和方法默认是`final`的
- `open`允许被继承/重写
- `abstract`必须被实现，抽象类不能被实例化

#### 题目2：数据类的限制

**问题：** 数据类有哪些限制？什么情况下不能使用数据类？

**答案：**
```kotlin
// 数据类的限制
// 1. 主构造函数至少需要一个参数
// data class Empty()  // 编译错误

// 2. 参数必须是val或var
// data class InvalidData(name: String)  // 编译错误

// 3. 不能是abstract、open、sealed或inner
// abstract data class AbstractData(val x: Int)  // 编译错误
// open data class OpenData(val x: Int)          // 编译错误

// 正确的数据类
data class ValidData(val x: Int, var y: String)

// 4. 继承限制
open class Base(val name: String)
// data class Derived(val age: Int) : Base("test")  // 可以继承，但不推荐

// 最佳实践：数据类应该只用于存储数据
data class User(val id: Long, val name: String, val email: String) {
    // 可以有方法，但应该避免复杂逻辑
    fun getDisplayName(): String = name.uppercase()
}
```

#### 题目3：密封类vs枚举类

**问题：** 什么时候使用密封类？什么时候使用枚举类？

**答案：**
```kotlin
// 枚举类 - 固定的常量集合
enum class HttpStatus(val code: Int) {
    OK(200),
    NOT_FOUND(404),
    SERVER_ERROR(500)
}

// 密封类 - 类型安全的类层次
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

// 使用场景对比
fun handleHttpStatus(status: HttpStatus) {
    when (status) {
        HttpStatus.OK -> println("请求成功")
        HttpStatus.NOT_FOUND -> println("页面未找到")  
        HttpStatus.SERVER_ERROR -> println("服务器错误")
    }
}

fun handleApiResponse(response: ApiResponse<String>) {
    when (response) {
        is ApiResponse.Success -> println("数据：${response.data}")
        is ApiResponse.Error -> println("错误：${response.message}")
        ApiResponse.Loading -> println("加载中")
    }
}
```

**选择原则：**
- **枚举类：** 固定的常量集合，每个值都是同一类型
- **密封类：** 需要携带不同数据的类型层次，支持泛型

#### 题目4：伴生对象vs静态方法

**问题：** Kotlin的伴生对象与Java的静态方法有什么区别？

**答案：**
```kotlin
class JavaStyle {
    companion object {
        const val CONSTANT = "常量"        // 编译为Java的static final
        
        fun staticMethod() {              // 编译为静态方法
            println("静态方法")
        }
        
        @JvmStatic                       // 明确生成静态方法
        fun jvmStaticMethod() {
            println("JVM静态方法")
        }
    }
}

// Kotlin中的调用
fun kotlinUsage() {
    println(JavaStyle.CONSTANT)         // 直接访问
    JavaStyle.staticMethod()            // 静态调用
}

// Java中的调用
// JavaStyle.CONSTANT                  // OK
// JavaStyle.staticMethod()            // 实际是 JavaStyle.Companion.staticMethod()
// JavaStyle.jvmStaticMethod()         // @JvmStatic使其真正静态
```

**区别：**
- 伴生对象是真正的对象，可以实现接口
- `@JvmStatic`注解生成真正的Java静态方法
- 伴生对象可以有多个，但只能有一个`companion object`

#### 题目5：类型检查和转换

**问题：** Kotlin中如何进行类型检查和转换？智能转换的原理是什么？

**答案：**
```kotlin
// 类型检查
fun checkType(obj: Any) {
    // is操作符 - 类型检查
    if (obj is String) {
        // 智能转换：obj自动转换为String类型
        println("字符串长度：${obj.length}")
    }
    
    // !is操作符 - 负向类型检查  
    if (obj !is Int) {
        println("不是整数类型")
    }
}

// when表达式中的类型检查
fun processValue(value: Any): String = when (value) {
    is String -> "字符串：${value.uppercase()}"
    is Int -> "整数：${value * 2}"
    is List<*> -> "列表大小：${value.size}"
    else -> "未知类型"
}

// 不安全转换 as
fun unsafeCast(obj: Any) {
    val str = obj as String  // 可能抛出ClassCastException
    println(str.length)
}

// 安全转换 as?
fun safeCast(obj: Any) {
    val str = obj as? String  // 转换失败返回null
    println(str?.length ?: "转换失败")
}

// 智能转换的限制
class Container(var value: Any)

fun smartCastLimitation(container: Container) {
    if (container.value is String) {
        // 智能转换不生效，因为value是var且可能被修改
        // println(container.value.length)  // 编译错误
        
        val value = container.value
        if (value is String) {
            println(value.length)  // OK，局部变量可以智能转换
        }
    }
}
```

---

## 本章小结

第2章我们深入学习了Kotlin的面向对象编程特性：

### 主要内容：
1. **类与构造函数：** 主构造函数、次构造函数、init块
2. **继承与多态：** open/final/abstract关键字、接口的默认实现
3. **数据类：** 自动生成的方法、copy函数、解构声明
4. **密封类：** 类型安全的类层次结构
5. **枚举类：** 更强大的枚举实现
6. **对象声明：** 单例模式、伴生对象

### 面试重点：
- **类的默认性质：** Kotlin中类默认是final的
- **数据类限制：** 主构造函数参数要求、不能是abstract/open等
- **密封类应用：** 状态管理、结果封装
- **智能转换：** is操作符后的自动类型转换
- **伴生对象：** 与Java静态方法的区别

下一章我们将学习Kotlin的特有特性：空安全、扩展函数和高阶函数。