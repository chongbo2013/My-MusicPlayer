package com.lewa.player.model;

public class MusicDownloadStatus {
	private int status;
	private long currentBytes;
	private long totalBytes;
	private String errorMsg;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getCurrentBytes() {
		return currentBytes;
	}
	public void setCurrentBytes(long currentBytes) {
		this.currentBytes = currentBytes;
	}
	public long getTotalBytes() {
		return totalBytes;
	}
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	
}
