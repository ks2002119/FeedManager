package proj.karthik.feed.reader.store;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import proj.karthik.feed.reader.TestUtil;
import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

import static org.junit.Assert.assertEquals;

import static proj.karthik.feed.reader.TestUtil.negative;

/**
 * Unit test for {@link DbFeedStore}
 */
public class DbSubscriptionStoreTest {
    private static final DataSource DATA_SOURCE = TestUtil.getTestDataSource(
            DbSubscriptionStoreTest.class.getName());
    private static final SQLUtils SQL_UTILS = new SQLUtils(DATA_SOURCE);
    private static final DbFeedStore FEED_STORE = new DbFeedStore(new Properties(), SQL_UTILS);
    private static final DbUserStore USER_STORE = new DbUserStore(SQL_UTILS);
    private static final DbSubscriptionStore SUBSCRIPTION_STORE =
            new DbSubscriptionStore(SQL_UTILS, USER_STORE);
    private static final String TEST = "test";
    private static final String USER = "user";

    @BeforeClass
    public static void setUp() throws Exception {
        FEED_STORE.init();
        USER_STORE.init();
        SUBSCRIPTION_STORE.init();
    }

    @Test
    public void testSubscriptionCrudOperations() throws Exception {
        FEED_STORE.add(TEST);
        USER_STORE.add(USER);
        List<User> users = USER_STORE.getId(USER);
        User user = users.get(0);

        List<Feed> feedSubscriptions = SUBSCRIPTION_STORE.getUserSubscriptionsByName(USER);
        assertEquals(0, feedSubscriptions.size());

        feedSubscriptions = subscribeByName(TEST, user);
        assertEquals(1, feedSubscriptions.size());
        feedSubscriptions = unsubscribeById(TEST, user);
        assertEquals(0, feedSubscriptions.size());

        feedSubscriptions = subscribeById(TEST, user);
        assertEquals(1, feedSubscriptions.size());
        feedSubscriptions = unsubscribeByName(TEST, user);
        assertEquals(0, feedSubscriptions.size());
    }

    @Test
    public void testSubscribeByNameWithNullFeedName() throws Exception {
        negative(() -> SUBSCRIPTION_STORE.addSubscriptionByName(null, USER),
                TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testSubscribeByIdWithNullFeedName() throws Exception {
        negative(() -> SUBSCRIPTION_STORE.addSubscriptionById(null, -1L),
                TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testSubscribeWithNullUserName() throws Exception {
        negative(() -> SUBSCRIPTION_STORE.addSubscriptionByName(TEST, null),
                TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testUnsubscribeByIdWithNullFeedName() throws Exception {
        negative(() -> SUBSCRIPTION_STORE.deleteSubscriptionById(null, -1L),
                TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testUnsubscribeWithNullUserName() throws Exception {
        negative(() -> SUBSCRIPTION_STORE.deleteSubscriptionByName(TEST, null),
                TestUtil::assertUserNameNullErrorMsg);
    }
    //------------------------------------- private methods --------------------------------------//
    private List<Feed> subscribeById(String feed, User user) {
        SUBSCRIPTION_STORE.addSubscriptionById(feed, user.getId());
        return SUBSCRIPTION_STORE.getUserSubscriptionsById(user.getId());
    }

    private List<Feed> subscribeByName(String feed, User user) {
        SUBSCRIPTION_STORE.addSubscriptionByName(feed, user.getName());
        return SUBSCRIPTION_STORE.getUserSubscriptionsByName(user.getName());
    }

    private List<Feed> unsubscribeById(String feed, User user) {
        SUBSCRIPTION_STORE.deleteSubscriptionById(feed, user.getId());
        return SUBSCRIPTION_STORE.getUserSubscriptionsById(user.getId());
    }

    private List<Feed> unsubscribeByName(String feed, User user) {
        SUBSCRIPTION_STORE.deleteSubscriptionByName(feed, user.getName());
        return SUBSCRIPTION_STORE.getUserSubscriptionsByName(user.getName());
    }
}
