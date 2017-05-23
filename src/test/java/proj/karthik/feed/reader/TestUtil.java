package proj.karthik.feed.reader;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Utility class containing DB related methods specifically for use within test classes.
 */
public class TestUtil {
    private static final String USER_NAME_ATTRIBUTE_CANNOT_BE_EMPTY = "Username " +
            "attribute cannot be empty";
    private static final String FEED_ATTRIBUTE_CANNOT_BE_EMPTY = "Feed attribute cannot be empty";
    private static final String ARTICLE_BODY_ATTRIBUTE_CANNOT_BE_EMPTY = "Article body attribute " +
            "cannot be empty";
    private static final String ARTICLE_TITLE_ATTRIBUTE_CANNOT_BE_EMPTY = "Article title " +
            "attribute cannot be empty";
    /**
     * Returns a {@link DataSource} for use within Tests. This uses H2 in-memory database with
     * the passed name as the database name.
     *
     * @return dataSource
     */
    public static DataSource getTestDataSource(String name) {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", String.format("jdbc:h2:mem:%s", name));
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");
        return new HikariDataSource(config);
    }

    /**
     * Helper to do exception testing. JUnit does not have a straight forward way to assert
     * exception messages.
     *
     * @param negativeTest
     * @param negativeAssertions
     */
    public static void negative(NegativeTest negativeTest, NegativeAssertions negativeAssertions) {
        try {
            negativeTest.run();
            fail("No exception thrown as expected");
        } catch (Throwable throwable) {
            negativeAssertions.runAssertions(throwable);
        }
    }

    /**
     * Asserts the exception for null user name message.
     * @param throwable
     */
    public static void assertUserNameNullErrorMsg(final Throwable throwable) {
        assertEquals(AppException.class, throwable.getClass());
        assertEquals(USER_NAME_ATTRIBUTE_CANNOT_BE_EMPTY, throwable.getMessage());
    }

    /**
     * Asserts the exception for null feed name message.
     * @param throwable
     */
    public static void assertFeedNullErrorMsg(final Throwable throwable) {
        assertEquals(AppException.class, throwable.getClass());
        assertEquals(FEED_ATTRIBUTE_CANNOT_BE_EMPTY, throwable.getMessage());
    }

    /**
     * Asserts the exception for null article body message.
     * @param throwable
     */
    public static void assertArticleBodyNullErrorMsg(final Throwable throwable) {
        assertEquals(AppException.class, throwable.getClass());
        assertEquals(ARTICLE_BODY_ATTRIBUTE_CANNOT_BE_EMPTY, throwable.getMessage());
    }

    /**
     * Asserts the exception for null article title message.
     * @param throwable
     */
    public static void assertArticleTitleNullErrorMsg(final Throwable throwable) {
        assertEquals(AppException.class, throwable.getClass());
        assertEquals(ARTICLE_TITLE_ATTRIBUTE_CANNOT_BE_EMPTY, throwable.getMessage());
    }
}
