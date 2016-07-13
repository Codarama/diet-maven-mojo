package org.codarama.diet;

import org.apache.maven.plugin.logging.Log;
import org.codarama.diet.api.reporting.listener.EventListener;
import org.codarama.diet.event.model.MinimizationEvent;

/**
 * A listener to attach so the user can see progress in the console while plugin is runnig.
 *
 * Created by Ayld on 7/10/16.
 */
public class ProgressListener implements EventListener<MinimizationEvent>{

    private final Log logger;

    public ProgressListener(Log logger) {
        this.logger = logger;
    }

    public void on(MinimizationEvent event) {
        logger.info(event.toString());
    }
}
