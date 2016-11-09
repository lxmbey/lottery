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

	public AuthorizeLog() {

	}

	public AuthorizeLog(String openId, Date authorizeDate, String nickname) {
		this.openId = openId;
		this.authorizeDate = authorizeDate;
		this.nickname = nickname;
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

}
