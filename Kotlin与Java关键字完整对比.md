# Kotlin与Java关键字完整对比指南

> 从Java到Kotlin的完美过渡，一份详尽的语言特性映射手册

## 一、类型检查与转换

### 1.1 类型检查

**Java的instanceof → Kotlin的is**

Java中使用instanceof检查对象类型，Kotlin使用is关键字，语义更直观。

```
Java:  if (obj instanceof String) { }
Kotlin: if (obj is String) { }
```

**否定形式：**
```
Java:  if (!(obj instanceof String)) { }
Kotlin: if (obj !is String) { }
```

**智能转换的优势：**
Kotlin在is检查后会自动转换类型，无需显式转换：
- Java需要：`((String) obj).length()`
- Kotlin直接：`obj.length`（is检查后自动转换）

### 1.2 类型转换

**Java的(Type)cast → Kotlin的as**

```
Java:  String str = (String) obj;
Kotlin: val str = obj as String
```

**安全转换as?：**
Kotlin独有的安全转换，失败返回null而不是抛异常：
```
Java:  需要try-catch或instanceof检查
Kotlin: val str = obj as? String  // 失败返回null
```

## 二、类与对象声明

### 2.1 类声明

**Java的class → Kotlin的class（有差异）**

| Java修饰符 | Kotlin对应 | 说明 |
|-----------|-----------|------|
| public class | class | Kotlin默认public |
| final class | class | Kotlin类默认final |
| abstract class | abstract class | 相同 |
| class extends | class : | 继承语法不同 |
| implements | : | 实现接口也用冒号 |

**特殊说明：**
- Kotlin类默认final，需要继承必须声明open
- Kotlin没有extends和implements关键字，统一用冒号

### 2.2 接口与抽象

**Java的interface → Kotlin的interface**

主要区别：
- Kotlin接口可以有属性声明
- Kotlin接口可以有默认实现（Java 8后也支持）
- Kotlin接口的属性不能有backing field

### 2.3 静态成员

**Java的static → Kotlin的companion object/object/顶层**

Java静态成员在Kotlin中的对应：
```
Java:  static field → Kotlin: companion object中的属性
Java:  static method → Kotlin: companion object中的方法
Java:  static class → Kotlin: 嵌套类（默认静态）
Java:  static block → Kotlin: companion object的init块
```

**@JvmStatic注解：**
让companion object成员生成真正的静态方法，便于Java调用。

## 三、变量与常量

### 3.1 变量声明

**Java的类型 变量名 → Kotlin的var/val**

| Java | Kotlin | 说明 |
|------|--------|------|
| Type variable | var variable: Type | 可变变量 |
| final Type variable | val variable: Type | 不可变变量 |
| Type variable = value | var variable = value | 类型推导 |

### 3.2 常量声明

**Java的final → Kotlin的val/const**

```
Java:  final int VALUE = 100;
Kotlin: val VALUE = 100  // 运行时常量
Kotlin: const val VALUE = 100  // 编译时常量
```

**const val的限制：**
- 只能修饰基本类型和String
- 必须在顶层或object中
- 必须立即初始化

## 四、访问修饰符

### 4.1 可见性对比

| Java | Kotlin | 作用域 |
|------|--------|--------|
| public | public | 所有地方可见 |
| protected | protected | 子类可见（Kotlin不包括同包） |
| default(包私有) | internal | Kotlin是模块可见 |
| private | private | 类/文件内可见 |

**重要区别：**
- Kotlin默认public，Java默认包私有
- Kotlin的protected不包括同包访问
- Kotlin新增internal（模块可见）
- Kotlin的private在顶层声明时表示文件私有

## 五、控制流

### 5.1 条件语句

**Java的switch → Kotlin的when**

```
Java:  switch-case-break-default
Kotlin: when-分支-else
```

**when的优势：**
- 不需要break
- 可以是表达式（有返回值）
- 支持任意类型和条件
- 支持区间和集合检查

### 5.2 循环语句

**Java的for循环 → Kotlin的for-in**

| Java | Kotlin | 说明 |
|------|--------|------|
| for(;;) | for(i in range) | 区间遍历 |
| for-each | for-in | 集合遍历 |
| while | while | 相同 |
| do-while | do-while | 相同 |

**区间操作符：**
- `..` 闭区间
- `until` 半开区间
- `downTo` 递减
- `step` 步长

### 5.3 跳转语句

| Java | Kotlin | 说明 |
|------|--------|------|
| break | break | 支持标签 |
| continue | continue | 支持标签 |
| return | return | 支持标签和隐式返回 |
| goto | 无 | Kotlin没有goto |

## 六、异常处理

### 6.1 异常声明

**Java的throws → Kotlin无需声明**

Kotlin没有受检异常概念，不需要throws声明：
```
Java:  void method() throws IOException { }
Kotlin: fun method() { }  // 不需要声明
```

### 6.2 try-catch

**基本相同，但Kotlin的try是表达式：**
```
Java:  只能作为语句
Kotlin: val result = try { ... } catch(e: Exception) { ... }
```

### 6.3 抛出异常

**Java的throw → Kotlin的throw（表达式）**

Kotlin的throw是表达式，可以用在Elvis操作符后：
```
val s = person.name ?: throw IllegalArgumentException("Name required")
```

## 七、泛型

### 7.1 泛型声明

| Java | Kotlin | 说明 |
|------|--------|------|
| `<T>` | `<T>` | 基本相同 |
| `<T extends Type>` | `<T : Type>` | 上界约束 |
| `<T super Type>` | 无直接对应 | Kotlin用in/out |
| `<?>` | `<*>` | 通配符 |

### 7.2 型变

**Java的extends/super → Kotlin的out/in**

```
Java:  List<? extends Number>  // 协变
Kotlin: List<out Number>

Java:  List<? super Number>    // 逆变
Kotlin: List<in Number>
```

## 八、包与导入

### 8.1 包声明

| Java | Kotlin | 说明 |
|------|--------|------|
| package | package | 相同，但Kotlin更灵活 |
| import | import | 支持别名导入 |
| import static | import | Kotlin不区分 |

**Kotlin特有：**
- 导入别名：`import com.example.foo as bar`
- 包与目录结构可以不一致

## 九、特殊关键字

### 9.1 空处理

| Java | Kotlin | 说明 |
|------|--------|------|
| null | null | 相同 |
| 无 | ?.  | 安全调用 |
| 无 | ?: | Elvis操作符 |
| 无 | !! | 非空断言 |

### 9.2 对象创建

| Java | Kotlin | 说明 |
|------|--------|------|
| new | 无 | Kotlin不需要new |
| this | this | 相同 |
| super | super | 相同 |

### 9.3 函数相关

| Java | Kotlin | 说明 |
|------|--------|------|
| void | Unit | 无返回值 |
| 无 | Nothing | 永不返回 |
| 无 | fun | 函数声明 |
| 无 | suspend | 挂起函数 |

## 十、Kotlin独有关键字

### 10.1 属性与委托

**Kotlin独有，Java无对应：**
- `by` - 委托
- `lateinit` - 延迟初始化
- `lazy` - 懒加载（标准库函数）
- `field` - backing field
- `get/set` - 属性访问器

### 10.2 类型相关

**Kotlin独有：**
- `object` - 单例/匿名对象
- `companion object` - 伴生对象
- `sealed` - 密封类
- `data` - 数据类
- `inline` - 内联类/函数
- `reified` - 具体化类型参数
- `value` - 值类（inline class）

### 10.3 函数修饰符

**Kotlin独有：**
- `tailrec` - 尾递归优化
- `operator` - 操作符重载
- `infix` - 中缀函数
- `inline/noinline/crossinline` - 内联控制
- `suspend` - 协程挂起

### 10.4 可见性修饰符

**Kotlin独有：**
- `internal` - 模块可见
- `open` - 可继承/重写
- `override` - 重写（Java用@Override注解）

## 十一、其他重要区别

### 11.1 原始类型

| Java原始类型 | Kotlin类型 | 说明 |
|-------------|-----------|------|
| int | Int | Kotlin统一对象类型 |
| long | Long | 自动装箱优化 |
| float | Float | 可空性区分 |
| double | Double | Int?是包装类型 |
| boolean | Boolean | 非空是原始类型 |
| char | Char | 编译器优化 |
| byte | Byte | |
| short | Short | |

### 11.2 数组

| Java | Kotlin | 说明 |
|------|--------|------|
| Type[] | Array<Type> | 对象数组 |
| int[] | IntArray | 原始类型数组 |
| T... | vararg T | 可变参数 |

### 11.3 注解

| Java | Kotlin | 说明 |
|------|--------|------|
| @interface | annotation class | 注解声明 |
| @Override | override | Kotlin是关键字 |
| @Deprecated | @Deprecated | 相同 |

## 十二、语法糖对比

### 12.1 字符串

| Java | Kotlin | 说明 |
|------|--------|------|
| "text" | "text" | 普通字符串 |
| 无 | """text""" | 原始字符串 |
| "Hello " + name | "Hello $name" | 字符串模板 |
| String.format() | "${ }" | 表达式模板 |

### 12.2 Lambda表达式

| Java 8+ | Kotlin | 说明 |
|---------|--------|------|
| () -> {} | { } | Lambda语法 |
| :: | :: | 方法引用 |
| 无 | it | 隐式参数 |

## 十三、面试必备对比要点

### 13.1 高频考点

**问：Kotlin如何实现Java的静态成员？**
答：三种方式：
1. companion object - 伴生对象
2. object - 单例对象
3. 顶层声明 - 包级别函数/属性

**问：Kotlin的is与Java的instanceof区别？**
答：功能相同，但Kotlin有智能转换，检查后自动转型，无需显式cast。

**问：Kotlin为什么没有受检异常？**
答：设计理念不同，Kotlin认为受检异常带来的麻烦大于好处，简化了异常处理。

### 13.2 易混淆点

1. **open vs final**：Kotlin类默认final，需要open才能继承；Java类默认可继承，需要final禁止继承。

2. **internal vs default**：Kotlin的internal是模块可见；Java的default是包可见。

3. **== vs ===**：Kotlin的==是结构相等（equals）；===是引用相等；Java的==是引用相等。

4. **Unit vs void**：Unit是真实的类型，可以作为泛型参数；void不是类型。

## 总结

从Java迁移到Kotlin，不仅是语法的改变，更是编程思维的升级。Kotlin在保持与Java互操作性的同时，通过更简洁的语法、更强的类型系统、更安全的空处理，让代码更加优雅和健壮。

掌握这些对应关系，你就能在两种语言间自如切换，在面试中展现全面的JVM语言掌握能力，在实际开发中选择最适合的工具解决问题。

记住：好的程序员精通一门语言，优秀的程序员理解语言背后的设计理念。