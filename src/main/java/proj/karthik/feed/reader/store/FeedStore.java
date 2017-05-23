package proj.karthik.feed.reader.store;

import java.util.List;

import proj.karthik.feed.reader.entity.Feed;

/**
 * {@link StoreService} interface for handling feed related operations.
 *
 * @author ksubramanian
 */
public interface FeedStore extends StoreService {

    /**
     * Adds a new feed to the application.
     *
     * @param feed
     */
    void add(String feed);

    /**
     * Deletes the feed with the given name.
     *
     * @param feed
     */
    void delete(String feed);

    /**
     * Lists all the available streams in the system.
     *
     * @return feeds
     */
    List<Feed> list();
}
