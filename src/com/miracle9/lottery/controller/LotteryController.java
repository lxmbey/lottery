package com.miracle9.lottery.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.miracle9.lottery.GameConfig;
import com.miracle9.lottery.GameController;
import com.miracle9.lottery.GameController.AwardType;
import com.miracle9.lottery.bean.LotteryResult;
import com.miracle9.lottery.bean.Result;
import com.miracle9.lottery.bean.SignResult;
import com.miracle9.lottery.bean.TimeValue;
import com.miracle9.lottery.entity.AuthorizeLog;
import com.miracle9.lottery.entity.LotteryLog;
import com.miracle9.lottery.service.AuthorizeLogService;
import com.miracle9.lottery.service.LotteryLogService;
import com.miracle9.lottery.utils.HttpUtil;
import com.miracle9.lottery.utils.LogManager;
import com.miracle9.lottery.utils.TextUtil;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/")
public class LotteryController {
	private Gson gson = new Gson();
	private String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
	private String ticketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
	@Autowired
	private LotteryLogService lotteryLogService;
	@Autowired
	private GameConfig gameConfig;
	@Autowired
	private AuthorizeLogService authorizeLogService;
	@Autowired
	private GameController gameController;

	/**
	 * 全局缓存
	 */
	public static Map<String, TimeValue> cache = new ConcurrentHashMap<>();

	// 抽奖
	@ResponseBody
	@RequestMapping("/getResult")
	public String getResult(String openId) {
		// openId授权验证
		// if (!AuthorizeLogService.openidCacheMap.containsKey(openId)) {
		// LotteryResult result = new LotteryResult(0, -1, new int[6], "未授权用户");
		// return gson.toJson(result);
		// }
		int awardType;
		int hour = TextUtil.getCurrentHour();
		if (hour < 9 || hour > 18) {
			awardType = AwardType.NOT.getValue();
		} else if (LotteryLogService.awardCacheMap.containsKey(openId)) {// 中过奖了
			awardType = AwardType.NOT.getValue();
		} else {
			awardType = gameController.draw();
		}

		if (awardType == AwardType.NOT.getValue()) {
			int[] balls = gameController.getBallsOfNum(0);

			LotteryResult result = new LotteryResult(1, awardType, balls, "");
			return gson.toJson(result);
		} else {
			int[] balls = gameController.getBallsOfNum(GameController.MAX_BALL - awardType);
			LotteryResult result = new LotteryResult(1, awardType, balls, "");

			// 保存到数据库
			lotteryLogService.add(new LotteryLog(openId, awardType));
			LotteryLogService.awardCacheMap.put(openId, true);
			return gson.toJson(result);
		}
	}

	// 上传联系方式
	@ResponseBody
	@RequestMapping("/uploadInfo")
	public String uploadInfo(String openId, String name, String phone, String cardId) {
		LotteryLog lottery = lotteryLogService.getByOpenId(openId);
		Result result = null;
		if (lottery == null) {
			result = new Result(0, "未找到中奖记录");
		} else {
			if (lottery.getPhone() == null) {
				lottery.setName(name);
				lottery.setPhone(phone);
				lottery.setCard(cardId);
				lotteryLogService.update(lottery);
			}
			result = new Result(1, "");
		}
		return gson.toJson(result);
	}

	// 获取签名
	@ResponseBody
	@RequestMapping("/getSign")
	public String getSign(String url) {
		TimeValue token = cache.get("token");
		// 为空或过期
		if (token == null || System.currentTimeMillis() - token.time >= 719000) {
			String param = "grant_type=client_credential&appid=" + gameConfig.getAppId() + "&secret="
					+ gameConfig.getAppSecret();
			String accessToken = HttpUtil.sendPost(tokenUrl, param);
			if (accessToken == "") {
				Result result = new Result(0, "获取access_token异常");
				return gson.toJson(result);
			}
			JSONObject jo = JSONObject.fromObject(accessToken);
			String tokenStr = jo.getString("access_token");
			if (tokenStr == null) {
				Result result = new Result(0, jo.getString("errmsg"));
				return gson.toJson(result);
			}
			cache.put("token", new TimeValue(System.currentTimeMillis(), tokenStr));

			token = cache.get("token");
		}

		// 获取ticket
		TimeValue ticket = cache.get("ticket");
		if (ticket == null || System.currentTimeMillis() - ticket.time >= 719000) {
			String param = "access_token=" + token.value + "&type=jsapi";
			String jsapiTicket = HttpUtil.sendPost(ticketUrl, param);
			if (jsapiTicket == "") {
				Result result = new Result(0, "获取jsapi_ticket异常");
				return gson.toJson(result);
			}
			JSONObject jo = JSONObject.fromObject(jsapiTicket);
			String ticketStr = jo.getString("ticket");
			if (ticketStr == null) {
				Result result = new Result(0, jo.getString("errmsg"));
				return gson.toJson(result);
			}
			cache.put("ticket", new TimeValue(System.currentTimeMillis(), ticketStr));

			ticket = cache.get("ticket");
		}
		LogManager.info("ticket=" + ticket.value);
		String nonceStr = TextUtil.getStr(16);
		long timestamp = System.currentTimeMillis() / 1000;
		String signStr = "jsapi_ticket=" + ticket.value + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url="
				+ url;

		String signature = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(signStr.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());
		} catch (Exception e) {
			LogManager.error(e);
		}
		SignResult sr = new SignResult(1, "", signature, timestamp, nonceStr, gameConfig.getAppId());
		sr.jsapiTicket = ticket.value;
		return gson.toJson(sr);
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	// 获取授权接口地址
	@ResponseBody
	@RequestMapping("/getAuthorizeUrl")
	public String getAuthorizeUrl() {
		try {
			// scope=snsapi_userinfo 弹出授权页面
			return "https://open.weixin.qq.com/connect/oauth2/authorize?" + "appid=" + gameConfig.getAppId()
					+ "&redirect_uri="
					+ URLEncoder.encode("http://h5.9shadow.com/lottery/shake/bin-release/index.html", "UTF-8")
					+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
		} catch (UnsupportedEncodingException e) {
			LogManager.error(e);
			return "";
		}
	}

	// 授权回调
	@ResponseBody
	@RequestMapping("/authorizeCallback")
	public String authorizeCallback(String code) {
		String getTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
		String param = "appid=" + gameConfig.getAppId() + "&" + "secret=" + gameConfig.getAppSecret() + "&code=" + code
				+ "&grant_type=authorization_code";
		String tokenJson = HttpUtil.sendPost(getTokenUrl, param);
		if (tokenJson != "") {
			JSONObject jo = JSONObject.fromObject(tokenJson);
			String openid = jo.getString("openid");
			if (openid != null && !AuthorizeLogService.openidCacheMap.containsKey(openid)) {
				authorizeLogService.add(new AuthorizeLog(jo.getString("openid"), new Date()));
				AuthorizeLogService.openidCacheMap.put(openid, true);
			}
		}
		return tokenJson;
	}

}
