package core.resolver.base;

import core.data.ActionType;
import core.data.InputAction;
import core.data.IntPoint;
import core.data.JudgeCode;
import core.networkbase.Message;
import core.networkbase.MessageType;

class JudgeCodeFilter {
    IJudgeResolver resolver;
    String active;
    String winner;
    String first;
    private boolean isStarted;

    JudgeCodeFilter(IJudgeResolver resolver) {
        this.resolver = resolver;
    }

    public boolean isStarted() {
        return this.isStarted;
    }

    public String getActive() {
        return this.active;
    }

    public String getWinner() {
        return this.winner;
    }

    public void handRelay(Object sender, Message msg) {
        System.out.println("......\nRelay --> " + msg.getType());
        if (msg.getType() == MessageType.Judge)
            onJudge(msg.getToken(), (Object[]) msg.getContent());
        else if (msg.getType() == MessageType.Action) {
            Object[] objs = (Object[]) msg.getContent();
            int size = ProxyEx.getValidSize(objs);
            if (size > 0) {
                ActionType type = (ActionType) objs[0];
                resolver.onAction(type, objs);
            }
        }
    }

    private void onJudge(String token, Object[] data) {
        int size = ProxyEx.getValidSize(data);
        if (size < 1)
            return;
        JudgeCode code = (JudgeCode) data[0];
        switch (code) {
            case Started:
                isStarted = true;
                winner = "";
                resolver.onStateChanged(code);
                break;
            case Ended:
                isStarted = false;
                resolver.onStateChanged(code);
                break;
            case Active:
                active = token;
                resolver.onStateChanged(code);
                break;
            case MarkFirst:
                first = token;
                resolver.onStateChanged(code);
            case NewWinner:
                winner = token;
                resolver.onStateChanged(code);
                break;
            case Reset:
                resolver.onStateChanged(code);
                break;
            default:
                break;
        }
        if (size == 2 && (code == JudgeCode.Input || code == JudgeCode.Undo)) {
            InputAction input = (InputAction) data[1];
            IntPoint p = (IntPoint) input.getData();
            resolver.onJudgeInput(token, code, p);
        }
        resolver.onBasicJudge(token, code);
        System.out.println("[JudgeCodeFilter]-[onJudge] <-- [Code: " + code + "] [Token: " + token + "]");
    }
}
