package wang.liangchen.matrix.bpmjob.domain.trigger.enumeration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Liangchen.Wang 2023-01-12 17:20
 */
public enum ExecutorType {
    JAVA_EXECUTOR(new HashSet<>() {{
        add("CLASS");
        add("METHOD");
    }}),
    SCRIPT_EXECUTOR(new HashSet<>() {{
        add("GROOVY");
        add("PYTHON");
        add("SHELL");
        add("POWERSHELL");
        add("NODEJS");
        add("PHP");
    }});
    private final Set<String> options;

    ExecutorType(Set<String> options) {
        this.options = options;
    }

    public Set<String> getOptions() {
        return options;
    }
}
