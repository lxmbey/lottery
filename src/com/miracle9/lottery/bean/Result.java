package com.miracle9.lottery.bean;

/**
 * 基础接口返回值
 */
public class Result {
	public int success;
	public String message = "";

	public Result(int success, String message) {
		super();
		this.success = success;
		this.message = message;
	}
}
