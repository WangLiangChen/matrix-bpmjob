package liangchen.wang.matrix.bpmjob.sdk.core;

/**
 * @author Liangchen.Wang 2023-02-15 8:05
 */
public class MainThread {

    static class HeartbeatThread extends Thread{
        public HeartbeatThread() {
            super("bpmjob-heartbeat-");
        }
    }
}
