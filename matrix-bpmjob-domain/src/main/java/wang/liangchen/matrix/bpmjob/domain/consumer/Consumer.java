package wang.liangchen.matrix.bpmjob.domain.consumer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.*;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.CommonEntity;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;

/**
 * 接入的App
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_consumer")
public class Consumer extends CommonEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    private Long consumerId;
    private String tenantCode;
    private String consumerCode;
    private String consumerName;
    private String algorithm;
    private String secret;
    private String providerPublic;
    private String providerPrivate;
    private String consumerPublic;
    private String consumerPrivate;

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getConsumerCode() {
        return consumerCode;
    }

    public void setConsumerCode(String consumerCode) {
        this.consumerCode = consumerCode;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getProviderPublic() {
        return providerPublic;
    }

    public void setProviderPublic(String providerPublic) {
        this.providerPublic = providerPublic;
    }

    public String getProviderPrivate() {
        return providerPrivate;
    }

    public void setProviderPrivate(String providerPrivate) {
        this.providerPrivate = providerPrivate;
    }

    public String getConsumerPublic() {
        return consumerPublic;
    }

    public void setConsumerPublic(String consumerPublic) {
        this.consumerPublic = consumerPublic;
    }

    public String getConsumerPrivate() {
        return consumerPrivate;
    }

    public void setConsumerPrivate(String consumerPrivate) {
        this.consumerPrivate = consumerPrivate;
    }
}