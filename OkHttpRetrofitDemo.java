package com.example.network.modern;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp和Retrofit最佳实践示例
 * 展示现代Android网络编程的推荐方式
 */
public class OkHttpRetrofitDemo {
    private static final String TAG = "OkHttpRetrofit";
    private static final String BASE_URL = "https://api.example.com/";
    
    // ================================ OkHttp 部分 ================================
    
    /**
     * OkHttp客户端配置
     * OkHttp是Square公司开源的高效HTTP客户端
     * 特点：
     * 1. 连接池复用
     * 2. GZIP压缩
     * 3. 响应缓存
     * 4. 自动重试和重定向
     * 5. WebSocket支持
     */
    public static class OkHttpManager {
        private static OkHttpClient okHttpClient;
        
        /**
         * 获取配置好的OkHttpClient单例
         */
        public static OkHttpClient getInstance() {
            if (okHttpClient == null) {
                synchronized (OkHttpManager.class) {
                    if (okHttpClient == null) {
                        okHttpClient = buildOkHttpClient();
                    }
                }
            }
            return okHttpClient;
        }
        
        /**
         * 构建OkHttpClient
         * 包含各种拦截器和配置
         */
        private static OkHttpClient buildOkHttpClient() {
            return new OkHttpClient.Builder()
                // 1. 超时设置
                .connectTimeout(15, TimeUnit.SECONDS)    // 连接超时
                .readTimeout(20, TimeUnit.SECONDS)       // 读取超时
                .writeTimeout(20, TimeUnit.SECONDS)      // 写入超时
                .callTimeout(60, TimeUnit.SECONDS)       // 完整请求超时
                
                // 2. 连接池配置
                .connectionPool(new ConnectionPool(
                    5,           // 最大空闲连接数
                    5,           // 保持时间
                    TimeUnit.MINUTES
                ))
                
                // 3. 缓存配置（需要Context获取缓存目录）
                // .cache(new Cache(cacheDir, 10 * 1024 * 1024)) // 10MB缓存
                
                // 4. 添加拦截器
                .addInterceptor(new HeaderInterceptor())        // 添加公共头部
                .addInterceptor(new TokenInterceptor())         // Token认证
                .addInterceptor(new RetryInterceptor())         // 重试机制
                .addNetworkInterceptor(new CacheInterceptor())  // 缓存控制
                .addInterceptor(loggingInterceptor())           // 日志拦截器
                
                // 5. 证书配置（如需自定义证书）
                // .sslSocketFactory(sslSocketFactory, trustManager)
                
                // 6. 代理设置（如需要）
                // .proxy(Proxy.NO_PROXY)
                
                // 7. Cookie管理
                .cookieJar(new SimpleCookieJar())
                
                // 8. 重试和重定向
                .retryOnConnectionFailure(true)  // 连接失败重试
                .followRedirects(true)           // 跟随重定向
                .followSslRedirects(true)        // 跟随HTTPS到HTTP重定向
                
                .build();
        }
        
        /**
         * 日志拦截器
         * 用于调试，生产环境应设置为NONE
         */
        private static HttpLoggingInterceptor loggingInterceptor() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(
                message -> Log.d(TAG, message)
            );
            // 设置日志级别
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return interceptor;
        }
    }
    
    /**
     * 请求头拦截器
     * 添加公共请求头
     */
    public static class HeaderInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            // 添加公共请求头
            Request request = original.newBuilder()
                .header("User-Agent", "Android OkHttp Demo")
                .header("Accept", "application/json")
                .header("Accept-Language", "zh-CN")
                .header("Platform", "Android")
                .header("Version", "1.0.0")
                .method(original.method(), original.body())
                .build();
                
            return chain.proceed(request);
        }
    }
    
    /**
     * Token认证拦截器
     * 自动添加认证信息
     */
    public static class TokenInterceptor implements Interceptor {
        private String token = "your_token_here";
        
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            // 如果已有Authorization头部，不覆盖
            if (original.header("Authorization") != null) {
                return chain.proceed(original);
            }
            
            // 添加Token
            Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
                
            okhttp3.Response response = chain.proceed(request);
            
            // 如果返回401，可能需要刷新Token
            if (response.code() == 401) {
                // 刷新Token逻辑
                String newToken = refreshToken();
                if (newToken != null) {
                    this.token = newToken;
                    
                    // 使用新Token重试请求
                    Request newRequest = original.newBuilder()
                        .header("Authorization", "Bearer " + newToken)
                        .build();
                    
                    response.close();
                    return chain.proceed(newRequest);
                }
            }
            
            return response;
        }
        
        private String refreshToken() {
            // 实现Token刷新逻辑
            return "new_token";
        }
    }
    
    /**
     * 重试拦截器
     * 自定义重试逻辑
     */
    public static class RetryInterceptor implements Interceptor {
        private int maxRetry = 3;
        
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            okhttp3.Response response = null;
            IOException lastException = null;
            
            for (int i = 0; i < maxRetry; i++) {
                try {
                    response = chain.proceed(request);
                    
                    // 如果请求成功，直接返回
                    if (response.isSuccessful()) {
                        return response;
                    }
                    
                    // 某些错误码不需要重试
                    if (response.code() == 404 || response.code() == 401) {
                        return response;
                    }
                    
                    Log.w(TAG, "Request failed, retry " + (i + 1) + "/" + maxRetry);
                    
                } catch (IOException e) {
                    lastException = e;
                    Log.e(TAG, "Request exception, retry " + (i + 1) + "/" + maxRetry, e);
                    
                    // 最后一次重试失败，抛出异常
                    if (i == maxRetry - 1) {
                        throw e;
                    }
                    
                    // 延迟重试（指数退避）
                    try {
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            if (response != null) {
                return response;
            }
            
            throw lastException != null ? lastException : new IOException("Unknown error");
        }
    }
    
    /**
     * 缓存控制拦截器
     * 实现离线缓存
     */
    public static class CacheInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            
            // 无网络时，强制使用缓存
            if (!isNetworkAvailable()) {
                request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
                Log.d(TAG, "No network, force cache");
            }
            
            okhttp3.Response response = chain.proceed(request);
            
            if (isNetworkAvailable()) {
                // 有网络时，设置缓存策略
                int maxAge = 60; // 缓存60秒
                response = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
            } else {
                // 无网络时，设置离线缓存
                int maxStale = 60 * 60 * 24 * 7; // 离线缓存7天
                response = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
            }
            
            return response;
        }
        
        private boolean isNetworkAvailable() {
            // 实现网络状态检查
            return true;
        }
    }
    
    /**
     * 简单的Cookie管理
     */
    public static class SimpleCookieJar implements CookieJar {
        private final Map<String, List<Cookie>> cookieStore = new java.util.HashMap<>();
        
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }
        
        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new java.util.ArrayList<>();
        }
    }
    
    /**
     * OkHttp使用示例
     */
    public static class OkHttpUsageExamples {
        private OkHttpClient client = OkHttpManager.getInstance();
        
        /**
         * 同步GET请求
         */
        public void syncGet(String url) throws IOException {
            Request request = new Request.Builder()
                .url(url)
                .build();
                
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Sync GET response: " + responseData);
                }
            }
        }
        
        /**
         * 异步GET请求
         */
        public void asyncGet(String url) {
            Request request = new Request.Builder()
                .url(url)
                .build();
                
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Async GET failed", e);
                }
                
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "Async GET response: " + responseData);
                    }
                }
            });
        }
        
        /**
         * POST JSON请求
         */
        public void postJson(String url, String json) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json);
            
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
                
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "POST failed", e);
                }
                
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    Log.d(TAG, "POST response code: " + response.code());
                }
            });
        }
        
        /**
         * 文件上传
         */
        public void uploadFile(String url, File file) {
            RequestBody fileBody = RequestBody.create(
                MediaType.parse("application/octet-stream"), 
                file
            );
            
            MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("description", "File upload demo")
                .build();
                
            Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
                
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Upload failed", e);
                }
                
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    Log.d(TAG, "Upload success: " + response.code());
                }
            });
        }
    }
    
    // ================================ Retrofit 部分 ================================
    
    /**
     * Retrofit配置
     * Retrofit是基于OkHttp的类型安全的REST客户端
     * 特点：
     * 1. 接口化API定义
     * 2. 自动序列化/反序列化
     * 3. 支持RxJava
     * 4. 注解式请求配置
     */
    public static class RetrofitManager {
        private static Retrofit retrofit;
        
        public static Retrofit getInstance() {
            if (retrofit == null) {
                synchronized (RetrofitManager.class) {
                    if (retrofit == null) {
                        retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(OkHttpManager.getInstance())
                            .addConverterFactory(GsonConverterFactory.create(new Gson()))
                            // .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                    }
                }
            }
            return retrofit;
        }
    }
    
    /**
     * API接口定义
     * 使用注解描述HTTP请求
     */
    public interface ApiService {
        
        // GET请求示例
        @GET("users/{id}")
        Call<User> getUser(@Path("id") int userId);
        
        // 带查询参数的GET
        @GET("users")
        Call<List<User>> getUsers(
            @Query("page") int page,
            @Query("size") int size,
            @QueryMap Map<String, String> filters
        );
        
        // POST请求示例
        @POST("users")
        Call<User> createUser(@Body User user);
        
        // PUT请求示例
        @PUT("users/{id}")
        Call<User> updateUser(
            @Path("id") int userId,
            @Body User user
        );
        
        // DELETE请求示例
        @DELETE("users/{id}")
        Call<Void> deleteUser(@Path("id") int userId);
        
        // 表单提交
        @FormUrlEncoded
        @POST("login")
        Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
        );
        
        // 文件上传
        @Multipart
        @POST("upload")
        Call<UploadResponse> uploadFile(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
        );
        
        // 文件下载
        @Streaming
        @GET
        Call<ResponseBody> downloadFile(@Url String fileUrl);
        
        // 自定义Header
        @Headers({
            "Accept: application/json",
            "Cache-Control: max-age=640000"
        })
        @GET("users")
        Call<List<User>> getUsersWithHeaders();
        
        // 动态Header
        @GET("users")
        Call<List<User>> getUsersWithDynamicHeader(
            @Header("Authorization") String auth
        );
    }
    
    /**
     * 数据模型
     */
    public static class User {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("created_at")
        private String createdAt;
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    
    public static class LoginResponse {
        @SerializedName("token")
        private String token;
        
        @SerializedName("user")
        private User user;
        
        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
    }
    
    public static class UploadResponse {
        @SerializedName("success")
        private boolean success;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("file_url")
        private String fileUrl;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    }
    
    /**
     * Retrofit使用示例
     */
    public static class RetrofitUsageExamples {
        private ApiService apiService = RetrofitManager.getInstance().create(ApiService.class);
        
        /**
         * 获取用户信息
         */
        public void getUserExample() {
            Call<User> call = apiService.getUser(123);
            
            // 异步请求
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        User user = response.body();
                        Log.d(TAG, "User: " + user.getName());
                        
                        // 获取响应头
                        Headers headers = response.headers();
                        String cacheControl = headers.get("Cache-Control");
                        
                        // 获取响应码
                        int code = response.code();
                        Log.d(TAG, "Response code: " + code);
                    } else {
                        // 处理错误
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Request failed", t);
                    
                    // 判断错误类型
                    if (t instanceof IOException) {
                        // 网络错误
                        Log.e(TAG, "Network error");
                    } else {
                        // 转换错误等其他错误
                        Log.e(TAG, "Conversion error");
                    }
                }
            });
        }
        
        /**
         * 创建用户
         */
        public void createUserExample() {
            User newUser = new User();
            newUser.setName("John Doe");
            newUser.setEmail("john@example.com");
            
            Call<User> call = apiService.createUser(newUser);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.code() == 201) {
                        User createdUser = response.body();
                        Log.d(TAG, "User created with ID: " + createdUser.getId());
                    }
                }
                
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Create user failed", t);
                }
            });
        }
        
        /**
         * 文件上传示例
         */
        public void uploadFileExample(File file) {
            // 创建文件请求体
            RequestBody fileReqBody = RequestBody.create(
                MediaType.parse("image/*"), 
                file
            );
            
            // 创建MultipartBody.Part
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file", 
                file.getName(), 
                fileReqBody
            );
            
            // 创建描述请求体
            RequestBody description = RequestBody.create(
                MediaType.parse("text/plain"), 
                "Image upload"
            );
            
            Call<UploadResponse> call = apiService.uploadFile(description, part);
            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful()) {
                        UploadResponse result = response.body();
                        Log.d(TAG, "Upload success: " + result.getFileUrl());
                    }
                }
                
                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Log.e(TAG, "Upload failed", t);
                }
            });
        }
        
        /**
         * 同步请求示例（需要在子线程执行）
         */
        public void syncRequestExample() {
            try {
                Response<User> response = apiService.getUser(123).execute();
                if (response.isSuccessful()) {
                    User user = response.body();
                    Log.d(TAG, "Sync request success: " + user.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Sync request failed", e);
            }
        }
        
        /**
         * 取消请求
         */
        public void cancelRequestExample() {
            Call<User> call = apiService.getUser(123);
            
            // 发起请求
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    // Handle response
                }
                
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    if (call.isCanceled()) {
                        Log.d(TAG, "Request was cancelled");
                    }
                }
            });
            
            // 取消请求
            call.cancel();
        }
    }
}