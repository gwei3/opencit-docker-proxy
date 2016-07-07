package com.intel.mtwilson.dockerproxy.vrtm.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;

import com.intel.mtwilson.dockerproxy.api.jaxb.MethodCall;
import com.intel.mtwilson.dockerproxy.api.jaxb.Param;
import com.intel.mtwilson.dockerproxy.api.jaxb.Params;
import com.intel.mtwilson.dockerproxy.api.jaxb.Value;
import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.common.DockerProxyCache;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;




public class VrtmManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VrtmManager.class);

    public void checkAndUpdateVrtm(String containerId, String state) throws DockerProxyException {
	/// Only update VRTM, if there is state change from previous flow update
	/// which we have already cached
	String previousState = DockerProxyCache.getVrtmStateForContainerId(containerId);
	log.debug("VRTM previousState cached :: {} and new state:: {}" , previousState , state);
	if (StringUtils.isBlank(state) || (StringUtils.isNotBlank(state) && !state.equalsIgnoreCase(previousState))) {
	    updateVrtm(containerId, state);
	    DockerProxyCache.setVrtmStateForContainerId(containerId, previousState);
	}
    }

    public void updateVrtm(String containerId, String state) throws DockerProxyException {
	RPClient rpClient = null;
	int statusValue = -1;
	/// We initialize buffer using vrtm remoteprocedure call index and
	/// status
	TCBuffer tcBuffer = new TCBuffer(Constants.VRTM_CONTAINER_STATUS_INDEX, 0);

	String xmlString;
	try {
	    /// Create vrtm xml request
	    xmlString = getUpdateContainerStatusXML(containerId, state);
	
	    log.debug("VRTM request xml created for :: {}", containerId);
	} catch (JAXBException e) {
	    log.error("Error creating a request xml, containerId"+containerId, e);
	    throw new DockerProxyException(e);
	}
	tcBuffer.setRPCPayload(xmlString.getBytes());

	rpClient = new RPClient(Constants.LOCALHOST, DockerProxyCache.getVrtmPort());

	try {
	    tcBuffer = rpClient.send(tcBuffer); /// send to vrtm
	} catch (IOException e) {
	    log.error("Error sending request to vRTM, containerId"+containerId, e);
	    throw new DockerProxyException(e);
	}finally{
	    rpClient.close(); 
	}
	String xmlResponse = tcBuffer.getPayloadAsString(); // Get xml response
							    // of vrtm from
							    // buffer
	log.trace("xmlResponse from VRTM:::" + xmlResponse);
	String status;
	try {
	    status = getVrtmStatus(xmlResponse);
	} catch (JAXBException e) {
	    log.error("Error reading vRTM response, containerId"+containerId, e);
	    throw new DockerProxyException(e);
	}
	log.info("########### status from vrtm::: {},containerId: {}",  status,containerId);
	statusValue = Integer.parseInt(status);

	if (statusValue < 0) {
	    log.debug("Vrtm  status update failed with statusValue::" + statusValue+", containerId:"+containerId);
	}

    }

    /// Parse xml string and get the status from response sent by vrtm
    public String getVrtmStatus(String xml) throws JAXBException {
	JAXBContext jaxbContext = JAXBContext.newInstance(MethodCall.class);
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	MethodCall methodCallObj = null;
	StringReader reader = new StringReader(xml);
	methodCallObj = (MethodCall) unmarshaller.unmarshal(reader);
	String base64BinaryStatus = methodCallObj.getParams().getParam().get(0).getValue().getString();
	String status = new String(DatatypeConverter.parseBase64Binary(base64BinaryStatus));
	return status;

    }

    /// Creates xml for vrtm request using containerId and state
    private String getUpdateContainerStatusXML(String containerId, String state) throws JAXBException {

	JAXBContext jc = JAXBContext.newInstance(MethodCall.class);

	MethodCall methodCall = new MethodCall();
	methodCall.setMethodName(Constants.METHODNAME_CONTAINER_UPDATE_STATUS);
	Params params = new Params();
	Param param1 = new Param();
	Value value1 = new Value();
	value1.setString(DatatypeConverter.printBase64Binary(containerId.getBytes()));
	param1.setValue(value1);
	Param param2 = new Param();
	Value value2 = new Value();
	value2.setString(DatatypeConverter.printBase64Binary(state.getBytes()));
	param2.setValue(value2);
	List<Param> paramList = new ArrayList<Param>();
	paramList.add(param1);
	paramList.add(param2);
	params.setParam(paramList);
	methodCall.setParams(params);
	Marshaller marshaller = jc.createMarshaller();
	java.io.StringWriter sw = new StringWriter();

	marshaller.marshal(methodCall, sw);
	String xml = sw.toString();

	return xml;
    }

}
