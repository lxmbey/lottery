package com.miracle9.lottery;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.miracle9.lottery.service.AuthorizeLogService;
import com.miracle9.lottery.service.AwardConfigService;
import com.miracle9.lottery.service.LotteryLogService;
import com.miracle9.lottery.utils.TextUtil;

@Component
public class MyApplicationContext implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		ctx.getBean(AwardConfigService.class).loadAllConfig();
		ctx.getBean(AuthorizeLogService.class).loadCache();
		ctx.getBean(LotteryLogService.class).loadCache();
		
		GameConfig gameConfig = ctx.getBean(GameConfig.class);
		gameConfig.begin = TextUtil.dateformat(gameConfig.getBeginDate());
		gameConfig.end = TextUtil.dateformat(gameConfig.getEndDate());
	}

}
