package core.interfaces;

public interface IAnalyzer<TData, TResult> {
    TResult analysis(TData data, int deep);
}
