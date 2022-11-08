package wang.liangchen.matrix.bpmjob.domain.task;

import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-10-22 10:38
 */
public enum TaskState {
    UNASSIGNED;

    public byte getState() {
        return (byte) (1 << this.ordinal());
    }

    public static TaskState valueOf(byte state) {
        for (TaskState value : TaskState.values()) {
            if (value.getState() == state) {
                return value;
            }
        }
        throw new MatrixWarnException("state value is invalided!");
    }
}
