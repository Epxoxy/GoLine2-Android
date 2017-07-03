package core.interfaces;

import core.data.JudgeCode;

public interface JudgedListener {
    void onJudged(String token, JudgeCode code, Object extra);
}
