package wang.liangchen.matrix.bpmjob.console.northbound_ohs.remote.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.liangchen.matrix.framework.data.enumeration.StateEnum;

/**
 * @author Liangchen.Wang 2022-10-19 19:19
 */
@RestController
@RequestMapping("/registry")
public class RegistryController {

    @GetMapping("/state")
    public StateEnum state(){
        return StateEnum.FAILED;
    }
}
