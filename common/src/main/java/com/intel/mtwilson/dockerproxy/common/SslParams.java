package com.intel.mtwilson.dockerproxy.common;

public class SslParams {

    public String caCertificatePath;
    public String serverCertificatePath;
    public String privateKeyPath;
    public String getCaCertificatePath() {
        return caCertificatePath;
    }
    public void setCaCertificatePath(String caCertificatePath) {
        this.caCertificatePath = caCertificatePath;
    }
    public String getServerCertificatePath() {
        return serverCertificatePath;
    }
    public void setServerCertificatePath(String serverCertificatePath) {
        this.serverCertificatePath = serverCertificatePath;
    }
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }
    @Override
    public String toString() {
	return "SslParmas [caCertificatePath=" + caCertificatePath
		+ ", serverCertificatePath=" + serverCertificatePath
		+ ", privateKeyPath=" + privateKeyPath + "]";
    }
    
    
    
    
}


