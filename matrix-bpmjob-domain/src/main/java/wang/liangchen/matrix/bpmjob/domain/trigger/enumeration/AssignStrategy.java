package wang.liangchen.matrix.bpmjob.domain.trigger.enumeration;

/**
 * @author Liangchen.Wang 2022-10-27 13:45
 */
public enum AssignStrategy {
    FIRST, LAST, ROUND, RANDOM, CONSISTENT_HASH, LEAST_FREQUENTLY_USED, LEAST_RECENTLY_USED, FAILOVER, BUSYOVER, SHARDING_BROADCAST;
}
