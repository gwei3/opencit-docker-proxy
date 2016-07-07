package com.intel.mtwilson.dockerproxy.api.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "param")
public class Param {

    public Value value;

    public Value getValue() {
	return value;
    }

    public void setValue(Value value) {
	this.value = value;
    }

}
