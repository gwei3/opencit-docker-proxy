package com.intel.mtwilson.dockerproxy.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;


/**
 * this class serves as an adapter. the sub class implements only the methods
 * that it is interested in.
 * 
 * @author gs-0681
 *
 */
public class BypassWorkflow extends RequestWorkflow {
    
    public BypassWorkflow(String requestMethod, String requestUri, String body) {
	super(requestMethod, requestUri, body);
		// TODO Auto-generated constructor stub
    }
    
    public BypassWorkflow(String requestMethod, String requestUri) {
	super(requestMethod, requestUri);
	// TODO Auto-generated constructor stub
    }

    private static final Logger log = LoggerFactory.getLogger(BypassWorkflow.class);

 

    @Override
    public boolean  validateClientRequestAndInit() throws DockerProxyException {
	log.debug("BypassWorkflow workflow does not involve any validation");
	return true;
    }

    @Override
    public void processResponseFromEngine() throws DockerProxyException {
	log.debug("BypassWorkflow workflow does not involve any engine response processing");
    }

}
