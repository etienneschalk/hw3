package common;

import java.io.Serializable;

public class TcpFile implements Serializable{
	private static final long serialVersionUID = 214120349654L;
	
	private byte[] fileContents;
	private String owner;
	private String filename;
	private int fileSize;
	private int responseCode;
	
	public byte[] getFileContents() {
		return fileContents;
	}
	public void setFileContents(byte[] fileContents) {
		this.fileContents = fileContents;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
}
