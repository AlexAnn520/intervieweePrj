# 第一行代码 Kotlin版 - 性能优化与最佳实践篇

> 掌握Kotlin性能优化技巧，构建高性能Android应用

## 目录

- [第11章 性能优化与最佳实践](#第11章-性能优化与最佳实践)
- [第12章 实战项目：完整Android应用](#第12章-实战项目完整android应用)

---

## 第11章 性能优化与最佳实践

性能优化是高级Android开发工程师必须掌握的核心技能。本章将深入探讨Kotlin在Android开发中的各种性能优化策略。

### 11.1 内存管理与优化

#### 11.1.1 内存分配模式优化

```kotlin
/**
 * 对象池模式 - 减少频繁的内存分配
 */
class ObjectPoolManager<T : Recyclable> {
    private val pool = mutableListOf<T>()
    private val maxSize: Int
    private val factory: () -> T

    constructor(maxSize: Int, factory: () -> T) {
        this.maxSize = maxSize
        this.factory = factory
    }

    /**
     * 获取对象实例
     */
    @Synchronized
    fun acquire(): T {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            factory()
        }
    }

    /**
     * 归还对象到池中
     */
    @Synchronized
    fun release(obj: T) {
        if (pool.size < maxSize) {
            obj.reset()
            pool.add(obj)
        }
    }

    /**
     * 清空对象池
     */
    @Synchronized
    fun clear() {
        pool.clear()
    }

    /**
     * 使用对象并自动归还
     */
    inline fun <R> use(action: (T) -> R): R {
        val obj = acquire()
        try {
            return action(obj)
        } finally {
            release(obj)
        }
    }
}

/**
 * 可回收对象接口
 */
interface Recyclable {
    fun reset()
}

/**
 * StringBuilder对象池的具体应用
 */
object StringBuilderPool : ObjectPoolManager<RecyclableStringBuilder>(
    maxSize = 50,
    factory = { RecyclableStringBuilder() }
)

class RecyclableStringBuilder : StringBuilder(), Recyclable {
    override fun reset() {
        setLength(0)
    }
}

/**
 * Bitmap对象池的应用
 */
class BitmapPool private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: BitmapPool? = null
        
        fun getInstance(): BitmapPool {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BitmapPool().also { INSTANCE = it }
            }
        }
    }

    private val bitmapPool = hashMapOf<String, MutableList<Bitmap>>()
    private val maxPoolSize = 20

    /**
     * 获取指定尺寸的Bitmap
     */
    @Synchronized
    fun getBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val key = createKey(width, height, config)
        val bitmaps = bitmapPool[key]
        
        return if (!bitmaps.isNullOrEmpty()) {
            bitmaps.removeAt(bitmaps.size - 1)
        } else {
            Bitmap.createBitmap(width, height, config)
        }
    }

    /**
     * 回收Bitmap到池中
     */
    @Synchronized
    fun recycleBitmap(bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        
        val key = createKey(bitmap.width, bitmap.height, bitmap.config)
        val bitmaps = bitmapPool.getOrPut(key) { mutableListOf() }
        
        if (bitmaps.size < maxPoolSize) {
            bitmaps.add(bitmap)
        } else {
            bitmap.recycle()
        }
    }

    private fun createKey(width: Int, height: Int, config: Bitmap.Config): String {
        return "${width}x${height}_${config.name}"
    }

    /**
     * 清理池中的资源
     */
    @Synchronized
    fun clearPool() {
        bitmapPool.values.forEach { bitmaps ->
            bitmaps.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }
        bitmapPool.clear()
    }
}

/**
 * 高效的字符串构建工具
 */
object EfficientStringBuilder {
    
    /**
     * 使用对象池构建字符串
     */
    fun buildString(action: StringBuilder.() -> Unit): String {
        return StringBuilderPool.use { builder ->
            builder.action()
            builder.toString()
        }
    }

    /**
     * 批量拼接字符串
     */
    fun joinToString(
        elements: Iterable<String>,
        separator: String = ", ",
        prefix: String = "",
        suffix: String = ""
    ): String {
        return buildString {
            append(prefix)
            var isFirst = true
            for (element in elements) {
                if (!isFirst) {
                    append(separator)
                }
                append(element)
                isFirst = false
            }
            append(suffix)
        }
    }
}

/**
 * 内存监控工具
 */
class MemoryMonitor(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    /**
     * 获取当前内存使用情况
     */
    fun getCurrentMemoryInfo(): MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        return MemoryInfo(
            totalSystemMemory = memoryInfo.totalMem,
            availableSystemMemory = memoryInfo.availMem,
            usedAppMemory = usedMemory,
            maxAppMemory = maxMemory,
            memoryUsagePercentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
        )
    }

    /**
     * 检查是否处于内存压力状态
     */
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    /**
     * 触发垃圾回收建议
     */
    fun suggestGarbageCollection() {
        val memoryInfo = getCurrentMemoryInfo()
        if (memoryInfo.memoryUsagePercentage > 80f) {
            System.gc()
        }
    }

    /**
     * 获取内存使用报告
     */
    fun generateMemoryReport(): String {
        val memoryInfo = getCurrentMemoryInfo()
        return buildString {
            appendLine("=== 内存使用报告 ===")
            appendLine("系统总内存: ${formatBytes(memoryInfo.totalSystemMemory)}")
            appendLine("系统可用内存: ${formatBytes(memoryInfo.availableSystemMemory)}")
            appendLine("应用使用内存: ${formatBytes(memoryInfo.usedAppMemory)}")
            appendLine("应用最大内存: ${formatBytes(memoryInfo.maxAppMemory)}")
            appendLine("内存使用率: ${"%.1f".format(memoryInfo.memoryUsagePercentage)}%")
            
            when {
                memoryInfo.memoryUsagePercentage > 90 -> appendLine("状态: 危险 - 建议立即释放内存")
                memoryInfo.memoryUsagePercentage > 70 -> appendLine("状态: 警告 - 建议清理缓存")
                else -> appendLine("状态: 正常")
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            bytes >= 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }
}

data class MemoryInfo(
    val totalSystemMemory: Long,
    val availableSystemMemory: Long,
    val usedAppMemory: Long,
    val maxAppMemory: Long,
    val memoryUsagePercentage: Float
)
```

#### 11.1.2 内存泄漏检测与修复

```kotlin
/**
 * 内存泄漏检测器
 */
class MemoryLeakDetector {
    
    private val weakReferences = mutableSetOf<WeakReference<Any>>()
    private val strongReferences = mutableSetOf<Any>()
    
    /**
     * 注册需要监控的对象
     */
    fun watch(obj: Any, description: String = obj::class.simpleName ?: "Unknown") {
        val weakRef = WeakReference(obj)
        weakReferences.add(weakRef)
        
        // 添加到强引用集合，防止被立即回收
        strongReferences.add(obj)
        
        // 延迟检查
        Handler(Looper.getMainLooper()).postDelayed({
            checkForLeak(weakRef, description)
        }, 5000) // 5秒后检查
    }
    
    private fun checkForLeak(weakRef: WeakReference<Any>, description: String) {
        System.gc() // 建议垃圾回收
        
        if (weakRef.get() != null) {
            Log.w("MemoryLeakDetector", "可能的内存泄漏: $description")
            // 在实际项目中可以上报到崩溃收集服务
        }
        
        // 清理检查完的引用
        weakReferences.remove(weakRef)
        strongReferences.removeAll { it === weakRef.get() }
    }
    
    /**
     * 清理所有监控的对象
     */
    fun clear() {
        weakReferences.clear()
        strongReferences.clear()
    }
}

/**
 * 安全的Context引用管理器
 */
class SafeContextManager {
    private var contextRef: WeakReference<Context>? = null
    
    fun setContext(context: Context) {
        contextRef = WeakReference(context)
    }
    
    fun getContext(): Context? = contextRef?.get()
    
    fun clear() {
        contextRef?.clear()
        contextRef = null
    }
    
    /**
     * 安全地执行需要Context的操作
     */
    inline fun <T> withContext(action: (Context) -> T): T? {
        return getContext()?.let(action)
    }
}

/**
 * 防内存泄漏的Handler
 */
class WeakHandler<T>(target: T) : Handler(Looper.getMainLooper()) where T : Any {
    private val targetRef = WeakReference(target)
    
    override fun handleMessage(msg: Message) {
        val target = targetRef.get()
        if (target != null) {
            handleMessage(msg, target)
        }
    }
    
    protected open fun handleMessage(msg: Message, target: T) {
        // 子类重写此方法
    }
    
    fun clearTarget() {
        targetRef.clear()
    }
}

/**
 * 防内存泄漏的AsyncTask替代方案
 */
abstract class SafeAsyncTask<Params, Progress, Result>(
    private val contextRef: WeakReference<Context>
) {
    
    @Volatile
    private var isCancelled = false
    
    fun execute(vararg params: Params) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = doInBackground(*params)
                
                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        contextRef.get()?.let {
                            onPostExecute(result)
                        }
                    }
                }
            } catch (e: Exception) {
                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        contextRef.get()?.let {
                            onError(e)
                        }
                    }
                }
            }
        }
    }
    
    protected abstract suspend fun doInBackground(vararg params: Params): Result
    
    protected abstract fun onPostExecute(result: Result)
    
    protected open fun onError(exception: Exception) {
        Log.e("SafeAsyncTask", "执行失败", exception)
    }
    
    fun cancel() {
        isCancelled = true
    }
}

/**
 * 资源自动清理管理器
 */
class ResourceCleanupManager : LifecycleEventObserver {
    private val cleanupTasks = mutableListOf<() -> Unit>()
    
    /**
     * 注册清理任务
     */
    fun registerCleanupTask(task: () -> Unit) {
        cleanupTasks.add(task)
    }
    
    /**
     * 注册需要自动清理的资源
     */
    fun registerResource(resource: AutoCloseable) {
        cleanupTasks.add { 
            try {
                resource.close()
            } catch (e: Exception) {
                Log.w("ResourceCleanup", "关闭资源失败", e)
            }
        }
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            performCleanup()
        }
    }
    
    private fun performCleanup() {
        cleanupTasks.forEach { task ->
            try {
                task()
            } catch (e: Exception) {
                Log.w("ResourceCleanup", "清理任务失败", e)
            }
        }
        cleanupTasks.clear()
    }
}

/**
 * 内存友好的图片加载管理器
 */
class MemoryFriendlyImageLoader private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: MemoryFriendlyImageLoader? = null
        
        fun getInstance(): MemoryFriendlyImageLoader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MemoryFriendlyImageLoader().also { INSTANCE = it }
            }
        }
    }
    
    private val imageCache = LruCache<String, Bitmap>(getCacheSize())
    private val bitmapPool = BitmapPool.getInstance()
    
    private fun getCacheSize(): Int {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        return (maxMemory / 8).toInt() // 使用最大内存的1/8作为缓存
    }
    
    /**
     * 内存友好的图片加载
     */
    fun loadImage(
        url: String,
        targetWidth: Int,
        targetHeight: Int,
        callback: (Bitmap?) -> Unit
    ) {
        // 检查内存缓存
        val cachedBitmap = imageCache.get(url)
        if (cachedBitmap != null && !cachedBitmap.isRecycled) {
            callback(cachedBitmap)
            return
        }
        
        // 异步加载
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = loadBitmapFromUrl(url, targetWidth, targetHeight)
                
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        imageCache.put(url, bitmap)
                        callback(bitmap)
                    } else {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
    
    private suspend fun loadBitmapFromUrl(
        url: String, 
        targetWidth: Int, 
        targetHeight: Int
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // 首先获取图片尺寸
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                
                // 这里应该是实际的网络请求逻辑
                // val inputStream = URL(url).openStream()
                // BitmapFactory.decodeStream(inputStream, null, options)
                
                // 计算缩放比例
                val inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                
                // 实际解码
                options.apply {
                    inJustDecodeBounds = false
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565 // 使用更少内存的格式
                }
                
                // 使用对象池中的Bitmap
                val bitmap = bitmapPool.getBitmap(
                    options.outWidth / inSampleSize,
                    options.outHeight / inSampleSize,
                    options.inPreferredConfig
                )
                
                // 这里应该是实际的解码逻辑
                // BitmapFactory.decodeStream(URL(url).openStream(), null, options)
                
                bitmap
            } catch (e: Exception) {
                Log.e("ImageLoader", "加载图片失败: $url", e)
                null
            }
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && 
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        imageCache.evictAll()
        bitmapPool.clearPool()
    }
    
    /**
     * 内存压力时的处理
     */
    fun onLowMemory() {
        clearCache()
        System.gc()
    }
}
```

### 11.2 编译时优化

#### 11.2.1 内联函数与编译优化

```kotlin
/**
 * 编译时优化的内联函数示例
 */
class CompileTimeOptimizations {
    
    /**
     * 高效的条件执行内联函数
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> T?.ifNotNull(action: (T) -> Unit) {
        if (this != null) action(this)
    }
    
    /**
     * 零开销的测量时间内联函数
     */
    inline fun <T> measureTimeInline(block: () -> T): Pair<T, Long> {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        return Pair(result, endTime - startTime)
    }
    
    /**
     * 条件编译的调试日志
     */
    inline fun debugLog(message: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d("Debug", message())
        }
        // 在Release版本中，整个函数调用会被编译器消除
    }
    
    /**
     * 高性能的集合操作内联函数
     */
    inline fun <T, R> List<T>.mapInline(transform: (T) -> R): List<R> {
        val result = ArrayList<R>(this.size)
        for (element in this) {
            result.add(transform(element))
        }
        return result
    }
    
    /**
     * 零分配的过滤操作
     */
    inline fun <T> List<T>.forEachFiltered(
        predicate: (T) -> Boolean,
        action: (T) -> Unit
    ) {
        for (element in this) {
            if (predicate(element)) {
                action(element)
            }
        }
    }
}

/**
 * 编译时常量优化
 */
object CompileTimeConstants {
    
    // 编译时常量 - 会被内联到使用点
    const val MAX_CACHE_SIZE = 1024
    const val DEFAULT_TIMEOUT = 30_000L
    const val API_VERSION = "v1"
    
    // 运行时常量 - 不会被内联
    val CURRENT_TIME = System.currentTimeMillis()
    
    /**
     * 使用编译时常量优化的配置类
     */
    @JvmInline
    value class CacheConfig(private val maxSize: Int = MAX_CACHE_SIZE) {
        fun getMaxSize(): Int = maxSize
        
        companion object {
            val DEFAULT = CacheConfig()
            val LARGE = CacheConfig(MAX_CACHE_SIZE * 2)
            val SMALL = CacheConfig(MAX_CACHE_SIZE / 2)
        }
    }
}

/**
 * 类型擦除优化的泛型工具
 */
object GenericOptimizations {
    
    /**
     * 实化泛型参数避免反射
     */
    inline fun <reified T> createInstance(): T {
        return when (T::class) {
            String::class -> "" as T
            Int::class -> 0 as T
            Boolean::class -> false as T
            List::class -> emptyList<Any>() as T
            else -> throw IllegalArgumentException("不支持的类型: ${T::class}")
        }
    }
    
    /**
     * 类型安全的强制转换
     */
    inline fun <reified T> Any?.safeAs(): T? {
        return this as? T
    }
    
    /**
     * 高效的类型检查
     */
    inline fun <reified T> Any.isInstanceOf(): Boolean {
        return this is T
    }
    
    /**
     * 零开销的类型转换管道
     */
    inline fun <T, reified R> T.castTo(): R {
        return this as R
    }
}

/**
 * 序列优化 - 延迟计算减少中间集合
 */
class SequenceOptimizations {
    
    /**
     * 链式操作优化示例
     */
    fun processLargeDataSet(data: List<Int>): List<String> {
        // 不好的实现 - 创建多个中间集合
        return data
            .filter { it > 0 }        // 创建新List
            .map { it * 2 }           // 创建新List
            .filter { it < 1000 }     // 创建新List
            .map { "Item: $it" }      // 创建新List
    }
    
    fun processLargeDataSetOptimized(data: List<Int>): List<String> {
        // 优化实现 - 使用序列避免中间集合
        return data.asSequence()
            .filter { it > 0 }
            .map { it * 2 }
            .filter { it < 1000 }
            .map { "Item: $it" }
            .toList()                 // 只在最后创建一个List
    }
    
    /**
     * 自定义高性能序列操作
     */
    fun <T> Sequence<T>.chunkedOptimized(size: Int): Sequence<List<T>> = sequence {
        val iterator = this@chunkedOptimized.iterator()
        
        while (iterator.hasNext()) {
            val chunk = ArrayList<T>(size)
            repeat(size) {
                if (iterator.hasNext()) {
                    chunk.add(iterator.next())
                }
            }
            if (chunk.isNotEmpty()) {
                yield(chunk)
            }
        }
    }
    
    /**
     * 无限序列的内存友好处理
     */
    fun generateFibonacci(): Sequence<Long> = sequence {
        var a = 0L
        var b = 1L
        
        while (true) {
            yield(a)
            val temp = a + b
            a = b
            b = temp
        }
    }
    
    /**
     * 高效的数据流处理
     */
    suspend fun processDataStream(
        dataSource: Flow<String>,
        batchSize: Int = 100
    ): Flow<List<ProcessedData>> = flow {
        dataSource
            .chunked(batchSize)
            .collect { batch ->
                val processedBatch = batch
                    .asSequence()
                    .map { data -> ProcessedData(data.uppercase()) }
                    .filter { it.isValid() }
                    .toList()
                
                if (processedBatch.isNotEmpty()) {
                    emit(processedBatch)
                }
            }
    }
}

data class ProcessedData(val value: String) {
    fun isValid(): Boolean = value.isNotEmpty()
}

/**
 * 流扩展函数优化
 */
suspend fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> = flow {
    val chunk = mutableListOf<T>()
    
    collect { item ->
        chunk.add(item)
        if (chunk.size == size) {
            emit(chunk.toList())
            chunk.clear()
        }
    }
    
    if (chunk.isNotEmpty()) {
        emit(chunk.toList())
    }
}
```

#### 11.2.2 R8/ProGuard优化配置

```kotlin
/**
 * 代码混淆优化的注解标记
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class KeepForReflection

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class KeepEntireClass

/**
 * 序列化友好的数据类
 */
@Keep
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    @KeepForReflection
    fun isSuccess(): Boolean = code == 200
}

/**
 * 反射安全的工具类
 */
@KeepEntireClass
class ReflectionSafeUtils {
    
    @KeepForReflection
    companion object {
        @JvmStatic
        fun getInstance(): ReflectionSafeUtils = ReflectionSafeUtils()
    }
    
    @KeepForReflection
    fun processData(input: String): String {
        return input.uppercase()
    }
}

/**
 * 编译时优化的配置管理
 */
object OptimizedBuildConfig {
    
    // 编译时决定的功能开关
    const val ENABLE_LOGGING = BuildConfig.DEBUG
    const val ENABLE_ANALYTICS = !BuildConfig.DEBUG
    const val ENABLE_CRASH_REPORTING = true
    
    /**
     * 条件编译的功能模块
     */
    inline fun withLogging(action: () -> Unit) {
        if (ENABLE_LOGGING) {
            action()
        }
    }
    
    inline fun withAnalytics(action: () -> Unit) {
        if (ENABLE_ANALYTICS) {
            action()
        }
    }
    
    /**
     * 编译时字符串优化
     */
    object Strings {
        const val APP_NAME = "Kotlin Demo"
        const val BASE_URL = if (BuildConfig.DEBUG) {
            "https://api-dev.example.com"
        } else {
            "https://api.example.com"
        }
    }
}
```

### 11.3 运行时性能优化

#### 11.3.1 协程性能调优

```kotlin
/**
 * 协程性能优化策略
 */
class CoroutinePerformanceOptimizer {
    
    /**
     * 自定义高性能调度器
     */
    class HighPerformanceDispatcher(
        threadCount: Int = Runtime.getRuntime().availableProcessors()
    ) : CoroutineDispatcher() {
        
        private val threadPool = Executors.newFixedThreadPool(
            threadCount,
            ThreadFactory { runnable ->
                Thread(runnable, "HighPerf-${Thread.currentThread().id}").apply {
                    isDaemon = false
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
        
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            threadPool.submit(block)
        }
        
        override fun close() {
            threadPool.shutdown()
        }
    }
    
    private val customDispatcher = HighPerformanceDispatcher()
    
    /**
     * 批量并行处理优化
     */
    suspend fun <T, R> List<T>.parallelMapOptimized(
        concurrency: Int = 10,
        transform: suspend (T) -> R
    ): List<R> = coroutineScope {
        this@parallelMapOptimized
            .chunked((size + concurrency - 1) / concurrency)
            .map { chunk ->
                async(customDispatcher) {
                    chunk.map { transform(it) }
                }
            }
            .flatMap { it.await() }
    }
    
    /**
     * 流量控制的并发处理
     */
    suspend fun <T, R> processWithBackpressure(
        items: Flow<T>,
        maxConcurrency: Int = 5,
        processor: suspend (T) -> R
    ): Flow<R> = items
        .buffer(maxConcurrency * 2) // 缓冲避免背压
        .map { item ->
            async(customDispatcher) {
                processor(item)
            }
        }
        .buffer(maxConcurrency) // 限制并发数
        .map { deferred ->
            deferred.await()
        }
    
    /**
     * 超时和重试的优化实现
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 10000L,
        factor: Double = 2.0,
        action: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return withTimeout(30000L) { // 30秒超时
                    action()
                }
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("重试失败")
    }
    
    /**
     * 协程池管理
     */
    class CoroutinePool(
        private val maxConcurrency: Int = 50
    ) {
        private val semaphore = Semaphore(maxConcurrency)
        private val activeCoroutines = AtomicInteger(0)
        
        suspend fun <T> execute(action: suspend () -> T): T {
            semaphore.acquire()
            activeCoroutines.incrementAndGet()
            
            try {
                return withContext(customDispatcher) {
                    action()
                }
            } finally {
                activeCoroutines.decrementAndGet()
                semaphore.release()
            }
        }
        
        fun getActiveCount(): Int = activeCoroutines.get()
        
        fun getAvailableCapacity(): Int = semaphore.availablePermits
    }
    
    private val coroutinePool = CoroutinePool()
    
    /**
     * 高效的数据加载管道
     */
    suspend fun <T> createDataPipeline(
        dataSource: suspend () -> List<T>,
        batchSize: Int = 100,
        processor: suspend (List<T>) -> Unit
    ) {
        coroutineScope {
            val channel = Channel<T>(capacity = batchSize * 2)
            
            // 生产者协程
            launch(customDispatcher) {
                try {
                    val data = dataSource()
                    for (item in data) {
                        channel.send(item)
                    }
                } finally {
                    channel.close()
                }
            }
            
            // 消费者协程
            launch(customDispatcher) {
                val batch = mutableListOf<T>()
                
                for (item in channel) {
                    batch.add(item)
                    
                    if (batch.size >= batchSize) {
                        processor(batch.toList())
                        batch.clear()
                    }
                }
                
                if (batch.isNotEmpty()) {
                    processor(batch)
                }
            }
        }
    }
    
    /**
     * 协程监控和性能分析
     */
    class CoroutineMonitor {
        private val metrics = mutableMapOf<String, PerformanceMetrics>()
        
        suspend fun <T> monitor(
            name: String,
            action: suspend () -> T
        ): T {
            val startTime = System.nanoTime()
            val startMemory = getUsedMemory()
            
            return try {
                action()
            } finally {
                val endTime = System.nanoTime()
                val endMemory = getUsedMemory()
                val duration = endTime - startTime
                val memoryDelta = endMemory - startMemory
                
                recordMetrics(name, duration, memoryDelta)
            }
        }
        
        private fun recordMetrics(name: String, duration: Long, memoryDelta: Long) {
            val existing = metrics[name] ?: PerformanceMetrics()
            val updated = existing.update(duration, memoryDelta)
            metrics[name] = updated
        }
        
        private fun getUsedMemory(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
        
        fun getReport(): String = buildString {
            appendLine("=== 协程性能报告 ===")
            metrics.forEach { (name, metrics) ->
                appendLine("$name:")
                appendLine("  平均耗时: ${metrics.averageDuration / 1_000_000}ms")
                appendLine("  最大耗时: ${metrics.maxDuration / 1_000_000}ms")
                appendLine("  平均内存变化: ${formatBytes(metrics.averageMemoryDelta)}")
                appendLine("  执行次数: ${metrics.executionCount}")
                appendLine()
            }
        }
        
        private fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
                bytes >= 1024 -> "${bytes / 1024}KB"
                else -> "${bytes}B"
            }
        }
    }
    
    data class PerformanceMetrics(
        val executionCount: Int = 0,
        val totalDuration: Long = 0,
        val maxDuration: Long = 0,
        val totalMemoryDelta: Long = 0
    ) {
        val averageDuration: Long get() = if (executionCount > 0) totalDuration / executionCount else 0
        val averageMemoryDelta: Long get() = if (executionCount > 0) totalMemoryDelta / executionCount else 0
        
        fun update(duration: Long, memoryDelta: Long): PerformanceMetrics {
            return copy(
                executionCount = executionCount + 1,
                totalDuration = totalDuration + duration,
                maxDuration = maxOf(maxDuration, duration),
                totalMemoryDelta = totalMemoryDelta + memoryDelta
            )
        }
    }
}
```

#### 11.3.2 集合与数据结构优化

```kotlin
/**
 * 高性能集合实现
 */
object OptimizedCollections {
    
    /**
     * 空间优化的稀疏数组
     */
    class OptimizedSparseArray<T> {
        private val keys = IntArray(16)
        private val values = arrayOfNulls<Any?>(16)
        private var size = 0
        
        @Suppress("UNCHECKED_CAST")
        operator fun get(key: Int): T? {
            val index = binarySearch(keys, 0, size, key)
            return if (index >= 0) values[index] as T? else null
        }
        
        operator fun set(key: Int, value: T?) {
            val index = binarySearch(keys, 0, size, key)
            
            if (index >= 0) {
                values[index] = value
            } else {
                val insertIndex = -(index + 1)
                insert(insertIndex, key, value)
            }
        }
        
        private fun insert(index: Int, key: Int, value: T?) {
            if (size >= keys.size) {
                // 扩容逻辑
                resize()
            }
            
            // 移动元素为新插入腾出空间
            System.arraycopy(keys, index, keys, index + 1, size - index)
            System.arraycopy(values, index, values, index + 1, size - index)
            
            keys[index] = key
            values[index] = value
            size++
        }
        
        private fun resize() {
            val newSize = size * 2
            val newKeys = keys.copyOf(newSize)
            val newValues = values.copyOf(newSize)
            // 更新引用...
        }
        
        private fun binarySearch(array: IntArray, start: Int, end: Int, value: Int): Int {
            var low = start
            var high = end - 1
            
            while (low <= high) {
                val mid = (low + high) ushr 1
                val midVal = array[mid]
                
                when {
                    midVal < value -> low = mid + 1
                    midVal > value -> high = mid - 1
                    else -> return mid
                }
            }
            return -(low + 1)
        }
        
        fun size(): Int = size
        
        fun isEmpty(): Boolean = size == 0
    }
    
    /**
     * 高性能的位图索引
     */
    class BitIndex {
        private var bits = LongArray(1)
        private var actualSize = 0
        
        fun set(index: Int) {
            ensureCapacity(index)
            val arrayIndex = index / 64
            val bitIndex = index % 64
            bits[arrayIndex] = bits[arrayIndex] or (1L shl bitIndex)
            actualSize = maxOf(actualSize, index + 1)
        }
        
        fun get(index: Int): Boolean {
            if (index >= actualSize) return false
            val arrayIndex = index / 64
            val bitIndex = index % 64
            return (bits[arrayIndex] and (1L shl bitIndex)) != 0L
        }
        
        fun clear(index: Int) {
            if (index >= actualSize) return
            val arrayIndex = index / 64
            val bitIndex = index % 64
            bits[arrayIndex] = bits[arrayIndex] and (1L shl bitIndex).inv()
        }
        
        private fun ensureCapacity(index: Int) {
            val requiredSize = (index / 64) + 1
            if (requiredSize > bits.size) {
                bits = bits.copyOf(requiredSize)
            }
        }
        
        fun cardinality(): Int {
            var count = 0
            for (i in 0 until (actualSize + 63) / 64) {
                count += bits[i].countOneBits()
            }
            return count
        }
    }
    
    /**
     * 内存优化的字符串池
     */
    class StringPool private constructor() {
        companion object {
            @Volatile
            private var INSTANCE: StringPool? = null
            
            fun getInstance(): StringPool {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: StringPool().also { INSTANCE = it }
                }
            }
        }
        
        private val pool = ConcurrentHashMap<String, WeakReference<String>>()
        
        fun intern(string: String): String {
            val existing = pool[string]?.get()
            if (existing != null) {
                return existing
            }
            
            pool[string] = WeakReference(string)
            return string
        }
        
        fun size(): Int = pool.size
        
        fun cleanup() {
            val iterator = pool.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.get() == null) {
                    iterator.remove()
                }
            }
        }
    }
    
    /**
     * 高性能的环形缓冲区
     */
    class RingBuffer<T>(private val capacity: Int) {
        private val buffer = arrayOfNulls<Any?>(capacity)
        private var head = 0
        private var tail = 0
        private var size = 0
        
        fun offer(item: T): Boolean {
            if (size == capacity) return false
            
            buffer[tail] = item
            tail = (tail + 1) % capacity
            size++
            return true
        }
        
        @Suppress("UNCHECKED_CAST")
        fun poll(): T? {
            if (size == 0) return null
            
            val item = buffer[head] as T?
            buffer[head] = null
            head = (head + 1) % capacity
            size--
            return item
        }
        
        @Suppress("UNCHECKED_CAST")
        fun peek(): T? {
            return if (size == 0) null else buffer[head] as T?
        }
        
        fun size(): Int = size
        
        fun isEmpty(): Boolean = size == 0
        
        fun isFull(): Boolean = size == capacity
    }
    
    /**
     * 批量操作优化的列表实现
     */
    class BatchList<T> : MutableList<T> {
        private val list = ArrayList<T>()
        private var batchMode = false
        private val pendingOperations = mutableListOf<() -> Unit>()
        
        override val size: Int get() = list.size
        
        fun beginBatch() {
            batchMode = true
        }
        
        fun endBatch() {
            if (batchMode) {
                pendingOperations.forEach { it() }
                pendingOperations.clear()
                batchMode = false
            }
        }
        
        override fun add(element: T): Boolean {
            return if (batchMode) {
                pendingOperations.add { list.add(element) }
                true
            } else {
                list.add(element)
            }
        }
        
        override fun remove(element: T): Boolean {
            return if (batchMode) {
                pendingOperations.add { list.remove(element) }
                true
            } else {
                list.remove(element)
            }
        }
        
        // 实现其他MutableList方法...
        override fun contains(element: T): Boolean = list.contains(element)
        override fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)
        override fun get(index: Int): T = list[index]
        override fun indexOf(element: T): Int = list.indexOf(element)
        override fun isEmpty(): Boolean = list.isEmpty()
        override fun iterator(): MutableIterator<T> = list.iterator()
        override fun lastIndexOf(element: T): Int = list.lastIndexOf(element)
        override fun add(index: Int, element: T) = list.add(index, element)
        override fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements)
        override fun addAll(elements: Collection<T>): Boolean = list.addAll(elements)
        override fun clear() = list.clear()
        override fun listIterator(): MutableListIterator<T> = list.listIterator()
        override fun listIterator(index: Int): MutableListIterator<T> = list.listIterator(index)
        override fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements.toSet())
        override fun removeAt(index: Int): T = list.removeAt(index)
        override fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements.toSet())
        override fun set(index: Int, element: T): T = list.set(index, element)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = list.subList(fromIndex, toIndex)
    }
}
```

### 11.4 性能监控与分析

#### 11.4.1 性能指标收集

```kotlin
/**
 * 性能监控系统
 */
class PerformanceMonitor private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PerformanceMonitor? = null
        
        fun getInstance(): PerformanceMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerformanceMonitor().also { INSTANCE = it }
            }
        }
    }
    
    private val metrics = ConcurrentHashMap<String, MetricCollector>()
    private val frameTimeCollector = FrameTimeCollector()
    private val memoryCollector = MemoryUsageCollector()
    
    /**
     * 方法执行时间监控
     */
    inline fun <T> measureMethod(
        methodName: String,
        block: () -> T
    ): T {
        val startTime = System.nanoTime()
        val startMemory = getUsedMemory()
        
        return try {
            block()
        } finally {
            val endTime = System.nanoTime()
            val endMemory = getUsedMemory()
            
            recordMethodExecution(
                methodName,
                endTime - startTime,
                endMemory - startMemory
            )
        }
    }
    
    /**
     * 异步方法监控
     */
    suspend fun <T> measureSuspendMethod(
        methodName: String,
        block: suspend () -> T
    ): T {
        val startTime = System.nanoTime()
        val startMemory = getUsedMemory()
        
        return try {
            block()
        } finally {
            val endTime = System.nanoTime()
            val endMemory = getUsedMemory()
            
            recordMethodExecution(
                methodName,
                endTime - startTime,
                endMemory - startMemory
            )
        }
    }
    
    private fun recordMethodExecution(
        methodName: String,
        duration: Long,
        memoryDelta: Long
    ) {
        val collector = metrics.getOrPut(methodName) { MetricCollector(methodName) }
        collector.addSample(duration, memoryDelta)
    }
    
    private fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    /**
     * 帧率监控
     */
    class FrameTimeCollector {
        private val frameTimes = ArrayDeque<Long>(120) // 保存最近2秒的帧时间
        private var lastFrameTime = 0L
        
        fun onFrameRendered() {
            val currentTime = System.nanoTime()
            
            if (lastFrameTime != 0L) {
                val frameTime = currentTime - lastFrameTime
                
                synchronized(frameTimes) {
                    frameTimes.addLast(frameTime)
                    
                    // 保持队列大小
                    while (frameTimes.size > 120) {
                        frameTimes.removeFirst()
                    }
                }
            }
            
            lastFrameTime = currentTime
        }
        
        fun getCurrentFPS(): Double {
            synchronized(frameTimes) {
                if (frameTimes.isEmpty()) return 0.0
                
                val averageFrameTime = frameTimes.average()
                return 1_000_000_000.0 / averageFrameTime // 纳秒转FPS
            }
        }
        
        fun getFrameTimePercentiles(): FrameTimeStats {
            synchronized(frameTimes) {
                if (frameTimes.isEmpty()) return FrameTimeStats(0.0, 0.0, 0.0, 0.0)
                
                val sorted = frameTimes.sorted()
                val size = sorted.size
                
                return FrameTimeStats(
                    p50 = sorted[size * 50 / 100] / 1_000_000.0, // 转换为毫秒
                    p90 = sorted[size * 90 / 100] / 1_000_000.0,
                    p95 = sorted[size * 95 / 100] / 1_000_000.0,
                    p99 = sorted[size * 99 / 100] / 1_000_000.0
                )
            }
        }
    }
    
    data class FrameTimeStats(
        val p50: Double, // 50%分位数
        val p90: Double, // 90%分位数
        val p95: Double, // 95%分位数
        val p99: Double  // 99%分位数
    )
    
    /**
     * 内存使用监控
     */
    class MemoryUsageCollector {
        private val memorySnapshots = ArrayDeque<MemorySnapshot>(100)
        
        fun takeSnapshot() {
            val runtime = Runtime.getRuntime()
            val snapshot = MemorySnapshot(
                timestamp = System.currentTimeMillis(),
                usedMemory = runtime.totalMemory() - runtime.freeMemory(),
                totalMemory = runtime.totalMemory(),
                maxMemory = runtime.maxMemory()
            )
            
            synchronized(memorySnapshots) {
                memorySnapshots.addLast(snapshot)
                
                while (memorySnapshots.size > 100) {
                    memorySnapshots.removeFirst()
                }
            }
        }
        
        fun getMemoryTrend(): List<MemorySnapshot> {
            synchronized(memorySnapshots) {
                return memorySnapshots.toList()
            }
        }
        
        fun getCurrentMemoryPressure(): MemoryPressure {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val usagePercentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
            
            return when {
                usagePercentage > 90 -> MemoryPressure.CRITICAL
                usagePercentage > 75 -> MemoryPressure.HIGH
                usagePercentage > 50 -> MemoryPressure.MEDIUM
                else -> MemoryPressure.LOW
            }
        }
    }
    
    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemory: Long,
        val totalMemory: Long,
        val maxMemory: Long
    ) {
        val usagePercentage: Float get() = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
    }
    
    enum class MemoryPressure {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * 指标收集器
     */
    class MetricCollector(private val name: String) {
        private val executionTimes = ArrayDeque<Long>(1000)
        private val memoryDeltas = ArrayDeque<Long>(1000)
        private var totalExecutions = 0L
        private var totalTime = 0L
        private var maxTime = 0L
        private var minTime = Long.MAX_VALUE
        
        @Synchronized
        fun addSample(executionTime: Long, memoryDelta: Long) {
            executionTimes.addLast(executionTime)
            memoryDeltas.addLast(memoryDelta)
            
            // 保持队列大小
            if (executionTimes.size > 1000) {
                executionTimes.removeFirst()
                memoryDeltas.removeFirst()
            }
            
            // 更新统计
            totalExecutions++
            totalTime += executionTime
            maxTime = maxOf(maxTime, executionTime)
            minTime = minOf(minTime, executionTime)
        }
        
        fun getStats(): MethodStats {
            return synchronized(this) {
                val avgTime = if (totalExecutions > 0) totalTime / totalExecutions else 0L
                val avgMemory = if (memoryDeltas.isNotEmpty()) {
                    memoryDeltas.average().toLong()
                } else 0L
                
                MethodStats(
                    methodName = name,
                    executionCount = totalExecutions,
                    averageTime = avgTime,
                    minTime = if (minTime != Long.MAX_VALUE) minTime else 0L,
                    maxTime = maxTime,
                    averageMemoryDelta = avgMemory
                )
            }
        }
    }
    
    data class MethodStats(
        val methodName: String,
        val executionCount: Long,
        val averageTime: Long, // 纳秒
        val minTime: Long,     // 纳秒
        val maxTime: Long,     // 纳秒
        val averageMemoryDelta: Long // 字节
    )
    
    /**
     * 生成性能报告
     */
    fun generatePerformanceReport(): PerformanceReport {
        val methodStats = metrics.values.map { it.getStats() }
        val frameStats = frameTimeCollector.getFrameTimePercentiles()
        val memoryTrend = memoryCollector.getMemoryTrend()
        val currentFPS = frameTimeCollector.getCurrentFPS()
        val memoryPressure = memoryCollector.getCurrentMemoryPressure()
        
        return PerformanceReport(
            methodStats = methodStats,
            frameStats = frameStats,
            memoryTrend = memoryTrend,
            currentFPS = currentFPS,
            memoryPressure = memoryPressure,
            timestamp = System.currentTimeMillis()
        )
    }
    
    data class PerformanceReport(
        val methodStats: List<MethodStats>,
        val frameStats: FrameTimeStats,
        val memoryTrend: List<MemorySnapshot>,
        val currentFPS: Double,
        val memoryPressure: MemoryPressure,
        val timestamp: Long
    )
    
    /**
     * 开始监控
     */
    fun startMonitoring() {
        // 定期收集内存快照
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                memoryCollector.takeSnapshot()
                delay(1000) // 每秒收集一次
            }
        }
    }
    
    /**
     * 获取格式化的报告字符串
     */
    fun getFormattedReport(): String {
        val report = generatePerformanceReport()
        
        return buildString {
            appendLine("=== 性能监控报告 ===")
            appendLine("生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(report.timestamp))}")
            appendLine()
            
            appendLine("--- 帧率统计 ---")
            appendLine("当前FPS: ${"%.1f".format(report.currentFPS)}")
            appendLine("帧时间P50: ${"%.1f".format(report.frameStats.p50)}ms")
            appendLine("帧时间P90: ${"%.1f".format(report.frameStats.p90)}ms")
            appendLine("帧时间P95: ${"%.1f".format(report.frameStats.p95)}ms")
            appendLine("帧时间P99: ${"%.1f".format(report.frameStats.p99)}ms")
            appendLine()
            
            appendLine("--- 内存状态 ---")
            appendLine("内存压力: ${report.memoryPressure}")
            if (report.memoryTrend.isNotEmpty()) {
                val latest = report.memoryTrend.last()
                appendLine("内存使用率: ${"%.1f".format(latest.usagePercentage)}%")
                appendLine("已用内存: ${formatBytes(latest.usedMemory)}")
                appendLine("最大内存: ${formatBytes(latest.maxMemory)}")
            }
            appendLine()
            
            appendLine("--- 方法执行统计 ---")
            report.methodStats
                .sortedByDescending { it.averageTime }
                .take(10)
                .forEach { stats ->
                    appendLine("${stats.methodName}:")
                    appendLine("  执行次数: ${stats.executionCount}")
                    appendLine("  平均耗时: ${"%.2f".format(stats.averageTime / 1_000_000.0)}ms")
                    appendLine("  最大耗时: ${"%.2f".format(stats.maxTime / 1_000_000.0)}ms")
                    appendLine("  内存影响: ${formatBytes(stats.averageMemoryDelta)}")
                    appendLine()
                }
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            bytes >= 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }
}
```

### 11.5 性能优化面试常考题

#### 题目1：内存优化策略

**问题：** Android应用中有哪些常见的内存问题？如何进行内存优化？

**答案：**
```kotlin
/**
 * 内存优化最佳实践总结
 */
class MemoryOptimizationGuide {
    
    /**
     * 1. 内存泄漏防护
     */
    
    // 问题：静态引用导致的内存泄漏
    class BadExample {
        companion object {
            var context: Context? = null // 危险！可能导致内存泄漏
        }
    }
    
    // 解决方案：使用弱引用或ApplicationContext
    class GoodExample {
        companion object {
            private var contextRef: WeakReference<Context>? = null
            
            fun setContext(context: Context) {
                contextRef = WeakReference(context.applicationContext)
            }
            
            fun getContext(): Context? = contextRef?.get()
        }
    }
    
    /**
     * 2. 集合优化
     */
    
    // 问题：不当的集合使用
    fun inefficientCollectionUsage() {
        val list = ArrayList<String>()
        // 频繁的add/remove操作会导致数组复制
        repeat(10000) {
            list.add("Item $it")
            if (it % 2 == 0) {
                list.removeAt(list.size - 1)
            }
        }
    }
    
    // 解决方案：选择合适的数据结构
    fun efficientCollectionUsage() {
        val deque = ArrayDeque<String>() // 双端队列，适合频繁增删
        repeat(10000) {
            deque.addLast("Item $it")
            if (it % 2 == 0) {
                deque.removeLast()
            }
        }
    }
    
    /**
     * 3. 对象创建优化
     */
    
    // 问题：频繁的对象创建
    fun inefficientObjectCreation(): String {
        val sb = StringBuilder()
        repeat(1000) {
            sb.append("Item $it, ")
        }
        return sb.toString()
    }
    
    // 解决方案：对象池
    fun efficientObjectCreation(): String {
        return StringBuilderPool.use { sb ->
            repeat(1000) {
                sb.append("Item $it, ")
            }
            sb.toString()
        }
    }
    
    /**
     * 4. Bitmap内存优化
     */
    
    fun loadBitmapEfficiently(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        // 首先获取图片尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)
        
        // 计算合适的采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        
        // 实际加载图片
        options.apply {
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.RGB_565 // 节省内存
            inMutable = false // 不可变bitmap节省内存
        }
        
        return BitmapFactory.decodeFile(filePath, options)
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
}
```

**内存优化要点：**
- **避免内存泄漏：** 使用WeakReference、及时清理监听器、避免静态引用Activity
- **选择合适数据结构：** SparseArray替代HashMap、ArrayDeque替代ArrayList
- **对象池复用：** StringBuilder、Bitmap等重对象的池化
- **Bitmap优化：** 合适采样率、RGB_565格式、及时回收

#### 题目2：启动优化策略

**问题：** 如何优化Android应用的启动时间？有哪些具体技术手段？

**答案：**
```kotlin
/**
 * 启动优化完整方案
 */
class StartupOptimization {
    
    /**
     * 1. Application优化
     */
    class OptimizedApplication : Application() {
        
        override fun onCreate() {
            super.onCreate()
            
            // 关键路径初始化（同步）
            initCriticalComponents()
            
            // 非关键组件异步初始化
            GlobalScope.launch(Dispatchers.IO) {
                initNonCriticalComponents()
            }
            
            // 预热关键类
            preloadCriticalClasses()
        }
        
        private fun initCriticalComponents() {
            // 只初始化启动必需的组件
            initLogger()
            initCrashReporting()
            initDI() // 依赖注入容器
        }
        
        private suspend fun initNonCriticalComponents() {
            // 异步初始化非关键组件
            delay(100) // 让主线程先处理UI
            
            parallel(
                { initAnalytics() },
                { initImageLoader() },
                { initNetworking() },
                { preloadCache() }
            )
        }
        
        private suspend fun parallel(vararg blocks: suspend () -> Unit) {
            blocks.map { block ->
                async(Dispatchers.IO) { block() }
            }.awaitAll()
        }
        
        private fun preloadCriticalClasses() {
            // 在后台线程预加载关键类
            Thread {
                try {
                    // 预加载Activity类
                    Class.forName("com.example.MainActivity")
                    Class.forName("com.example.SplashActivity")
                    
                    // 预加载常用工具类
                    Class.forName("com.example.utils.NetworkUtils")
                    Class.forName("com.example.data.UserRepository")
                } catch (e: ClassNotFoundException) {
                    // 忽略预加载失败
                }
            }.start()
        }
    }
    
    /**
     * 2. 启动Activity优化
     */
    class SplashActivity : AppCompatActivity() {
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // 使用主题背景，避免白屏
            // 在styles.xml中定义启动主题
            
            // 检查是否已初始化完成
            if (isAppReady()) {
                navigateToMain()
            } else {
                // 显示启动界面，监听初始化完成
                showSplashScreen()
                waitForInitialization()
            }
        }
        
        private fun isAppReady(): Boolean {
            // 检查关键组件是否已初始化
            return DependencyContainer.isInitialized()
        }
        
        private fun showSplashScreen() {
            setContentView(R.layout.activity_splash)
            // 设置简单的启动动画
        }
        
        private fun waitForInitialization() {
            lifecycleScope.launch {
                // 等待初始化完成
                while (!isAppReady()) {
                    delay(50)
                }
                
                // 确保最小展示时间（用户体验）
                delay(1000)
                
                navigateToMain()
            }
        }
        
        private fun navigateToMain() {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
    
    /**
     * 3. 依赖注入优化
     */
    object DependencyContainer {
        private var initialized = false
        private val lazy = mutableMapOf<String, Lazy<*>>()
        
        fun isInitialized(): Boolean = initialized
        
        fun init() {
            if (initialized) return
            
            // 注册延迟初始化的依赖
            registerDependencies()
            initialized = true
        }
        
        private fun registerDependencies() {
            lazy["UserRepository"] = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
                UserRepository(getNetworkService(), getLocalDatabase())
            }
            
            lazy["NetworkService"] = lazy(LazyThreadSafetyMode.NONE) {
                NetworkService.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .build()
            }
        }
        
        @Suppress("UNCHECKED_CAST")
        fun <T> get(key: String): T {
            return (lazy[key]?.value as? T) 
                ?: throw IllegalArgumentException("未找到依赖: $key")
        }
    }
    
    /**
     * 4. 预加载策略
     */
    class PreloadManager {
        
        suspend fun preloadEssentialData() = coroutineScope {
            // 并行预加载关键数据
            val userProfile = async { loadUserProfile() }
            val appConfig = async { loadAppConfig() }
            val criticalCache = async { warmupCache() }
            
            // 等待关键数据加载完成
            val results = listOf(userProfile, appConfig, criticalCache)
            results.awaitAll()
        }
        
        private suspend fun loadUserProfile(): UserProfile? {
            return try {
                // 从本地缓存快速加载用户信息
                LocalStorage.getUserProfile()
            } catch (e: Exception) {
                null
            }
        }
        
        private suspend fun loadAppConfig(): AppConfig {
            return try {
                // 加载应用配置
                RemoteConfig.getConfig()
            } catch (e: Exception) {
                AppConfig.DEFAULT
            }
        }
        
        private suspend fun warmupCache() {
            // 预热关键缓存
            ImageCache.warmup()
            StringCache.warmup()
        }
    }
    
    /**
     * 5. 启动性能监控
     */
    class StartupPerformanceTracker {
        private val milestones = mutableMapOf<String, Long>()
        
        fun recordMilestone(name: String) {
            milestones[name] = System.currentTimeMillis()
        }
        
        fun getStartupReport(): StartupReport {
            val appStart = milestones["app_start"] ?: 0L
            val splashShow = milestones["splash_show"] ?: 0L
            val mainShow = milestones["main_show"] ?: 0L
            
            return StartupReport(
                coldStartTime = splashShow - appStart,
                splashToMainTime = mainShow - splashShow,
                totalStartupTime = mainShow - appStart,
                milestones = milestones.toMap()
            )
        }
        
        fun logStartupMetrics() {
            val report = getStartupReport()
            Log.i("StartupPerf", "启动性能报告:")
            Log.i("StartupPerf", "  冷启动时间: ${report.coldStartTime}ms")
            Log.i("StartupPerf", "  启动页到主页: ${report.splashToMainTime}ms")
            Log.i("StartupPerf", "  总启动时间: ${report.totalStartupTime}ms")
            
            // 发送到性能监控平台
            Analytics.logStartupMetrics(report)
        }
    }
    
    data class StartupReport(
        val coldStartTime: Long,
        val splashToMainTime: Long,
        val totalStartupTime: Long,
        val milestones: Map<String, Long>
    )
}
```

**启动优化要点：**
- **Application优化：** 异步初始化非关键组件、预加载关键类
- **Activity优化：** 主题背景避免白屏、合理的启动流程
- **依赖注入：** 延迟初始化、按需加载
- **预加载策略：** 并行加载关键数据、缓存预热
- **性能监控：** 记录关键节点、量化启动时间

---

## 本章小结

第11章我们深入学习了Kotlin性能优化：

### 主要内容：
1. **内存管理：** 对象池、内存泄漏检测、资源自动清理
2. **编译优化：** 内联函数、R8配置、编译时常量
3. **运行时优化：** 协程调优、集合优化、数据结构选择
4. **性能监控：** 指标收集、帧率监控、内存分析

### 面试重点：
- **内存优化：** 泄漏检测、对象复用、Bitmap优化
- **启动优化：** 异步初始化、预加载、依赖管理
- **协程优化：** 调度器选择、并发控制、性能监控
- **工具使用：** Profiler、MAT、LeakCanary的使用经验

性能优化是一个持续的过程，需要结合实际业务场景，运用合适的技术手段，同时建立完善的监控体系。

---