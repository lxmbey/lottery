package com.miracle9.lottery.bean;

/**
 * 签名返回
 */
public class SignResult extends Result {
	public String signature;
	public long timestamp;
	public String noncestr;
	public String appId;

	public SignResult(int success, String message, String signature, long timestamp, String noncestr, String appId) {
		super(success, message);
		this.signature = signature;
		this.timestamp = timestamp;
		this.noncestr = noncestr;
		this.appId = appId;
	}

}
