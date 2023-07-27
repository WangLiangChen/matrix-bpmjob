package wang.liangchen.matrix.bpmjob.trigger.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.trigger.datasource.ConnectionManager;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.TriggerTime;
import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liangchen.Wang 2023-07-12 21:31
 */
public abstract class AbstractTriggerRepository implements ITriggerRepository {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTriggerRepository.class);

    public List<TriggerTime> loadWaitingTriggers(int scopeSecond, int limit) {
        // benchDateTime = now + scope
        String sql = "select trigger_id,trigger_instant from bpmjob_trigger_time where trigger_instant<? limit ? offset 0";
        LocalDateTime benchDateTime = LocalDateTime.now().plusSeconds(scopeSecond);
        Object[] args = new Object[]{benchDateTime, limit};
        return queryInConnection(sql, args, resultSet -> {
            List<TriggerTime> list = new ArrayList<>();
            while (resultSet.next()) {
                long triggerId = resultSet.getLong(1);
                Timestamp triggerInstant = resultSet.getTimestamp(2);
                TriggerTime triggerTime = new TriggerTime();
                triggerTime.setTriggerId(triggerId);
                triggerTime.setTriggerInstant(triggerInstant.toLocalDateTime());
                list.add(triggerTime);
            }
            return list;
        });
    }

    public boolean compareAndSwapTriggerTime(Long triggerId, LocalDateTime oldValue, LocalDateTime newValue) {
        String sql = "update bpmjob_trigger_time set trigger_instant=? where trigger_id=? and  trigger_instant=?";
        Object[] args = new Object[]{newValue, triggerId, oldValue};
        return 1 == updateInConnection(sql, args);
    }

    public List<Long> loadWaitingWals(int scopeSecond, int limit) {
        // benchDateTime = now - scope
        LocalDateTime benchDateTime = LocalDateTime.now().minusSeconds(scopeSecond);
        String sql = "select wal_id from bpmjob_wal where trigger_instant<? limit ? offset 0";
        Object[] args = new Object[]{benchDateTime, limit};
        return queryInConnection(sql, args, resultSet -> {
            List<Long> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getLong(1));
            }
            return list;
        });
    }

    public boolean confirmWal(Long walId) {
        String sql = "update bpmjob_wal set state=? where wal_id=?";
        Object[] args = new Object[]{"", walId};
        return 1 == updateInConnection(sql, args);
    }

    public boolean confirmWalAndPublishEvent(Long walId) {
        return executeInConnection(true, connection -> {
            // 发布事件
            //BpmjobEventPublisher.INSTANCE.publishEvent();

            return false;
        });
    }

    protected <R> R queryInConnection(String sql, Object[] args, SQLFunction<ResultSet, R> function) {
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

    protected int updateInConnection(String sql, Object[] args) {
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

    protected void executeInManagedConnection(boolean startTransaction, SQLConsumer<Connection> consumer) {
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

    protected <R> R executeInManagedConnection(boolean startTransaction, SQLFunction<Connection, R> function) {
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

    protected void executeInConnection(boolean startTransaction, SQLConsumer<Connection> consumer) {
        Connection connection = ConnectionManager.INSTANCE.getConnectionProvider().getConnection();
        if (startTransaction) {
            executeInTransaction(connection, consumer);
        }
        executeInNonTransaction(connection, consumer);
    }

    protected <R> R executeInConnection(boolean startTransaction, SQLFunction<Connection, R> function) {
        Connection connection = ConnectionManager.INSTANCE.getConnectionProvider().getConnection();
        if (startTransaction) {
            return executeInTransaction(connection, function);
        }
        return executeInNonTransaction(connection, function);
    }

    private void executeInTransaction(Connection connection, SQLConsumer<Connection> consumer) {
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

    private <R> R executeInTransaction(Connection connection, SQLFunction<Connection, R> function) {
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

    private void executeInNonTransaction(Connection connection, SQLConsumer<Connection> consumer) {
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

    private <R> R executeInNonTransaction(Connection connection, SQLFunction<Connection, R> function) {
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

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Object[] args) throws SQLException {
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

    interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
