package com.sen.api.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 环境中心（ENV Center）
 * 支持多环境管理：dev / test / stage / prod
 * 通过命令参数切换：-Denv=test
 * 不同环境的 URL / token / headers 自动切换
 *
 * @author sen
 */
public class EnvCenter {

    private static final Logger logger = LoggerFactory.getLogger(EnvCenter.class);

    /**
     * 环境枚举
     */
    public enum Environment {
        DEV("dev", "开发环境"),
        TEST("test", "测试环境"),
        STAGE("stage", "预发布环境"),
        PROD("prod", "生产环境");

        private final String code;
        private final String name;

        Environment(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static Environment fromCode(String code) {
            for (Environment env : values()) {
                if (env.code.equalsIgnoreCase(code)) {
                    return env;
                }
            }
            logger.warn("未知环境: {}, 使用默认环境DEV", code);
            return DEV;
        }
    }

    /**
     * 环境配置
     */
    public static class EnvConfig {
        private String rootUrl;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params = new HashMap<>();
        private Map<String, String> tokens = new HashMap<>();
        private DatabaseConfig database;
        private MockConfig mock;

        // Getters and Setters
        public String getRootUrl() {
            return rootUrl;
        }

        public void setRootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
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

        public Map<String, String> getTokens() {
            return tokens;
        }

        public void setTokens(Map<String, String> tokens) {
            this.tokens = tokens;
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
    }

    /**
     * 数据库配置
     */
    public static class DatabaseConfig {
        private boolean enabled;
        private String url;
        private String username;
        private String password;
        private String driver = "com.mysql.cj.jdbc.Driver";

        // Getters and Setters
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

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }
    }

    /**
     * Mock配置
     */
    public static class MockConfig {
        private boolean enabled;
        private int port = 8089;
        private String host = "localhost";

        // Getters and Setters
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

    // 单例实例
    private static volatile EnvCenter instance;

    // 当前环境
    private Environment currentEnv;

    // 环境配置缓存
    private final Map<Environment, EnvConfig> envConfigs = new ConcurrentHashMap<>();

    // 当前生效的配置
    private EnvConfig currentConfig;

    private EnvCenter() {
        // 从系统属性或环境变量获取环境
        String envCode = System.getProperty("env");
        if (envCode == null || envCode.isEmpty()) {
            envCode = System.getenv("ENV");
        }
        if (envCode == null || envCode.isEmpty()) {
            envCode = System.getProperty("spring.profiles.active", "dev");
        }

        this.currentEnv = Environment.fromCode(envCode);
        logger.info("EnvCenter 初始化，当前环境: {} ({})", currentEnv.getName(), currentEnv.getCode());
    }

    /**
     * 获取单例实例
     */
    public static EnvCenter getInstance() {
        if (instance == null) {
            synchronized (EnvCenter.class) {
                if (instance == null) {
                    instance = new EnvCenter();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化并加载所有环境配置
     */
    public void init() {
        init("env-config.yml");
    }

    /**
     * 从指定文件初始化环境配置
     */
    @SuppressWarnings("unchecked")
    public void init(String configPath) {
        logger.info("加载环境配置文件: {}", configPath);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> allConfig;

            // 尝试从classpath加载
            InputStream is = getClass().getClassLoader().getResourceAsStream(configPath);
            if (is != null) {
                allConfig = mapper.readValue(is, Map.class);
                is.close();
            } else {
                // 尝试从文件系统加载
                File file = new File(configPath);
                if (file.exists()) {
                    allConfig = mapper.readValue(file, Map.class);
                } else {
                    logger.warn("环境配置文件不存在: {}, 使用默认配置", configPath);
                    loadDefaultConfigs();
                    return;
                }
            }

            // 解析各环境配置
            Map<String, Object> environments = (Map<String, Object>) allConfig.get("environments");
            if (environments != null) {
                for (Environment env : Environment.values()) {
                    Map<String, Object> envData = (Map<String, Object>) environments.get(env.getCode());
                    if (envData != null) {
                        EnvConfig config = parseEnvConfig(envData);
                        envConfigs.put(env, config);
                        logger.debug("已加载环境配置: {}", env.getCode());
                    }
                }
            }

            // 设置当前配置
            currentConfig = envConfigs.get(currentEnv);
            if (currentConfig == null) {
                logger.warn("当前环境 {} 无配置，使用默认配置", currentEnv.getCode());
                currentConfig = new EnvConfig();
                currentConfig.setRootUrl("https://jsonplaceholder.typicode.com");
            }

            logger.info("环境配置加载完成，当前环境: {}, Root URL: {}",
                currentEnv.getCode(), currentConfig.getRootUrl());

        } catch (IOException e) {
            logger.error("加载环境配置文件失败: {}", e.getMessage());
            loadDefaultConfigs();
        }
    }

    /**
     * 解析环境配置
     */
    @SuppressWarnings("unchecked")
    private EnvConfig parseEnvConfig(Map<String, Object> data) {
        EnvConfig config = new EnvConfig();

        if (data.get("rootUrl") != null) {
            config.setRootUrl(data.get("rootUrl").toString());
        }

        if (data.get("headers") != null) {
            Map<String, Object> headers = (Map<String, Object>) data.get("headers");
            headers.forEach((k, v) -> config.getHeaders().put(k, v != null ? v.toString() : ""));
        }

        if (data.get("params") != null) {
            Map<String, Object> params = (Map<String, Object>) data.get("params");
            params.forEach((k, v) -> config.getParams().put(k, v != null ? v.toString() : ""));
        }

        if (data.get("tokens") != null) {
            Map<String, Object> tokens = (Map<String, Object>) data.get("tokens");
            tokens.forEach((k, v) -> config.getTokens().put(k, v != null ? v.toString() : ""));
        }

        // 解析数据库配置
        if (data.get("database") != null) {
            Map<String, Object> dbData = (Map<String, Object>) data.get("database");
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setEnabled(Boolean.parseBoolean(dbData.getOrDefault("enabled", "false").toString()));
            dbConfig.setUrl((String) dbData.get("url"));
            dbConfig.setUsername((String) dbData.get("username"));
            dbConfig.setPassword((String) dbData.get("password"));
            if (dbData.get("driver") != null) {
                dbConfig.setDriver(dbData.get("driver").toString());
            }
            config.setDatabase(dbConfig);
        }

        // 解析Mock配置
        if (data.get("mock") != null) {
            Map<String, Object> mockData = (Map<String, Object>) data.get("mock");
            MockConfig mockConfig = new MockConfig();
            mockConfig.setEnabled(Boolean.parseBoolean(mockData.getOrDefault("enabled", "false").toString()));
            if (mockData.get("port") != null) {
                mockConfig.setPort(Integer.parseInt(mockData.get("port").toString()));
            }
            if (mockData.get("host") != null) {
                mockConfig.setHost(mockData.get("host").toString());
            }
            config.setMock(mockConfig);
        }

        return config;
    }

    /**
     * 加载默认配置
     */
    private void loadDefaultConfigs() {
        // DEV环境
        EnvConfig devConfig = new EnvConfig();
        devConfig.setRootUrl("https://jsonplaceholder.typicode.com");
        devConfig.getHeaders().put("Content-Type", "application/json");
        devConfig.getHeaders().put("Accept", "application/json");
        MockConfig devMock = new MockConfig();
        devMock.setEnabled(true);
        devConfig.setMock(devMock);
        envConfigs.put(Environment.DEV, devConfig);

        // TEST环境
        EnvConfig testConfig = new EnvConfig();
        testConfig.setRootUrl("https://api-test.example.com");
        testConfig.getHeaders().put("Content-Type", "application/json");
        testConfig.getHeaders().put("Accept", "application/json");
        envConfigs.put(Environment.TEST, testConfig);

        // STAGE环境
        EnvConfig stageConfig = new EnvConfig();
        stageConfig.setRootUrl("https://api-stage.example.com");
        stageConfig.getHeaders().put("Content-Type", "application/json");
        stageConfig.getHeaders().put("Accept", "application/json");
        envConfigs.put(Environment.STAGE, stageConfig);

        // PROD环境
        EnvConfig prodConfig = new EnvConfig();
        prodConfig.setRootUrl("https://api.example.com");
        prodConfig.getHeaders().put("Content-Type", "application/json");
        prodConfig.getHeaders().put("Accept", "application/json");
        envConfigs.put(Environment.PROD, prodConfig);

        currentConfig = envConfigs.get(currentEnv);
        logger.info("已加载默认环境配置");
    }

    /**
     * 切换环境
     */
    public void switchEnvironment(Environment env) {
        logger.info("切换环境: {} -> {}", currentEnv.getCode(), env.getCode());
        this.currentEnv = env;
        this.currentConfig = envConfigs.get(env);
        if (this.currentConfig == null) {
            logger.warn("环境 {} 无配置，创建空配置", env.getCode());
            this.currentConfig = new EnvConfig();
        }
    }

    /**
     * 切换环境（通过环境代码）
     */
    public void switchEnvironment(String envCode) {
        switchEnvironment(Environment.fromCode(envCode));
    }

    /**
     * 获取当前环境
     */
    public Environment getCurrentEnv() {
        return currentEnv;
    }

    /**
     * 获取当前环境代码
     */
    public String getCurrentEnvCode() {
        return currentEnv.getCode();
    }

    /**
     * 获取当前环境配置
     */
    public EnvConfig getCurrentConfig() {
        return currentConfig;
    }

    /**
     * 获取指定环境配置
     */
    public EnvConfig getConfig(Environment env) {
        return envConfigs.get(env);
    }

    /**
     * 获取当前环境的Root URL
     */
    public String getRootUrl() {
        return currentConfig != null ? currentConfig.getRootUrl() : "";
    }

    /**
     * 获取当前环境的Headers
     */
    public Map<String, String> getHeaders() {
        return currentConfig != null ? currentConfig.getHeaders() : new HashMap<>();
    }

    /**
     * 获取当前环境的参数
     */
    public Map<String, String> getParams() {
        return currentConfig != null ? currentConfig.getParams() : new HashMap<>();
    }

    /**
     * 获取当前环境的Token配置
     */
    public Map<String, String> getTokens() {
        return currentConfig != null ? currentConfig.getTokens() : new HashMap<>();
    }

    /**
     * 获取指定账号的Token
     */
    public String getToken(String account) {
        if (currentConfig != null && currentConfig.getTokens() != null) {
            return currentConfig.getTokens().get(account);
        }
        return null;
    }

    /**
     * 设置Token（运行时动态设置）
     */
    public void setToken(String account, String token) {
        if (currentConfig != null) {
            currentConfig.getTokens().put(account, token);
            logger.debug("设置Token: {} = {}", account, token.substring(0, Math.min(10, token.length())) + "...");
        }
    }

    /**
     * 获取数据库配置
     */
    public DatabaseConfig getDatabaseConfig() {
        return currentConfig != null ? currentConfig.getDatabase() : null;
    }

    /**
     * 获取Mock配置
     */
    public MockConfig getMockConfig() {
        return currentConfig != null ? currentConfig.getMock() : null;
    }

    /**
     * 判断Mock是否启用
     */
    public boolean isMockEnabled() {
        MockConfig mock = getMockConfig();
        return mock != null && mock.isEnabled();
    }

    /**
     * 判断数据库是否启用
     */
    public boolean isDatabaseEnabled() {
        DatabaseConfig db = getDatabaseConfig();
        return db != null && db.isEnabled();
    }

    /**
     * 更新环境配置（运行时）
     */
    public void updateConfig(String key, String value) {
        if (currentConfig == null) {
            currentConfig = new EnvConfig();
        }
        switch (key.toLowerCase()) {
            case "rooturl":
                currentConfig.setRootUrl(value);
                break;
            default:
                currentConfig.getParams().put(key, value);
        }
        logger.debug("更新配置: {} = {}", key, value);
    }

    /**
     * 添加Header
     */
    public void addHeader(String key, String value) {
        if (currentConfig != null) {
            currentConfig.getHeaders().put(key, value);
        }
    }

    /**
     * 移除Header
     */
    public void removeHeader(String key) {
        if (currentConfig != null) {
            currentConfig.getHeaders().remove(key);
        }
    }

    /**
     * 重置为默认配置
     */
    public void reset() {
        loadDefaultConfigs();
        logger.info("环境配置已重置");
    }

    /**
     * 打印当前环境信息
     */
    public void printCurrentEnvInfo() {
        logger.info("========== 当前环境信息 ==========");
        logger.info("环境: {} ({})", currentEnv.getName(), currentEnv.getCode());
        logger.info("Root URL: {}", getRootUrl());
        logger.info("Headers: {}", getHeaders());
        logger.info("Params: {}", getParams());
        logger.info("Mock启用: {}", isMockEnabled());
        logger.info("数据库启用: {}", isDatabaseEnabled());
        logger.info("==================================");
    }
}
