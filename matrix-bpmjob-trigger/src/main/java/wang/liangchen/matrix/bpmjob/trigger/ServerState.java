package wang.liangchen.matrix.bpmjob.trigger;

import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-09-26 9:06
 */
public enum ServerState {
    ONLINE("在线"), OFFLINE("离线"), DEAD("僵死");

    private final String stateText;

    ServerState(String stateText) {
        this.stateText = stateText;
    }

    public byte getState() {
        return (byte) (1 << this.ordinal());
    }

    public String getStateText() {
        return stateText;
    }

    public ServerState valueOf(byte state) {
        for (ServerState serverState : ServerState.values()) {
            if (serverState.getState() == state) {
                return serverState;
            }
        }
        throw new MatrixWarnException("state: '{}' out of range", state);
    }
}
