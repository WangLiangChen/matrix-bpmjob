package wang.liangchen.matrix.bpmjob.sdk.connector;

import io.vertx.core.MultiMap;
import io.vertx.ext.auth.authentication.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.BpmJobClientProperties;
import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Liangchen.Wang 2023-06-23 23:41
 */
public class DefaultHttpConnectorProperties extends BpmJobClientProperties {

    private final static Logger logger = LoggerFactory.getLogger(DefaultHttpConnectorProperties.class);
    private static volatile boolean instantiated = false;
    private static DefaultHttpConnectorProperties instance;

    public static DefaultHttpConnectorProperties getInstance() {
        if (instantiated) {
            return instance;
        }
        synchronized (DefaultHttpConnectorProperties.class) {
            if (instantiated) {
                return instance;
            }
            try (InputStream resourceAsStream = DefaultHttpConnectorProperties.class.getClassLoader().getResourceAsStream("bpmjob.properties")) {
                if (null == resourceAsStream) {
                    logger.error("Read 'bpmjob.properties' error. The file does not exist.");
                    throw new BpmJobException("Read 'bpmjob.properties' error. The file does not exist.");
                }
                Properties properties = new Properties();
                properties.load(resourceAsStream);
                String taskUri = properties.getProperty("taskUri");
                if (null == taskUri || taskUri.isEmpty()) {
                    properties.setProperty("taskUri", properties.getProperty("uri"));
                }
                instance = new DefaultHttpConnectorProperties();
                instance.setTenantId(properties.getProperty("tenantId"));
                instance.setAppId(properties.getProperty("appId"));
                instance.setSecret(properties.getProperty("secret"));
                instance.setUri(properties.getProperty("uri"));
                instance.setTaskUri(properties.getProperty("taskUri"));
                instantiated = true;
            } catch (IOException e) {
                logger.error("Read 'bpmjob.properties' error. {}", e.getMessage());
                throw new BpmJobException(e);
            }
        }
        return instance;
    }

    /**
     * 签名验签密钥
     */
    private String secret;
    /**
     * 请求服务端的URI
     */
    private String uri;
    /**
     * 请求任务独立的URI
     * 未设置则taskUri = uri
     */
    private String taskUri;
    private MultiMap headers;
    private Credentials credentials;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTaskUri() {
        return taskUri;
    }

    public void setTaskUri(String taskUri) {
        this.taskUri = taskUri;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
