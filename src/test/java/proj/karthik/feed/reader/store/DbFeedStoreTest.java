package proj.karthik.feed.reader.store;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import proj.karthik.feed.reader.Constants;
import proj.karthik.feed.reader.TestUtil;
import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.sql.SQLUtils;

import static org.junit.Assert.assertEquals;

import static proj.karthik.feed.reader.TestUtil.negative;

/**
 * Unit test for {@link DbFeedStore}
 */
public class DbFeedStoreTest {
    private static final DataSource DATA_SOURCE = TestUtil.getTestDataSource(
            DbFeedStoreTest.class.getName());
    private static final SQLUtils SQL_UTILS = new SQLUtils(DATA_SOURCE);
    private static final Properties APP_CONFIG = new Properties() {
        {
            setProperty(Constants.DEFAULT_FEEDS, "test1, test2");
        }
    };

    private static final DbFeedStore feedStore = new DbFeedStore(APP_CONFIG, SQL_UTILS);
    private static final String TEST = "test";

    @BeforeClass
    public static void setUp() throws Exception {
        feedStore.init();
        List<Feed> feeds = feedStore.list();
        assertEquals(2, feeds.size());
        feedStore.delete("test1");
        feedStore.delete("test2");
        feeds = feedStore.list();
        assertEquals(0, feeds.size());
    }

    @Test
    public void testFeedCrudOperations() throws Exception {
        feedStore.add(TEST);
        List<Feed> feeds = feedStore.list();
        assertEquals(1, feeds.size());
        assertEquals(TEST, feeds.get(0).getName());
        feedStore.delete(TEST);
        feeds = feedStore.list();
        assertEquals(0, feeds.size());
    }

    @Test
    public void testAddWithNullFeedName() throws Exception {
        negative(() -> feedStore.add(null), TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testDeleteWithNullFeedName() throws Exception {
        negative(() -> feedStore.delete(null), TestUtil::assertFeedNullErrorMsg);
    }
}
