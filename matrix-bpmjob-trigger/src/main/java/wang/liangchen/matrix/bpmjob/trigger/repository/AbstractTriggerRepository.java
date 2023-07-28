package wang.liangchen.matrix.bpmjob.trigger.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.common.utils.DateTimeUtil;
import wang.liangchen.matrix.bpmjob.trigger.datasource.ConnectionManager;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.TriggerTime;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Wal;
import wang.liangchen.matrix.bpmjob.trigger.enumeration.WalState;
import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;
import wang.liangchen.matrix.bpmjob.common.utils.NumbericUid;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Liangchen.Wang 2023-07-12 21:31
 */
public abstract class AbstractTriggerRepository implements ITriggerRepository {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTriggerRepository.class);
    private final DateTimeFormatter datetimeKeyFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public List<TriggerTime> loadWaitingTriggers(Duration duration, int limit) {
        // 获取未来scope秒内应该触发的触发器 benchDateTime = now + scope
        String sql = "select trigger_id,trigger_instant from bpmjob_trigger_time where trigger_instant<? order by trigger_instant desc limit ? offset 0";
        LocalDateTime benchDateTime = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond().plus(duration);
        Object[] args = new Object[]{benchDateTime, limit};
        return ConnectionManager.INSTANCE.queryInConnection(sql, args, resultSet -> {
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

    @Override
    public boolean compareAndSwapTriggerTime(Long triggerId, LocalDateTime oldValue, LocalDateTime newValue) {
        String sql = "update bpmjob_trigger_time set trigger_instant=? where trigger_id=? and  trigger_instant=?";
        Object[] args = new Object[]{newValue, triggerId, oldValue};
        return 1 == ConnectionManager.INSTANCE.updateInConnection(sql, args);
    }

    @Override
    public Trigger loadWaitingTrigger(Long triggerId) {
        String sql = "select trigger_cron,missed_threshold,missed_strategy,state from bpmjob_trigger where trigger_id=?";
        Object[] args = new Object[]{triggerId};
        return ConnectionManager.INSTANCE.queryInConnection(sql, args, resultSet -> {
            Trigger trigger = new Trigger();
            trigger.setTriggerId(triggerId);
            trigger.setTriggerCron(resultSet.getString(1));
            trigger.setMissedThreshold(resultSet.getByte(2));
            trigger.setMissedStrategy(resultSet.getString(3));
            trigger.setState(resultSet.getString(4));
            return trigger;
        });
    }

    @Override
    public Trigger loadTriggerForTask(Long triggerId) {
        String sql = "select tenant_code,app_code,executor_type,executor_settings,script_code,sharding_number,trigger_params,running_duration_threshold,state from bpmjob_trigger where trigger_id=?";
        Object[] args = new Object[]{triggerId};
        return ConnectionManager.INSTANCE.queryInConnection(sql, args, resultSet -> {
            Trigger trigger = new Trigger();
            trigger.setTriggerId(triggerId);
            trigger.setTenantCode(resultSet.getString(1));
            trigger.setAppCode(resultSet.getString(2));
            trigger.setExecutorType(resultSet.getString(3));
            trigger.setExecutorSettings(resultSet.getString(4));
            trigger.setScriptCode(resultSet.getString(5));
            trigger.setShardingNumber(resultSet.getByte(6));
            trigger.setTriggerParams(resultSet.getString(7));
            trigger.setRunningDurationThreshold(resultSet.getInt(8));
            trigger.setState(resultSet.getString(9));
            return trigger;
        });
    }

    @Override
    public Optional<Wal> createWal(String hostLabel, Long triggerId, LocalDateTime expectedDatetime, LocalDateTime nextDatetime) {
        String sql = "update bpmjob_trigger_time set trigger_instant=? where trigger_id=? and  trigger_instant=?";
        Object[] args = new Object[]{nextDatetime, triggerId, expectedDatetime};
        return ConnectionManager.INSTANCE.executeInConnection(true, connection -> {
            PreparedStatement preparedStatement = ConnectionManager.INSTANCE.createPreparedStatement(connection, sql, args);
            int rows = preparedStatement.executeUpdate();
            ConnectionManager.INSTANCE.closeStatement(preparedStatement);
            if (0 == rows) {
                return Optional.empty();
            }
            Wal wal = new Wal();
            wal.setWalId(NumbericUid.INSTANCE.nextId());
            wal.setTriggerId(triggerId);
            String walKey = expectedDatetime.format(datetimeKeyFormatter).concat(":").concat(Long.toString(triggerId));
            wal.setWalKey(walKey);
            wal.setHostLabel(hostLabel);
            wal.setTaskParams("");
            wal.setExpectedDatetime(expectedDatetime);
            wal.setCreateDatetime(LocalDateTime.now());
            wal.setState(WalState.PENDING.getState());
            String walSql = "insert into bpmjob_wal values(?,?,?,?,?,?,?,?)";
            Object[] walArgs = new Object[]{wal.getWalId(), triggerId, walKey, hostLabel, wal.getTaskParams(), expectedDatetime, wal.getCreateDatetime(), wal.getState()};
            preparedStatement = ConnectionManager.INSTANCE.createPreparedStatement(connection, walSql, walArgs);
            rows = preparedStatement.executeUpdate();
            ConnectionManager.INSTANCE.closeStatement(preparedStatement);
            if (0 == rows) {
                return Optional.empty();
            }
            return Optional.of(wal);
        });
    }

    @Override
    public List<Long> loadWaitingWals(Duration duration, int limit) {
        //  // 获取当前时间scope秒之前的wal benchDateTime = now - scope
        LocalDateTime benchDateTime = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond().minus(duration);
        String sql = "select wal_id from bpmjob_wal where create_datetime<? and state=? order by create_datetime limit ? offset 0";
        Object[] args = new Object[]{benchDateTime, limit, WalState.PENDING.getState()};
        return ConnectionManager.INSTANCE.queryInConnection(sql, args, resultSet -> {
            List<Long> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getLong(1));
            }
            return list;
        });
    }

    @Override
    public Wal loadWal(Long walId) {
        String sql = "select trigger_id,host_label,task_params,expected_datetime,create_datetime,state from bpmjob_wal where wal_id=?";
        Object[] args = new Object[]{walId};
        return ConnectionManager.INSTANCE.queryInConnection(sql, args, resultSet -> {
            Wal wal = new Wal();
            wal.setWalId(walId);
            wal.setTriggerId(resultSet.getLong(1));
            wal.setHostLabel(resultSet.getString(2));
            wal.setTaskParams(resultSet.getString(3));
            wal.setExpectedDatetime(resultSet.getTimestamp(4).toLocalDateTime());
            wal.setCreateDatetime(resultSet.getTimestamp(5).toLocalDateTime());
            wal.setState(resultSet.getByte(6));
            return wal;
        });
    }

    @Override
    public boolean confirmWal(Long walId) {
        String sql = "update bpmjob_wal set state=? where wal_id=?";
        Object[] args = new Object[]{WalState.CONFIRMED.getState(), walId};
        return 1 == ConnectionManager.INSTANCE.updateInConnection(sql, args);
    }

    @Override
    public void confirmWal(Wal wal, Consumer<Boolean> consumer) {
        String sql = "update bpmjob_wal set state=? where wal_id=?";
        Object[] args = new Object[]{WalState.CONFIRMED.getState(), wal.getWalId()};
        ConnectionManager.INSTANCE.executeInConnection(true, connection -> {
            PreparedStatement preparedStatement = ConnectionManager.INSTANCE.createPreparedStatement(connection, sql, args);
            int rows = preparedStatement.executeUpdate();
            ConnectionManager.INSTANCE.closeStatement(preparedStatement);
            consumer.accept(1 == rows);
        });
    }

}
