package wang.liangchen.matrix.bpmjob.domain.task;

/**
 * @author Liangchen.Wang 2023-01-19 17:59
 */
public class TaskReport {
    private Long taskId;
    private String taskGroup;
    private String hostLabel;
    /**
     * 要获取的任务数量
     */
    private Byte number;
    /**
     * 上报的进度值
     */
    private Byte process;
    private Boolean aborted;
    private String completeSummary;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
    }

    public String getHostLabel() {
        return hostLabel;
    }

    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }

    public Byte getNumber() {
        return number;
    }

    public void setNumber(Byte number) {
        this.number = number;
    }

    public Byte getProcess() {
        return process;
    }

    public void setProcess(Byte process) {
        this.process = process;
    }

    public Boolean getAborted() {
        return aborted;
    }

    public void setAborted(Boolean aborted) {
        this.aborted = aborted;
    }

    public String getCompleteSummary() {
        return completeSummary;
    }

    public void setCompleteSummary(String completeSummary) {
        this.completeSummary = completeSummary;
    }
}
