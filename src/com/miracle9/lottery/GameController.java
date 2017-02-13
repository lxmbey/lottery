package com.miracle9.lottery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle9.lottery.service.AwardConfigService;
import com.miracle9.lottery.utils.TextUtil;

/**
 * 抽奖控制器
 */
@Component
public class GameController {
	@Autowired
	private GameConfig gameConfig;

	/**
	 * 开奖类型
	 */
	public enum AwardType {
		NOT(0), TWO(1), FIVE(2);
		private int value;

		private AwardType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	public Random random = new Random();
	public static int MAX_BALL = 6;
	/**
	 * 所有球类型
	 */
	public static List<Integer> ballList = new ArrayList<>();
	/**
	 * 可中奖的球类型
	 */
	public static List<Integer> awardBallList = new ArrayList<>();
	static {
		for (int i = 1; i <= 10; i++) {
			ballList.add(i);
		}
		for (int i = 1; i <= 6; i++) {
			awardBallList.add(i);
		}
	}

	/**
	 * 获取出奖类型
	 * 
	 * @return int
	 */
	public synchronized int draw() {
		long interval = getInterval();
		if (interval == 0) {
			return AwardType.NOT.value;
		}
		if (System.currentTimeMillis() - AwardConfigService.awardTime >= interval) {
			AwardConfigService.awardTime = System.currentTimeMillis();
			return randomAward();
		}
		return AwardType.NOT.value;
	}

	public int randomAward() {
		int i = 100;
		if (TextUtil.random.nextInt(i) < 6) {
			return AwardType.FIVE.value;
		}
		return AwardType.TWO.value;
	}

	/**
	 * 获取出奖间隔
	 * 
	 * @return
	 */
	private long getInterval() {
		int hour = TextUtil.getCurrentHour();
		long interval = 0;
		if (isFirstDay()) {
			if (hour >= 9 && hour < 11) {
				interval = 18000;
			} else if (hour >= 11 && hour < 23) {
				interval = 7500;
			}
		} else {
			if (hour >= 9 && hour < 11) {
				interval = 30000;
			} else if (hour >= 10 && hour < 15) {
				interval = 12000;
			}
		}
		return interval;
	}

	/**
	 * 归还未填写联系方式的奖项
	 * 
	 * @param awardType
	 */
	public synchronized void repayAward(int awardType) {
		long interval = getInterval();
		AwardConfigService.awardTime = AwardConfigService.awardTime - interval;
	}

	private boolean isFirstDay() {
		return TextUtil.isSameDay(gameConfig.begin, new Date());
	}

	/**
	 * 根据中奖球的个数随机出奖
	 * 
	 * @param num
	 * @return
	 */
	public int[] getBallsOfNum(int num) {
		int[] balls = new int[MAX_BALL];
		if (num < 0 || num > 6) {
			return balls;
		}
		List<Integer> ballTemp = new ArrayList<>(ballList);
		List<Integer> awardBallTemp = new ArrayList<>(awardBallList);

		for (int n = 0; n < num; n++) {
			// 随机一个中奖球
			int i = awardBallTemp.get(random.nextInt(awardBallTemp.size()));
			balls[i - 1] = i;
			awardBallTemp.remove((Integer) i);
			ballTemp.remove((Integer) i);
		}

		for (int j = 1; j <= MAX_BALL; j++) {
			if (!awardBallTemp.contains(j)) {// 已经出奖的球
				continue;
			}
			// 随机一个不能出奖的球
			int t = ballTemp.get(random.nextInt(ballTemp.size()));
			while (t == j) {
				t = ballTemp.get(random.nextInt(ballTemp.size()));
			}
			balls[j - 1] = t;
			ballTemp.remove((Integer) t);
		}

		return balls;
	}

}
