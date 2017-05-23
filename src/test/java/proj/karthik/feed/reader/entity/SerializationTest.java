package proj.karthik.feed.reader.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import proj.karthik.feed.reader.CoreModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static proj.karthik.feed.reader.Constants.CREATED_ON;
import static proj.karthik.feed.reader.Constants.ID;
import static proj.karthik.feed.reader.Constants.NAME;

/**
 * Serialization test for {@link Article}
 */
public class SerializationTest {
    private static final Map<String, String> EXPECTED = new HashMap<String, String>(){
        {
            put("title", "test title");
            put("body", "test body");
            put("created_on", null);
        }
    };
    private static final Map<String, String> EXPECTED_WITH_NO_BODY = new HashMap<String, String>(){
        {
            put("title", "test title");
            put("body", null);
            put("created_on", null);
        }
    };
    private static final Injector INJECTOR = Guice.createInjector(new JsonModule(new Properties(),
            Paths.get(".")));
    private static final ObjectMapper OBJECT_MAPPER = INJECTOR.getInstance(ObjectMapper.class);

    @Test
    public void testArticleSerialization() throws Exception {
        Article article = new Article("test title", "test body");
        String serializedArticle = OBJECT_MAPPER.writeValueAsString(article);
        assertMapEquals(EXPECTED, OBJECT_MAPPER.readValue(serializedArticle, new
                TypeReference<Map<String, String>>(){}));
    }

    @Test
    public void testArticleWithNulls() throws Exception {
        Article article = new Article("test title", null);
        String serializedArticle = OBJECT_MAPPER.writeValueAsString(article);
        assertMapEquals(EXPECTED_WITH_NO_BODY, OBJECT_MAPPER.readValue(serializedArticle, new
                TypeReference<Map<String, String>>(){}));
    }

    @Test
    public void testFeedSerialization() throws Exception {
        Timestamp createdOn = new Timestamp(System.currentTimeMillis());
        Feed feed = new Feed("test", createdOn);
        String serializedArticle = OBJECT_MAPPER.writeValueAsString(feed);
        assertMapEquals(getExpectedFeedMap(createdOn), OBJECT_MAPPER.readValue(serializedArticle,
                new TypeReference<Map<String, String>>(){}));
    }

    @Test
    public void testUserSerialization() throws Exception {
        User user = new User("test");
        String serializedArticle = OBJECT_MAPPER.writeValueAsString(user);
        assertMapEquals(getExpectedUserMap(), OBJECT_MAPPER.readValue(serializedArticle,
                new TypeReference<Map<String, String>>(){}));
    }

    //--------------------------------- Private methods ----------------------------------------//

    private void assertMapEquals(Map<String, String> expected, Map<String, String> actual) {
        assertEquals(expected.keySet().size(), actual.keySet().size());
        expected.entrySet().stream().forEach(entry -> {
            assertTrue(actual.containsKey(entry.getKey()));
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        });
    }

    private Map<String, String> getExpectedFeedMap(final Timestamp timestamp) {
        return new HashMap<String, String>() {
            {
                put(NAME, "test");
                put(CREATED_ON, String.valueOf(timestamp.getTime()));
            }
        };
    }

    private Map<String, String> getExpectedUserMap() {
        return new HashMap<String, String>() {
            {
                put(ID, null);
                put(NAME, "test");
                put(CREATED_ON, null);
            }
        };
    }

    //----------------------------------- Private Class ------------------------------------------//
    private static class JsonModule extends CoreModule {
        public JsonModule(final Properties appConfig, final Path configDirectoryPath) {
            super(appConfig, configDirectoryPath);
        }

        @Override
        protected void addDatabaseDependencies() {
        }

        @Override
        protected void addServices() {
        }
    }
}
