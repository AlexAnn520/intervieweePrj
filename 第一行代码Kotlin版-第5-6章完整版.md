# 第一行代码 Kotlin版 - 第5-6章完整版

> 接续前面章节，深入学习Kotlin协程和Android实践

---

## 第5章 协程编程 - 异步编程的艺术

协程是Kotlin最具革命性的特性之一，彻底改变了我们处理异步编程的方式。如果说前面的章节让你感受到了Kotlin的简洁和强大，那么协程将让你体验到异步编程的优雅与高效。

### 5.1 协程基础概念

#### 5.1.1 什么是协程？

还记得Android开发中那些复杂的异步操作吗？AsyncTask、Thread、Handler、RxJava... 每种方案都有各自的问题。协程的出现彻底解决了这些痛点。

```kotlin
// 传统的异步编程（回调地狱）
fun fetchUserDataTraditional(userId: String, callback: (User?) -> Unit) {
    thread {
        try {
            val userResponse = networkService.getUser(userId)  // 网络请求
            val user = parseUser(userResponse)
            
            // 还需要获取用户详细信息
            val detailResponse = networkService.getUserDetail(user.id)
            val detail = parseUserDetail(detailResponse)
            
            runOnUiThread {
                callback(user.copy(detail = detail))
            }
        } catch (e: Exception) {
            runOnUiThread {
                callback(null)
            }
        }
    }
}

// 协程的优雅解决方案
suspend fun fetchUserDataWithCoroutine(userId: String): User? {
    return try {
        val userResponse = networkService.getUser(userId)      // 挂起，不阻塞线程
        val user = parseUser(userResponse)
        
        val detailResponse = networkService.getUserDetail(user.id)  // 继续挂起
        val detail = parseUserDetail(detailResponse)
        
        user.copy(detail = detail)  // 直接返回结果
    } catch (e: Exception) {
        null
    }
}
```

看到区别了吗？协程让异步代码写起来就像同步代码一样简洁！

#### 5.1.2 协程的核心概念

```kotlin
fun demonstrateCoroutineBasics() {
    // 1. CoroutineScope - 协程作用域
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // 2. launch - 启动协程（fire-and-forget）
    scope.launch {
        println("协程开始执行")
        delay(1000)  // 挂起函数，不阻塞线程
        println("协程执行完成")
    }
    
    // 3. async - 启动协程并返回结果
    val deferred = scope.async {
        delay(500)
        "协程的返回值"
    }
    
    // 4. runBlocking - 阻塞当前线程直到协程完成
    runBlocking {
        val result = deferred.await()  // 等待async协程的结果
        println("获取到结果: $result")
    }
}
```

### 5.2 挂起函数（Suspend Functions）

#### 5.2.1 挂起函数的特性

```kotlin
// suspend关键字标记挂起函数
suspend fun performNetworkRequest(): String {
    delay(1000)  // 模拟网络延迟，挂起当前协程
    return "网络请求结果"
}

// 挂起函数只能在协程或其他挂起函数中调用
suspend fun complexAsyncOperation() {
    println("开始复杂的异步操作")
    
    // 可以调用其他挂起函数
    val result1 = performNetworkRequest()
    println("第一步完成: $result1")
    
    // 挂起函数可以像普通函数一样使用控制流
    if (result1.isNotEmpty()) {
        val result2 = performNetworkRequest()
        println("第二步完成: $result2")
    }
    
    println("复杂操作完成")
}

// 挂起函数的组合
suspend fun fetchUserProfile(userId: String): UserProfile {
    // 并行执行多个网络请求
    val userDeferred = async { fetchUser(userId) }
    val settingsDeferred = async { fetchUserSettings(userId) }
    val avatarDeferred = async { fetchUserAvatar(userId) }
    
    // 等待所有请求完成
    val user = userDeferred.await()
    val settings = settingsDeferred.await()
    val avatar = avatarDeferred.await()
    
    return UserProfile(user, settings, avatar)
}

suspend fun fetchUser(userId: String): User {
    delay(500)  // 模拟网络延迟
    return User(userId, "用户$userId")
}

suspend fun fetchUserSettings(userId: String): UserSettings {
    delay(300)
    return UserSettings(userId, "设置")
}

suspend fun fetchUserAvatar(userId: String): String {
    delay(200)
    return "avatar_$userId.jpg"
}
```

#### 5.2.2 挂起函数的错误处理

```kotlin
// 协程中的异常处理
suspend fun robustNetworkOperation(): Result<String> = try {
    val result = performRiskyNetworkRequest()
    Result.success(result)
} catch (e: NetworkException) {
    Result.failure(e)
} catch (e: TimeoutException) {
    Result.failure(e)
}

suspend fun performRiskyNetworkRequest(): String {
    delay(1000)
    if (Random.nextBoolean()) {
        throw NetworkException("网络连接失败")
    }
    return "请求成功"
}

// 使用supervisorScope处理部分失败
suspend fun fetchMultipleDataWithErrorHandling(): List<String> {
    val results = mutableListOf<String>()
    
    supervisorScope {  // 一个子协程失败不会影响其他协程
        val jobs = (1..5).map { id ->
            async {
                try {
                    delay(100 * id.toLong())
                    if (id == 3) throw Exception("任务$id 失败")
                    "任务$id 成功"
                } catch (e: Exception) {
                    "任务$id 失败: ${e.message}"
                }
            }
        }
        
        jobs.forEach { job ->
            results.add(job.await())
        }
    }
    
    return results
}
```

### 5.3 协程构建器

#### 5.3.1 launch vs async

```kotlin
class CoroutineBuilders {
    
    fun demonstrateLaunchVsAsync() = runBlocking {
        println("=== launch vs async 对比 ===")
        
        // launch - 启动协程但不返回结果
        val job = launch {
            delay(1000)
            println("launch: 执行完成")
        }
        
        // async - 启动协程并返回Deferred<T>
        val deferred = async {
            delay(500)
            "async: 返回结果"
        }
        
        println("协程已启动，继续执行其他代码...")
        
        // 等待协程完成
        job.join()                    // 等待launch完成，无返回值
        val result = deferred.await() // 等待async完成，获取返回值
        
        println("获取到async结果: $result")
    }
    
    // 实际应用：并行网络请求
    suspend fun fetchUserData(userId: String): UserData {
        return coroutineScope {  // 创建子作用域
            // 并行启动多个请求
            val profileAsync = async { fetchUserProfile(userId) }
            val friendsAsync = async { fetchUserFriends(userId) }
            val postsAsync = async { fetchUserPosts(userId) }
            
            // 等待所有请求完成
            UserData(
                profile = profileAsync.await(),
                friends = friendsAsync.await(),
                posts = postsAsync.await()
            )
        }
    }
}

data class UserData(
    val profile: UserProfile,
    val friends: List<String>,
    val posts: List<String>
)

suspend fun fetchUserFriends(userId: String): List<String> {
    delay(300)
    return listOf("朋友1", "朋友2", "朋友3")
}

suspend fun fetchUserPosts(userId: String): List<String> {
    delay(400)
    return listOf("帖子1", "帖子2")
}
```

#### 5.3.2 runBlocking的使用场景

```kotlin
class RunBlockingExamples {
    
    // 1. 测试中的使用
    @Test
    fun testSuspendFunction() = runBlocking {
        val result = performAsyncOperation()
        assertEquals("expected", result)
    }
    
    // 2. main函数中的使用
    fun main() = runBlocking {
        println("程序开始")
        delay(1000)
        println("程序结束")
    }
    
    // 3. 从普通函数调用挂起函数
    fun normalFunctionCallingSuspend() {
        // 注意：runBlocking会阻塞当前线程，谨慎使用
        val result = runBlocking {
            performAsyncOperation()
        }
        println("结果: $result")
    }
    
    // 4. Android中的错误示例（不要这样做）
    /*
    fun onButtonClick() {
        // 错误！不要在主线程中使用runBlocking
        runBlocking {
            delay(5000)  // 这会导致ANR
        }
    }
    */
    
    // 正确的做法
    fun onButtonClickCorrect() {
        lifecycleScope.launch {  // 使用适当的协程作用域
            delay(5000)
            // 更新UI
        }
    }
    
    suspend fun performAsyncOperation(): String {
        delay(500)
        return "操作完成"
    }
}
```

### 5.4 协程作用域和上下文

#### 5.4.1 CoroutineScope的层次结构

```kotlin
class CoroutineScopeExamples {
    
    fun demonstrateScope() {
        // 1. 全局作用域（不推荐）
        GlobalScope.launch {
            delay(1000)
            println("GlobalScope中的协程")
        }
        
        // 2. 自定义作用域
        val customScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        
        customScope.launch {
            println("自定义作用域中的协程")
            
            // 3. 子作用域
            coroutineScope {  // 继承父协程的上下文
                launch {
                    delay(500)
                    println("子作用域中的协程1")
                }
                
                launch {
                    delay(1000)
                    println("子作用域中的协程2")
                }
                // coroutineScope会等待所有子协程完成
            }
            
            println("所有子协程完成")
        }
        
        // 4. 取消作用域
        customScope.cancel()  // 取消作用域中的所有协程
    }
    
    // 结构化并发示例
    suspend fun structuredConcurrency() = coroutineScope {
        println("开始结构化并发示例")
        
        val job1 = launch {
            repeat(5) { i ->
                println("协程1: $i")
                delay(100)
            }
        }
        
        val job2 = launch {
            repeat(3) { i ->
                println("协程2: $i")
                delay(150)
            }
        }
        
        // 如果这里抛出异常，所有子协程都会被取消
        // throw Exception("模拟异常")
        
        println("等待所有子协程完成...")
        // coroutineScope会自动等待所有子协程
    }
}
```

#### 5.4.2 协程上下文和调度器

```kotlin
class CoroutineContextExamples {
    
    fun demonstrateDispatchers() = runBlocking {
        println("=== 不同调度器示例 ===")
        
        // 1. Dispatchers.Main - 主线程（Android UI线程）
        launch(Dispatchers.Main) {
            println("Main: ${Thread.currentThread().name}")
            // 更新UI操作应该在这里进行
        }
        
        // 2. Dispatchers.IO - I/O操作
        launch(Dispatchers.IO) {
            println("IO: ${Thread.currentThread().name}")
            // 网络请求、文件读写应该在这里进行
            delay(100)
        }
        
        // 3. Dispatchers.Default - CPU密集型操作
        launch(Dispatchers.Default) {
            println("Default: ${Thread.currentThread().name}")
            // 复杂计算应该在这里进行
            repeat(1000) {
                // 模拟CPU密集型操作
            }
        }
        
        // 4. Dispatchers.Unconfined - 不限定调度器
        launch(Dispatchers.Unconfined) {
            println("Unconfined 1: ${Thread.currentThread().name}")
            delay(100)
            println("Unconfined 2: ${Thread.currentThread().name}")
            // 挂起后可能在不同线程恢复
        }
        
        delay(200)  // 等待所有协程完成
    }
    
    // 上下文切换示例
    suspend fun contextSwitching() {
        println("开始上下文切换示例")
        
        // 在IO调度器中执行网络请求
        val data = withContext(Dispatchers.IO) {
            println("网络请求: ${Thread.currentThread().name}")
            delay(500)  // 模拟网络延迟
            "网络数据"
        }
        
        // 在Default调度器中处理数据
        val processedData = withContext(Dispatchers.Default) {
            println("数据处理: ${Thread.currentThread().name}")
            data.uppercase()  // 模拟数据处理
        }
        
        // 在Main调度器中更新UI
        withContext(Dispatchers.Main) {
            println("UI更新: ${Thread.currentThread().name}")
            println("处理后的数据: $processedData")
            // 更新UI
        }
    }
    
    // 自定义协程上下文
    fun customCoroutineContext() = runBlocking {
        val customContext = Dispatchers.IO + 
                CoroutineName("CustomCoroutine") + 
                SupervisorJob()
        
        launch(customContext) {
            println("自定义上下文: ${coroutineContext[CoroutineName]}")
            println("运行线程: ${Thread.currentThread().name}")
        }
    }
}
```

### 5.5 Flow数据流

#### 5.5.1 Flow基础

Flow是Kotlin协程中处理异步数据流的解决方案，类似于RxJava的Observable，但更加轻量和协程友好。

```kotlin
class FlowBasics {
    
    // 创建Flow的几种方式
    fun createFlows() = runBlocking {
        println("=== 创建Flow示例 ===")
        
        // 1. flow builder
        val numbersFlow = flow {
            for (i in 1..5) {
                delay(100)  // 模拟异步操作
                emit(i)     // 发射数据
            }
        }
        
        numbersFlow.collect { value ->
            println("收集到数字: $value")
        }
        
        // 2. flowOf - 固定数据
        val staticFlow = flowOf("A", "B", "C")
        staticFlow.collect { value ->
            println("收集到字母: $value")
        }
        
        // 3. asFlow - 集合转Flow
        val listFlow = listOf(1, 2, 3, 4, 5).asFlow()
        listFlow.collect { value ->
            println("来自列表: $value")
        }
        
        // 4. 冷流特性演示
        println("\n=== 冷流特性 ===")
        val coldFlow = flow {
            println("Flow开始执行")
            for (i in 1..3) {
                delay(100)
                emit(i)
            }
        }
        
        println("第一次收集:")
        coldFlow.collect { println("值: $it") }
        
        println("第二次收集:")
        coldFlow.collect { println("值: $it") }  // Flow会重新执行
    }
}
```

#### 5.5.2 Flow的操作符

```kotlin
class FlowOperators {
    
    fun demonstrateFlowOperators() = runBlocking {
        val sourceFlow = flow {
            for (i in 1..10) {
                delay(100)
                emit(i)
            }
        }
        
        println("=== Flow操作符示例 ===")
        
        // map - 转换数据
        sourceFlow
            .map { it * it }  // 平方
            .filter { it > 10 }  // 过滤
            .take(3)  // 只取前3个
            .collect { value ->
                println("转换后的值: $value")
            }
        
        // 实际应用：网络请求处理
        fetchUserUpdates()
    }
    
    suspend fun fetchUserUpdates() {
        val userUpdatesFlow = flow {
            repeat(5) { id ->
                delay(500)
                emit(User("user$id", "用户$id"))
            }
        }
        
        userUpdatesFlow
            .map { user ->
                // 为每个用户获取详细信息
                UserWithDetails(user, fetchUserDetails(user.id))
            }
            .filter { userWithDetails ->
                // 过滤掉无效用户
                userWithDetails.details.isNotEmpty()
            }
            .collect { userWithDetails ->
                println("用户更新: ${userWithDetails.user.name} - ${userWithDetails.details}")
            }
    }
    
    private suspend fun fetchUserDetails(userId: String): String {
        delay(200)  // 模拟网络请求
        return "详细信息_$userId"
    }
}

data class User(val id: String, val name: String)
data class UserWithDetails(val user: User, val details: String)
```

#### 5.5.3 Flow的异常处理和背压

```kotlin
class FlowAdvanced {
    
    // Flow异常处理
    fun flowExceptionHandling() = runBlocking {
        println("=== Flow异常处理 ===")
        
        val problematicFlow = flow {
            for (i in 1..5) {
                if (i == 3) {
                    throw RuntimeException("流中发生错误")
                }
                emit(i)
            }
        }
        
        // 1. try-catch处理
        try {
            problematicFlow.collect { value ->
                println("收集到: $value")
            }
        } catch (e: Exception) {
            println("捕获到异常: ${e.message}")
        }
        
        // 2. catch操作符
        problematicFlow
            .catch { exception ->
                println("Flow内部捕获异常: ${exception.message}")
                emit(-1)  // 发射默认值
            }
            .collect { value ->
                println("处理后的值: $value")
            }
    }
    
    // Flow背压处理
    fun flowBackpressure() = runBlocking {
        println("\n=== Flow背压处理 ===")
        
        val fastProducerFlow = flow {
            for (i in 1..100) {
                delay(10)   // 快速生产
                emit(i)
            }
        }
        
        // buffer - 缓冲处理
        val startTime = System.currentTimeMillis()
        fastProducerFlow
            .buffer(capacity = 10)  // 缓冲10个元素
            .collect { value ->
                delay(100)  // 慢速消费
                println("缓冲处理: $value")
            }
        
        val bufferedTime = System.currentTimeMillis() - startTime
        println("缓冲处理用时: ${bufferedTime}ms")
        
        // conflate - 合并处理（只保留最新值）
        val conflateStartTime = System.currentTimeMillis()
        fastProducerFlow
            .conflate()
            .collect { value ->
                delay(100)
                println("合并处理: $value")
            }
        
        val conflatedTime = System.currentTimeMillis() - conflateStartTime
        println("合并处理用时: ${conflatedTime}ms")
    }
    
    // StateFlow和SharedFlow
    fun stateAndSharedFlow() = runBlocking {
        println("\n=== StateFlow和SharedFlow ===")
        
        // StateFlow - 状态流，始终有最新值
        val stateFlow = MutableStateFlow("初始状态")
        
        launch {
            stateFlow.collect { state ->
                println("StateFlow状态: $state")
            }
        }
        
        delay(100)
        stateFlow.value = "更新状态1"
        delay(100)
        stateFlow.value = "更新状态2"
        
        // SharedFlow - 共享流，可以有多个订阅者
        val sharedFlow = MutableSharedFlow<String>()
        
        // 多个收集者
        launch {
            sharedFlow.collect { value ->
                println("收集者1: $value")
            }
        }
        
        launch {
            sharedFlow.collect { value ->
                println("收集者2: $value")
            }
        }
        
        delay(100)
        sharedFlow.emit("共享数据1")
        delay(100)
        sharedFlow.emit("共享数据2")
        
        delay(500)  // 等待所有协程完成
    }
}
```

### 5.6 Channel通道

#### 5.6.1 Channel基础使用

```kotlin
class ChannelBasics {
    
    fun basicChannelUsage() = runBlocking {
        println("=== Channel基础使用 ===")
        
        // 创建Channel
        val channel = Channel<Int>()
        
        // 生产者协程
        launch {
            for (i in 1..5) {
                delay(100)
                channel.send(i)  // 发送数据
                println("发送: $i")
            }
            channel.close()  // 关闭通道
        }
        
        // 消费者协程
        launch {
            for (value in channel) {  // 迭代接收数据
                println("接收: $value")
                delay(150)  // 模拟处理时间
            }
        }
        
        delay(1000)  // 等待完成
    }
    
    // 不同类型的Channel
    fun channelTypes() = runBlocking {
        println("\n=== 不同类型的Channel ===")
        
        // 1. Unlimited Channel - 无限容量
        val unlimitedChannel = Channel<String>(Channel.UNLIMITED)
        
        // 2. Conflated Channel - 只保留最新值
        val conflatedChannel = Channel<String>(Channel.CONFLATED)
        
        // 3. Buffered Channel - 指定缓冲大小
        val bufferedChannel = Channel<String>(capacity = 3)
        
        // 4. Rendezvous Channel - 零缓冲（默认）
        val rendezvousChannel = Channel<String>()
        
        // 演示buffered channel
        launch {
            repeat(5) { i ->
                bufferedChannel.send("Item $i")
                println("发送 Item $i")
            }
            bufferedChannel.close()
        }
        
        launch {
            for (item in bufferedChannel) {
                delay(200)  // 慢速消费
                println("处理 $item")
            }
        }
        
        delay(2000)
    }
    
    // 生产者-消费者模式
    fun producerConsumerPattern() = runBlocking {
        println("\n=== 生产者-消费者模式 ===")
        
        // 使用produce构建器创建生产者
        val producer = produce {
            for (i in 1..10) {
                delay(100)
                send("数据$i")
                println("生产: 数据$i")
            }
        }
        
        // 多个消费者
        repeat(3) { consumerId ->
            launch {
                for (data in producer) {
                    println("消费者$consumerId 处理: $data")
                    delay(200)
                }
            }
        }
    }
}
```

#### 5.6.2 Channel的实际应用

```kotlin
class ChannelApplications {
    
    // 任务分发系统
    suspend fun taskDistributionSystem() = coroutineScope {
        println("=== 任务分发系统 ===")
        
        val taskChannel = Channel<Task>(capacity = 10)
        val resultChannel = Channel<TaskResult>()
        
        // 任务生产者
        launch {
            repeat(20) { taskId ->
                val task = Task(taskId, "任务$taskId", Random.nextInt(100, 1000))
                taskChannel.send(task)
                delay(50)
            }
            taskChannel.close()
        }
        
        // 多个工作者处理任务
        repeat(3) { workerId ->
            launch {
                for (task in taskChannel) {
                    println("工作者$workerId 开始处理: ${task.name}")
                    delay(task.duration.toLong())  // 模拟处理时间
                    
                    val result = TaskResult(task.id, workerId, "完成", System.currentTimeMillis())
                    resultChannel.send(result)
                }
            }
        }
        
        // 结果收集器
        launch {
            var completedTasks = 0
            for (result in resultChannel) {
                completedTasks++
                println("任务${result.taskId} 由工作者${result.workerId} 完成")
                
                if (completedTasks == 20) {
                    resultChannel.close()
                    break
                }
            }
        }
    }
    
    // 实时数据处理管道
    suspend fun realTimeDataPipeline() = coroutineScope {
        println("\n=== 实时数据处理管道 ===")
        
        val rawDataChannel = Channel<RawData>()
        val processedDataChannel = Channel<ProcessedData>()
        val finalResultChannel = Channel<FinalResult>()
        
        // 数据源
        launch {
            repeat(10) { id ->
                val rawData = RawData(id, "原始数据$id", Random.nextDouble())
                rawDataChannel.send(rawData)
                delay(200)
            }
            rawDataChannel.close()
        }
        
        // 第一阶段处理
        launch {
            for (rawData in rawDataChannel) {
                val processedData = ProcessedData(
                    rawData.id,
                    rawData.content.uppercase(),
                    rawData.value * 2
                )
                processedDataChannel.send(processedData)
                println("第一阶段处理: ${processedData.content}")
            }
            processedDataChannel.close()
        }
        
        // 第二阶段处理
        launch {
            for (processedData in processedDataChannel) {
                val finalResult = FinalResult(
                    processedData.id,
                    processedData.content + "_FINAL",
                    processedData.processedValue.toInt()
                )
                finalResultChannel.send(finalResult)
                println("最终结果: ${finalResult.result}")
            }
            finalResultChannel.close()
        }
        
        // 结果输出
        for (result in finalResultChannel) {
            println("输出最终结果: $result")
        }
    }
}

data class Task(val id: Int, val name: String, val duration: Int)
data class TaskResult(val taskId: Int, val workerId: Int, val status: String, val timestamp: Long)
data class RawData(val id: Int, val content: String, val value: Double)
data class ProcessedData(val id: Int, val content: String, val processedValue: Double)
data class FinalResult(val id: Int, val result: String, val finalValue: Int)
```

### 5.7 协程的最佳实践

#### 5.7.1 Android中的协程使用

```kotlin
// ViewModel中的协程最佳实践
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // 使用viewModelScope自动管理生命周期
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = UiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "未知错误")
                _events.emit(UiEvent.ShowError("加载用户失败"))
            }
        }
    }
    
    // 处理用户交互
    fun onRefresh() {
        viewModelScope.launch {
            try {
                val users = userRepository.refreshUsers()
                _events.emit(UiEvent.ShowMessage("刷新完成"))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowError("刷新失败"))
            }
        }
    }
    
    // 批量操作
    fun performBulkOperation(userIds: List<String>) {
        viewModelScope.launch {
            val results = userIds.map { userId ->
                async {
                    try {
                        userRepository.updateUser(userId)
                        OperationResult.Success(userId)
                    } catch (e: Exception) {
                        OperationResult.Error(userId, e.message)
                    }
                }
            }.awaitAll()
            
            val successCount = results.count { it is OperationResult.Success }
            _events.emit(UiEvent.ShowMessage("操作完成: $successCount/${results.size}"))
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val user: User) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class ShowError(val message: String) : UiEvent
}

sealed class OperationResult {
    data class Success(val userId: String) : OperationResult()
    data class Error(val userId: String, val message: String?) : OperationResult()
}
```

#### 5.7.2 Repository中的协程模式

```kotlin
class UserRepository(
    private val apiService: UserApiService,
    private val localDatabase: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    // 缓存优先策略
    suspend fun getUser(userId: String): User = withContext(ioDispatcher) {
        try {
            // 先尝试从缓存获取
            val cachedUser = localDatabase.getUser(userId)
            if (cachedUser != null && !isCacheExpired(cachedUser)) {
                return@withContext cachedUser
            }
            
            // 缓存过期或不存在，从网络获取
            val networkUser = apiService.getUser(userId)
            
            // 更新缓存
            localDatabase.insertUser(networkUser.copy(cacheTime = System.currentTimeMillis()))
            
            networkUser
        } catch (networkException: Exception) {
            // 网络失败，尝试返回缓存数据
            localDatabase.getUser(userId) 
                ?: throw UserNotFoundException("用户不存在且无缓存数据")
        }
    }
    
    // 分页数据加载
    fun getUsersPaged(): Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize = 20, prefetchDistance = 5),
        pagingSourceFactory = { UserPagingSource(apiService) }
    ).flow.cachedIn(ProcessLifecycleOwner.get().lifecycleScope)
    
    // 实时数据同步
    fun getUserUpdatesFlow(userId: String): Flow<User> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                val user = apiService.getUser(userId)
                emit(user)
                delay(30000)  // 30秒轮询一次
            } catch (e: Exception) {
                // 发射错误或使用缓存数据
                localDatabase.getUser(userId)?.let { emit(it) }
                delay(60000)  // 错误时延长轮询间隔
            }
        }
    }.distinctUntilChanged()  // 只有数据变化时才发射
    
    // 批量操作优化
    suspend fun updateUsers(updates: List<UserUpdate>): List<UpdateResult> = 
        withContext(ioDispatcher) {
            updates.chunked(10)  // 分批处理，每批10个
                .flatMap { batch ->
                    batch.map { update ->
                        async {
                            try {
                                val updatedUser = apiService.updateUser(update.userId, update.data)
                                localDatabase.updateUser(updatedUser)
                                UpdateResult.Success(update.userId, updatedUser)
                            } catch (e: Exception) {
                                UpdateResult.Error(update.userId, e.message)
                            }
                        }
                    }.awaitAll()
                }
        }
    
    private fun isCacheExpired(user: User): Boolean {
        val cacheAge = System.currentTimeMillis() - (user.cacheTime ?: 0)
        return cacheAge > TimeUnit.HOURS.toMillis(1)  // 1小时过期
    }
}

data class UserUpdate(val userId: String, val data: Map<String, Any>)
sealed class UpdateResult {
    data class Success(val userId: String, val user: User) : UpdateResult()
    data class Error(val userId: String, val message: String?) : UpdateResult()
}
```

### 5.8 协程面试常考题

#### 题目1：协程vs线程的区别

**问题：** 协程和线程有什么区别？协程是如何实现轻量级并发的？

**答案：**
```kotlin
// 协程vs线程对比演示
fun coroutineVsThread() {
    println("=== 协程vs线程对比 ===")
    
    // 线程方式 - 重量级
    val threadStartTime = System.currentTimeMillis()
    val threads = (1..1000).map { id ->
        Thread {
            Thread.sleep(100)
            println("线程$id 完成")
        }.apply { start() }
    }
    threads.forEach { it.join() }
    println("线程方式耗时: ${System.currentTimeMillis() - threadStartTime}ms")
    
    // 协程方式 - 轻量级
    runBlocking {
        val coroutineStartTime = System.currentTimeMillis()
        (1..1000).map { id ->
            async {
                delay(100)
                println("协程$id 完成")
            }
        }.awaitAll()
        println("协程方式耗时: ${System.currentTimeMillis() - coroutineStartTime}ms")
    }
}

// 协程的轻量级特性
fun demonstrateCoroutineLightweight() = runBlocking {
    // 创建100,000个协程（线程无法做到）
    val jobs = (1..100_000).map { id ->
        launch {
            delay(1000)
            if (id % 10000 == 0) println("协程$id 运行中")
        }
    }
    
    println("成功创建${jobs.size}个协程")
    jobs.forEach { it.cancel() }  // 取消所有协程
}
```

**要点总结：**
- **协程：** 用户态调度，轻量级，一个线程可以运行数万个协程
- **线程：** 内核态调度，重量级，创建成本高，上下文切换开销大
- **协程调度：** 通过挂起/恢复实现，不阻塞线程
- **内存占用：** 协程只需几KB，线程需要几MB栈空间

#### 题目2：挂起函数的原理

**问题：** suspend函数是如何工作的？编译器做了什么优化？

**答案：**
```kotlin
// 原始挂起函数
suspend fun fetchData(): String {
    delay(1000)
    return "数据"
}

// 编译器生成的等价代码（简化版）
fun fetchDataCompiled(continuation: Continuation<String>): Any {
    class FetchDataContinuation : Continuation<String> {
        var result: Any? = null
        var label = 0
        
        override fun resumeWith(result: Result<Any?>) {
            when (label) {
                0 -> {
                    label = 1
                    delay(1000, this)  // 传递continuation
                }
                1 -> {
                    return "数据"
                }
            }
        }
    }
    
    val cont = continuation as? FetchDataContinuation ?: FetchDataContinuation()
    return cont.resumeWith(Result.success(Unit))
}

// 状态机原理演示
suspend fun stateMachineExample(): String {
    println("状态0: 开始")
    delay(100)  // 挂起点1
    
    println("状态1: 第一次恢复")
    delay(100)  // 挂起点2
    
    println("状态2: 第二次恢复")
    return "完成"
}
```

**要点总结：**
- 挂起函数被编译为状态机，每个挂起点是一个状态
- Continuation负责保存和恢复执行状态
- 挂起时保存局部变量，恢复时还原上下文
- 整个过程不阻塞线程，实现真正的异步

#### 题目3：协程的异常处理

**问题：** 协程中的异常是如何传播的？如何正确处理异常？

**答案：**
```kotlin
fun coroutineExceptionHandling() = runBlocking {
    println("=== 协程异常处理 ===")
    
    // 1. 结构化并发中的异常传播
    try {
        coroutineScope {  // 子协程异常会取消兄弟协程
            launch {
                delay(100)
                throw RuntimeException("子协程1异常")
            }
            
            launch {
                delay(200)
                println("子协程2执行")  // 不会执行，因为兄弟协程异常
            }
        }
    } catch (e: Exception) {
        println("捕获异常: ${e.message}")
    }
    
    // 2. SupervisorJob - 异常隔离
    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    
    scope.launch {
        throw RuntimeException("独立协程异常")
    }
    
    scope.launch {
        delay(100)
        println("兄弟协程正常执行")  // 会正常执行
    }
    
    delay(200)
    
    // 3. 异常处理器
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("全局异常处理器: ${exception.message}")
    }
    
    val scopeWithHandler = CoroutineScope(Dispatchers.Default + exceptionHandler)
    scopeWithHandler.launch {
        throw RuntimeException("未捕获异常")
    }
    
    delay(100)
}

// async异常处理
suspend fun asyncExceptionHandling() = coroutineScope {
    try {
        val deferred = async {
            delay(100)
            throw RuntimeException("async中的异常")
        }
        
        deferred.await()  // 异常在这里抛出
    } catch (e: Exception) {
        println("捕获async异常: ${e.message}")
    }
}
```

#### 题目4：Flow vs RxJava

**问题：** Kotlin Flow和RxJava有什么区别？为什么选择Flow？

**答案：**
```kotlin
// Flow特性演示
fun flowVsRxJava() = runBlocking {
    println("=== Flow特性 ===")
    
    // 1. 冷流特性
    val coldFlow = flow {
        println("Flow开始执行")
        repeat(3) {
            emit(it)
            delay(100)
        }
    }
    
    println("第一次收集:")
    coldFlow.take(2).collect { println(it) }
    
    println("第二次收集:")
    coldFlow.collect { println(it) }
    
    // 2. 结构化并发
    coroutineScope {
        val job = launch {
            flow {
                repeat(100) {
                    emit(it)
                    delay(100)
                }
            }.collect { println("收集: $it") }
        }
        
        delay(500)
        job.cancel()  // 取消flow收集
        println("Flow已取消")
    }
    
    // 3. 异常透明性
    flow {
        emit(1)
        throw RuntimeException("Flow异常")
        emit(2)  // 不会执行
    }.catch { e ->
        println("捕获Flow异常: ${e.message}")
        emit(-1)  // 发射恢复值
    }.collect { println("接收: $it") }
}
```

**对比总结：**

| 特性 | Flow | RxJava |
|------|------|--------|
| **语言集成** | Kotlin原生 | Java/Kotlin兼容 |
| **协程集成** | 完美集成 | 需要适配 |
| **背压处理** | suspend机制 | 复杂的背压策略 |
| **异常处理** | 结构化异常 | onError回调 |
| **学习曲线** | 相对平缓 | 较陡峭 |
| **性能** | 轻量级 | 相对重量级 |

#### 题目5：协程取消和超时

**问题：** 如何正确取消协程？协程取消的原理是什么？

**答案：**
```kotlin
fun coroutineCancellation() = runBlocking {
    println("=== 协程取消 ===")
    
    // 1. 基本取消
    val job = launch {
        repeat(1000) { i ->
            println("工作中: $i")
            delay(100)
        }
    }
    
    delay(500)
    job.cancel()  // 取消协程
    job.join()    // 等待协程完成清理
    
    // 2. 取消检查
    val nonCooperativeJob = launch {
        repeat(1000) { i ->
            if (i % 100 == 0) {
                ensureActive()  // 检查取消状态
            }
            // 没有挂起点的CPU密集型工作
            Thread.sleep(10)
        }
    }
    
    delay(500)
    nonCooperativeJob.cancel()
    
    // 3. 资源清理
    val resourceJob = launch {
        try {
            repeat(1000) { i ->
                println("使用资源: $i")
                delay(100)
            }
        } finally {
            withContext(NonCancellable) {  // 防止清理被取消
                println("清理资源")
                delay(100)  // 模拟清理工作
            }
        }
    }
    
    delay(300)
    resourceJob.cancelAndJoin()
    
    // 4. 超时处理
    try {
        withTimeout(1000) {  // 1秒超时
            repeat(10) {
                println("工作: $it")
                delay(200)
            }
        }
    } catch (e: TimeoutCancellationException) {
        println("操作超时")
    }
    
    // 5. 可空超时
    val result = withTimeoutOrNull(800) {
        repeat(10) {
            delay(100)
        }
        "完成"
    }
    
    println("超时结果: $result")  // null表示超时
}
```

---

## 第5章小结

第5章我们深入学习了Kotlin协程的各个方面：

### 主要内容：
1. **协程基础：** 协程概念、挂起函数、协程构建器
2. **协程作用域：** CoroutineScope、协程上下文、调度器
3. **Flow数据流：** 创建、操作符、异常处理、背压
4. **Channel通道：** 基础使用、不同类型、实际应用
5. **最佳实践：** Android开发中的协程模式

### 面试重点：
- **协程原理：** 状态机实现、挂起恢复机制
- **异常处理：** 结构化异常传播、SupervisorJob、异常处理器
- **Flow特性：** 冷流、操作符、与RxJava对比
- **取消机制：** 协作式取消、资源清理、超时处理

下一章我们将学习Android开发中的Kotlin实践应用。

---

## 第6章 Android中的Kotlin实践

经过前面几章的学习，我们已经掌握了Kotlin的核心特性。现在让我们看看如何在实际的Android开发中应用这些知识，打造现代化、高效的Android应用。

### 6.1 现代Android架构

#### 6.1.1 MVVM + Data Binding + ViewModel

```kotlin
// 现代化的Activity实现
class MainActivity : AppCompatActivity() {
    
    // 使用by viewModels()委托自动创建ViewModel
    private val viewModel: MainViewModel by viewModels()
    
    // 使用by lazy延迟初始化DataBinding
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupBinding()
        observeViewModel()
        setupClickListeners()
        
        // 初始化数据加载
        viewModel.loadInitialData()
    }
    
    private fun setupBinding() {
        binding.apply {
            lifecycleOwner = this@MainActivity  // 设置生命周期感知
            viewModel = this@MainActivity.viewModel  // 绑定ViewModel
        }
    }
    
    private fun observeViewModel() {
        // 观察UI状态
        viewModel.uiState.observe(this) { state ->
            handleUiState(state)
        }
        
        // 观察一次性事件
        viewModel.events.observe(this) { event ->
            handleEvent(event)
        }
        
        // 使用Flow + lifecycleScope
        lifecycleScope.launch {
            viewModel.userFlow.flowWithLifecycle(lifecycle)
                .collect { user ->
                    updateUserInfo(user)
                }
        }
    }
    
    private fun handleUiState(state: MainUiState) {
        when (state) {
            MainUiState.Loading -> {
                binding.progressBar.isVisible = true
                binding.contentGroup.isVisible = false
            }
            is MainUiState.Success -> {
                binding.progressBar.isVisible = false
                binding.contentGroup.isVisible = true
            }
            is MainUiState.Error -> {
                binding.progressBar.isVisible = false
                showError(state.message)
            }
        }
    }
    
    private fun handleEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ShowToast -> {
                this.toast(event.message)
            }
            is MainEvent.NavigateToDetail -> {
                startActivity(DetailActivity.createIntent(this, event.itemId))
            }
            MainEvent.ShowLoading -> {
                // 显示加载对话框
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            viewModel.onAddClicked()
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.refresh()
        }
    }
    
    private fun updateUserInfo(user: User?) {
        user?.let {
            binding.textUserName.text = it.name
            binding.textUserEmail.text = it.email
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("重试") { viewModel.retry() }
            .show()
    }
}

// ViewModel的现代化实现
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // 使用StateFlow管理UI状态
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 使用SharedFlow处理一次性事件
    private val _events = MutableSharedFlow<MainEvent>()
    val events: LiveData<MainEvent> = _events.asLiveData()
    
    // 用户数据流
    val userFlow: Flow<User?> = repository.userFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 列表数据 - 使用Paging3
    val itemsFlow: Flow<PagingData<Item>> = repository.getItemsPaged()
        .cachedIn(viewModelScope)
    
    fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = MainUiState.Loading
                
                // 并行加载多个数据源
                val userData = async { repository.getCurrentUser() }
                val settingsData = async { repository.getSettings() }
                
                val user = userData.await()
                val settings = settingsData.await()
                
                _uiState.value = MainUiState.Success(user, settings)
                
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    fun onAddClicked() {
        viewModelScope.launch {
            _events.emit(MainEvent.NavigateToDetail("new"))
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            try {
                _events.emit(MainEvent.ShowLoading)
                repository.refreshData()
                _events.emit(MainEvent.ShowToast("刷新成功"))
            } catch (e: Exception) {
                _events.emit(MainEvent.ShowToast("刷新失败: ${e.message}"))
            }
        }
    }
    
    fun retry() {
        loadInitialData()
    }
}

// UI状态密封类
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val user: User, val settings: Settings) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

// 事件密封类
sealed class MainEvent {
    data class ShowToast(val message: String) : MainEvent
    data class NavigateToDetail(val itemId: String) : MainEvent
    object ShowLoading : MainEvent
}
```

#### 6.1.2 Fragment的现代化实现

```kotlin
@AndroidEntryPoint
class ItemListFragment : Fragment() {
    
    private val viewModel: ItemListViewModel by viewModels()
    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!
    
    // 列表适配器
    private val adapter by lazy {
        ItemListAdapter(
            onItemClick = { item -> viewModel.onItemSelected(item) },
            onItemLongClick = { item -> viewModel.onItemLongPressed(item) }
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
        setupSearch()
        
        // 恢复状态
        savedInstanceState?.let {
            val scrollPosition = it.getInt("scroll_position", 0)
            binding.recyclerView.scrollToPosition(scrollPosition)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存滚动位置
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        outState.putInt("scroll_position", layoutManager.findFirstVisibleItemPosition())
    }
    
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@ItemListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            
            // 添加分割线
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
            
            // 添加滚动监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // 向上滚动时隐藏FAB，向下滚动时显示
                    if (dy > 0) {
                        binding.fab.hide()
                    } else if (dy < 0) {
                        binding.fab.show()
                    }
                }
            })
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.color_primary,
                R.color.color_secondary,
                R.color.color_accent
            )
            
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }
    
    private fun observeViewModel() {
        // 观察分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        
        // 观察加载状态
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
                
                // 处理错误状态
                if (loadState.refresh is LoadState.Error) {
                    val error = loadState.refresh as LoadState.Error
                    showError(error.error.message ?: "加载失败")
                }
            }
        }
        
        // 观察UI事件
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ItemListEvent.ShowItemDetail -> {
                    findNavController().navigate(
                        ItemListFragmentDirections.actionToItemDetail(event.itemId)
                    )
                }
                is ItemListEvent.ShowDeleteConfirmation -> {
                    showDeleteConfirmationDialog(event.item)
                }
                is ItemListEvent.ShowMessage -> {
                    view?.let { 
                        Snackbar.make(it, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // 观察搜索结果
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            updateSearchResults(results)
        }
    }
    
    private fun setupSearch() {
        binding.searchView.apply {
            // 设置搜索监听
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.search(it) }
                    return true
                }
                
                override fun onQueryTextChange(newText: String?): Boolean {
                    // 实时搜索（防抖）
                    viewModel.onSearchQueryChanged(newText.orEmpty())
                    return true
                }
            })
            
            // 搜索建议
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean = false
                
                override fun onSuggestionClick(position: Int): Boolean {
                    // 处理搜索建议点击
                    return true
                }
            })
        }
    }
    
    private fun showDeleteConfirmationDialog(item: Item) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除「${item.title}」吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun updateSearchResults(results: List<Item>) {
        // 更新搜索结果UI
        if (results.isEmpty()) {
            binding.emptyView.isVisible = true
            binding.recyclerView.isVisible = false
        } else {
            binding.emptyView.isVisible = false
            binding.recyclerView.isVisible = true
        }
    }
    
    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction("重试") { viewModel.retry() }
                .show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### 6.2 RecyclerView的现代化实现

#### 6.2.1 高效的ListAdapter

```kotlin
class ItemListAdapter(
    private val onItemClick: (Item) -> Unit,
    private val onItemLongClick: (Item) -> Unit
) : ListAdapter<Item, ItemListAdapter.ItemViewHolder>(ItemDiffCallback()) {
    
    // ViewHolder实现
    class ItemViewHolder(
        private val binding: ItemListItemBinding,
        private val onItemClick: (Item) -> Unit,
        private val onItemLongClick: (Item) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Item) {
            binding.apply {
                this.item = item  // DataBinding
                executePendingBindings()
                
                // 设置点击监听
                root.setOnClickListener { onItemClick(item) }
                root.setOnLongClickListener { 
                    onItemLongClick(item)
                    true
                }
                
                // 加载图片
                loadImage(item.imageUrl)
                
                // 设置标签
                setupTags(item.tags)
            }
        }
        
        private fun loadImage(imageUrl: String?) {
            Glide.with(binding.imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .transform(RoundedCorners(8.dp))
                .into(binding.imageView)
        }
        
        private fun setupTags(tags: List<String>) {
            binding.tagContainer.removeAllViews()
            tags.take(3).forEach { tag ->  // 最多显示3个标签
                val tagView = createTagView(tag)
                binding.tagContainer.addView(tagView)
            }
        }
        
        private fun createTagView(tag: String): View {
            return LayoutInflater.from(binding.root.context)
                .inflate(R.layout.item_tag, binding.tagContainer, false)
                .apply {
                    findViewById<TextView>(R.id.text_tag).text = tag
                }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, onItemClick, onItemLongClick)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    // 支持部分更新
    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = getItem(position)
            // 根据payload进行部分更新
            payloads.forEach { payload ->
                when (payload) {
                    "title" -> holder.binding.textTitle.text = item.title
                    "status" -> updateItemStatus(holder, item)
                }
            }
        }
    }
    
    private fun updateItemStatus(holder: ItemViewHolder, item: Item) {
        holder.binding.apply {
            statusIndicator.isVisible = item.status != ItemStatus.NORMAL
            statusIndicator.setColorFilter(
                ContextCompat.getColor(root.context, item.status.colorRes)
            )
        }
    }
}

// 高效的DiffCallback
class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
    
    // 返回变更的字段，支持部分更新
    override fun getChangePayload(oldItem: Item, newItem: Item): Any? {
        val changes = mutableListOf<String>()
        
        if (oldItem.title != newItem.title) changes.add("title")
        if (oldItem.status != newItem.status) changes.add("status")
        if (oldItem.imageUrl != newItem.imageUrl) changes.add("image")
        
        return changes.takeIf { it.isNotEmpty() }
    }
}
```

#### 6.2.2 Paging3集成

```kotlin
// Paging3的Repository实现
@Singleton
class ItemRepository @Inject constructor(
    private val apiService: ItemApiService,
    private val database: ItemDatabase
) {
    
    @OptIn(ExperimentalPagingApi::class)
    fun getItemsPaged(): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ItemRemoteMediator(apiService, database),
            pagingSourceFactory = { database.itemDao().getAllPaged() }
        ).flow
    }
    
    // 搜索分页
    fun searchItemsPaged(query: String): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { ItemSearchPagingSource(apiService, query) }
        ).flow
    }
}

// RemoteMediator实现缓存策略
@OptIn(ExperimentalPagingApi::class)
class ItemRemoteMediator(
    private val apiService: ItemApiService,
    private val database: ItemDatabase
) : RemoteMediator<Int, Item>() {
    
    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.HOURS.toMillis(1)
        return if (System.currentTimeMillis() - getLastRefreshTime() < cacheTimeout) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Item>
    ): MediatorResult {
        return try {
            val pageKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    lastItem?.nextPageKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            
            val response = apiService.getItems(page = pageKey, size = state.config.pageSize)
            val items = response.items
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.itemDao().clearAll()
                    saveLastRefreshTime()
                }
                
                database.itemDao().insertAll(items)
            }
            
            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
            
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
    
    private suspend fun getLastRefreshTime(): Long {
        return database.refreshTimeDao().getRefreshTime("items") ?: 0
    }
    
    private suspend fun saveLastRefreshTime() {
        database.refreshTimeDao().insertOrUpdate(
            RefreshTime("items", System.currentTimeMillis())
        )
    }
}

// PagingDataAdapter
class ItemPagingAdapter(
    private val onItemClick: (Item) -> Unit
) : PagingDataAdapter<Item, ItemPagingAdapter.ItemViewHolder>(ItemDiffCallback()) {
    
    class ItemViewHolder(
        private val binding: ItemListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Item?, onItemClick: (Item) -> Unit) {
            item?.let {
                binding.item = it
                binding.executePendingBindings()
                binding.root.setOnClickListener { onItemClick(it) }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}
```

### 6.3 网络请求与数据管理

#### 6.3.1 Retrofit + 协程的现代化实现

```kotlin
// API接口定义
interface ItemApiService {
    
    @GET("items")
    suspend fun getItems(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("category") category: String? = null
    ): ApiResponse<List<Item>>
    
    @GET("items/{id}")
    suspend fun getItem(@Path("id") itemId: String): ApiResponse<Item>
    
    @POST("items")
    suspend fun createItem(@Body item: CreateItemRequest): ApiResponse<Item>
    
    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") itemId: String,
        @Body item: UpdateItemRequest
    ): ApiResponse<Item>
    
    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") itemId: String): ApiResponse<Unit>
    
    // 文件上传
    @Multipart
    @POST("items/{id}/image")
    suspend fun uploadImage(
        @Path("id") itemId: String,
        @Part image: MultipartBody.Part
    ): ApiResponse<ImageUploadResponse>
    
    // 批量操作
    @POST("items/batch")
    suspend fun batchUpdateItems(
        @Body requests: List<BatchUpdateRequest>
    ): ApiResponse<List<Item>>
}

// 网络响应封装
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val success: Boolean
) {
    fun isSuccess(): Boolean = success && code == 200
    
    fun dataOrThrow(): T {
        if (isSuccess() && data != null) {
            return data
        } else {
            throw ApiException(code, message)
        }
    }
}

// 自定义异常
class ApiException(val code: Int, message: String) : Exception(message)

// 网络请求包装器
@Singleton
class NetworkManager @Inject constructor(
    private val apiService: ItemApiService
) {
    
    // 统一的网络请求包装
    suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(ApiException(response.code, response.message))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException("网络连接失败"))
        } catch (e: HttpException) {
            Result.failure(ApiException(e.code(), "服务器错误"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 带重试机制的网络请求
    suspend fun <T> safeApiCallWithRetry(
        maxRetries: Int = 3,
        delayMillis: Long = 1000,
        apiCall: suspend () -> ApiResponse<T>
    ): Result<T> {
        repeat(maxRetries) { attempt ->
            val result = safeApiCall(apiCall)
            if (result.isSuccess) {
                return result
            }
            
            if (attempt < maxRetries - 1) {
                delay(delayMillis * (attempt + 1))  // 指数退避
            }
        }
        
        return safeApiCall(apiCall)  // 最后一次尝试
    }
    
    // 文件上传
    suspend fun uploadImage(itemId: String, imageUri: Uri, context: Context): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(IllegalArgumentException("无法读取文件"))
            
            val requestFile = inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
            
            val response = apiService.uploadImage(itemId, imagePart)
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data.imageUrl)
            } else {
                Result.failure(ApiException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class NetworkException(message: String) : Exception(message)
```

#### 6.3.2 Room数据库集成

```kotlin
// Entity定义
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false
) {
    // 转换为业务模型
    fun toItem(): Item {
        return Item(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            category = ItemCategory.valueOf(category),
            status = ItemStatus.valueOf(status),
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            isFavorite = isFavorite
        )
    }
}

// DAO接口
@Dao
interface ItemDao {
    
    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    fun getAllPaged(): PagingSource<Int, ItemEntity>
    
    @Query("SELECT * FROM items WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteItems(): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE category = :category ORDER BY updatedAt DESC")
    fun getItemsByCategory(category: String): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchItems(query: String): List<ItemEntity>
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): ItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)
    
    @Update
    suspend fun updateItem(item: ItemEntity)
    
    @Delete
    suspend fun deleteItem(item: ItemEntity)
    
    @Query("DELETE FROM items")
    suspend fun clearAll()
    
    @Query("UPDATE items SET isFavorite = :isFavorite WHERE id = :itemId")
    suspend fun updateFavoriteStatus(itemId: String, isFavorite: Boolean)
    
    // 复杂查询
    @Query("""
        SELECT * FROM items 
        WHERE (:category IS NULL OR category = :category)
        AND (:isFavorite IS NULL OR isFavorite = :isFavorite)
        AND (:query IS NULL OR title LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN :sortBy = 'title' THEN title END ASC,
            CASE WHEN :sortBy = 'date' THEN updatedAt END DESC
    """)
    fun getFilteredItems(
        category: String?,
        isFavorite: Boolean?,
        query: String?,
        sortBy: String
    ): PagingSource<Int, ItemEntity>
}

// 数据库定义
@Database(
    entities = [ItemEntity::class, RefreshTime::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun refreshTimeDao(): RefreshTimeDao
}

// 类型转换器
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}
```

### 6.4 依赖注入与架构组件

#### 6.4.1 Hilt依赖注入

```kotlin
// Application类
@HiltAndroidApp
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // 初始化Crash收集
        setupCrashReporting()
    }
    
    private fun setupCrashReporting() {
        // 设置未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Timber.e(exception, "未捕获的异常: ${thread.name}")
            // 发送崩溃报告
        }
    }
}

// 网络模块
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .addInterceptor(AuthInterceptor())  // 认证拦截器
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideItemApiService(retrofit: Retrofit): ItemApiService {
        return retrofit.create(ItemApiService::class.java)
    }
}

// 数据库模块
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideItemDatabase(@ApplicationContext context: Context): ItemDatabase {
        return Room.databaseBuilder(
            context,
            ItemDatabase::class.java,
            "item_database"
        )
        .addMigrations(MIGRATION_1_2)  // 数据库迁移
        .fallbackToDestructiveMigration()  // 开发阶段可以使用
        .build()
    }
    
    @Provides
    fun provideItemDao(database: ItemDatabase): ItemDao {
        return database.itemDao()
    }
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE items ADD COLUMN isFavorite INTEGER DEFAULT 0 NOT NULL")
        }
    }
}

// Repository模块
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideItemRepository(
        apiService: ItemApiService,
        itemDao: ItemDao,
        networkManager: NetworkManager
    ): ItemRepository {
        return ItemRepositoryImpl(apiService, itemDao, networkManager)
    }
}

// 认证拦截器
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = preferencesManager.getAuthToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(newRequest)
        
        // 处理401未授权
        if (response.code == 401) {
            preferencesManager.clearAuthToken()
            // 发送登录失效事件
            // EventBus.post(AuthExpiredEvent())
        }
        
        return response
    }
}
```

#### 6.4.2 偏好设置管理

```kotlin
// DataStore偏好设置
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    // 认证相关
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }
    
    fun getAuthToken(): String? = runBlocking {
        dataStore.data.first()[AUTH_TOKEN_KEY]
    }
    
    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
    
    // 用户设置
    val userSettingsFlow: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            isDarkMode = preferences[DARK_MODE_KEY] ?: false,
            language = preferences[LANGUAGE_KEY] ?: "zh",
            notificationEnabled = preferences[NOTIFICATION_KEY] ?: true,
            autoSync = preferences[AUTO_SYNC_KEY] ?: true
        )
    }
    
    suspend fun updateDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }
    
    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    
    // 缓存设置
    suspend fun saveLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME_KEY] = timestamp
        }
    }
    
    fun getLastSyncTime(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME_KEY] ?: 0L
    }
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val NOTIFICATION_KEY = booleanPreferencesKey("notification_enabled")
        private val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync")
        private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    }
}

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

data class UserSettings(
    val isDarkMode: Boolean,
    val language: String,
    val notificationEnabled: Boolean,
    val autoSync: Boolean
)
```

### 6.5 Jetpack Compose集成

#### 6.5.1 Compose与传统View的混合使用

```kotlin
// 在传统Activity中使用Compose
class ComposeIntegrationActivity : AppCompatActivity() {
    
    private val viewModel: ComposeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_compose_integration)
        
        // 在XML中使用ComposeView
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            AppTheme {
                ItemListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
        
        // 传统View组件
        setupTraditionalViews()
    }
    
    private fun setupTraditionalViews() {
        findViewById<TextView>(R.id.title).text = "混合界面示例"
        findViewById<Button>(R.id.btn_action).setOnClickListener {
            viewModel.performAction()
        }
    }
}

// Compose屏幕实现
@Composable
fun ItemListScreen(
    viewModel: ComposeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()
    
    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onItemClick = { viewModel.selectItem(it) }
                        )
                    }
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = uiState.message,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        // 浮动操作按钮
        FloatingActionButton(
            onClick = { viewModel.addNewItem() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加")
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 异步加载图片
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(R.drawable.placeholder_image),
                error = painterResource(R.drawable.error_image),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // 状态标签
                if (item.status != ItemStatus.NORMAL) {
                    StatusBadge(
                        status = item.status,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // 收藏按钮
            IconButton(
                onClick = { 
                    // 切换收藏状态
                }
            ) {
                Icon(
                    imageVector = if (item.isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "收藏",
                    tint = if (item.isFavorite) Color.Red else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        ItemStatus.URGENT -> Triple(Color.Red, Color.White, "紧急")
        ItemStatus.IMPORTANT -> Triple(Color.Orange, Color.White, "重要")
        ItemStatus.COMPLETED -> Triple(Color.Green, Color.White, "已完成")
        ItemStatus.NORMAL -> Triple(Color.Transparent, Color.Transparent, "")
    }
    
    if (status != ItemStatus.NORMAL) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
        ) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

### 6.6 Android面试常考题

#### 题目1：ViewModel的生命周期

**问题：** ViewModel的生命周期是怎样的？它如何在配置更改时保持数据？

**答案：**
```kotlin
// ViewModel生命周期演示
class DemoViewModel : ViewModel() {
    
    init {
        println("ViewModel: 创建")
    }
    
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
    
    fun updateData(newData: String) {
        _data.value = newData
        println("ViewModel: 数据更新为 $newData")
    }
    
    override fun onCleared() {
        super.onCleared()
        println("ViewModel: 销毁，清理资源")
        // 取消协程、清理监听器等
    }
}

class DemoActivity : AppCompatActivity() {
    private val viewModel: DemoViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Activity: onCreate")
        
        viewModel.data.observe(this) { data ->
            println("Activity: 观察到数据变化 $data")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        println("Activity: onDestroy")
    }
}

// 屏幕旋转测试
// 1. Activity onCreate -> ViewModel 创建 -> 观察数据
// 2. 旋转屏幕 -> Activity onDestroy -> Activity onCreate -> ViewModel复用 -> 观察数据
// 3. 按返回键 -> Activity onDestroy -> ViewModel onCleared
```

**要点总结：**
- ViewModel生命周期长于Activity/Fragment
- 配置更改（如旋转）时ViewModel不会被销毁
- 只有在Activity真正结束时才调用onCleared()
- 通过ViewModelStore和ViewModelProvider实现

#### 题目2：LiveData vs StateFlow

**问题：** LiveData和StateFlow有什么区别？什么时候使用哪个？

**答案：**
```kotlin
class DataComparisonViewModel : ViewModel() {
    
    // LiveData方式
    private val _liveData = MutableLiveData<String>("初始值")
    val liveData: LiveData<String> = _liveData
    
    // StateFlow方式
    private val _stateFlow = MutableStateFlow("初始值")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()
    
    fun updateData(value: String) {
        _liveData.value = value    // 主线程
        _stateFlow.value = value   // 任何线程
    }
    
    fun updateDataBackground(value: String) {
        viewModelScope.launch {
            // LiveData需要postValue
            _liveData.postValue(value)
            // StateFlow直接赋值
            _stateFlow.value = value
        }
    }
}

// 使用对比
class ComparisonActivity : AppCompatActivity() {
    private val viewModel: DataComparisonViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // LiveData观察（生命周期感知）
        viewModel.liveData.observe(this) { value ->
            println("LiveData: $value")
            // 只在活跃状态下接收更新
        }
        
        // StateFlow观察（需要手动管理生命周期）
        lifecycleScope.launch {
            viewModel.stateFlow.flowWithLifecycle(lifecycle)
                .collect { value ->
                    println("StateFlow: $value")
                }
        }
    }
}
```

**对比总结：**

| 特性 | LiveData | StateFlow |
|------|----------|-----------|
| **生命周期感知** | 自动 | 需要手动处理 |
| **初始值** | 可选 | 必须 |
| **线程安全** | setValue主线程，postValue任意线程 | 任意线程 |
| **操作符** | Transformations | Flow操作符 |
| **协程集成** | 一般 | 完美 |
| **性能** | 较重 | 轻量级 |

#### 题目3：数据库迁移策略

**问题：** Room数据库版本升级时如何进行数据迁移？

**答案：**
```kotlin
// 数据库迁移示例
@Database(
    entities = [User::class, UserProfile::class],
    version = 3,  // 当前版本
    exportSchema = true  // 导出schema用于测试
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        // 版本1到版本2：添加新字段
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加新列
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN phone TEXT"
                )
            }
        }
        
        // 版本2到版本3：创建新表
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新表
                database.execSQL("""
                    CREATE TABLE user_profiles (
                        id INTEGER PRIMARY KEY NOT NULL,
                        userId INTEGER NOT NULL,
                        avatar TEXT,
                        bio TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id)
                    )
                """)
                
                // 创建索引
                database.execSQL(
                    "CREATE INDEX index_user_profiles_userId ON user_profiles(userId)"
                )
            }
        }
        
        // 复杂迁移：重建表
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 创建新表
                database.execSQL("""
                    CREATE TABLE users_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        phone TEXT,
                        created_at INTEGER NOT NULL
                    )
                """)
                
                // 2. 复制数据
                database.execSQL("""
                    INSERT INTO users_new (id, username, email, phone, created_at)
                    SELECT id, name, email, phone, 0 FROM users
                """)
                
                // 3. 删除旧表
                database.execSQL("DROP TABLE users")
                
                // 4. 重命名新表
                database.execSQL("ALTER TABLE users_new RENAME TO users")
                
                // 5. 重建索引
                database.execSQL("CREATE INDEX index_users_email ON users(email)")
            }
        }
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                // 开发阶段可以使用，生产环境谨慎使用
                // .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 迁移测试
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // 插入测试数据
            execSQL("INSERT INTO users (id, name, email) VALUES (1, 'Test', 'test@example.com')")
            close()
        }
        
        // 执行迁移
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        
        // 验证迁移结果
        db.query("SELECT * FROM users").use { cursor ->
            assert(cursor.columnCount == 4)  // id, name, email, phone
            assert(cursor.moveToFirst())
            assert(cursor.getString(cursor.getColumnIndex("name")) == "Test")
        }
    }
    
    companion object {
        private const val TEST_DB = "migration-test"
    }
}
```

#### 题目4：内存泄漏预防

**问题：** Android开发中常见的内存泄漏场景有哪些？如何预防？

**答案：**
```kotlin
// 1. Handler内存泄漏 - 错误示例
class BadActivity : AppCompatActivity() {
    
    private val handler = Handler(Looper.getMainLooper()) // 持有Activity引用
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 延迟任务可能导致内存泄漏
        handler.postDelayed({
            // 如果Activity已经销毁，这里仍持有引用
            updateUI()
        }, 60000)
    }
    
    private fun updateUI() {
        // 更新UI
    }
}

// 正确做法
class GoodActivity : AppCompatActivity() {
    
    private val handler = Handler(Looper.getMainLooper())
    
    // 使用静态内部类 + 弱引用
    private class SafeRunnable(activity: GoodActivity) : Runnable {
        private val activityRef = WeakReference(activity)
        
        override fun run() {
            activityRef.get()?.updateUI()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val runnable = SafeRunnable(this)
        handler.postDelayed(runnable, 60000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)  // 清理所有回调
    }
    
    private fun updateUI() {
        // 更新UI
    }
}

// 2. 观察者模式内存泄漏
class EventManager {
    private val listeners = mutableListOf<EventListener>()
    
    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }
    
    fun notifyEvent(event: String) {
        listeners.forEach { it.onEvent(event) }
    }
}

interface EventListener {
    fun onEvent(event: String)
}

// 错误示例 - 忘记注销监听器
class BadFragment : Fragment(), EventListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventManager.getInstance().addListener(this)  // 注册但忘记注销
    }
    
    override fun onEvent(event: String) {
        // 处理事件
    }
    
    // 忘记注销，导致Fragment无法被GC回收
}

// 正确示例
class GoodFragment : Fragment(), EventListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventManager.getInstance().addListener(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        EventManager.getInstance().removeListener(this)  // 及时注销
    }
    
    override fun onEvent(event: String) {
        // 处理事件
    }
}

// 3. 协程内存泄漏预防
class CoroutineActivity : AppCompatActivity() {
    
    // 错误：使用GlobalScope
    fun badCoroutineUsage() {
        GlobalScope.launch {
            delay(10000)
            updateUI()  // Activity可能已经销毁
        }
    }
    
    // 正确：使用lifecycleScope
    fun goodCoroutineUsage() {
        lifecycleScope.launch {
            delay(10000)
            updateUI()  // 生命周期结束时自动取消
        }
    }
    
    private fun updateUI() {
        // 更新UI
    }
}

// 4. 使用LeakCanary检测内存泄漏
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            // 自动检测内存泄漏
            LeakCanary.install(this)
        }
    }
}

// 内存泄漏检测工具类
object LeakDetector {
    
    fun detectLeaks(activity: Activity) {
        if (BuildConfig.DEBUG) {
            // 手动触发内存泄漏检测
            LeakCanary.dumpHeap()
        }
    }
    
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        
        Timber.d("内存使用: ${usedMemory}MB / ${maxMemory}MB")
    }
}
```

**内存泄漏预防总结：**
- **及时清理：** 在适当的生命周期方法中注销监听器、取消任务
- **使用弱引用：** 对于可能长期持有的引用使用WeakReference
- **选择合适的作用域：** 协程使用lifecycleScope而非GlobalScope
- **避免静态引用：** 静态变量持有Context引用
- **使用工具检测：** LeakCanary等工具帮助发现内存泄漏

#### 题目5：性能优化策略

**问题：** Android应用性能优化有哪些方面？具体如何实施？

**答案：**
```kotlin
// 1. 启动优化
class OptimizedApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 延迟初始化非关键组件
        initCriticalComponents()        // 同步初始化关键组件
        initNonCriticalAsync()         // 异步初始化非关键组件
        preloadResources()             // 预加载资源
    }
    
    private fun initCriticalComponents() {
        // 只初始化必需的组件
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else ReleaseTree())
    }
    
    private fun initNonCriticalAsync() {
        // 在后台线程初始化
        Thread {
            // 初始化分析SDK
            // 初始化图片加载库
            // 预连接数据库
        }.start()
    }
    
    private fun preloadResources() {
        // 预加载关键类，避免首次使用时的类加载开销
        Thread {
            try {
                Class.forName("com.example.MainActivity")
                Class.forName("com.example.SplashActivity")
            } catch (e: ClassNotFoundException) {
                Timber.w("类预加载失败: ${e.message}")
            }
        }.start()
    }
}

// 2. 列表性能优化
class OptimizedAdapter : ListAdapter<Item, OptimizedAdapter.ViewHolder>(ItemDiffCallback()) {
    
    // ViewHolder复用池
    private val viewHolderPool = RecycledViewPool()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        
        // 预加载下一批图片
        preloadImages(position)
    }
    
    private fun preloadImages(position: Int) {
        val preloadRange = 5
        for (i in position + 1..minOf(position + preloadRange, itemCount - 1)) {
            if (i < itemCount) {
                val item = getItem(i)
                Glide.with(context)
                    .load(item.imageUrl)
                    .preload()
            }
        }
    }
    
    class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Item) {
            // 使用DataBinding减少findViewById
            binding.item = item
            binding.executePendingBindings()
            
            // 图片加载优化
            loadImageOptimized(item.imageUrl)
        }
        
        private fun loadImageOptimized(imageUrl: String?) {
            Glide.with(binding.imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // 缓存策略
                .skipMemoryCache(false)                    // 内存缓存
                .into(binding.imageView)
        }
    }
}

// 3. 网络性能优化
@Singleton
class OptimizedNetworkManager @Inject constructor() {
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))  // 连接池
            .addInterceptor(createCacheInterceptor())                // 缓存拦截器
            .addInterceptor(createCompressionInterceptor())          // 压缩拦截器
            .build()
    }
    
    private fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            
            // 根据网络状态决定缓存策略
            if (!isNetworkAvailable()) {
                request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build()
            }
            
            val response = chain.proceed(request)
            
            if (isNetworkAvailable()) {
                // 网络可用时的缓存策略
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=60")
                    .build()
            } else {
                // 网络不可用时使用缓存
                response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=86400")
                    .build()
            }
        }
    }
    
    private fun createCompressionInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept-Encoding", "gzip")
                .build()
            chain.proceed(request)
        }
    }
    
    // 批量请求优化
    suspend fun batchRequest(requests: List<ApiRequest>): List<ApiResponse> {
        return requests.chunked(5).flatMap { chunk ->  // 分批处理
            chunk.map { request ->
                async { executeRequest(request) }
            }.awaitAll()
        }
    }
}

// 4. 内存性能优化
class MemoryOptimizer {
    
    // 对象池减少GC压力
    private val stringBuilderPool = object : Pools.Pool<StringBuilder> {
        private val pool = Pools.SimplePool<StringBuilder>(10)
        
        override fun acquire(): StringBuilder {
            return pool.acquire() ?: StringBuilder()
        }
        
        override fun release(instance: StringBuilder): Boolean {
            instance.setLength(0)  // 重置状态
            return pool.release(instance)
        }
    }
    
    fun buildString(block: StringBuilder.() -> Unit): String {
        val sb = stringBuilderPool.acquire()
        try {
            sb.block()
            return sb.toString()
        } finally {
            stringBuilderPool.release(sb)
        }
    }
    
    // 图片内存优化
    fun optimizeImageMemory(imageView: ImageView, imageUrl: String) {
        val width = imageView.measuredWidth
        val height = imageView.measuredHeight
        
        if (width > 0 && height > 0) {
            Glide.with(imageView.context)
                .load(imageUrl)
                .override(width, height)  // 按View尺寸加载
                .format(DecodeFormat.PREFER_RGB_565)  // 使用RGB_565格式减少内存
                .into(imageView)
        }
    }
    
    // 监控内存使用
    fun monitorMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercentage = (usedMemory.toFloat() / maxMemory) * 100
        
        if (usagePercentage > 80) {
            Timber.w("内存使用率过高: $usagePercentage%")
            // 触发内存清理
            System.gc()  // 建议GC
        }
    }
}

// 5. UI渲染优化
class RenderOptimizer {
    
    // 避免过度绘制
    fun optimizeLayoutHierarchy(viewGroup: ViewGroup) {
        // 减少嵌套层次
        // 使用ConstraintLayout替代多层嵌套
        // 合理使用ViewStub延迟加载
    }
    
    // 异步布局inflation
    fun inflateAsync(
        context: Context,
        @LayoutRes layoutRes: Int,
        parent: ViewGroup?,
        callback: (View) -> Unit
    ) {
        AsyncLayoutInflater(context).inflate(layoutRes, parent) { view, _, _ ->
            callback(view)
        }
    }
    
    // RecyclerView优化
    fun optimizeRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)  // 固定尺寸优化
            setItemViewCacheSize(20)  // 增加View缓存
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }
    }
}
```

---

## 第6章小结

第6章我们学习了Android开发中的Kotlin实践：

### 主要内容：
1. **现代架构：** MVVM + Data Binding + ViewModel的完整实现
2. **Fragment实践：** 生命周期管理、状态保存、事件处理
3. **RecyclerView优化：** ListAdapter、Paging3、ViewHolder复用
4. **网络数据管理：** Retrofit协程集成、Room数据库、Repository模式
5. **依赖注入：** Hilt的使用、模块化设计
6. **Compose集成：** 与传统View的混合使用

### 面试重点：
- **ViewModel生命周期：** 配置更改时的数据保持机制
- **LiveData vs StateFlow：** 使用场景和性能对比
- **数据库迁移：** Room版本升级策略
- **内存泄漏预防：** 常见场景和解决方案
- **性能优化：** 启动优化、列表优化、内存优化、渲染优化

## 全书总结

通过六章的学习，我们全面掌握了Kotlin在Android开发中的应用：

### 技术体系：
1. **语言基础：** 变量、函数、控制流程
2. **面向对象：** 类、继承、接口、数据类、密封类
3. **特有特性：** 空安全、扩展函数、高阶函数、作用域函数
4. **集合泛型：** List/Set/Map操作、序列优化、型变机制
5. **协程编程：** 挂起函数、Flow、Channel、异步编程
6. **Android实践：** 现代架构、组件使用、性能优化

### 面试准备：
- **基础概念理解**：每个特性的原理和使用场景
- **实践经验展示**：真实项目中的应用案例
- **性能优化意识**：代码质量和性能考虑
- **问题解决能力**：常见问题的分析和解决方案

这套教程涵盖了从Kotlin基础到Android高级应用的完整知识体系，既适合学习使用，也是面试准备的绝佳资料。希望能帮助你在Kotlin和Android开发的道路上越走越远！