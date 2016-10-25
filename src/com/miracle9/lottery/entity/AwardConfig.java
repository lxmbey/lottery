package com.miracle9.lottery.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * 奖项配置
 */
@Entity
public class AwardConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int awardType;
	private int firstDayNum;
	private int otherDayNum;
	private int hourNum;// 每天的开奖小时数

	@Transient
	private long firstDayinterval;// 首日开奖间隔毫秒
	@Transient
	private long otherDayinterval;// 其他日开奖间毫秒

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAwardType() {
		return awardType;
	}

	public void setAwardType(int awardType) {
		this.awardType = awardType;
	}

	public int getFirstDayNum() {
		return firstDayNum;
	}

	public void setFirstDayNum(int firstDayNum) {
		this.firstDayNum = firstDayNum;
	}

	public int getOtherDayNum() {
		return otherDayNum;
	}

	public void setOtherDayNum(int otherDayNum) {
		this.otherDayNum = otherDayNum;
	}

	public int getHourNum() {
		return hourNum;
	}

	public void setHourNum(int hourNum) {
		this.hourNum = hourNum;
	}

	public long getFirstDayinterval() {
		return firstDayinterval;
	}

	public void setFirstDayinterval(long firstDayinterval) {
		this.firstDayinterval = firstDayinterval;
	}

	public long getOtherDayinterval() {
		return otherDayinterval;
	}

	public void setOtherDayinterval(long otherDayinterval) {
		this.otherDayinterval = otherDayinterval;
	}

	@Override
	public String toString() {
		return "AwardConfig [id=" + id + ", awardType=" + awardType + ", firstDayNum=" + firstDayNum + ", otherDayNum="
				+ otherDayNum + ", hourNum=" + hourNum + ", firstDayinterval=" + firstDayinterval
				+ ", otherDayinterval=" + otherDayinterval + "]";
	}

}
