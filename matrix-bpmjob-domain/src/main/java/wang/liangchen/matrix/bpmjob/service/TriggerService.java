package wang.liangchen.matrix.bpmjob.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.domain.trigger.TriggerManager;
import wang.liangchen.matrix.framework.commons.exception.ExceptionLevel;
import wang.liangchen.matrix.framework.commons.validation.InsertGroup;
import wang.liangchen.matrix.framework.commons.validation.ValidationUtil;

/**
 * @author Liangchen.Wang 2023-01-16 14:26
 */
@Service
public class TriggerService {
    private final TriggerManager triggerManager;

    @Inject
    public TriggerService(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
    }

    @Transactional
    public void createTrigger(TriggerRequest triggerRequest) {
        ValidationUtil.INSTANCE.validate(ExceptionLevel.INFO, triggerRequest, InsertGroup.class);
        Trigger trigger = Trigger.valueOf(triggerRequest);
        this.triggerManager.createTrigger(trigger);
    }

    @Transactional
    public void disableTrigger(Long triggerId) {
        this.triggerManager.disableTrigger(triggerId);
    }

    @Transactional
    public void enableTrigger(Long triggerId) {
        this.triggerManager.enableTrigger(triggerId);
    }

}
