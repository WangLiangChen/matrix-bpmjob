package wang.liangchen.matrix.bpmjob.trigger.enumeration;

import wang.liangchen.matrix.bpmjob.trigger.exception.TriggerRuntimeException;

/**
 * @author Liangchen.Wang 2022-10-27 9:24
 */
public enum WalState {
    PENDING, CONFIRMED;

    public byte getState() {
        return (byte) (1 << this.ordinal());
    }

    public static WalState valueOf(byte state) {
        for (WalState value : WalState.values()) {
            if (value.getState() == state) {
                return value;
            }
        }
        throw new TriggerRuntimeException("state value is invalided!");
    }
}
