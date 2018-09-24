package com.example.vmec.forkmonitor;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.vmec.forkmonitor.utils.PermissionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import timber.log.Timber;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Stofanak on 13/09/2018.
 */
public class FileLoggingTree extends Timber.DebugTree {
    static Logger mLogger = LoggerFactory.getLogger(FileLoggingTree.class);
    private static final String LOG_PREFIX = "forkmonitor-log";

    public FileLoggingTree(Context context) {
        checkPermissions(context);
    }

    private void init(final Context context) {
        File logsFolder = new File(Environment.getExternalStorageDirectory() + "/ForkMonitor/logs");
        boolean isPresent = true;
        if (!logsFolder.exists()) {
            isPresent = logsFolder.mkdirs();
        }
        if (!isPresent) {
            //TODO: Show ui error
            Timber.e("Failed to write text.txt");
        }

        final String logDirectory = logsFolder.getAbsolutePath();
        configureLogger(logDirectory);
    }

    private void checkPermissions(final Context context) {
        if(!PermissionUtils.hasPermissions(context, WRITE_EXTERNAL_STORAGE)){
            Timber.e("Missing permission to write external storage");
        } else {
            init(context);
        }
    }

    @Override protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE) {
            return;
        }

        String logMessage = tag + ": " + message;
        switch (priority) {
            case Log.DEBUG:
                mLogger.debug(logMessage);
                break;
            case Log.INFO:
                mLogger.info(logMessage);
                break;
            case Log.WARN:
                mLogger.warn(logMessage);
                break;
            case Log.ERROR:
                mLogger.error(logMessage);
                break;
        }
    }

    private void configureLogger(String logDirectory) {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setFile(logDirectory + "/" + LOG_PREFIX + "-latest.txt");

        SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
        fileNamingPolicy.setContext(loggerContext);
        fileNamingPolicy.setMaxFileSize("5MB");

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(logDirectory + "/" + LOG_PREFIX + ".%d{yyyy-MM-dd}.%i.txt");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
        rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
        rollingPolicy.start();

//        HTMLLayout htmlLayout = new LayoutBase<>();
//        htmlLayout.setContext(loggerContext);
//        htmlLayout.setPattern("%d{HH:mm:ss.SSS}%level%thread%msg");
//        htmlLayout.start();

//        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
//        encoder.setContext(loggerContext);
//        encoder.setLayout(htmlLayout);
//        encoder.start();

        // Alternative text encoder - very clean pattern, takes up less space
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setPattern("%date %level [%thread] %msg%n");
        encoder.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(rollingFileAppender);

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(loggerContext);
    }
}
