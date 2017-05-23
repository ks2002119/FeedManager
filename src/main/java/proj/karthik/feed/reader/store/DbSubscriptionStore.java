package proj.karthik.feed.reader.store;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

/**
 * {@link FeedStore} implementation that uses database as the backing store.
 *
 * @author ksubramanian
 */
public class DbSubscriptionStore extends DatabaseStoreService implements SubscriptionStore {
    private static final Logger LOG = LoggerFactory.getLogger(DbSubscriptionStore.class);
    private static final String SUBSCRIPTION_SQL_PROPERTIES = "/subscription_sql.properties";
    private final Properties subscriptionSqlStatements = new Properties();
    private final UserStore userStore;

    @Inject
    public DbSubscriptionStore(SQLUtils sqlUtils, UserStore userStore) {
        super(sqlUtils);
        this.userStore = userStore;
        loadStatements(SUBSCRIPTION_SQL_PROPERTIES, subscriptionSqlStatements);
    }

    @Override
    public void init() {
        sqlUtils.ddl(statement -> {
            // create table IF NOT EXISTS SUBSCRIPTION(user_id bigint, feed_name varchar,
            // PRIMARY KEY (user_id, feed_name)
            // FOREIGN KEY(feed_name) REFERENCES public.feed(name),
            // FOREIGN KEY(user_id) REFERENCES public.user(id));
            statement.execute(subscriptionSqlStatements.getProperty("CREATE_TBL_SUBSCRIPTION"));
        });
    }

    @Override
    public void addSubscriptionByName(final String feed, final String userName) {
        checkForNull("Username", userName);
        checkForNull("Feed", feed);
        LOG.info("Subscribing {} to feed:{}", userName, feed);
        write(() -> sqlUtils.batchDML(connection -> {
            PreparedStatement subscribeById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("INSERT_SUBSCRIPTION_BY_ID"));
            List<User> userIds = userStore.getId(userName);
            return sqlUtils.batch(subscribeById, userIds, feed);
        }));
    }

    @Override
    public void addSubscriptionById(final String feed, final long userId) {
        checkForNull("Feed", feed);
        LOG.info("Subscribing user with id:{} to feed:{}", userId, feed);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement subscribeById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("INSERT_SUBSCRIPTION_BY_ID"));
            subscribeById.setLong(1, userId);
            subscribeById.setString(2, feed);
            return subscribeById;
        }));
    }

    @Override
    public void deleteSubscriptionByName(final String feed, final String userName) {
        checkForNull("Username", userName);
        checkForNull("Feed", feed);
        LOG.info("Unsubscribing {} to feed:{}", userName, feed);
        write(() -> sqlUtils.batchDML(connection -> {
            PreparedStatement unsubscribeById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("DELETE_SUBSCRIPTION_BY_ID"));
            List<User> userIds = userStore.getId(userName);
            return sqlUtils.batch(unsubscribeById, userIds, feed);
        }));
    }

    @Override
    public void deleteSubscriptionById(final String feed, final Long userId) {
        checkForNull("Feed", feed);
        LOG.info("Unsubscribing user with id:{} to feed:{}", userId, feed);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement unsubscribeById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("DELETE_SUBSCRIPTION_BY_ID"));
            unsubscribeById.setLong(1, userId);
            unsubscribeById.setString(2, feed);
            return unsubscribeById;
        }));
    }

    @Override
    public List<Feed> getUserSubscriptionsByName(final String userName) {
        checkForNull("Username", userName);
        LOG.info("Getting subscriptions for user:{}", userName);
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement listSubscriptionsById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("LIST_SUBSCRIPTIONS_BY_ID"));
            List<User> userIds = userStore.getId(userName);
            return sqlUtils.batch(listSubscriptionsById, userIds);
        }, resultSet -> new Feed(resultSet.getString(1), resultSet.getTimestamp(2))));
    }

    @Override
    public List<Feed> getUserSubscriptionsById(final long userId) {
        LOG.info("Looking up subscriptions for user with id:{}", userId);
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement listSubscriptionsById = connection.prepareStatement(
                    subscriptionSqlStatements.getProperty("LIST_SUBSCRIPTIONS_BY_ID"));
            listSubscriptionsById.setLong(1, userId);
            return listSubscriptionsById;
        }, resultSet -> new Feed(resultSet.getString(1), resultSet.getTimestamp(2))));
    }
}
