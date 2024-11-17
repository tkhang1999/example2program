package synth.util;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    private static final String FORMAT = "[%1$tF %1$tT.%1$tL] [%2$s] [%3$s.%4$s] %5$s %n";
    
    @Override
    public synchronized String format(LogRecord lr) {
        return String.format(FORMAT, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
                lr.getSourceClassName(), lr.getSourceMethodName(), lr.getMessage());
    }
}
