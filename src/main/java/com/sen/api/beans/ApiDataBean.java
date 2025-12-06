package com.sen.api.beans;

/**
 * API测试数据Bean
 * 包含测试用例的所有配置信息
 */
public class ApiDataBean extends BaseBean {

    // ==================== 基础字段 ====================

    /**
     * 是否执行该用例
     */
    private boolean run;

    /**
     * 接口描述
     */
    private String desc;

    /**
     * 接口URL（支持相对路径和绝对路径）
     */
    private String url;

    /**
     * HTTP请求方法（GET/POST/PUT/DELETE/UPLOAD）
     */
    private String method;

    /**
     * 请求参数（JSON格式）
     */
    private String param;

    /**
     * 验证方式：true-包含验证，false-精确验证
     */
    private boolean contains;

    /**
     * 预期HTTP状态码（0表示不验证）
     */
    private int status;

    /**
     * 响应验证表达式（JsonPath格式）
     * 格式: $.id=1;$.name=test
     */
    private String verify;

    /**
     * 保存响应数据到公共参数池
     * 格式: userId=$.id;userName=$.name
     */
    private String save;

    /**
     * 前置参数（函数生成）
     * 格式: randomId=__random(8,true);currentDate=__date(yyyy-MM-dd)
     */
    private String preParam;

    /**
     * 执行前等待时间（秒）
     */
    private int sleep;

    // ==================== 数据库相关字段 ====================

    /**
     * 前置SQL（测试执行前执行）
     * 用于准备测试数据或清理数据
     * 多条SQL用分号分隔
     */
    private String preSql;

    /**
     * 后置SQL（测试执行后执行）
     * 用于清理测试数据
     * 多条SQL用分号分隔
     */
    private String postSql;

    /**
     * 数据库验证表达式
     * 格式: $.fieldName=expectedValue;$.fieldName2=expectedValue2
     */
    private String dbVerify;

    /**
     * 数据库验证SQL（用于dbVerify的查询）
     */
    private String dbSql;

    // ==================== 接口依赖相关字段 ====================

    /**
     * 依赖的接口ID列表（逗号分隔）
     */
    private String dependsOn;

    /**
     * 当前接口ID（用于依赖链管理）
     */
    private String caseId;

    // ==================== 测试套件管理字段 ====================

    /**
     * 测试标签（逗号分隔）
     * 用于筛选和分组执行测试用例
     * 例如: "smoke,login,P0"
     */
    private String tags;

    /**
     * 测试分组
     * 例如: "auth", "user", "order"
     */
    private String group;

    /**
     * 优先级（1-5，1最高）
     */
    private int priority = 3;

    // ==================== Getters and Setters ====================

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public boolean isContains() {
        return contains;
    }

    public void setContains(boolean contains) {
        this.contains = contains;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getPreParam() {
        return preParam;
    }

    public void setPreParam(String preParam) {
        this.preParam = preParam;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public String getPreSql() {
        return preSql;
    }

    public void setPreSql(String preSql) {
        this.preSql = preSql;
    }

    public String getPostSql() {
        return postSql;
    }

    public void setPostSql(String postSql) {
        this.postSql = postSql;
    }

    public String getDbVerify() {
        return dbVerify;
    }

    public void setDbVerify(String dbVerify) {
        this.dbVerify = dbVerify;
    }

    public String getDbSql() {
        return dbSql;
    }

    public void setDbSql(String dbSql) {
        this.dbSql = dbSql;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 检查是否包含指定标签
     */
    public boolean hasTag(String tag) {
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        String[] tagArray = tags.split(",");
        for (String t : tagArray) {
            if (t.trim().equalsIgnoreCase(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取标签数组
     */
    public String[] getTagArray() {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        String[] tagArray = tags.split(",");
        for (int i = 0; i < tagArray.length; i++) {
            tagArray[i] = tagArray[i].trim();
        }
        return tagArray;
    }

    @Override
    public String toString() {
        return String.format("ApiDataBean{caseId='%s', desc='%s', method='%s', url='%s', tags='%s', group='%s', priority=%d}",
                this.caseId, this.desc, this.method, this.url, this.tags, this.group, this.priority);
    }
}
