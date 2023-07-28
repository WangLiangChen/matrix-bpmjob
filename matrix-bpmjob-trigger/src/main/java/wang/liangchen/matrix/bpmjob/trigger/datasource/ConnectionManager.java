package wang.liangchen.matrix.bpmjob.trigger.datasource;

import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2023-07-12 21:50
 */
public enum ConnectionManager {
    INSTANCE;
    public ConnectionProvider connectionProvider;
    /**
     * 外部管理事务
     */
    public ConnectionProvider managedConnectionProvider;

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

    public <R> R queryInConnection(String sql, Object[] args, SQLFunction<ResultSet, R> function) {
        return executeInConnection(false, connection -> {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = createPreparedStatement(connection, sql, args);
                resultSet = preparedStatement.executeQuery();
                return function.apply(resultSet);
            } catch (SQLException e) {
                throw new TriggerRuntimeException(e);
            } finally {
                ConnectionManager.INSTANCE.closeResultSet(resultSet);
                ConnectionManager.INSTANCE.closeStatement(preparedStatement);
            }
        });
    }

    public int updateInConnection(String sql, Object[] args) {
        return executeInConnection(false, connection -> {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = createPreparedStatement(connection, sql, args);
                return preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new TriggerRuntimeException(e);
            } finally {
                ConnectionManager.INSTANCE.closeStatement(preparedStatement);
            }
        });
    }

    public void executeInManagedConnection(boolean startTransaction, SQLConsumer<Connection> consumer) {
        ConnectionManager.ConnectionProvider managedConnectionProvider = ConnectionManager.INSTANCE.getManagedConnectionProvider();
        if (null == managedConnectionProvider) {
            executeInConnection(startTransaction, consumer);
            return;
        }
        // managed,the transaction follow manager rule
        try {
            consumer.accept(managedConnectionProvider.getConnection());
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public <R> R executeInManagedConnection(boolean startTransaction, SQLFunction<Connection, R> function) {
        ConnectionManager.ConnectionProvider managedConnectionProvider = ConnectionManager.INSTANCE.getManagedConnectionProvider();
        if (null == managedConnectionProvider) {
            return executeInConnection(startTransaction, function);
        }
        // managed,the transaction follow manager rule
        try {
            return function.apply(managedConnectionProvider.getConnection());
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        }
    }

    public void executeInConnection(boolean startTransaction, SQLConsumer<Connection> consumer) {
        Connection connection = ConnectionManager.INSTANCE.getConnectionProvider().getConnection();
        if (startTransaction) {
            executeInTransaction(connection, consumer);
        }
        executeInNonTransaction(connection, consumer);
    }

    public <R> R executeInConnection(boolean startTransaction, SQLFunction<Connection, R> function) {
        Connection connection = ConnectionManager.INSTANCE.getConnectionProvider().getConnection();
        if (startTransaction) {
            return executeInTransaction(connection, function);
        }
        return executeInNonTransaction(connection, function);
    }

    public void executeInTransaction(Connection connection, SQLConsumer<Connection> consumer) {
        // 开启事务
        ConnectionManager.INSTANCE.disableAutoCommit(connection);
        try {
            // 执行逻辑
            consumer.accept(connection);
            // 提交事务
            ConnectionManager.INSTANCE.commitConnection(connection);
        } catch (Exception e) {
            // 回滚事务 抛出异常
            ConnectionManager.INSTANCE.rollbackConnection(connection);
            throw new TriggerRuntimeException(e);
        } finally {
            // 关闭连接
            ConnectionManager.INSTANCE.closeConnection(connection);
        }
    }

    public <R> R executeInTransaction(Connection connection, SQLFunction<Connection, R> function) {
        // 开启事务
        ConnectionManager.INSTANCE.disableAutoCommit(connection);
        try {
            // 执行逻辑
            R r = function.apply(connection);
            // 提交事务
            ConnectionManager.INSTANCE.commitConnection(connection);
            return r;
        } catch (Exception e) {
            // 回滚事务 抛出异常
            ConnectionManager.INSTANCE.rollbackConnection(connection);
            throw new TriggerRuntimeException(e);
        } finally {
            // 关闭连接
            ConnectionManager.INSTANCE.closeConnection(connection);
        }
    }

    public void executeInNonTransaction(Connection connection, SQLConsumer<Connection> consumer) {
        // 关闭事务
        ConnectionManager.INSTANCE.enableAutoCommit(connection);
        try {
            // 执行逻辑
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        } finally {
            // 关闭连接
            ConnectionManager.INSTANCE.closeConnection(connection);
        }
    }

    public <R> R executeInNonTransaction(Connection connection, SQLFunction<Connection, R> function) {
        // 关闭事务
        ConnectionManager.INSTANCE.enableAutoCommit(connection);
        try {
            // 执行逻辑
            return function.apply(connection);
        } catch (SQLException e) {
            throw new TriggerRuntimeException(e);
        } finally {
            // 关闭连接
            ConnectionManager.INSTANCE.closeConnection(connection);
        }
    }

    public PreparedStatement createPreparedStatement(Connection connection, String sql, Object[] args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (null == arg) {
                preparedStatement.setObject(i + 1, null);
                continue;
            }
            if (arg instanceof String) {
                preparedStatement.setString(i + 1, (String) arg);
                continue;
            }
            if (arg instanceof Byte) {
                preparedStatement.setByte(i + 1, (Byte) arg);
                continue;
            }
            if (arg instanceof Short) {
                preparedStatement.setShort(i + 1, (Short) arg);
                continue;
            }
            if (arg instanceof Integer) {
                preparedStatement.setInt(i + 1, (Integer) arg);
                continue;
            }
            if (arg instanceof Long) {
                preparedStatement.setLong(i + 1, (Long) arg);
                continue;
            }
            if (arg instanceof LocalDateTime) {
                preparedStatement.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) arg));
                continue;
            }
            if (arg instanceof LocalDate) {
                LocalDate localDate = (LocalDate) arg;
                LocalDateTime localDateTime = localDate.atStartOfDay();
                preparedStatement.setTimestamp(i + 1, Timestamp.valueOf(localDateTime));
                continue;
            }
            preparedStatement.setObject(i + 1, arg);
        }
        return preparedStatement;
    }

    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
