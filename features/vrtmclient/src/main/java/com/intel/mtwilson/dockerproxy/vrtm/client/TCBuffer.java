package com.intel.mtwilson.dockerproxy.vrtm.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TCBuffer {
	private static final String EMPTY = "EMPTY";
	private ByteBuffer brpcCallIndex;
	private ByteBuffer brpcPayloadSize;
	private ByteBuffer brpcCallStatus;
	private byte[] rpcPayload;

	public String getPayloadAsString() {
		return new String(rpcPayload);
	}

	public TCBuffer() {

		brpcCallIndex = ByteBuffer.allocate(4);
		brpcPayloadSize = ByteBuffer.allocate(4);
		brpcCallStatus = ByteBuffer.allocate(4);

		rpcPayload = EMPTY.getBytes();

		brpcCallIndex.order(ByteOrder.LITTLE_ENDIAN);
		brpcPayloadSize.order(ByteOrder.LITTLE_ENDIAN);
		brpcCallStatus.order(ByteOrder.LITTLE_ENDIAN);

	}

	public TCBuffer(int rpcCallIndex, int rpcCallStatus, String rpcPayload) {
		this();
		setRPCCallIndex(rpcCallIndex);
		setRPCPayloadSize(rpcPayload.length());
		setRPCCallStatus(rpcCallStatus);
		setRPCPayload(rpcPayload.getBytes());
	}

	public TCBuffer(int rpcCallIndex, int rpcCallStatus) {
		this(rpcCallIndex, rpcCallStatus, EMPTY);
	}

	public String getRPCPayload() {
		return new String(rpcPayload);
	}

	public void setRPCPayload(byte[] rpcPayload) {
		this.rpcPayload = rpcPayload;
		setRPCPayloadSize(this.rpcPayload.length);
	}

	public int getRPCCallIndex() {
		if (null != brpcCallIndex) {
			brpcCallIndex.rewind();
			return brpcCallIndex.getInt();
		}
		return -1;
	}

	public void setRPCCallIndex(int rpcCallIndex) {
		brpcCallIndex.clear();
		brpcCallIndex.putInt(rpcCallIndex);
	}

	public int getRPCCallStatus() {
		if (null != brpcCallStatus) {
			brpcCallStatus.rewind();
			return brpcCallStatus.getInt();
		}
		return -1;
	}

	public void setRPCCallStatus(int callStatus) {
		brpcCallStatus.clear();
		brpcCallStatus.putInt(callStatus);
	}

	public void setRPCPayloadSize(int payloadSize) {
		brpcPayloadSize.clear();
		brpcPayloadSize.putInt(payloadSize);
	}

	public int getRPCPayloadSize() {
		if (null != brpcPayloadSize) {
			brpcPayloadSize.rewind();
			return brpcPayloadSize.getInt();
		}
		return -1;
	}

	public void serializeTCBuffer(OutputStream out) throws IOException {

		out.write(brpcCallIndex.array());
		out.write(brpcPayloadSize.array());
		out.write(brpcCallStatus.array());
		out.write(rpcPayload);
		out.flush();
	}

	private void setInternalValues(ByteBuffer buffer, byte[] b) {
		buffer.clear();
		buffer.put(b);
	}

	/**
	 * This function only set first 20 byte of TCBuffer
	 * 
	 * @param tcBufferByteStream
	 * @throws IOException
	 */
	public void deSerializeTCBuffer(InputStream in) throws IOException {
		byte[] bigBytes = new byte[4];
		// in.read(bigBytes);
		// setInternalValues(brpId, bigBytes);
		int read = in.read(bigBytes);
		if(read == -1){
			return;
		}
		setInternalValues(brpcCallIndex, bigBytes);
		read = in.read(bigBytes);
		if(read == -1){
			return;
		}
		setInternalValues(brpcPayloadSize, bigBytes);
		read = in.read(bigBytes);
		if(read == -1){
			return;
		}
		setInternalValues(brpcCallStatus, bigBytes);
		// in.read(bigBytes);
		// setInternalValues(boriginalRpId, bigBytes);
		rpcPayload = new byte[getRPCPayloadSize()];
		read = in.read(rpcPayload);
		if(read == -1){
			return;
		}

	}
}
