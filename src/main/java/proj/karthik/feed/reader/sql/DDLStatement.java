package proj.karthik.feed.reader.sql;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Interface implemented to perform SQL DDL operations
 */
public interface DDLStatement {

    /**
     * Executes the DDL Statement using the given {@link Statement}.
     *
     * @param statement
     * @throws SQLException
     */
    void execute(Statement statement) throws SQLException;
}
