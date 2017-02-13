package com.miracle9.lottery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miracle9.lottery.dao.BaseDao;
import com.miracle9.lottery.entity.Chat;

@Service
public class ChatService {
	@Autowired
	private BaseDao baseDao;
	
	public void add(Chat chat){
		baseDao.add(chat);
	}
	
	public Chat getByOpenId(String openId){
		return baseDao.getByField(Chat.class, "from Chat where openId = ?", openId);
	}
}
