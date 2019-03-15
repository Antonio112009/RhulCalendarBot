/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package runable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadConfig {

    public void start(){
        Task task = new Task();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        executorService.scheduleAtFixedRate(task::downloadCalendars, 0, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(task::cutEvents, 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(task::sendNotificationEveryHour, 2, 1, TimeUnit.MINUTES);
    }
}
