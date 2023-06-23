package wang.liangchen.bpmjob.service.provider;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.liangchen.matrix.bpmjob.api.ExecutorMethod;
import wang.liangchen.matrix.framework.commons.exception.MatrixErrorException;

import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-27 22:31
 */
@RestController
@RequestMapping("/report")
public class ReportProvider {
    @PostMapping("/executorMethods")
    public Object executorMethods(@RequestBody Set<ExecutorMethod> executorMethodSet) {
        if(executorMethodSet.size()>0){
            throw new MatrixErrorException("i am a error");
        }
       return executorMethodSet;
    }
}
