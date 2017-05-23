package proj.karthik.feed.reader.sql;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.AppException;

/**
 * Utility class that holds various SQL related methods.
 */
public class SQLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SQLUtils.class);
    private final DataSource dataSource;

    @Inject
    public SQLUtils(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes the given {@link DDLStatement}
     *
     * @param ddlStatement
     */
    public void ddl(final DDLStatement ddlStatement) {
        LOG.info("Executing DDL Statement");
        Connection connection = getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ddlStatement.execute(statement);
            connection.commit();
        } catch (SQLException e) {
            throw new AppException(500, "Error writing to database", e);
        } finally {
            close(connection, statement);
        }

    }

    /**
     * Performs a DML operation using the given {@link DMLStatement}
     *
     * @param dmlStatement
     */
    public void dml(final DMLStatement dmlStatement) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dmlStatement.execute(connection);
            int updateCount = preparedStatement.executeUpdate();
            LOG.debug("DML statement resulted in {} changes", updateCount);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new AppException(500, "Error rolling back sql operations", e1);
            }
            throw new AppException(500, "Error executing DML statement", e);
        } finally {
            close(connection, preparedStatement);
        }
    }

    /**
     * Performs a DML operation as a batch using the given {@link DMLStatement}
     *
     * @param dmlStatement
     */
    public void batchDML(final DMLStatement dmlStatement) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dmlStatement.execute(connection);
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new AppException(500, "Error rolling back sql operations", e1);
            }
            throw new AppException(500, "Error executing DML statement", e);
        } finally {
            close(connection, preparedStatement);
        }
    }

    /**
     * Executes the given DML query statement, applies the {@link ResultProcessor} and returns the
     * processed result as {@link List}.
     *
     * @param dmlStatement
     * @param processor
     * @param <T> Result type
     * @return
     */
    public <T> List<T> query(final DMLStatement dmlStatement, ResultProcessor<T> processor) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = dmlStatement.execute(connection);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = new LinkedList<>();
            while (resultSet.next()) {
                results.add(processor.process(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw new AppException(500, "Error querying database", e);
        } finally {
            close(connection, preparedStatement);
        }
    }

    /**
     * Creates a batch using the given {@link PreparedStatement} for the given list of user ids.
     *
     * @param batchStatement
     * @param userIds
     * @return batchStatement
     */
    public PreparedStatement batch(PreparedStatement batchStatement, List<User> userIds) {
        return batch(batchStatement, userIds, null);
    }

    /**
     * Creates a batch using the given {@link PreparedStatement} for the given list of user ids
     * and feed.
     *
     * @param batchStatement
     * @param userIds
     * @param feed
     * @return batchStatement
     */
    public PreparedStatement batch(PreparedStatement batchStatement, List<User> userIds,
            String feed) {
        userIds.stream()
                .forEach(
                        userId -> {
                            try {
                                batchStatement.setLong(1, userId.getId());
                                if (feed != null) {
                                    batchStatement.setString(2, feed);
                                }
                                batchStatement.addBatch();
                            } catch (SQLException e) {
                                throw new AppException(500, "Error setting attribute values" +
                                        " for sql statement", e);
                            }
                        });
        return batchStatement;
    }

    /**
     * Closes the {@link ResultSet}
     * @param resultSet
     */
    public void close(final ResultSet resultSet) {
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new AppException(500, "Failed to close SQL objects", e);
        }
    }

    /**
     * Closes {@link Connection} and {@link Statement} if they are non-null.
     *
     * @param connection
     * @param statement
     */
    public void close(final Connection connection, final Statement statement) {
        close(statement);
        close(connection);
    }

    /**
     * Closes {@link Connection}.
     *
     * @param connection
     */
    public void close(final Connection connection) {
        try {
            if (connection != null) {
                LOG.debug("Releasing connection to pool");
                connection.close();
            }
        } catch (SQLException e) {
            throw new AppException(500, "Failed to close SQL connection", e);
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param statement
     */
    public void close(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            throw new AppException(500, "Failed to close SQL statement", e);
        }
    }

    //---------------------------------------- Private Methods ----------------------------------//

    private Connection getConnection() {
        try {
            LOG.debug("Checking out connection from pool");
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new AppException(500, "Error getting connection from the pool", e);
        }
    }
}
