package proj.karthik.feed.reader.store;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import proj.karthik.feed.reader.entity.Article;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

/**
 * {@link ArticleStore} implementation that uses database as the backing store.
 *
 * @author ksubramanian
 */
public class DbArticleStore extends DatabaseStoreService implements ArticleStore {
    private static final Logger LOG = LoggerFactory.getLogger(DbArticleStore.class);
    private static final String ARTICLE_SQL_PROPERTIES = "/article_sql.properties";
    private final Properties articleSqlStatements = new Properties();
    private final UserStore userStore;

    @Inject
    public DbArticleStore(SQLUtils sqlUtils, UserStore userStore) {
        super(sqlUtils);
        this.userStore = userStore;
        loadStatements(ARTICLE_SQL_PROPERTIES, articleSqlStatements);
    }

    @Override
    public void init() {
        sqlUtils.ddl(statement -> {
            // create table IF NOT EXISTS ARTICLE(id bigint auto_increment PRIMARY KEY,
            // title varchar(1024), created_on timestamp, body varchar, feed_name varchar,
            // FOREIGN KEY (feed_name) REFERENCES public.feed(name));
            statement.execute(articleSqlStatements.getProperty("CREATE_TBL_ARTICLE"));
        });
    }

    @Override
    public void add(final Article article, final String feed) {
        checkForNull("Feed", feed);
        checkForNull("Article title", article.getTitle());
        checkForNull("Article body", article.getBody());
        LOG.info("Adding article: {} to feed: {}", article.getTitle(), feed);
        write(() -> sqlUtils.dml(connection -> {
            // Insert into ARTICLE values(default, ?, CURRENT_TIMESTAMP(), ?, ?);
            PreparedStatement feedLookup = connection.prepareStatement(
                    articleSqlStatements.getProperty("INSERT_ARTICLE"));
            feedLookup.setString(1, article.getTitle());
            feedLookup.setString(2, article.getBody());
            feedLookup.setString(3, feed);
            return feedLookup;
        }));
    }

    @Override
    public List<Article> getArticles(final String userName) {
        checkForNull("Username", userName);
        LOG.info("Getting articles for user: {}", userName);
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement selectArticleFromFeed = connection.prepareStatement(
                    articleSqlStatements.getProperty("SELECT_ARTICLE_FOR_USERID"),
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            List<User> userIds = userStore.getId(userName);
            return sqlUtils.batch(selectArticleFromFeed, userIds);
        }, this::getArticleResultProcessor));
    }

    @Override
    public List<Article> getArticles(final long userId) {
        LOG.info("Getting articles for user with id: {}", userId);
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement selectArticleFromFeed = connection.prepareStatement(
                    articleSqlStatements.getProperty("SELECT_ARTICLE_FOR_USERID"),
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            selectArticleFromFeed.setLong(1, userId);
            return selectArticleFromFeed;
        }, this::getArticleResultProcessor));
    }

    //------------------------------------- Private methods -------------------------------------//

    private Article getArticleResultProcessor(ResultSet resultSet) throws SQLException {
        Article article = new Article();
        article.setTitle(resultSet.getString(1));
        article.setCreatedOn(resultSet.getTimestamp(2));
        article.setBody(resultSet.getString(3));
        return article;
    }
}
