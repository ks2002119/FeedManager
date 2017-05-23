package proj.karthik.feed.reader.store;

import java.util.List;

import proj.karthik.feed.reader.entity.User;

/**
 * {@link StoreService} interface for handling user related operations.
 *
 * @author ksubramanian
 */
public interface UserStore extends StoreService {

    /**
     * Adds a new user with the given username.
     *
     * @param userName
     */
    void add(String userName);

    /**
     * Deletes an user with the given username.
     *
     * @param userName
     */
    void delete(String userName);

    /**
     * Deletes an user with the given id.
     *
     * @param userId
     */
    void delete(long userId);

    /**
     * Returns the list of users with the given user name.
     *
     * @param name
     * @return users
     */
    List<User> getId(String name);
}
