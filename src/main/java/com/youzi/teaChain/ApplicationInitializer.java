package com.youzi.teaChain;

import com.alibaba.fastjson.JSON;
import com.youzi.teaChain.dao.ConfigMapper;
import com.youzi.teaChain.service.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@Component
public class ApplicationInitializer implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ConfigMapper c = event.getApplicationContext().getBean(ConfigMapper.class);
//		Configs conf = LinkCoinApp.conf = c.selectConfig();

        RedisClient redisClient = event.getApplicationContext().getBean(RedisClient.class);
		try(Jedis jedis = redisClient.getResource()) {
				boolean p = jedis.exists("globalDeal");
				if (!p) {
					long now = new Date().getTime();
					Map<String, String> hash = new HashMap<>();
					String[] a = {"100", "1400", "6300", "16813", "44799", "119365", "318043", "847412", "2257893", "6016057", "16029521", "42709958", "113798815", "303211964", "807895010", "2152600903", "5735510913", "15282017854", "40718267860", "108492042962", "289072301070", "770220496956", "2052218810777", "5468047220180", "14569372546974", "38819455623782", "103432740845088", "275592011964563", "734302856504867", "1956517829480996"};
					String[] b = {"25", "50", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400", "204800", "409600", "819200", "1638400", "3276800", "6553600", "13107200", "26214400", "52428800", "104857600", "209715200", "419430400", "838860800", "1677721600", "3355443200", "6710886400", "13421772800"};
					hash.put("sumUsedPoint", "0.0");
					hash.put("sumTicket", "0.0");
					hash.put("price", "1.0");
					hash.put("startTime", String.valueOf(now));
					hash.put("openHoleCost", "[180,540,1080,4860]");
					hash.put("speedUpByHole", "[0.05,0.2,0.4,1]");
					hash.put("vipSpeed", "[0,0.1,0,0]");
					hash.put("rewardType", "[1,1,1,2,2,2,3]");
					hash.put("rewardNumber", "[8888,28888,48888,888,1888,2888,10]");
					hash.put("genMoneyMap", JSON.toJSON(b).toString());
					hash.put("initPrice", JSON.toJSON(a).toString());
					hash.put("increasePara", "[1.15,1.3,1.3,1.4]");

					jedis.hmset("globalDeal", hash);
				}
			}catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
		}
		
	}

}
