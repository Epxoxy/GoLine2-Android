package core.interfaces;

public interface Match<T>{
    boolean validate(T value);
}