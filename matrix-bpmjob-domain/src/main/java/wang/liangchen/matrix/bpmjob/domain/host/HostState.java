package wang.liangchen.matrix.bpmjob.domain.host;

import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-10-22 10:38
 */
public enum HostState {
    ONLINE, OFFLINE, DEAD;

    public byte getState() {
        return (byte) (1 << this.ordinal());
    }

    public static HostState valueOf(byte state) {
        for (HostState value : HostState.values()) {
            if (value.getState() == state) {
                return value;
            }
        }
        throw new MatrixWarnException("state value is invalided!");
    }
}
