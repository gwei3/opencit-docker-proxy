package com.intel.mtwilson.dockerproxy.vrtm.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RPClient {
	private InetSocketAddress rpcoreEndpoint;
	private Socket rpSock;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RPClient.class);

	public RPClient(String hostName, int port) {
		this.rpcoreEndpoint = new InetSocketAddress(hostName, port);
		this.rpSock = new Socket();
	}

	public TCBuffer send(TCBuffer outTCBuffer) throws IOException {
		if (!rpSock.isConnected()) {
			rpSock.connect(rpcoreEndpoint);
		}
		ByteArrayOutputStream rpOutStream = new ByteArrayOutputStream();
		/*
		 * String rpId = String.valueOf(outTCBuffer.getRpId());
		 * 
		 * byte[] arr = rpId.getBytes(); rpOutStream.write(arr);
		 * rpOutStream.write('\0');
		 */

		OutputStream rpcoreOut = rpSock.getOutputStream();
		rpOutStream.writeTo(rpcoreOut);
		rpOutStream.flush();
		rpcoreOut.flush();

		outTCBuffer.serializeTCBuffer(rpcoreOut);

		InputStream rpcoreIn = rpSock.getInputStream();
		TCBuffer inTCBuffer = new TCBuffer();
		inTCBuffer.deSerializeTCBuffer(rpcoreIn);

		return inTCBuffer;
	}

	public void close() {
		try {
			log.info("###### Closing vrtm socket");
			rpSock.shutdownOutput();
			rpSock.shutdownInput();
			rpSock.close();
		} catch (Exception e) {
			log.error("Unable to close socket RPCLient", e);
		}
	}

}
