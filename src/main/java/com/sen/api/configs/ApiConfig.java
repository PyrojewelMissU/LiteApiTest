package com.sen.api.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sen.api.utils.ReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API配置类
 * 支持YAML和XML格式配置文件（推荐使用YAML）
 *
 * @author sen
 */
public class ApiConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);

    private String rootUrl = "";
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String projectName = "API自动化测试平台";

    /**
     * 从YAML文件加载配置
     */
    public ApiConfig(String configFilePath) throws IOException {
        logger.info("加载API配置文件: {}", configFilePath);

        if (configFilePath.toLowerCase().endsWith(".yml") || configFilePath.toLowerCase().endsWith(".yaml")) {
            loadFromYaml(configFilePath);
        } else {
            throw new IllegalArgumentException("不支持的配置文件格式，请使用YAML格式: " + configFilePath);
        }

        // 设置报告名称
        if (projectName != null && !projectName.isEmpty()) {
            ReportUtil.setReportName(projectName);
        }

        logger.info("配置加载完成 - rootUrl: {}, headers: {}, params: {}",
                rootUrl, headers.keySet(), params.keySet());
    }

    /**
     * 从YAML加载配置
     */
    @SuppressWarnings("unchecked")
    private void loadFromYaml(String yamlPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File file = new File(yamlPath);

        if (!file.exists()) {
            throw new IOException("配置文件不存在: " + yamlPath);
        }

        Map<String, Object> config = mapper.readValue(file, Map.class);

        // 读取api配置
        Map<String, Object> apiConfig = (Map<String, Object>) config.get("api");
        if (apiConfig != null) {
            if (apiConfig.get("rootUrl") != null) {
                this.rootUrl = apiConfig.get("rootUrl").toString();
            }

            Map<String, Object> headersMap = (Map<String, Object>) apiConfig.get("headers");
            if (headersMap != null) {
                headersMap.forEach((key, value) -> {
                    if (value != null) {
                        this.headers.put(key, value.toString());
                    }
                });
            }

            Map<String, Object> paramsMap = (Map<String, Object>) apiConfig.get("params");
            if (paramsMap != null) {
                paramsMap.forEach((key, value) -> {
                    if (value != null) {
                        this.params.put(key, value.toString());
                    }
                });
            }
        }

        // 读取项目配置
        Map<String, Object> projectConfig = (Map<String, Object>) config.get("project");
        if (projectConfig != null && projectConfig.get("name") != null) {
            this.projectName = projectConfig.get("name").toString();
        }

        logger.debug("YAML配置加载成功");
    }

    /**
     * 默认构造函数（用于Spring注入）
     */
    public ApiConfig() {
    }

    /**
     * 从ApiProperties创建配置
     */
    public static ApiConfig fromProperties(ApiProperties properties) {
        ApiConfig config = new ApiConfig();
        config.rootUrl = properties.getRootUrl();
        config.headers = new HashMap<>(properties.getHeaders());
        config.params = new HashMap<>(properties.getParams());
        config.projectName = properties.getProjectName();

        if (config.projectName != null && !config.projectName.isEmpty()) {
            ReportUtil.setReportName(config.projectName);
        }

        return config;
    }

    // Getters
    public String getRootUrl() {
        return rootUrl;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getProjectName() {
        return projectName;
    }

    // Setters (用于Spring配置)
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
        if (projectName != null && !projectName.isEmpty()) {
            ReportUtil.setReportName(projectName);
        }
    }
}
