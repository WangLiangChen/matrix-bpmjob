package wang.liangchen.matrix.bpmjob.domain.task;

import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-10-22 10:38
 */
public enum TaskState {
    UNASSIGNED("未分配"), ASSIGNED("已分配"), ACKED("确认"), EXECUTING("执行中"), COMPLETED("已完成"), ABORTED("完成(异常)");

    private final String summary;

    TaskState(String summary) {
        this.summary = summary;
    }

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

    public String getSummary() {
        return summary;
    }
}
