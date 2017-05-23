package proj.karthik.feed.reader.store;

import java.util.List;

import proj.karthik.feed.reader.entity.Feed;

/**
 * {@link StoreService} interface for handling Subscription related operations.
 *
 * @author ksubramanian
 */
public interface SubscriptionStore extends StoreService {

    /**
     * Subscribes an user with the given user name to the feed
     *
     * @param feed
     * @param user
     */
    void addSubscriptionByName(String feed, String user);

    /**
     * Subscribes an user with the given user id to the feed
     *
     * @param feed
     * @param userId
     */
    void addSubscriptionById(String feed, long userId);

    /**
     * Unsubscribes an user with the given user name from the feed
     *
     * @param feed
     * @param user
     */
    void deleteSubscriptionByName(String feed, String user);

    /**
     * Unsubscribes an user with the given user id from the feed
     *
     * @param feed
     * @param userId
     */
    void deleteSubscriptionById(String feed, Long userId);

    /**
     * Lists the feeds that an user with the given user name is subscribed to.
     *
     * @param user
     * @return feeds
     */
    List<Feed> getUserSubscriptionsByName(String user);

    /**
     * Lists the feeds that an user with the given user id is subscribed to.
     *
     * @param userId
     * @return feeds
     */
    List<Feed> getUserSubscriptionsById(long userId);
}
