package proj.karthik.feed.reader.store;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;

/**
 * {@link ArticleStore} implementation that uses database as the backing store.
 *
 * @author ksubramanian
 */
public class DbUserStore extends DatabaseStoreService implements UserStore {
    private static final Logger LOG = LoggerFactory.getLogger(DbUserStore.class);
    private static final String USER_SQL_PROPERTIES = "/user_sql.properties";
    private final Properties userSqlStatements = new Properties();

    @Inject
    public DbUserStore(SQLUtils sqlUtils) {
        super(sqlUtils);
        loadStatements(USER_SQL_PROPERTIES, userSqlStatements);
    }

    @Override
    public void init() {
        sqlUtils.ddl(statement -> {
            // create table IF NOT EXISTS USER(id bigint auto_increment PRIMARY KEY, name varchar,
            // created_on timestamp);
            statement.execute(userSqlStatements.getProperty("CREATE_TBL_USER"));
        });
    }

    @Override
    public void add(final String userName) {
        checkForNull("Username", userName);
        LOG.info("Adding user: {}", userName);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement insertUser = connection.prepareStatement(
                    userSqlStatements.getProperty("INSERT_USER"));
            insertUser.setString(1, userName);
            return insertUser;
        }));
    }

    @Override
    public void delete(final String userName) {
        checkForNull("Username", userName);
        LOG.info("Deleting user: {}", userName);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement deleteUserByName = connection.prepareStatement(
                    userSqlStatements.getProperty("DELETE_USER_BY_NAME"));
            deleteUserByName.setString(1, userName);
            return deleteUserByName;
        }));
    }

    @Override
    public void delete(final long userId) {
        LOG.info("Adding user with id: {}", userId);
        write(() -> sqlUtils.dml(connection -> {
            PreparedStatement deleteUserByName = connection.prepareStatement(
                    userSqlStatements.getProperty("DELETE_USER_BY_ID"));
            deleteUserByName.setLong(1, userId);
            return deleteUserByName;
        }));
    }

    @Override
    public List<User> getId(final String userName) {
        checkForNull("Username", userName);
        LOG.info("Looking up id for user: {}", userName);
        return read(() -> sqlUtils.query(connection -> {
            PreparedStatement listUserByName = connection.prepareStatement(
                    userSqlStatements.getProperty("LIST_USER_BY_NAME"));
            listUserByName.setString(1, userName);
            return listUserByName;
        }, resultSet -> new User(resultSet.getLong(1), resultSet.getString(2),
                resultSet .getTimestamp(3))));
    }
}
