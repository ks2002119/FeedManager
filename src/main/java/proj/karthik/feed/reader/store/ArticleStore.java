package proj.karthik.feed.reader.store;

import java.util.List;

import proj.karthik.feed.reader.entity.Article;

/**
 * {@link StoreService} interface for handling article related operations.
 */
public interface ArticleStore extends StoreService {
    /**
     * Adds an {@link Article} to the specified feed.
     * @param article
     * @param feed
     */
    void add(Article article, String feed);

    /**
     * Get {@link Article}s for the given user from the subscribed feeds.
     *
     * @param userName
     * @return articles
     */
    List<Article> getArticles(String userName);

    /**
     * Get {@link Article}s for the given user id from the subscribed feeds.
     *
     * @param userId
     * @return articles
     */
    List<Article> getArticles(long userId);
}
