package com.sintrue.bpmjob.example.test;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author Liangchen.Wang 2023-06-26 9:15
 */
@Component
public class ProxyObject {

    @Cacheable
    public void proxyedMethod(){

    }
}
