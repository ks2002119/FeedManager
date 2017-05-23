package proj.karthik.feed.reader.store;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import proj.karthik.feed.reader.sql.SQLUtils;
import proj.karthik.feed.reader.AppException;

/**
 * DatabaseStoreService is an implementation of {@link StoreService} that uses database as the
 * backing store.
 */
public abstract class DatabaseStoreService implements StoreService {
    protected final SQLUtils sqlUtils;
    protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock readLock = readWriteLock.readLock();
    protected final Lock writeLock = readWriteLock.writeLock();

    @Inject
    public DatabaseStoreService(SQLUtils sqlUtils) {
        this.sqlUtils = sqlUtils;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    //------------------------------- protected methods ---------------------------------------//
    protected void loadStatements(String resourceFile, Properties properties) {
        URL statements = DatabaseStoreService.class.getResource(resourceFile);
        try (InputStream inStream = statements.openStream()) {
            properties.load(inStream);
        } catch (IOException e) {
            throw new AppException(500, "Error loading sql statements from %s", e,
                    resourceFile);
        }
    }

    protected void checkForNull(final String field, final String value) {
        if (StringUtils.isBlank(value)) {
            throw new AppException(400, String.format("%s attribute cannot be empty", field));
        }
    }

    protected <T> T read(ReadProtected<T> runnable) {
        readLock.lock();
        try {
            return runnable.read();
        } finally {
            readLock.unlock();
        }
    }

    protected void write(WriteProtected writeProtected) {
        writeLock.lock();
        try {
            writeProtected.write();
        } finally {
            writeLock.unlock();
        }
    }
}
