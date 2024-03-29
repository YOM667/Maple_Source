package top.youm.maple.utils.tools;
public class CatchHandlers {
    @FunctionalInterface
    public interface CatchHandler<T> {
        T handle() throws Exception;
    }
    @FunctionalInterface
    public interface CatchRunnable{
        void run() throws Exception;
    }
}
