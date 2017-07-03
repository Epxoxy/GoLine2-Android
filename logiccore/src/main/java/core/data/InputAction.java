package core.data;

public class InputAction {
    private ActionType type;
    private Object data;

    public InputAction(ActionType type, Object data){
        this.type = type;
        this.data = data;
    }

    public ActionType getType() {
        return type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setType(ActionType type) {
        this.type = type;
    }
}
