package com.miracle9.lottery.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
import com.miracle9.lottery.entity.Chat;
import com.miracle9.lottery.entity.LotteryLog;
import com.miracle9.lottery.service.AuthorizeLogService;
import com.miracle9.lottery.service.ChatService;
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
	@Autowired
	private ChatService chatService;

	private String token = "UFWSF6XdPSlTMl99U4SW7pcDNSBdzIiB";

	/**
	 * 全局缓存
	 */
	public static Map<String, TimeValue> cache = new ConcurrentHashMap<>();

	// 抽奖
	@ResponseBody
	@RequestMapping(value = "/getResult", produces = "text/html;charset=UTF-8")
	public String getResult(String openId) {
		// openId授权验证
		if (openId == null || !AuthorizeLogService.openidCacheMap.containsKey(openId)) {
			LotteryResult result = new LotteryResult(1, 0, "未授权用户");
			return gson.toJson(result);
		}
		AuthorizeLog log = AuthorizeLogService.openidCacheMap.get(openId);
		if (log.getRefreshDate() == null || !TextUtil.isSameDay(log.getRefreshDate(), new Date())) {
			log.setRefreshDate(new Date());
			log.setAwardNum(3);
		}
		if (log.getAwardNum() <= 0) {
			LotteryResult result = new LotteryResult(1, 3, "今日抽奖次数已用完");
			return gson.toJson(result);
		}
		log.setAwardNum(log.getAwardNum() - 1);
		authorizeLogService.update(log);
		int awardType = 0;
		if (LotteryLogService.awardCacheMap.containsKey(openId) || !isCanDraw()) {
			awardType = AwardType.NOT.getValue();
		} else {
			awardType = gameController.draw();
		}

		if (awardType == AwardType.NOT.getValue()) {
			// int[] balls = gameController.getBallsOfNum(0);

			LotteryResult result = new LotteryResult(1, awardType, "");
			return gson.toJson(result);
		} else {
			LotteryResult result = new LotteryResult(1, awardType, "");

			// 保存到数据库
			LotteryLog lottery = lotteryLogService.getByOpenId(openId);
			if (lottery == null) {
				lotteryLogService.add(new LotteryLog(openId, awardType));
			} else {// 更新中奖类型
				int oldAwardType = lottery.getAwardType();
				if (oldAwardType != awardType) {
					lottery.setAwardType(awardType);
					lottery.setAwardDate(new Date());
					lotteryLogService.update(lottery);
				}
				// 上次中奖还回去
				gameController.repayAward(oldAwardType);
			}
			return gson.toJson(result);
		}
	}

	private boolean isCanDraw() {
		return TextUtil.isBetween(gameConfig.begin, gameConfig.end);
	}

	// 上传联系方式
	@ResponseBody
	@RequestMapping(value = "/uploadInfo", produces = "text/html;charset=UTF-8")
	public String uploadInfo(String openId, String name, String phone, String cardId) {
		LotteryLog lottery = lotteryLogService.getByOpenId(openId);
		Result result = null;
		if (lottery == null) {
			result = new Result(0, "未找到中奖记录");
		} else if (StringUtils.isBlank(phone)) {
			result = new Result(0, "请填写联系方式");
		} else if (lotteryLogService.getByPhone(phone) != null) {
			result = new Result(0, "手机号码已存在，请填写其他真实手机号码");
		} else {
			if (lottery.getPhone() == null) {
				lottery.setName(name);
				lottery.setPhone(phone);
				lottery.setCard(cardId);
				lotteryLogService.update(lottery);
			}
			result = new Result(1, "");
			LotteryLogService.awardCacheMap.put(openId, true);
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
	public String getAuthorizeUrl(String callback) {
		try {
			// snsapi_base
			// scope=snsapi_userinfo 弹出授权页面
			return "https://open.weixin.qq.com/connect/oauth2/authorize?" + "appid=" + gameConfig.getAppId()
					+ "&redirect_uri=" + URLEncoder.encode(callback, "UTF-8")
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
				AuthorizeLog log = new AuthorizeLog(jo.getString("openid"), new Date(), "", 3);
				authorizeLogService.add(log);
				AuthorizeLogService.openidCacheMap.put(openid, log);
				// 拉取用户信息
				// String access_token = jo.getString("access_token");
				// String getUserInfoUrl =
				// "https://api.weixin.qq.com/sns/userinfo";
				// param = "access_token=" + access_token + "&openid=" + openid;
				//
				// String userInfo = HttpUtil.sendPost(getUserInfoUrl, param);
				// if (userInfo != "") {
				// jo = JSONObject.fromObject(userInfo);
				// String nickname = jo.getString("nickname");
				// if (nickname != null) {
				// authorizeLogService.add(new
				// AuthorizeLog(jo.getString("openid"), new Date(),
				// nickname,3));
				// AuthorizeLogService.openidCacheMap.put(openid, nickname);
				// }
				// }
			}
		}
		return tokenJson;
	}

	// 微信留言
	@ResponseBody
	@RequestMapping("/chat")
	public String chat(HttpServletRequest request, HttpServletResponse response) {
		LogManager.info("进入Chat");
		boolean isGet = request.getMethod().toLowerCase().equals("get");
		if (isGet) {
			return access(request, response);
		} else {
			// 进入POST聊天处理
			try {
				// 接收消息并返回消息
				acceptMessage(request, response);
			} catch (IOException e) {
				LogManager.error(e);
			}
		}
		return "";
	}

	/**
	 * 验证URL真实性
	 * 
	 * @author morning
	 * @date 2015年2月17日 上午10:53:07
	 * @param request
	 * @param response
	 * @return String
	 */
	private String access(HttpServletRequest request, HttpServletResponse response) {
		// 验证URL真实性
		String signature = request.getParameter("signature");// 微信加密签名
		String timestamp = request.getParameter("timestamp");// 时间戳
		String nonce = request.getParameter("nonce");// 随机数
		String echostr = request.getParameter("echostr");// 随机字符串
		List<String> params = new ArrayList<String>();
		params.add(token);
		params.add(timestamp);
		params.add(nonce);
		// 1. 将token、timestamp、nonce三个参数进行字典序排序
		Collections.sort(params, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		// 2. 将三个参数字符串拼接成一个字符串进行sha1加密
		String temp = null;
		try {
			temp = params.get(0) + params.get(1) + params.get(2);
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(temp.getBytes("UTF-8"));
			temp = byteToHex(crypt.digest());
		} catch (Exception e) {
			LogManager.error(e);
		}
		if (temp.equals(signature)) {
			// try {
			// response.getWriter().write(echostr);
			LogManager.info("成功返回 echostr：" + echostr);
			return echostr;
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
		LogManager.info("失败 认证");
		return null;
	}

	private void acceptMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 处理接收消息
		ServletInputStream in = request.getInputStream();
		// 将流转换为字符串
		StringBuilder xmlMsg = new StringBuilder();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			xmlMsg.append(new String(b, 0, n, "UTF-8"));
		}
		LogManager.info(xmlMsg.toString());
		Map<String, String> result = null;
		try {
			result = parseXml(xmlMsg.toString());
		} catch (Exception e) {
			LogManager.error(e);
		}
		// 根据消息类型获取对应的消息内容
		if (result.get("MsgType").equals("text")) {
			// 文本消息
			String toUserName = result.get("ToUserName");
			String openId = result.get("FromUserName");
			String content = result.get("Content");
			if (LotteryLogService.awardCacheMap.containsKey(openId)) {
				chatService.add(new Chat(openId, content, new Date()));
			}
			// StringBuffer str = new StringBuffer();
			// str.append("<xml>");
			// str.append("<ToUserName><![CDATA[" + custermname +
			// "]]></ToUserName>");
			// str.append("<FromUserName><![CDATA[" + servername +
			// "]]></FromUserName>");
			// str.append("<CreateTime>" + returnTime + "</CreateTime>");
			// str.append("<MsgType><![CDATA[" + msgType + "]]></MsgType>");
			// str.append("<Content><![CDATA[你说的是：" + inputMsg.getContent() +
			// "，吗？]]></Content>");
			// str.append("</xml>");
			// System.out.println(str.toString());
			// response.getWriter().write(str.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> parseXml(String msg) throws Exception {

		// 将解析结果存储在HashMap中

		Map<String, String> map = new HashMap<String, String>();

		// 从request中取得输入流

		InputStream inputStream = new ByteArrayInputStream(msg.getBytes("UTF-8"));

		// 读取输入流

		SAXReader reader = new SAXReader();

		Document document = reader.read(inputStream);

		// 得到xml根元素

		Element root = document.getRootElement();

		// 得到根元素的所有子节点

		List<Element> elementList = root.elements();

		// 遍历所有子节点

		for (Element e : elementList)

			map.put(e.getName(), e.getText());

		// 释放资源

		inputStream.close();

		inputStream = null;

		return map;

	}
}
