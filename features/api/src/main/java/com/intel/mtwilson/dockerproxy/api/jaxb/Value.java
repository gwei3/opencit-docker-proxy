package com.intel.mtwilson.dockerproxy.api.jaxb;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "value")
public class Value {

    public String string;

    public String getString() {
	return string;
    }

    public void setString(String string) {
	this.string = string;
    }

}
