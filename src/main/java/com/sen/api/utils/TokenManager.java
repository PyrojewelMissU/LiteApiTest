package com.sen.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token管理器
 * 支持：
 * - login 自动获取 token
 * - token 缓存池
 * - token 过期自动刷新
 * - 多账号自动切换（admin/user/guest…）
 *
 * @author sen
 */
public class TokenManager {

    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    /**
     * Token信息
     */
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
        private Instant expiresAt;
        private Instant refreshExpiresAt;
        private String tokenType = "Bearer";
        private Instant createdAt;
        private int refreshCount = 0;

        public TokenInfo() {
            this.createdAt = Instant.now();
        }

        public TokenInfo(String accessToken, long expiresInSeconds) {
            this();
            this.accessToken = accessToken;
            this.expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        }

        public TokenInfo(String accessToken, String refreshToken, long expiresInSeconds, long refreshExpiresInSeconds) {
            this(accessToken, expiresInSeconds);
            this.refreshToken = refreshToken;
            this.refreshExpiresAt = Instant.now().plusSeconds(refreshExpiresInSeconds);
        }

        public boolean isExpired() {
            if (expiresAt == null) {
                return false; // 没有设置过期时间，认为永不过期
            }
            // 提前60秒认为过期，给刷新留出时间
            return Instant.now().isAfter(expiresAt.minusSeconds(60));
        }

        public boolean isRefreshTokenExpired() {
            if (refreshExpiresAt == null) {
                return true;
            }
            return Instant.now().isAfter(refreshExpiresAt);
        }

        public String getAuthorizationHeader() {
            return tokenType + " " + accessToken;
        }

        // Getters and Setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }

        public Instant getRefreshExpiresAt() {
            return refreshExpiresAt;
        }

        public void setRefreshExpiresAt(Instant refreshExpiresAt) {
            this.refreshExpiresAt = refreshExpiresAt;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public int getRefreshCount() {
            return refreshCount;
        }

        public void incrementRefreshCount() {
            this.refreshCount++;
        }
    }

    /**
     * 账号配置
     */
    public static class AccountConfig {
        private String username;
        private String password;
        private String loginUrl;
        private String refreshUrl;
        private String tokenPath = "$.data.accessToken";
        private String refreshTokenPath = "$.data.refreshToken";
        private String expiresInPath = "$.data.expiresIn";
        private long defaultExpiresIn = 7200; // 默认2小时
        private Map<String, String> extraParams = new HashMap<>();

        // Getters and Setters
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

        public String getLoginUrl() {
            return loginUrl;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public String getRefreshUrl() {
            return refreshUrl;
        }

        public void setRefreshUrl(String refreshUrl) {
            this.refreshUrl = refreshUrl;
        }

        public String getTokenPath() {
            return tokenPath;
        }

        public void setTokenPath(String tokenPath) {
            this.tokenPath = tokenPath;
        }

        public String getRefreshTokenPath() {
            return refreshTokenPath;
        }

        public void setRefreshTokenPath(String refreshTokenPath) {
            this.refreshTokenPath = refreshTokenPath;
        }

        public String getExpiresInPath() {
            return expiresInPath;
        }

        public void setExpiresInPath(String expiresInPath) {
            this.expiresInPath = expiresInPath;
        }

        public long getDefaultExpiresIn() {
            return defaultExpiresIn;
        }

        public void setDefaultExpiresIn(long defaultExpiresIn) {
            this.defaultExpiresIn = defaultExpiresIn;
        }

        public Map<String, String> getExtraParams() {
            return extraParams;
        }

        public void setExtraParams(Map<String, String> extraParams) {
            this.extraParams = extraParams;
        }
    }

    // 单例实例
    private static volatile TokenManager instance;

    // Token缓存池（账号 -> TokenInfo）
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    // 账号配置（账号 -> AccountConfig）
    private final Map<String, AccountConfig> accountConfigs = new ConcurrentHashMap<>();

    // 当前账号
    private String currentAccount = "default";

    // Root URL
    private String rootUrl = "";

    // 定时刷新调度器
    private ScheduledExecutorService refreshScheduler;

    // 是否启用自动刷新
    private boolean autoRefreshEnabled = true;

    private TokenManager() {
        logger.info("TokenManager 初始化");
    }

    /**
     * 获取单例实例
     */
    public static TokenManager getInstance() {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化配置
     */
    public void init(String rootUrl) {
        this.rootUrl = rootUrl;
        loadAccountConfigs();
        if (autoRefreshEnabled) {
            startAutoRefresh();
        }
    }

    /**
     * 从配置文件加载账号配置
     */
    @SuppressWarnings("unchecked")
    private void loadAccountConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            // 尝试从classpath加载
            InputStream is = getClass().getClassLoader().getResourceAsStream("env-config.yml");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("token-config.yml");
            }

            if (is != null) {
                Map<String, Object> config = mapper.readValue(is, Map.class);
                is.close();

                Map<String, Object> accounts = (Map<String, Object>) config.get("accounts");
                if (accounts != null) {
                    for (Map.Entry<String, Object> entry : accounts.entrySet()) {
                        String accountName = entry.getKey();
                        Map<String, Object> accountData = (Map<String, Object>) entry.getValue();
                        AccountConfig accountConfig = parseAccountConfig(accountData);
                        accountConfigs.put(accountName, accountConfig);
                        logger.debug("加载账号配置: {}", accountName);
                    }
                }
            }

            logger.info("已加载 {} 个账号配置", accountConfigs.size());

        } catch (Exception e) {
            logger.warn("加载账号配置失败: {}", e.getMessage());
        }
    }

    /**
     * 解析账号配置
     */
    @SuppressWarnings("unchecked")
    private AccountConfig parseAccountConfig(Map<String, Object> data) {
        AccountConfig config = new AccountConfig();
        if (data.get("username") != null) {
            config.setUsername(data.get("username").toString());
        }
        if (data.get("password") != null) {
            config.setPassword(data.get("password").toString());
        }
        if (data.get("loginUrl") != null) {
            config.setLoginUrl(data.get("loginUrl").toString());
        }
        if (data.get("refreshUrl") != null) {
            config.setRefreshUrl(data.get("refreshUrl").toString());
        }
        if (data.get("tokenPath") != null) {
            config.setTokenPath(data.get("tokenPath").toString());
        }
        if (data.get("refreshTokenPath") != null) {
            config.setRefreshTokenPath(data.get("refreshTokenPath").toString());
        }
        if (data.get("expiresInPath") != null) {
            config.setExpiresInPath(data.get("expiresInPath").toString());
        }
        if (data.get("defaultExpiresIn") != null) {
            config.setDefaultExpiresIn(Long.parseLong(data.get("defaultExpiresIn").toString()));
        }
        if (data.get("extraParams") != null) {
            Map<String, Object> extra = (Map<String, Object>) data.get("extraParams");
            extra.forEach((k, v) -> config.getExtraParams().put(k, v != null ? v.toString() : ""));
        }
        return config;
    }

    /**
     * 启动自动刷新定时任务
     */
    private void startAutoRefresh() {
        if (refreshScheduler != null) {
            refreshScheduler.shutdown();
        }

        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TokenRefreshScheduler");
            t.setDaemon(true);
            return t;
        });

        // 每分钟检查一次Token是否需要刷新
        refreshScheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndRefreshTokens();
            } catch (Exception e) {
                logger.error("Token自动刷新异常: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);

        logger.info("Token自动刷新调度器已启动");
    }

    /**
     * 检查并刷新过期的Token
     */
    private void checkAndRefreshTokens() {
        for (Map.Entry<String, TokenInfo> entry : tokenCache.entrySet()) {
            String account = entry.getKey();
            TokenInfo tokenInfo = entry.getValue();

            if (tokenInfo.isExpired()) {
                logger.info("Token即将过期，尝试刷新: {}", account);
                try {
                    if (tokenInfo.getRefreshToken() != null && !tokenInfo.isRefreshTokenExpired()) {
                        refreshToken(account);
                    } else {
                        // RefreshToken也过期了，重新登录
                        login(account);
                    }
                } catch (Exception e) {
                    logger.error("刷新Token失败: {} - {}", account, e.getMessage());
                }
            }
        }
    }

    /**
     * 登录获取Token
     */
    public TokenInfo login(String account) {
        AccountConfig config = accountConfigs.get(account);
        if (config == null) {
            throw new RuntimeException("账号配置不存在: " + account);
        }

        String loginUrl = rootUrl + config.getLoginUrl();
        logger.info("登录获取Token: {} -> {}", account, loginUrl);

        try {
            // 构建登录请求参数
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("username", config.getUsername());
            loginParams.put("password", config.getPassword());
            loginParams.putAll(config.getExtraParams());

            // 发送登录请求
            Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .post(loginUrl);

            if (response.getStatusCode() != 200) {
                throw new RuntimeException("登录失败，状态码: " + response.getStatusCode());
            }

            String responseBody = response.getBody().asString();
            logger.debug("登录响应: {}", responseBody);

            // 解析Token
            String accessToken = JsonPath.read(responseBody, config.getTokenPath());
            String refreshToken = null;
            try {
                refreshToken = JsonPath.read(responseBody, config.getRefreshTokenPath());
            } catch (Exception e) {
                logger.debug("未找到refreshToken");
            }

            long expiresIn = config.getDefaultExpiresIn();
            try {
                Object expiresInObj = JsonPath.read(responseBody, config.getExpiresInPath());
                if (expiresInObj != null) {
                    expiresIn = Long.parseLong(expiresInObj.toString());
                }
            } catch (Exception e) {
                logger.debug("未找到expiresIn，使用默认值: {}", expiresIn);
            }

            // 创建TokenInfo并缓存
            TokenInfo tokenInfo = new TokenInfo(accessToken, refreshToken, expiresIn, expiresIn * 2);
            tokenCache.put(account, tokenInfo);

            logger.info("登录成功: {}, Token有效期: {}秒", account, expiresIn);
            return tokenInfo;

        } catch (Exception e) {
            logger.error("登录失败: {} - {}", account, e.getMessage());
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 刷新Token
     */
    public TokenInfo refreshToken(String account) {
        TokenInfo currentToken = tokenCache.get(account);
        if (currentToken == null || currentToken.getRefreshToken() == null) {
            logger.warn("无法刷新Token，重新登录: {}", account);
            return login(account);
        }

        AccountConfig config = accountConfigs.get(account);
        if (config == null || config.getRefreshUrl() == null) {
            logger.warn("无刷新Token配置，重新登录: {}", account);
            return login(account);
        }

        String refreshUrl = rootUrl + config.getRefreshUrl();
        logger.info("刷新Token: {} -> {}", account, refreshUrl);

        try {
            Map<String, Object> refreshParams = new HashMap<>();
            refreshParams.put("refreshToken", currentToken.getRefreshToken());

            Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(refreshParams)
                .post(refreshUrl);

            if (response.getStatusCode() != 200) {
                logger.warn("刷新Token失败，重新登录: {}", account);
                return login(account);
            }

            String responseBody = response.getBody().asString();

            // 解析新Token
            String accessToken = JsonPath.read(responseBody, config.getTokenPath());
            String refreshTokenNew = currentToken.getRefreshToken();
            try {
                refreshTokenNew = JsonPath.read(responseBody, config.getRefreshTokenPath());
            } catch (Exception ignored) {
            }

            long expiresIn = config.getDefaultExpiresIn();
            try {
                Object expiresInObj = JsonPath.read(responseBody, config.getExpiresInPath());
                if (expiresInObj != null) {
                    expiresIn = Long.parseLong(expiresInObj.toString());
                }
            } catch (Exception ignored) {
            }

            // 更新TokenInfo
            TokenInfo newTokenInfo = new TokenInfo(accessToken, refreshTokenNew, expiresIn, expiresIn * 2);
            newTokenInfo.incrementRefreshCount();
            tokenCache.put(account, newTokenInfo);

            logger.info("Token刷新成功: {}, 刷新次数: {}", account, newTokenInfo.getRefreshCount());
            return newTokenInfo;

        } catch (Exception e) {
            logger.error("刷新Token失败，重新登录: {} - {}", account, e.getMessage());
            return login(account);
        }
    }

    /**
     * 获取Token（自动登录/刷新）
     */
    public String getToken(String account) {
        TokenInfo tokenInfo = tokenCache.get(account);

        // 没有缓存或已过期
        if (tokenInfo == null || tokenInfo.isExpired()) {
            if (tokenInfo != null && tokenInfo.getRefreshToken() != null && !tokenInfo.isRefreshTokenExpired()) {
                tokenInfo = refreshToken(account);
            } else {
                tokenInfo = login(account);
            }
        }

        return tokenInfo.getAccessToken();
    }

    /**
     * 获取当前账号的Token
     */
    public String getCurrentToken() {
        return getToken(currentAccount);
    }

    /**
     * 获取Authorization头
     */
    public String getAuthorizationHeader(String account) {
        TokenInfo tokenInfo = tokenCache.get(account);
        if (tokenInfo == null) {
            tokenInfo = login(account);
        } else if (tokenInfo.isExpired()) {
            tokenInfo = refreshToken(account);
        }
        return tokenInfo.getAuthorizationHeader();
    }

    /**
     * 获取当前账号的Authorization头
     */
    public String getCurrentAuthorizationHeader() {
        return getAuthorizationHeader(currentAccount);
    }

    /**
     * 切换当前账号
     */
    public void switchAccount(String account) {
        logger.info("切换账号: {} -> {}", currentAccount, account);
        this.currentAccount = account;
    }

    /**
     * 获取当前账号
     */
    public String getCurrentAccount() {
        return currentAccount;
    }

    /**
     * 手动设置Token
     */
    public void setToken(String account, String accessToken, long expiresInSeconds) {
        TokenInfo tokenInfo = new TokenInfo(accessToken, expiresInSeconds);
        tokenCache.put(account, tokenInfo);
        logger.debug("手动设置Token: {}", account);
    }

    /**
     * 手动设置Token（带RefreshToken）
     */
    public void setToken(String account, String accessToken, String refreshToken, long expiresInSeconds) {
        TokenInfo tokenInfo = new TokenInfo(accessToken, refreshToken, expiresInSeconds, expiresInSeconds * 2);
        tokenCache.put(account, tokenInfo);
        logger.debug("手动设置Token: {}", account);
    }

    /**
     * 清除指定账号的Token
     */
    public void clearToken(String account) {
        tokenCache.remove(account);
        logger.debug("清除Token: {}", account);
    }

    /**
     * 清除所有Token
     */
    public void clearAllTokens() {
        tokenCache.clear();
        logger.info("清除所有Token");
    }

    /**
     * 添加账号配置
     */
    public void addAccountConfig(String account, AccountConfig config) {
        accountConfigs.put(account, config);
        logger.debug("添加账号配置: {}", account);
    }

    /**
     * 移除账号配置
     */
    public void removeAccountConfig(String account) {
        accountConfigs.remove(account);
        tokenCache.remove(account);
        logger.debug("移除账号配置: {}", account);
    }

    /**
     * 获取所有已登录的账号
     */
    public Map<String, TokenInfo> getAllTokens() {
        return new HashMap<>(tokenCache);
    }

    /**
     * 设置Root URL
     */
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * 启用/禁用自动刷新
     */
    public void setAutoRefreshEnabled(boolean enabled) {
        this.autoRefreshEnabled = enabled;
        if (enabled && refreshScheduler == null) {
            startAutoRefresh();
        } else if (!enabled && refreshScheduler != null) {
            refreshScheduler.shutdown();
            refreshScheduler = null;
        }
    }

    /**
     * 关闭TokenManager
     */
    public void shutdown() {
        if (refreshScheduler != null) {
            refreshScheduler.shutdown();
            try {
                if (!refreshScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    refreshScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                refreshScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        tokenCache.clear();
        logger.info("TokenManager 已关闭");
    }

    /**
     * 打印Token状态
     */
    public void printTokenStatus() {
        logger.info("========== Token状态 ==========");
        logger.info("当前账号: {}", currentAccount);
        for (Map.Entry<String, TokenInfo> entry : tokenCache.entrySet()) {
            TokenInfo info = entry.getValue();
            logger.info("账号: {}, 过期: {}, 刷新次数: {}",
                entry.getKey(),
                info.isExpired() ? "是" : "否",
                info.getRefreshCount());
        }
        logger.info("================================");
    }
}
