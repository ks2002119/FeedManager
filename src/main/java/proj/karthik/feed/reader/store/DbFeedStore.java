package proj.karthik.feed.reader.store;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import proj.karthik.feed.reader.Constants;
import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.sql.SQLUtils;

/**
 * {@link FeedStore} implementation that uses database as the backing store.
 *
 * @author ksubramanian
 */
public class DbFeedStore extends DatabaseStoreService implements FeedStore {
    private static final Logger LOG = LoggerFactory.getLogger(DbFeedStore.class);
    private static final String FEED_SQL_PROPERTIES = "/feed_sql.properties";
    private static final Pattern PATTERN_COMMA = Pattern.compile(",");
    private final Properties appConfig;
    private final Properties feedSqlStatements = new Properties();

    @Inject
    public DbFeedStore(Properties appConfig, SQLUtils sqlUtils) {
        super(sqlUtils);
        this.appConfig = appConfig;
        loadStatements(FEED_SQL_PROPERTIES, feedSqlStatements);
    }

    @Override
    public void init() {
        sqlUtils.ddl(statement -> {
            // create table IF NOT EXISTS FEED(name varchar PRIMARY KEY, created_on timestamp);
            statement.execute(feedSqlStatements.getProperty("CREATE_TBL_FEED"));
        });
        String defaultFeeds = this.appConfig.getProperty(Constants.DEFAULT_FEEDS);
        if (StringUtils.isNotBlank(defaultFeeds)) {
            String[] feeds = PATTERN_COMMA.split(defaultFeeds.trim());
            Arrays.stream(feeds)
                    .forEach(feed -> add(feed.trim()));
        }
    }

    @Override
    public void add(final String feed) {
        checkForNull("Feed", feed);
        LOG.debug("Adding new feed: {}", feed);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement insertFeed = connection.prepareStatement(
                    feedSqlStatements.getProperty("INSERT_FEED"));
            insertFeed.setString(1, feed);
            return insertFeed;
        }));
    }

    @Override
    public void delete(final String feed) {
        checkForNull("Feed", feed);
        LOG.debug("Deleting feed: {}", feed);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement deleteFeed = connection.prepareStatement(
                    feedSqlStatements.getProperty("DELETE_FEED"));
            deleteFeed.setString(1, feed);
            return deleteFeed;
        }));
    }

    @Override
    public List<Feed> list() {
        LOG.debug("List all feeds");
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement listFeeds = connection.prepareStatement(
                    feedSqlStatements.getProperty("LIST_FEEDS"),
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            return listFeeds;
        }, resultSet -> new Feed(resultSet.getString(1), resultSet.getTimestamp(2))));
    }
}
