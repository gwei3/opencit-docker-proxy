package com.intel.mtwilson.dockerproxy.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyThreadExecutor {
	
	 public static ExecutorService executorService = null;

	    static {
	        executorService = Executors.newFixedThreadPool(10);
	    }

	    public static void submitTask(UnmountTask unmountTask) {
	        executorService.execute(unmountTask);
	
	    }

	
	
}
