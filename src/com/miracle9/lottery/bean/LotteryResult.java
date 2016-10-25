package com.miracle9.lottery.bean;

/**
 * 抽奖返回
 */
public class LotteryResult extends Result {
	public int resultType;
	public int[] result;

	public LotteryResult(int success, int resultType, int[] result, String message) {
		super(success, message);
		this.resultType = resultType;
		this.result = result;
	}

}
