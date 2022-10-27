package wang.liangchen.matrix.bpmjob.trigger.domain.trigger.enumeration;

import wang.liangchen.matrix.bpmjob.trigger.domain.host.RegistryState;
import wang.liangchen.matrix.framework.commons.exception.MatrixWarnException;

/**
 * @author Liangchen.Wang 2022-10-27 9:24
 */
public enum WalState {
    ACQUIRED, TRIGGERED;

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
