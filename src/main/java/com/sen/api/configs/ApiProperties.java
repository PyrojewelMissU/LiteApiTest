package com.sen.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * API配置属性类
 * 使用Spring Boot配置绑定，替代XML解析
 */
@Component
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    /**
     * API根路径
     */
    private String rootUrl = "";

    /**
     * 项目名称
     */
    private String projectName = "API自动化测试平台";

    /**
     * 全局请求头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 全局参数
     */
    private Map<String, String> params = new HashMap<>();

    /**
     * 数据库配置
     */
    private DatabaseConfig database = new DatabaseConfig();

    /**
     * Mock配置
     */
    private MockConfig mock = new MockConfig();

    /**
     * 测试配置
     */
    private TestConfig test = new TestConfig();

    // Getters and Setters
    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public MockConfig getMock() {
        return mock;
    }

    public void setMock(MockConfig mock) {
        this.mock = mock;
    }

    public TestConfig getTest() {
        return test;
    }

    public void setTest(TestConfig test) {
        this.test = test;
    }

    /**
     * 数据库配置
     */
    public static class DatabaseConfig {
        private boolean enabled = false;
        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }

    /**
     * Mock服务配置
     */
    public static class MockConfig {
        private boolean enabled = false;
        private int port = 8089;
        private String host = "localhost";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    /**
     * 测试配置
     */
    public static class TestConfig {
        private int parallelThreads = 5;
        private int retryCount = 2;
        private int timeoutSeconds = 60;
        private String dataPath = "data/api-test-data.json";
        private String dataFormat = "json";

        /**
         * 超时配置
         */
        private TimeoutConfig timeout = new TimeoutConfig();

        public int getParallelThreads() {
            return parallelThreads;
        }

        public void setParallelThreads(int parallelThreads) {
            this.parallelThreads = parallelThreads;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public String getDataPath() {
            return dataPath;
        }

        public void setDataPath(String dataPath) {
            this.dataPath = dataPath;
        }

        public String getDataFormat() {
            return dataFormat;
        }

        public void setDataFormat(String dataFormat) {
            this.dataFormat = dataFormat;
        }

        public TimeoutConfig getTimeout() {
            return timeout;
        }

        public void setTimeout(TimeoutConfig timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * HTTP超时配置
     */
    public static class TimeoutConfig {
        /**
         * 连接超时（毫秒）
         */
        private int connect = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int read = 30000;

        /**
         * 写入超时（毫秒）
         */
        private int write = 30000;

        /**
         * 连接池获取超时（毫秒）
         */
        private int connectionRequest = 5000;

        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }

        public int getWrite() {
            return write;
        }

        public void setWrite(int write) {
            this.write = write;
        }

        public int getConnectionRequest() {
            return connectionRequest;
        }

        public void setConnectionRequest(int connectionRequest) {
            this.connectionRequest = connectionRequest;
        }
    }
}
