package core.resolver.base;

import core.data.ActionType;
import core.data.IntPoint;
import core.data.JudgeCode;

public interface IJudgeResolver {
    void onBasicJudge(String token, JudgeCode code);
    void onJudgeInput(String token, JudgeCode code, IntPoint p);
    void onAction(ActionType type, Object[] content);
    void onStateChanged(JudgeCode code);
}
