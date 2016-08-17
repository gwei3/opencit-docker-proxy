package com.intel.mtwilson.dockerproxy.common;

public class UnmountTask implements Runnable {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnmountTask.class);

	private String imageId;
	private String containerId;
	

	public UnmountTask() {

	}

	public UnmountTask(String imageId, String containerId) {
		this.imageId = imageId;

		this.containerId=containerId;

	}

	@Override
	public void run() {
		log.debug("Going to unmount for containerId::"+containerId+" imageId::"+imageId);
		MountUtil.unmountImage(containerId, imageId);
		
	}

}
