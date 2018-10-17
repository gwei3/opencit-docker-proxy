package com.intel.mtwilson.dockerproxy.exception;

public class DockerProxyException extends Exception {

  
    private static final long serialVersionUID = 1L;

    public DockerProxyException() {
	super();
    }

    public DockerProxyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

    public DockerProxyException(String message, Throwable cause) {
	super(message, cause);
    }

    public DockerProxyException(String message) {
	super(message);
    }

    public DockerProxyException(Throwable cause) {
	super(cause);
    }

}
