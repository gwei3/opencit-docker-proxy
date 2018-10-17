package com.intel.mtwilson.dockerproxy.api.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "params")
public class Params {

    List<Param> param = new ArrayList<Param>();

    public List<Param> getParam() {
	return param;
    }

    public void setParam(List<Param> param) {
	this.param = param;
    }

}
