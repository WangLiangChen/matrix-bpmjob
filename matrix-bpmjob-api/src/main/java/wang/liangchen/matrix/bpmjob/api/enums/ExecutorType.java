package wang.liangchen.matrix.bpmjob.api.enums;

/**
 * @author Liangchen.Wang 2023-01-12 17:20
 */
public enum ExecutorType {
    JAVABEAN(false), JAVA(true), GROOVY(true), SHELL(true), POWERSHELL(true), NODEJS(true), PHP(true);

    private final boolean script;

    ExecutorType(boolean script) {
        this.script = script;
    }

    public boolean isScript() {
        return script;
    }
}
