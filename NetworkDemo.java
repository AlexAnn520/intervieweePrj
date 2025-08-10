package com.example.network;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Android原生网络请求实现
 * 演示HttpURLConnection的基本使用
 */
public class NetworkDemo {
    private static final String TAG = "NetworkDemo";
    private static final int TIMEOUT_CONNECT = 15000; // 连接超时15秒
    private static final int TIMEOUT_READ = 10000;    // 读取超时10秒
    
    /**
     * GET请求实现
     * 特点：
     * 1. 幂等性：多次请求结果相同
     * 2. 可缓存：浏览器会缓存GET请求
     * 3. 有长度限制：URL长度限制（一般2048字符）
     * 4. 参数在URL中：安全性较低
     */
    public static class GetRequest extends AsyncTask<String, Void, String> {
        
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            
            try {
                // 1. 创建URL对象
                URL url = new URL(urlString);
                
                // 2. 打开连接
                connection = (HttpURLConnection) url.openConnection();
                
                // 3. 设置请求方法
                connection.setRequestMethod("GET");
                
                // 4. 设置超时时间
                connection.setConnectTimeout(TIMEOUT_CONNECT);
                connection.setReadTimeout(TIMEOUT_READ);
                
                // 5. 设置请求头
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Android Network Demo");
                connection.setRequestProperty("Cache-Control", "max-age=0");
                
                // 6. 获取响应码
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);
                
                // 7. 读取响应
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    
                    return response.toString();
                } else {
                    Log.e(TAG, "GET request failed with code: " + responseCode);
                    return null;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "GET request error", e);
                return null;
            } finally {
                // 8. 关闭资源
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, "GET Response: " + result);
            }
        }
    }
    
    /**
     * POST请求实现
     * 特点：
     * 1. 非幂等性：多次请求可能产生不同结果
     * 2. 不可缓存：默认不缓存POST请求
     * 3. 无长度限制：数据在请求体中
     * 4. 相对安全：参数不在URL中显示
     */
    public static class PostRequest extends AsyncTask<String, Void, String> {
        private String postData;
        
        public PostRequest(String data) {
            this.postData = data;
        }
        
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                
                // 设置为POST请求
                connection.setRequestMethod("POST");
                connection.setDoOutput(true); // 允许输出数据
                connection.setDoInput(true);  // 允许输入数据
                
                // 设置超时
                connection.setConnectTimeout(TIMEOUT_CONNECT);
                connection.setReadTimeout(TIMEOUT_READ);
                
                // 设置请求头
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Connection", "keep-alive"); // HTTP/1.1持久连接
                
                // 写入POST数据
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                
                // 获取响应
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "POST Response Code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK || 
                    responseCode == HttpURLConnection.HTTP_CREATED) {
                    
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    
                    return response.toString();
                } else {
                    // 读取错误信息
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder error = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }
                        Log.e(TAG, "Error response: " + error.toString());
                    }
                    return null;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "POST request error", e);
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
    
    /**
     * HTTPS请求实现
     * 演示SSL/TLS配置
     */
    public static class HttpsRequest {
        
        /**
         * 配置信任所有证书（仅用于测试环境）
         * 生产环境应该使用正确的证书验证
         */
        private static void trustAllCertificates() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        
                        public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        
                        public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };
                
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to trust all certificates", e);
            }
        }
        
        /**
         * 执行HTTPS请求
         */
        public static String executeHttpsGet(String urlString) {
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            
            try {
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                
                // 设置请求属性
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_CONNECT);
                connection.setReadTimeout(TIMEOUT_READ);
                
                // 添加安全相关的请求头
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                
                // 获取响应
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "HTTPS Response Code: " + responseCode);
                
                // 获取服务器证书信息
                if (connection.getServerCertificates() != null) {
                    Log.d(TAG, "Server certificates count: " + 
                          connection.getServerCertificates().length);
                }
                
                // 读取响应数据
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    
                    return response.toString();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "HTTPS request error", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            
            return null;
        }
    }
    
    /**
     * 文件上传实现
     * 使用multipart/form-data格式
     */
    public static class FileUploadRequest {
        private static final String BOUNDARY = "----WebKitFormBoundary" + System.currentTimeMillis();
        private static final String LINE_END = "\r\n";
        
        public static String uploadFile(String urlString, String filePath, Map<String, String> params) {
            HttpURLConnection connection = null;
            
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                
                // 设置请求属性
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                
                // 设置Content-Type为multipart/form-data
                connection.setRequestProperty("Content-Type", 
                    "multipart/form-data; boundary=" + BOUNDARY);
                connection.setRequestProperty("Connection", "Keep-Alive");
                
                OutputStream outputStream = connection.getOutputStream();
                
                // 添加普通参数
                if (params != null) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("--").append(BOUNDARY).append(LINE_END);
                        sb.append("Content-Disposition: form-data; name=\"")
                          .append(entry.getKey()).append("\"").append(LINE_END);
                        sb.append(LINE_END);
                        sb.append(entry.getValue()).append(LINE_END);
                        
                        outputStream.write(sb.toString().getBytes("UTF-8"));
                    }
                }
                
                // 添加文件
                StringBuilder fileSb = new StringBuilder();
                fileSb.append("--").append(BOUNDARY).append(LINE_END);
                fileSb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                      .append(filePath).append("\"").append(LINE_END);
                fileSb.append("Content-Type: application/octet-stream").append(LINE_END);
                fileSb.append(LINE_END);
                
                outputStream.write(fileSb.toString().getBytes("UTF-8"));
                
                // 写入文件内容（这里简化处理）
                // 实际应该读取文件并写入
                outputStream.write("File content here".getBytes());
                
                // 结束标记
                String endLine = LINE_END + "--" + BOUNDARY + "--" + LINE_END;
                outputStream.write(endLine.getBytes("UTF-8"));
                
                outputStream.flush();
                outputStream.close();
                
                // 获取响应
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 读取响应...
                    return "Upload successful";
                }
                
            } catch (Exception e) {
                Log.e(TAG, "File upload error", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            
            return null;
        }
    }
    
    /**
     * 断点续传下载实现
     * 支持暂停和恢复下载
     */
    public static class ResumeDownload {
        
        public static void downloadWithResume(String urlString, String savePath, long startPos) {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                
                // 设置断点续传的Range头
                if (startPos > 0) {
                    connection.setRequestProperty("Range", "bytes=" + startPos + "-");
                    Log.d(TAG, "Resume download from position: " + startPos);
                }
                
                connection.setConnectTimeout(TIMEOUT_CONNECT);
                connection.setReadTimeout(TIMEOUT_READ);
                
                int responseCode = connection.getResponseCode();
                
                // 206表示部分内容，用于断点续传
                if (responseCode == HttpURLConnection.HTTP_OK || 
                    responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    
                    // 获取文件总大小
                    long contentLength = connection.getContentLength();
                    Log.d(TAG, "File size: " + contentLength);
                    
                    inputStream = connection.getInputStream();
                    
                    // 读取并保存文件（简化处理）
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = startPos;
                    
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // 写入文件...
                        totalBytesRead += bytesRead;
                        
                        // 计算下载进度
                        int progress = (int) ((totalBytesRead * 100) / contentLength);
                        Log.d(TAG, "Download progress: " + progress + "%");
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Download error", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}