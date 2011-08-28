package me.sheimi.nutch.hdfs;

public class HdfsError extends HdfsException{
	
	private int code;
	
	public int getCode(int code) {
		return code;
	}
	
	public HdfsError(int code) {
		super("File Error: " + code);
		this.code = code;
	}

}
