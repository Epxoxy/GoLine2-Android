package core.data;

public class JudgeResult {
    private JudgeCode code;
    private Object extra;

    public JudgeResult(JudgeCode code, Object extra){
        this.code = code;
        this.extra = extra;
    }

    public JudgeCode getCode() {
        return code;
    }

    public Object getExtra() {
        return extra;
    }
}