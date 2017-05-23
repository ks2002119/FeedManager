package proj.karthik.feed.reader.store;

/**
 * StoreService is the service that provides storage related API methods.
 */
public interface StoreService {

    /**
     * Initializes the service
     */
    void init();

    /**
     * Shuts down the service
     */
    void shutdown();
}
