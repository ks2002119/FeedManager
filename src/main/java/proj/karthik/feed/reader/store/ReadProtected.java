package proj.karthik.feed.reader.store;

import java.util.concurrent.locks.Lock;

/**
 * Interface for wrapping an operation within read {@link Lock}.
 * @param <T> Result type
 */
@FunctionalInterface
public interface ReadProtected<T> {

    /**
     * Calls the read operation and returns the result of type T
     *
     * @return result
     */
    T read();
}
