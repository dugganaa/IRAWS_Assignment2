package iraws_group;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import jdk.jfr.internal.LogLevel;

public class Log {
    private Logger logger;
    private Handler outputHandler;
    public Log(String name, String outputFolder) {
        try {
            File directory = new File(outputFolder);
            if (! directory.exists()){
                directory.mkdir();
            }

            logger = Logger.getLogger(name);
            LogManager.getLogManager().reset();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date date = new Date();
            outputHandler = new FileHandler(outputFolder + "/" + dateFormat.format(date) + ".txt");
            outputHandler.setFormatter(new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
      
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
            logger.addHandler(outputHandler);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void LogInfo(String msg) {
        logger.info(msg);
    }

    public void LogWarn(String msg) {
        logger.warning(msg);
    }
}
