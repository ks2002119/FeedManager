package proj.karthik.feed.reader.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface implemented to perform SQL DML operations like dml, update, delete, etc.
 */
public interface DMLStatement {

    /**
     * Executes the dml statement using the given {@link Connection}.
     *
     * @param connection
     * @return preparedStatement
     * @throws SQLException
     */
    PreparedStatement execute(Connection connection) throws SQLException;
}
