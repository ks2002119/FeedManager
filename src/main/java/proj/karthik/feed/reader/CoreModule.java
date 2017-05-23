package proj.karthik.feed.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

import javax.sql.DataSource;

import proj.karthik.feed.reader.sql.SQLUtils;
import proj.karthik.feed.reader.store.ArticleStore;
import proj.karthik.feed.reader.store.DbArticleStore;
import proj.karthik.feed.reader.store.DbFeedStore;
import proj.karthik.feed.reader.store.DbSubscriptionStore;
import proj.karthik.feed.reader.store.DbUserStore;
import proj.karthik.feed.reader.store.FeedStore;
import proj.karthik.feed.reader.store.SubscriptionStore;
import proj.karthik.feed.reader.store.UserStore;
import proj.karthik.feed.reader.web.RouteManager;

import static proj.karthik.feed.reader.Constants.DB_CONFIG_PROPS;

/**
 * CoreModule manages various dependencies within the application.
 */
public class CoreModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(CoreModule.class);
    public static final String JDBC_URL = "jdbcUrl";
    public static final String PATTERN_JDBC_URL = "jdbc:h2:%sfeedstore;DB_CLOSE_DELAY=-1;";
    private final Properties appConfig;
    private final Path confDirPath;

    /**
     * Initalizes core module dependencies.
     *
     * @param appConfig
     * @param configDirectoryPath
     */
    public CoreModule(Properties appConfig, Path configDirectoryPath) {
        this.appConfig = appConfig;
        this.confDirPath = configDirectoryPath;
    }

    @Override
    protected void configure() {
        bind(Properties.class)
                .annotatedWith(Names.named(Constants.APP_CONF))
                .toInstance(appConfig);

        addJsonDependencies();
        addDatabaseDependencies();
        addServices();
    }

    //---------------------------------- protected methods -------------------------------------//

    protected void addJsonDependencies() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.CLOSE_CLOSEABLE,
                SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        bind(ObjectMapper.class).toInstance(objectMapper);
    }

    protected void addDatabaseDependencies() {
        File dbConfig = new File(this.confDirPath.toFile(), DB_CONFIG_PROPS);
        if (dbConfig.exists()) {
            LOG.info("Loading db configuration from {}", dbConfig.toString());
            HikariConfig config = new HikariConfig(dbConfig.getAbsolutePath());
            HikariDataSource hikariDataSource = new HikariDataSource(config);
            bind(DataSource.class).toInstance(hikariDataSource);
        } else {
            LOG.warn("DB configuration file {} does not exist. Using defaults.",
                    dbConfig.toString());
            Properties props = getDefaultProperties();
            if (!props.containsKey(JDBC_URL)) {
                String dataDir = getDataDir();
                String jdbcUrl = String.format(PATTERN_JDBC_URL, dataDir);
                LOG.info("Using JDBC url: {}", jdbcUrl);
                props.setProperty(JDBC_URL, jdbcUrl);
            }
            HikariConfig config = new HikariConfig(props);
            bind(DataSource.class).toInstance(new HikariDataSource(config));
            bind(SQLUtils.class).asEagerSingleton();
        }
    }

    protected Properties getDefaultProperties() {
        URL dbConfigUrl = getClass().getResource("/db-config.properties");
        Properties props = new Properties();
        try (InputStream inStream = dbConfigUrl.openStream()) {
            props.load(inStream);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading db configuration from " +
                    "classpath:%s", dbConfigUrl.toString()), e);
        }
        return props;
    }

    protected void addServices() {
        bind(RouteManager.class).asEagerSingleton();
        bind(ArticleStore.class).to(DbArticleStore.class).asEagerSingleton();
        bind(FeedStore.class).to(DbFeedStore.class).asEagerSingleton();
        bind(UserStore.class).to(DbUserStore.class).asEagerSingleton();
        bind(SubscriptionStore.class).to(DbSubscriptionStore.class).asEagerSingleton();
    }

    protected String getDataDir() {
        String dataDir = this.appConfig.getProperty(Constants.DATA_DIR);
        if (!dataDir.endsWith(File.separator)) {
            dataDir += dataDir + File.separator;
        }
        return dataDir;
    }
}
