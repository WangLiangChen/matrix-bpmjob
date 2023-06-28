package wang.liangchen.matrix.bpmjob.sdk.connector;

import wang.liangchen.matrix.bpmjob.sdk.core.connector.Connector;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.ConnectorFactory;

/**
 * @author Liangchen.Wang 2023-06-23 23:10
 */
public class DefaultHttpConnectorFactory implements ConnectorFactory {
    private final DefaultHttpConnectorProperties properties;

    public DefaultHttpConnectorFactory(DefaultHttpConnectorProperties properties) {
        this.properties = properties;
    }

    @Override
    public Connector createConnector() {
        return new DefaultHttpConnector(properties);
    }
}
