package pa.com.segurossura.logsandaudit.config.logs;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.Iterator;

public class AuditRoutingAppender extends AppenderBase<ILoggingEvent>
        implements AppenderAttachable<ILoggingEvent> {

    private final AppenderAttachableImpl<ILoggingEvent> appenderAttachable = new AppenderAttachableImpl<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        String logType = eventObject.getMDCPropertyMap().get("log_type");
        Iterator<Appender<ILoggingEvent>> it = appenderAttachable.iteratorForAppenders();
        while (it.hasNext()) {
            Appender<ILoggingEvent> appender = it.next();
            String name = appender.getName();
            if ("AUDIT".equalsIgnoreCase(logType) && "AUDIT_LOG_FILE".equalsIgnoreCase(name)) {
                appender.doAppend(eventObject);
                return;
            } else if (!"AUDIT".equalsIgnoreCase(logType) && "APP_LOG_FILE".equalsIgnoreCase(name)) {
                appender.doAppend(eventObject);
                return;
            }
        }
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        appenderAttachable.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return appenderAttachable.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return appenderAttachable.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return appenderAttachable.isAttached(appender);
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return appenderAttachable.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return appenderAttachable.detachAppender(name);
    }

    @Override
    public void detachAndStopAllAppenders() {
        appenderAttachable.detachAndStopAllAppenders();
    }
}
