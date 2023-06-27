package wang.liangchen.matrix.bpmjob.api;

import java.util.List;

/**
 * @author Liangchen.Wang 2023-06-24 16:11
 */
public class HeartbeatRequest {
    private List<BpmJobThreadInfo> threadInfos;

    public List<BpmJobThreadInfo> getThreadInfos() {
        return threadInfos;
    }

    public void setThreadInfos(List<BpmJobThreadInfo> threadInfos) {
        this.threadInfos = threadInfos;
    }
}
