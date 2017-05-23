package proj.karthik.feed.reader.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface used to process the resultset.
 *
 * @param <T> Processed result type
 */
public interface ResultProcessor<T> {

    /**
     * Processes the {@link ResultSet} and returns a processed entity of type T.
     * @param resultSet
     * @return
     */
    T process(ResultSet resultSet) throws SQLException;
}
