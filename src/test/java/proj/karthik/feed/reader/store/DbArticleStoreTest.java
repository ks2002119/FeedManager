package proj.karthik.feed.reader.store;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import proj.karthik.feed.reader.TestUtil;
import proj.karthik.feed.reader.entity.Article;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static proj.karthik.feed.reader.TestUtil.negative;

/**
 * Unit test for {@link DbFeedStore}
 */
public class DbArticleStoreTest {
    private static final DataSource DATA_SOURCE = TestUtil.getTestDataSource(
            DbArticleStoreTest.class.getName());
    private static final SQLUtils SQL_UTILS = new SQLUtils(DATA_SOURCE);
    private static final DbFeedStore FEED_STORE = new DbFeedStore(new Properties(), SQL_UTILS);
    private static final DbUserStore USER_STORE = new DbUserStore(SQL_UTILS);
    private static final DbSubscriptionStore subscriptionStore =
            new DbSubscriptionStore(SQL_UTILS, USER_STORE);
    private static final DbArticleStore articleStore = new DbArticleStore(SQL_UTILS, USER_STORE);
    private static final String TEST = "test";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final Article ARTICLE = new Article(TITLE, BODY);
    private static final String USER = "user";

    @BeforeClass
    public static void setUp() throws Exception {
        FEED_STORE.init();
        USER_STORE.init();
        subscriptionStore.init();
        articleStore.init();
    }

    @Test
    public void testArticleCrudOperations() throws Exception {
        FEED_STORE.add(TEST);
        USER_STORE.add(USER);
        subscriptionStore.addSubscriptionByName(TEST, USER);

        int totalArticles = 10;
        for (int i = 0; i < totalArticles; i++) {
            articleStore.add(ARTICLE, TEST);
        }
        List<Article> articles = articleStore.getArticles(USER);
        assertArticles(totalArticles, articles);
        List<User> users = USER_STORE.getId(USER);
        articles = articleStore.getArticles(users.get(0).getId());
        assertArticles(totalArticles, articles);

    }

    @Test
    public void testAddWithNullFeedName() throws Exception {
        negative(() -> articleStore.add(ARTICLE, null), TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testAddWithNullArticleTitle() throws Exception {
        negative(() -> articleStore.add(new Article(null, BODY), TEST),
                TestUtil::assertArticleTitleNullErrorMsg);
    }

    @Test
    public void testAddWithNullArticleBody() throws Exception {
        negative(() -> articleStore.add(new Article(TITLE, null), TEST),
                TestUtil::assertArticleBodyNullErrorMsg);
    }

    @Test
    public void testGetArticlesWithNullUser() throws Exception {
        negative(() -> articleStore.getArticles(null), TestUtil::assertUserNameNullErrorMsg);
    }

    //----------------------------------- Private methods ----------------------------------------//
    private void assertArticles(final int totalArticles, final List<Article> articles) {
        assertEquals(totalArticles, articles.size());
        articles.stream().forEach(article1 -> {
            assertEquals(TITLE, article1.getTitle());
            assertEquals(BODY, article1.getBody());
            assertNotNull(article1.getCreatedOn());
        });
    }

}
