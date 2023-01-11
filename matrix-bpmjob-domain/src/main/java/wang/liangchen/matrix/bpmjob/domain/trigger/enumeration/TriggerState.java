package wang.liangchen.matrix.bpmjob.domain.trigger.enumeration;

import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;

/**
 * @author Liangchen.Wang 2022-10-27 9:24
 */
public class TriggerState extends ConstantEnum {
    public final static ConstantEnum NORMAL = new ConstantEnum("NORMAL", "正常");
    public final static ConstantEnum SUSPENDED = new ConstantEnum("SUSPENDED", "暂停");

    public TriggerState(String key, String value) {
        super(key, value);
    }
}
