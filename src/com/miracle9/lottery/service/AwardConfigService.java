package com.miracle9.lottery.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miracle9.lottery.dao.BaseDao;
import com.miracle9.lottery.entity.AwardConfig;

@Service
public class AwardConfigService {
	public static Map<Integer, AwardConfig> awardMap = new HashMap<>();

	/**
	 * 开奖时间
	 */
	public static Long awardTime;

	@Autowired
	private BaseDao baseDao;

	public void loadAllConfig() {
		List<AwardConfig> list = baseDao.getList(AwardConfig.class, "from AwardConfig");
		for (AwardConfig c : list) {
			if (c.getFirstDayNum() > 0) {
				c.setFirstDayinterval(c.getHourNum() * 60 * 60 * 1000 / c.getFirstDayNum());
			} else {
				c.setFirstDayinterval(Long.MAX_VALUE);
			}
			if (c.getOtherDayNum() > 0) {
				c.setOtherDayinterval(c.getHourNum() * 60 * 60 * 1000 / c.getOtherDayNum());
			} else {
				c.setOtherDayinterval(Long.MAX_VALUE);
			}
			awardMap.put(c.getAwardType(), c);

		}
		// 初始化开奖时间
		awardTime  = System.currentTimeMillis();
	}
}
