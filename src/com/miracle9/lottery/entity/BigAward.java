package com.miracle9.lottery.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 指定的大奖所属者
 */
@Entity
public class BigAward {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String openId;

	public BigAward() {

	}

	public BigAward(String openId) {
		this.openId = openId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

}
