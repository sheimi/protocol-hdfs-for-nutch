package me.sheimi.nutch.hdfs;

import org.apache.nutch.protocol.ProtocolException;

public class HdfsException extends ProtocolException {
	public HdfsException() {
		super();
	}
	
	public HdfsException(String message) {
		super(message);
	}
	
	public HdfsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public HdfsException(Throwable cause) {
		super(cause);
	}
}
