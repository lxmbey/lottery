package com.miracle9.lottery.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miracle9.lottery.dao.BaseDao;
import com.miracle9.lottery.entity.LotteryLog;

@Service
public class LotteryLogService {
	public static Map<String, Boolean> awardCacheMap = new ConcurrentHashMap<>();

	@Autowired
	private BaseDao baseDao;

	public void add(LotteryLog log) {
		baseDao.add(log);
	}

	public LotteryLog getByOpenId(String openId) {
		return baseDao.getByField(LotteryLog.class, "from LotteryLog where openId = ?", openId);
	}

	public void update(LotteryLog log) {
		baseDao.update(log);
	}

	public void loadCache() {
		List<LotteryLog> logs = baseDao.getList(LotteryLog.class, "from LotteryLog");
		for (LotteryLog l : logs) {
			awardCacheMap.put(l.getOpenId(), true);
		}
	}
}