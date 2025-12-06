package com.sen.api.utils;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testng.Assert;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 数据库断言工具类
 * 用于测试前后的数据库验证
 */
public class DbAssertUtil {

    private static final Logger logger = LoggerFactory.getLogger(DbAssertUtil.class);

    private JdbcTemplate jdbcTemplate;
    private boolean enabled = false;

    public DbAssertUtil() {
        // 默认构造，未启用
    }

    /**
     * 初始化数据库连接
     */
    public DbAssertUtil(String url, String username, String password) {
        this(url, username, password, "com.mysql.cj.jdbc.Driver");
    }

    /**
     * 初始化数据库连接（指定驱动）
     */
    public DbAssertUtil(String url, String username, String password, String driverClassName) {
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            this.jdbcTemplate = new JdbcTemplate(dataSource);
            this.enabled = true;
            logger.info("Database assertion initialized: {}", url);
        } catch (Exception e) {
            logger.error("Failed to initialize database connection", e);
            this.enabled = false;
        }
    }

    /**
     * 使用已有DataSource初始化
     */
    public DbAssertUtil(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.enabled = true;
    }

    /**
     * 检查是否已启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 执行前置SQL（通常用于清理数据或准备测试数据）
     */
    public void executePreSql(String sql) {
        if (!enabled || StringUtil.isBlank(sql)) {
            return;
        }
        try {
            String[] sqls = sql.split(";");
            for (String s : sqls) {
                if (StringUtil.isNotBlank(s)) {
                    logger.debug("Executing pre-SQL: {}", s.trim());
                    jdbcTemplate.execute(s.trim());
                }
            }
            logger.info("Pre-SQL executed successfully");
        } catch (Exception e) {
            logger.error("Failed to execute pre-SQL: {}", sql, e);
            throw new RuntimeException("执行前置SQL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行后置SQL（通常用于验证数据或清理）
     */
    public void executePostSql(String sql) {
        if (!enabled || StringUtil.isBlank(sql)) {
            return;
        }
        try {
            String[] sqls = sql.split(";");
            for (String s : sqls) {
                if (StringUtil.isNotBlank(s)) {
                    logger.debug("Executing post-SQL: {}", s.trim());
                    jdbcTemplate.execute(s.trim());
                }
            }
            logger.info("Post-SQL executed successfully");
        } catch (Exception e) {
            logger.error("Failed to execute post-SQL: {}", sql, e);
            throw new RuntimeException("执行后置SQL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询单个值
     */
    public Object queryForObject(String sql, Class<?> requiredType) {
        if (!enabled) {
            logger.warn("Database not enabled, skip query");
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(sql, requiredType);
        } catch (Exception e) {
            logger.error("Failed to query: {}", sql, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询单行数据
     */
    public Map<String, Object> queryForMap(String sql) {
        if (!enabled) {
            logger.warn("Database not enabled, skip query");
            return null;
        }
        try {
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            logger.error("Failed to query map: {}", sql, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询多行数据
     */
    public List<Map<String, Object>> queryForList(String sql) {
        if (!enabled) {
            logger.warn("Database not enabled, skip query");
            return null;
        }
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Failed to query list: {}", sql, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 数据库断言 - 验证查询结果
     *
     * @param sql      查询SQL
     * @param jsonPath JsonPath表达式
     * @param expected 期望值
     */
    public void assertQuery(String sql, String jsonPath, Object expected) {
        if (!enabled) {
            logger.warn("Database not enabled, skip assertion");
            return;
        }

        List<Map<String, Object>> result = queryForList(sql);
        String jsonResult = JsonUtil.toJsonString(result);
        logger.debug("Query result: {}", jsonResult);

        Object actual = JsonPath.read(jsonResult, jsonPath);
        logger.info("DB Assert: {} = {} (expected: {})", jsonPath, actual, expected);

        Assert.assertEquals(String.valueOf(actual), String.valueOf(expected),
                "数据库验证失败: " + jsonPath);
    }

    /**
     * 数据库断言 - 验证记录数量
     */
    public void assertCount(String sql, int expectedCount) {
        if (!enabled) {
            logger.warn("Database not enabled, skip assertion");
            return;
        }

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        logger.info("DB Assert Count: {} (expected: {})", count, expectedCount);

        Assert.assertEquals(count.intValue(), expectedCount,
                "数据库记录数验证失败");
    }

    /**
     * 数据库断言 - 验证记录存在
     */
    public void assertExists(String sql) {
        if (!enabled) {
            logger.warn("Database not enabled, skip assertion");
            return;
        }

        List<Map<String, Object>> result = queryForList(sql);
        Assert.assertFalse(result.isEmpty(), "数据库记录不存在");
        logger.info("DB Assert Exists: passed");
    }

    /**
     * 数据库断言 - 验证记录不存在
     */
    public void assertNotExists(String sql) {
        if (!enabled) {
            logger.warn("Database not enabled, skip assertion");
            return;
        }

        List<Map<String, Object>> result = queryForList(sql);
        Assert.assertTrue(result.isEmpty(), "数据库记录不应存在但实际存在");
        logger.info("DB Assert Not Exists: passed");
    }

    /**
     * 验证数据库字段值
     *
     * @param dbVerify 验证表达式，格式: "$.fieldName=expectedValue;$.fieldName2=expectedValue2"
     * @param sql      查询SQL
     */
    public void verifyDbResult(String sql, String dbVerify) {
        if (!enabled || StringUtil.isBlank(dbVerify)) {
            return;
        }

        List<Map<String, Object>> result = queryForList(sql);
        if (result.isEmpty()) {
            Assert.fail("数据库查询无结果: " + sql);
            return;
        }

        String jsonResult = JsonUtil.toJsonString(result);
        logger.debug("DB Query Result: {}", jsonResult);

        // 解析验证表达式
        String[] verifies = dbVerify.split(";");
        for (String verify : verifies) {
            if (StringUtil.isBlank(verify)) {
                continue;
            }
            String[] parts = verify.split("=", 2);
            if (parts.length != 2) {
                continue;
            }

            String jsonPath = parts[0].trim();
            String expected = parts[1].trim();

            Object actual = JsonPath.read(jsonResult, jsonPath);
            logger.info("DB Verify: {} = {} (expected: {})", jsonPath, actual, expected);

            Assert.assertEquals(String.valueOf(actual), expected,
                    "数据库验证失败: " + jsonPath);
        }
    }

    /**
     * 更新数据
     */
    public int update(String sql) {
        if (!enabled) {
            logger.warn("Database not enabled, skip update");
            return 0;
        }
        try {
            int rows = jdbcTemplate.update(sql);
            logger.info("Updated {} rows", rows);
            return rows;
        } catch (Exception e) {
            logger.error("Failed to update: {}", sql, e);
            throw new RuntimeException("更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除数据
     */
    public int delete(String sql) {
        return update(sql);
    }

    /**
     * 插入数据
     */
    public int insert(String sql) {
        return update(sql);
    }
}
