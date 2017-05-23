package proj.karthik.feed.reader.store;

import java.util.concurrent.locks.Lock;

/**
 * Interface for wrapping an operation within write {@link Lock}.
 */
@FunctionalInterface
public interface WriteProtected {

    /**
     * Calls the write operation
     */
    void write();
}
