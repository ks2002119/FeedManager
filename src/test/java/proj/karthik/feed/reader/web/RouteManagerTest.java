package proj.karthik.feed.reader.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.easymock.Capture;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import proj.karthik.feed.reader.TestUtil;
import proj.karthik.feed.reader.entity.Article;
import proj.karthik.feed.reader.entity.Feed;
import proj.karthik.feed.reader.entity.User;
import proj.karthik.feed.reader.sql.SQLUtils;
import proj.karthik.feed.reader.store.DbArticleStore;
import proj.karthik.feed.reader.store.DbFeedStore;
import proj.karthik.feed.reader.store.DbSubscriptionStore;
import proj.karthik.feed.reader.store.DbSubscriptionStoreTest;
import proj.karthik.feed.reader.store.DbUserStore;
import spark.Request;
import spark.Response;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import static proj.karthik.feed.reader.Constants.BODY;
import static proj.karthik.feed.reader.Constants.ID;
import static proj.karthik.feed.reader.Constants.NAME;
import static proj.karthik.feed.reader.Constants.TITLE;
import static proj.karthik.feed.reader.TestUtil.negative;

/**
 * Unit test for {@link RouteManager}
 */
public class RouteManagerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DataSource DATA_SOURCE = TestUtil.getTestDataSource(
            DbSubscriptionStoreTest.class.getName());
    private static final SQLUtils SQL_UTILS = new SQLUtils(DATA_SOURCE);
    private static final DbFeedStore FEED_STORE = new DbFeedStore(new Properties(), SQL_UTILS);
    private static final DbUserStore USER_STORE = new DbUserStore(SQL_UTILS);
    private static final DbArticleStore ARTICLE_STORE = new DbArticleStore(SQL_UTILS, USER_STORE);
    private static final DbSubscriptionStore SUBSCRIPTION_STORE =
            new DbSubscriptionStore(SQL_UTILS, USER_STORE);
    private static final RouteManager ROUTE_MANAGER = new RouteManager(OBJECT_MAPPER, ARTICLE_STORE,
            FEED_STORE, SUBSCRIPTION_STORE, USER_STORE);
    private static final String FEED = "feed";
    private static final String ADD_USER = "addUser";

    @BeforeClass
    public static void setUp() throws Exception {
        FEED_STORE.init();
        FEED_STORE.add(FEED);
        USER_STORE.init();
        SUBSCRIPTION_STORE.init();
        ARTICLE_STORE.init();
    }

    @Test
    public void testAddUser() throws Exception {
        Request request = createMock(Request.class);
        expect(request.queryParams(NAME)).andReturn(ADD_USER);
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.addUser(request, response);
        verify(request, response);
        USER_STORE.delete(ADD_USER);
    }

    @Test
    public void testAddNullUser() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(NAME)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.addUser(request, response);
            verify(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testDeleteUserById() throws Exception {
        USER_STORE.add("deleteById");
        List<User> users = USER_STORE.getId("deleteById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(String.valueOf(users.get(0).getId()));
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.deleteUser(request, response);
        verify(request, response);
        users = USER_STORE.getId("deleteById");
        assertEquals(0, users.size());
    }

    @Test
    public void testDeleteUserWithNull() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(ID)).andReturn(null);
            expect(request.queryParams(NAME)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.deleteUser(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testDeleteUserByName() throws Exception {
        USER_STORE.add("deleteById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(null);
        expect(request.queryParams(NAME)).andReturn("deleteById");
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.deleteUser(request, response);
        verify(request, response);
        List<User> users = USER_STORE.getId("deleteById");
        assertEquals(0, users.size());
    }

    @Test
    public void testSubscribeByName() throws Exception {
        USER_STORE.add("subscribeByName");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(null);
        expect(request.queryParams(NAME)).andReturn("subscribeByName");
        expect(request.queryParams(FEED)).andReturn(FEED);
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.subscribe(request, response);
        verify(request, response);
        List<Feed> subscriptions = SUBSCRIPTION_STORE.getUserSubscriptionsByName("subscribeByName");
        assertEquals(1, subscriptions.size());
        SUBSCRIPTION_STORE.deleteSubscriptionByName(FEED, "subscribeByName");
    }

    @Test
    public void testSubscribeById() throws Exception {
        USER_STORE.add("subscribeById");
        List<User> users = USER_STORE.getId("subscribeById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(String.valueOf(users.get(0).getId()));
        expect(request.queryParams(NAME)).andReturn(null);
        expect(request.queryParams(FEED)).andReturn(FEED);
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.subscribe(request, response);
        verify(request, response);
        List<Feed> subscriptions = SUBSCRIPTION_STORE.getUserSubscriptionsById(
                users.get(0).getId());
        assertEquals(1, subscriptions.size());
        SUBSCRIPTION_STORE.deleteSubscriptionById(FEED, users.get(0).getId());
    }

    @Test
    public void testSubscribeWithNull() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(ID)).andReturn(null);
            expect(request.queryParams(NAME)).andReturn(null);
            expect(request.queryParams(FEED)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.subscribe(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testUnsubscribeByName() throws Exception {
        USER_STORE.add("unsubscribeByName");
        SUBSCRIPTION_STORE.addSubscriptionByName(FEED, "unsubscribeByName");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(null);
        expect(request.queryParams(NAME)).andReturn("unsubscribeByName");
        expect(request.queryParams(FEED)).andReturn(FEED);
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.unsubscribe(request, response);
        verify(request, response);
        List<Feed> subscriptions = SUBSCRIPTION_STORE.getUserSubscriptionsByName("subscribeByName");
        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testUnsubscribeById() throws Exception {
        USER_STORE.add("unsubscribeById");
        SUBSCRIPTION_STORE.addSubscriptionByName(FEED, "unsubscribeById");
        List<User> users = USER_STORE.getId("unsubscribeById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(String.valueOf(users.get(0).getId()));
        expect(request.queryParams(NAME)).andReturn(null);
        expect(request.queryParams(FEED)).andReturn(FEED);
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.unsubscribe(request, response);
        verify(request, response);
        List<Feed> subscriptions = SUBSCRIPTION_STORE.getUserSubscriptionsById(
                users.get(0).getId());
        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testUnsubscribeWithNull() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(ID)).andReturn(null);
            expect(request.queryParams(NAME)).andReturn(null);
            expect(request.queryParams(FEED)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.unsubscribe(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testAddArticle() throws Exception {
        USER_STORE.add("addArticle");
        SUBSCRIPTION_STORE.addSubscriptionByName(FEED, "addArticle");
        List<Article> articles = ARTICLE_STORE.getArticles("addArticle");
        assertEquals(0, articles.size());
        Request request = createMock(Request.class);
        expect(request.queryParams(FEED)).andReturn(FEED);
        expect(request.body()).andReturn(OBJECT_MAPPER.writeValueAsString(getArticleMap()));
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.addArticle(request, response);
        verify(request, response);
        articles = ARTICLE_STORE.getArticles("addArticle");
        assertEquals(1, articles.size());
    }

    @Test
    public void testAddArticleWithNullFeed() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(FEED)).andReturn(null);
            expect(request.body()).andReturn(OBJECT_MAPPER.writeValueAsString(getArticleMap()));
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.addArticle(request, response);
        }, TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testAddFeed() throws Exception {
        List<Feed> feeds = FEED_STORE.list();
        Request request = createMock(Request.class);
        expect(request.queryParams(FEED)).andReturn("addFeed");
        Response response = getSuccessResponse();
        replay(request, response);
        ROUTE_MANAGER.addFeed(request, response);
        verify(request, response);
        assertEquals(feeds.size() + 1, FEED_STORE.list().size());
    }

    @Test
    public void testAddFeedWithNullFeed() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(FEED)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.addFeed(request, response);
        }, TestUtil::assertFeedNullErrorMsg);
    }

    @Test
    public void testGetFeedsByName() throws Exception {
        USER_STORE.add("getFeedsByName");
        SUBSCRIPTION_STORE.addSubscriptionByName(FEED, "getFeedsByName");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(null);
        expect(request.queryParams(NAME)).andReturn("getFeedsByName");
        Response response = getSuccessResponse();
        response.type("application/json");
        Capture<String> captured = Capture.newInstance();
        response.body(capture(captured));
        replay(request, response);
        ROUTE_MANAGER.getFeed(request, response);
        verify(request, response);
        assertEquals(1, OBJECT_MAPPER.readValue(captured.getValue(), List.class).size());
        SUBSCRIPTION_STORE.deleteSubscriptionByName(FEED, "getFeedsByName");
    }

    @Test
    public void testGetFeedsById() throws Exception {
        USER_STORE.add("getFeedsById");
        SUBSCRIPTION_STORE.addSubscriptionByName(FEED, "getFeedsById");
        List<User> users = USER_STORE.getId("getFeedsById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(String.valueOf(users.get(0).getId()));
        expect(request.queryParams(NAME)).andReturn(null);
        Response response = getSuccessResponse();
        Capture<String> captured = Capture.newInstance();
        response.body(capture(captured));
        response.type("application/json");
        replay(request, response);
        ROUTE_MANAGER.getFeed(request, response);
        verify(request, response);
        assertEquals(1, OBJECT_MAPPER.readValue(captured.getValue(), List.class).size());
        SUBSCRIPTION_STORE.deleteSubscriptionByName(FEED, "getFeedsById");
    }

    @Test
    public void testGetFeedsWithNull() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(ID)).andReturn(null);
            expect(request.queryParams(NAME)).andReturn(null);
            expect(request.queryParams(FEED)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.getFeed(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }

    @Test
    public void testGetArticlesByName() throws Exception {
        FEED_STORE.add("getArticlesByName");
        USER_STORE.add("getArticlesByName");
        SUBSCRIPTION_STORE.addSubscriptionByName("getArticlesByName", "getArticlesByName");
        ARTICLE_STORE.add(new Article("title", "body"), "getArticlesByName");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(null);
        expect(request.queryParams(NAME)).andReturn("getArticlesByName");
        Response response = getSuccessResponse();
        response.type("application/json");
        Capture<String> captured = Capture.newInstance();
        response.body(capture(captured));
        replay(request, response);
        ROUTE_MANAGER.getArticles(request, response);
        verify(request, response);
        assertEquals(1, OBJECT_MAPPER.readValue(captured.getValue(), List.class).size());
        SUBSCRIPTION_STORE.deleteSubscriptionByName("getArticlesByName", "getArticlesByName");
    }

    @Test
    public void testGetArticlesById() throws Exception {
        FEED_STORE.add("getArticlesById");
        USER_STORE.add("getArticlesById");
        SUBSCRIPTION_STORE.addSubscriptionByName("getArticlesById", "getArticlesById");
        ARTICLE_STORE.add(new Article("title", "body"), "getArticlesById");
        List<User> users = USER_STORE.getId("getArticlesById");
        Request request = createMock(Request.class);
        expect(request.queryParams(ID)).andReturn(String.valueOf(users.get(0).getId()));
        expect(request.queryParams(NAME)).andReturn(null);
        Response response = getSuccessResponse();
        Capture<String> captured = Capture.newInstance();
        response.body(capture(captured));
        response.type("application/json");
        replay(request, response);
        ROUTE_MANAGER.getArticles(request, response);
        verify(request, response);
        assertEquals(1, OBJECT_MAPPER.readValue(captured.getValue(), List.class).size());
        SUBSCRIPTION_STORE.deleteSubscriptionByName(FEED, "getArticlesById");
    }

    @Test
    public void testGetArticlesWithNull() throws Exception {
        negative(() -> {
            Request request = createMock(Request.class);
            expect(request.queryParams(ID)).andReturn(null);
            expect(request.queryParams(NAME)).andReturn(null);
            expect(request.queryParams(FEED)).andReturn(null);
            Response response = createMock(Response.class);
            replay(request, response);
            ROUTE_MANAGER.getArticles(request, response);
        }, TestUtil::assertUserNameNullErrorMsg);
    }
    //------------------------------ Private methods ---------------------------------------------//
    private Response getSuccessResponse() {
        return getResponse(200);
    }

    private Response getResponse(int code) {
        Response response = createMock(Response.class);
        response.status(code);
        return response;
    }

    private Map<String, String> getArticleMap() {
        return new HashMap<String, String>(){
            {
                put(TITLE, "title");
                put(BODY, "body");
            }
        };
    }
}
