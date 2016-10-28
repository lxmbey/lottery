package com.miracle9.lottery.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miracle9.lottery.dao.BaseDao;
import com.miracle9.lottery.entity.BigAward;

@Service
public class BigAwardService {
	public static List<String> bigAwardOpenIds = new ArrayList<>();

	@Autowired
	private BaseDao baseDao;

	public void loadCache() {
		List<BigAward> awards = baseDao.getList(BigAward.class, "from BigAward");
		for (BigAward a : awards) {
			bigAwardOpenIds.add(a.getOpenId());
		}
	}
}
