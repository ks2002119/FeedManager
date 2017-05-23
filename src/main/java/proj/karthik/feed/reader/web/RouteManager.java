package proj.karthik.feed.reader.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import proj.karthik.feed.reader.AppException;
import proj.karthik.feed.reader.entity.Article;
import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.store.ArticleStore;
import proj.karthik.feed.reader.store.FeedStore;
import proj.karthik.feed.reader.store.SubscriptionStore;
import proj.karthik.feed.reader.store.UserStore;
import spark.Request;
import spark.Response;

import static proj.karthik.feed.reader.Constants.APPLICATION_JSON;
import static proj.karthik.feed.reader.Constants.FEED;
import static proj.karthik.feed.reader.Constants.ID;
import static proj.karthik.feed.reader.Constants.NAME;
import static proj.karthik.feed.reader.Constants.PORT;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

/**
 * RouteManager is responsible for managing the HTTP routes.
 */
public class RouteManager {
    private static final Logger LOG = LoggerFactory.getLogger(RouteManager.class);

    private final ObjectMapper objectMapper;
    private final ArticleStore articleStore;
    private final FeedStore feedStore;
    private final SubscriptionStore subscriptionStore;
    private final UserStore userStore;

    @Inject
    public RouteManager(ObjectMapper objectMapper,
            ArticleStore articleStore, FeedStore feedStore, SubscriptionStore subscriptionStore,
            UserStore userStore ) {
        this.objectMapper = objectMapper;
        this.articleStore = articleStore;
        this.feedStore = feedStore;
        this.subscriptionStore = subscriptionStore;
        this.userStore = userStore;
    }

    public void initRoutes(Properties conf) {
        if (conf.containsKey(PORT)) {
            int port = Integer.parseInt(conf.getProperty(PORT));
            LOG.info("Starting web server at port:{}", port);
            port(port);
        }
        post("/api/1/users", this::addUser);
        delete("/api/1/users", this::deleteUser);
        post("/api/1/subscriptions", this::subscribe);
        delete("/api/1/subscriptions", this::unsubscribe);
        post("/api/1/articles", this::addArticle);
        get("/api/1/articles", this::getArticles);
        post("/api/1/feeds", this::addFeed);
        get("/api/1/feeds", this::getFeed);
        exception(AppException.class, this::addExceptionHandler);
    }

    protected Response addUser(final Request req, final Response res) {
        String userName = req.queryParams(NAME);
        userStore.add(userName);
        res.status(200);
        return res;
    }

    protected Response deleteUser(final Request req, final Response res) {
        String id = req.queryParams(ID);
        if (StringUtils.isNotBlank(id)) {
            long userId = Long.parseLong(id.trim());
            userStore.delete(userId);
        } else {
            String userName = req.queryParams(NAME);
            userStore.delete(userName);
        }
        res.status(200);
        return res;
    }

    protected Response subscribe(final Request request, final Response response) {
        String userName = request.queryParams(NAME);
        String id = request.queryParams(ID);
        String feed = request.queryParams(FEED);
        if (StringUtils.isNotBlank(id)) {
            long userId = Long.parseLong(id.trim());
            subscriptionStore.addSubscriptionById(feed, userId);
        } else {
            subscriptionStore.addSubscriptionByName(feed, userName);
        }
        response.status(200);
        return response;
    }

    protected Response unsubscribe(final Request request, final Response response) {
        String userName = request.queryParams(NAME);
        String id = request.queryParams(ID);
        String feed = request.queryParams(FEED);
        if (StringUtils.isNotBlank(id)) {
            long userId = Long.parseLong(id.trim());
            subscriptionStore.deleteSubscriptionById(feed, userId);
        } else {
            subscriptionStore.deleteSubscriptionByName(feed, userName);
        }
        response.status(200);
        return response;
    }

    protected Response addFeed(final Request req, final Response res) {
        String feed = req.queryParams(FEED);
        feedStore.add(feed);
        res.status(200);
        return res;
    }

    protected Response getFeed(Request request, Response response) {
        String userName = request.queryParams(NAME);
        String id = request.queryParams(ID);
        List<Feed> feeds;
        if (StringUtils.isNotBlank(id)) {
            long userId = Long.parseLong(id.trim());
            feeds = subscriptionStore.getUserSubscriptionsById(userId);
        } else {
            feeds = subscriptionStore.getUserSubscriptionsByName(userName);
        }
        response.status(200);
        try {
            response.type(APPLICATION_JSON);
            response.body(objectMapper.writeValueAsString(feeds));
        } catch (JsonProcessingException e) {
            throw new AppException(500, "Error formatting result as JSON", e);
        }
        return response;
    }

    protected Response addArticle(final Request req, final Response res) {
        String feed = req.queryParams(FEED);
        try {
            Article article = objectMapper.readValue(req.body(), new TypeReference<Article>() {});
            articleStore.add(article, feed);
            res.status(200);
            return res;
        } catch (IOException e) {
            throw new AppException("Error parsing article object", e, 400, "Article object must " +
                    "have a valid title and body");
        }
    }

    protected Response getArticles(final Request request, final Response response) {
        String userName = request.queryParams(NAME);
        String id = request.queryParams(ID);
        List<Article> articles;
        if (StringUtils.isNotBlank(id)) {
            long userId = Long.parseLong(id.trim());
            articles = articleStore.getArticles(userId);
        } else {
            articles = articleStore.getArticles(userName);
        }
        response.status(200);
        try {
            response.type(APPLICATION_JSON);
            response.body(objectMapper.writeValueAsString(articles));
        } catch (JsonProcessingException e) {
            throw new AppException(500, "Error formatting result as JSON", e);
        }
        return response;
    }

    protected Response addExceptionHandler(AppException exception, Request request,
            Response response) {
        LOG.error("Error while processing request", exception);
        response.status(exception.getCode());
        try {
            response.body(objectMapper.writeValueAsString(exception.getErrorMap()));
        } catch (JsonProcessingException e) {
            LOG.warn("Unable to convert error map to JSON string", e);
            response.body(exception.getLocalizedMessage());
        }
        return response;
    }
}
