package proj.karthik.feed.reader;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import proj.karthik.feed.reader.store.ArticleStore;
import proj.karthik.feed.reader.store.FeedStore;
import proj.karthik.feed.reader.store.SubscriptionStore;
import proj.karthik.feed.reader.store.UserStore;
import proj.karthik.feed.reader.web.RouteManager;

/**
 * Entry point into the application
 */
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final Options OPTIONS = new Options();
    private static final String CONF = "conf";

    static {
        OPTIONS.addOption(CONF, "Directory containing configuration files. Application looks " +
                "for 3 configuration files - app-config.properties, db-config.properties. Please" +
                " note the names are case sensitive");
    }

    public static void main(String[] args) throws IOException {
        CommandLineParser cliParser = new DefaultParser();
        try {
            Properties appConfig = new Properties();
            setupDirs(appConfig);
            setupLogFile();
            CommandLine commandLine = cliParser.parse(OPTIONS, args);
            Path confDirectory;
            if (commandLine.hasOption(CONF)) {
                confDirectory = Paths.get(commandLine.getOptionValue(CONF));
            } else {
                confDirectory = Paths.get(".", CONF).toAbsolutePath();
            }
            File confDirectoryObj = confDirectory.toFile();
            File configFile = new File(confDirectoryObj, Constants.APP_CONFIG_PROPS);

            if (configFile.exists()) {
                try (InputStream propStream = configFile.toURI().toURL().openStream()) {
                    appConfig.load(propStream);
                }
            }
            if (confDirectoryObj.exists() && confDirectoryObj.isDirectory()) {
                Injector injector = Guice.createInjector(new CoreModule(appConfig, confDirectory));
                if (appConfig.containsKey(Constants.PORT)) {
                    System.out.println("Web server listening at " +
                            appConfig.getProperty(Constants.PORT));
                } else {
                    System.out.println("Web server listening at localhost:4567");
                }
                initStores(injector);
                RouteManager routeManager = injector.getInstance(RouteManager.class);
                routeManager.initRoutes(appConfig);
            } else {
                throw new RuntimeException(String.format( "Configuration directory %s is not " +
                        "valid or readable",confDirectory.toString()));
            }
        } catch (Throwable throwable) {
            System.err.println(throwable.getMessage());
            printHelp();
        }
    }

    private static void initStores(final Injector injector) {
        FeedStore feedStore = injector.getInstance(FeedStore.class);
        LOGGER.info("Initializing feed store");
        feedStore.init();
        UserStore userStore = injector.getInstance(UserStore.class);
        LOGGER.info("Initializing user store");
        userStore.init();
        SubscriptionStore subscriptionStore = injector.getInstance(SubscriptionStore.class);
        LOGGER.info("Initializing subscription store");
        subscriptionStore.init();
        ArticleStore articleStore = injector.getInstance(ArticleStore.class);
        LOGGER.info("Initializing article store");
        articleStore.init();
    }

    private static void setupDirs(Properties conf) {
        Path dataDir = Paths.get(".", "data", "store");
        if (!dataDir.toFile().exists()) {
            System.out.printf("Creating new data store directory: %s%n", dataDir.toString());
            dataDir.toFile().mkdirs();
        }

        Path feedDir = Paths.get(".", "data", "feed");
        if (!feedDir.toFile().exists()) {
            System.out.printf("Creating new feed directory: %s%n", feedDir.toString());
            feedDir.toFile().mkdirs();
        }
        conf.setProperty(Constants.FEED_DIR, dataDir.toAbsolutePath().toString());
    }

    private static void setupLogFile() throws IOException {
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setName("FileLogger");
        Path logDir = Paths.get(".", "log");
        if (!logDir.toFile().exists()) {
            System.out.printf("Creating new log directory: %s%n", logDir.toString());
            logDir.toFile().mkdirs();
        }
        Path logFilePath = Paths.get(".", "log", "feedsystem.log");
        if (!logFilePath.toFile().exists()) {
            System.out.printf("Creating new log file: %s%n", logFilePath.toString());
            Files.createFile(logFilePath);
        }
        fileAppender.setFile(logFilePath.toFile().getAbsolutePath());
        fileAppender.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fileAppender.setThreshold(Level.DEBUG);
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize("10MB");
        fileAppender.setMaxBackupIndex(5);
        fileAppender.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar feedsystem.jar ", OPTIONS);
    }
}
