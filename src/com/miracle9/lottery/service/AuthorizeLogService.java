package com.miracle9.lottery.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miracle9.lottery.dao.BaseDao;
import com.miracle9.lottery.entity.AuthorizeLog;

@Service
public class AuthorizeLogService {
	/**
	 * 授权过的openid->nickname缓存
	 */
	public static Map<String, String> openidCacheMap = new ConcurrentHashMap<>();

	@Autowired
	private BaseDao baseDao;

	public void add(AuthorizeLog log) {
		baseDao.add(log);
	}

	public void loadCache() {
		List<AuthorizeLog> logs = baseDao.getList(AuthorizeLog.class, "from AuthorizeLog");
		for (AuthorizeLog l : logs) {
			openidCacheMap.put(l.getOpenId(), l.getNickname());
		}
	}
}
