package wang.liangchen.matrix.bpmjob.trigger.domain.host;

import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-10-22 10:38
 */
public enum RegistryState {
    ONLINE, OFFLINE, DEAD;

    public byte getState() {
        return (byte) (1 << this.ordinal());
    }

    public static RegistryState valueOf(byte state) {
        for (RegistryState value : RegistryState.values()) {
            if (value.getState() == state) {
                return value;
            }
        }
        throw new MatrixWarnException("state value is invalided!");
    }
}
