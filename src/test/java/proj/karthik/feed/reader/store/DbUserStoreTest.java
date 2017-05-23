package proj.karthik.feed.reader.store;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import proj.karthik.feed.reader.TestUtil;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

import static org.junit.Assert.assertEquals;

import static proj.karthik.feed.reader.TestUtil.negative;

/**
 * Unit test for {@link DbFeedStore}
 */
public class DbUserStoreTest {
    private static final DataSource DATA_SOURCE = TestUtil.getTestDataSource(
            DbUserStoreTest.class.getName());
    private static final SQLUtils SQL_UTILS = new SQLUtils(DATA_SOURCE);
    private static final DbUserStore USER_STORE = new DbUserStore(SQL_UTILS);
    private static final String TEST = "test";

    @BeforeClass
    public static void setUp() throws Exception {
        USER_STORE.init();
    }

    @Test
    public void testAddDeleteUserByName() throws Exception {
        USER_STORE.add(TEST);
        List<User> users = USER_STORE.getId(TEST);
        assertEquals(1, users.size());
        assertEquals(TEST, users.get(0).getName());
        USER_STORE.delete(TEST);
        users = USER_STORE.getId(TEST);
        assertEquals(0, users.size());
    }

    @Test
    public void testAddDeleteUserById() throws Exception {
        USER_STORE.add(TEST);
        List<User> users = USER_STORE.getId(TEST);
        assertEquals(1, users.size());
        assertEquals(TEST, users.get(0).getName());
        USER_STORE.delete(users.get(0).getId());
        users = USER_STORE.getId(TEST);
        assertEquals(0, users.size());
    }

    @Test
    public void testNonUniqueUserNames() throws Exception {
        int userCount = 10;
        for (int i = 0; i < userCount; i++) {
            USER_STORE.add(TEST);
        }

        List<User> users = USER_STORE.getId(TEST);
        assertEquals(10, users.size());

        AtomicInteger currentCount = new AtomicInteger(10);
        users.stream().forEach(user -> {
            USER_STORE.delete(user.getId());
            List<User> currentUsers = USER_STORE.getId(TEST);
            assertEquals(currentUsers.size(), currentCount.decrementAndGet());
        });
    }

    @Test
    public void testNonExistentUser() throws Exception {
        List<User> users = USER_STORE.getId(TEST);
        assertEquals(0, users.size());

        USER_STORE.delete(TEST);
        USER_STORE.delete(1);
    }

    @Test
    public void testAddNullUser() throws Exception {
        negative(() -> USER_STORE.add(null), TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testDeleteNullUser() throws Exception {
        negative(() -> USER_STORE.delete(null), TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testGetIdForNullUser() throws Exception {
        negative(() -> USER_STORE.getId(null), TestUtil::assertUserNameNullErrorMsg);
    }
}
