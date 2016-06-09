package uk.ac.kcl.utils;

import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by rich on 09/06/16.
 */
public class LoggerHelper implements InitializingBean {
    private String contextID;

    public LoggerHelper() {};

    public void setContextID(String id) {
        this.contextID = id;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if ( contextID != null )
            MDC.put("contextID", contextID.toString());
    }
}
