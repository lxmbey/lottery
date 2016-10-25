package com.miracle9.lottery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle9.lottery.entity.AwardConfig;
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
		NOT(-1), BIG(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);
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
		for (AwardConfig w : AwardConfigService.awardMap.values()) {
			Long time = AwardConfigService.awardTime.get(w.getAwardType());
			if (isFirstDay()) {
				if (System.currentTimeMillis() - time >= w.getFirstDayinterval()) {
					AwardConfigService.awardTime.put(w.getAwardType(), System.currentTimeMillis());
					return w.getAwardType();
				}
			} else {
				if (System.currentTimeMillis() - time >= w.getOtherDayinterval()) {
					AwardConfigService.awardTime.put(w.getAwardType(), System.currentTimeMillis());
					return w.getAwardType();
				}
			}
		}
		return -1;
	}

	private boolean isFirstDay() {
		return TextUtil.isSameDay(TextUtil.dateformat(gameConfig.getBeginDate()), new Date());
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
