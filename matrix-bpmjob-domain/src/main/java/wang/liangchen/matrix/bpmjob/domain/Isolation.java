package wang.liangchen.matrix.bpmjob.domain;

/**
 * @author Liangchen.Wang 2023-06-24 14:23
 */
public class Isolation {
    private String tenantCode;
    private String appCode;

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
