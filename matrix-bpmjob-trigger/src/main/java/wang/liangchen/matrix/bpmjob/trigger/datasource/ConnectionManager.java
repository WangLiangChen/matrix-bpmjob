package wang.liangchen.matrix.bpmjob.trigger.datasource;

import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Liangchen.Wang 2023-07-12 21:50
 */
public enum ConnectionManager {
    INSTANCE;
    private ConnectionProvider connectionProvider;
    /**
     * 外部管理事务
     */
    private ConnectionProvider managedConnectionProvider;

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public ConnectionProvider getManagedConnectionProvider() {
        return managedConnectionProvider;
    }

    public void setManagedConnectionProvider(ConnectionProvider managedConnectionProvider) {
        this.managedConnectionProvider = managedConnectionProvider;
    }

    public void closeConnection(Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            if (connection.isClosed()) {
                return;
            }
            connection.close();
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void commitConnection(Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            if (connection.getAutoCommit() || connection.isReadOnly()) {
                return;
            }
            connection.commit();
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void rollbackConnection(Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            if (connection.getAutoCommit() || connection.isReadOnly()) {
                return;
            }
            connection.rollback();
        } catch (Exception e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void closeStatement(Statement statement) {
        if (null == statement) {
            return;
        }
        try {
            if (statement.isClosed()) {
                return;
            }
            statement.close();
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void closeResultSet(ResultSet resultSet) {
        if (null == resultSet) {
            return;
        }
        try {
            if (resultSet.isClosed()) {
                return;
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public boolean isAutoCommit(Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void enableTransaction(Connection connection) {
        disableAutoCommit(connection);
    }

    public void disableTransaction(Connection connection) {
        enableAutoCommit(connection);
    }

    public void enableAutoCommit(Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            if (connection.getAutoCommit()) {
                return;
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void disableAutoCommit(Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public interface ConnectionProvider {
        Connection getConnection();
    }
}
