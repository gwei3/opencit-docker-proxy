package com.intel.mtwilson.dockerproxy.api.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "methodCall")
public class MethodCall {

    public String methodName;

    public Params params;

    public String getMethodName() {
	return methodName;
    }

    public void setMethodName(String methodName) {
	this.methodName = methodName;
    }

    public Params getParams() {
	return params;
    }

    public void setParams(Params params) {
	this.params = params;
    }

}
