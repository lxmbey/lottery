package com.miracle9.lottery.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AuthorizeLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(nullable = false, unique = true)
	private String openId;
	private Date authorizeDate;
	private String nickname;
	private int awardNum = 3;
	private Date refreshDate;

	public AuthorizeLog() {

	}

	public AuthorizeLog(String openId, Date authorizeDate, String nickname, int awardNum) {
		this.openId = openId;
		this.authorizeDate = authorizeDate;
		this.nickname = nickname;
		this.awardNum = awardNum;
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

	public Date getAuthorizeDate() {
		return authorizeDate;
	}

	public void setAuthorizeDate(Date authorizeDate) {
		this.authorizeDate = authorizeDate;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getAwardNum() {
		return awardNum;
	}

	public void setAwardNum(int awardNum) {
		this.awardNum = awardNum;
	}

	public Date getRefreshDate() {
		return refreshDate;
	}

	public void setRefreshDate(Date refreshDate) {
		this.refreshDate = refreshDate;
	}

}
