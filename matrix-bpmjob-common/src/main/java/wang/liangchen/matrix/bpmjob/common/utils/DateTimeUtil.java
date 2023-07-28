package wang.liangchen.matrix.bpmjob.common.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2023-07-28 16:53
 */
public enum DateTimeUtil {
    INSTANCE;

    public LocalDateTime alignLocalDateTimeSecond() {
        long timestamp = alignSecond();
        return new Timestamp(timestamp).toLocalDateTime();
    }

    public long alignSecond() {
        long ms = System.currentTimeMillis();
        long difference = 1000 - ms % 1000;
        ThreadUtil.INSTANCE.sleep(difference);
        return ms + difference;
    }
}
