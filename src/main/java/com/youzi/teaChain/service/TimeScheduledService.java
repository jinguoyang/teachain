package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainCoinCalc;
import com.youzi.teaChain.common.StringUtil;
import com.youzi.teaChain.dao.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TimeScheduledService {

    private static final Logger log = LoggerFactory.getLogger(TimeScheduledService.class);

    private ScheduledExecutorService executor;

    @Resource
    ConfigService configService;

    @Resource
    UserService userService;

    @Resource
    WalletService walletService;

    @Resource
    WalletHistoryService walletHistoryService;

    @Resource
    private CoinCalcService coinCalcService;

    @Resource
    private ConfigMapper configMapper;

	public void start() {
		executor = Executors.newScheduledThreadPool(1);
		long now = (System.currentTimeMillis() / 1000) * 1000; //整秒修正
		long d = new Date().getTime() - now;
		if (d < 0) {
			d = d + 980;
		} else {
			d = 980 - d;
		}

        List calcPowerTimeList = configMapper.selectCalcPowerToday();
		executor.scheduleAtFixedRate(new ScheduleThread(calcPowerTimeList), d, 1000, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		executor.shutdown();
	}
	
	class ScheduleThread implements Runnable {
	    List calcPowerTimeList = null;
        public ScheduleThread(List aa) {
            calcPowerTimeList = aa;
        }

        @Override
		public void run() {
			try {
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                String date = df.format(new Date());
                Date dt = df.parse("00:01:00");
//                Date dt = df.parse("03:00:00");
//                System.out.println(date);
                String date2 = df.format(dt);
                if (date.equals(date2)) {
                    calcPowerTimeList = configMapper.selectCalcPowerToday();
//                    calcTeaCoinToday();       // 2018-12-26 注掉
                }

//                // 配置距离第一期开服时间后计算每日阳光分配
//                if (new Date().getTime() > 1548259200000L) {
//                    Map time1 = (Map) calcPowerTimeList.get(0);
//                    String hour1 = time1.get("keyValue").toString();
//                    Map time2 = (Map) calcPowerTimeList.get(1);
//                    String hour2 = time2.get("keyValue").toString();
//                    Map time3 = (Map) calcPowerTimeList.get(2);
//                    String hour3 = time3.get("keyValue").toString();
////                date = "09:00:00";
////                System.out.println(date);
//
//                    if (date.equals(StringUtil.autoComplementZero(hour1,2)+":00:00")) {
//                        walletService.todaySunExtendYunCoin(1);
//                    } else if (date.equals(StringUtil.autoComplementZero(hour2,2)+":00:00")) {
//                        walletService.todaySunExtendYunCoin(2);
//                    } else if (date.equals(StringUtil.autoComplementZero(hour3,2)+":00:00")) {
//                        walletService.todaySunExtendYunCoin(3);
//                    }
//                }

                Date dt3 = df.parse("23:59:59");
//                Date dt3 = df.parse("18:53:00");
//                System.out.println(date);
                String date3 = df.format(dt3);
                if (date.equals(date3)) {
                    walletHistoryService.updateHistory();
                    log.info("每日添加钱包历史完成" + new Date());
                }
			} catch (Exception e) {
				e.printStackTrace();
				log.error("循环任务每日计算算力出错" + e.getMessage());
			}
		}
	}

    /**
     * 每日按算力结算茶币
     */
    private void calcTeaCoinToday() {
        Map map = configService.selectTeaCoinToday();
        int teaCoin = Integer.parseInt(map.get("keyValue").toString());

        String minCalcPower = configService.selectConfig("minProvideCalcPower");
        String minTeaCoin = configService.selectConfig("minProvideTeaCoin");
        BigDecimal totalCalcPower = userService.selectTotalCalcPower(new BigDecimal(minCalcPower), new BigDecimal(minTeaCoin));

        List<Map> list = userService.selectCalcUser(new BigDecimal(minCalcPower), new BigDecimal(minTeaCoin));
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(new Date());

        for (int i=0;i<list.size();i++) {
            Map<String, Object> user = list.get(i);
            String uuid = user.get("uuid").toString();

            BigDecimal calcPower = new BigDecimal(user.get("calcPower").toString());
            BigDecimal applyCoin = calcPower.divide(totalCalcPower, 8, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(teaCoin));
            BigDecimal aveAmount = applyCoin.divide(new BigDecimal(21), 8, BigDecimal.ROUND_HALF_UP);
            BigDecimal amount1 = independentRandCalc(aveAmount);
            BigDecimal amount2 = independentRandCalc(aveAmount);
            BigDecimal amount3 = independentRandCalc(aveAmount);
            BigDecimal amount4 = independentRandCalc(aveAmount);
            BigDecimal amount5 = independentRandCalc(aveAmount);
            BigDecimal amount6 = independentRandCalc(aveAmount);
            BigDecimal amount7 = independentRandCalc(aveAmount);
            BigDecimal amount8 = independentRandCalc(aveAmount);
            BigDecimal amount9 = independentRandCalc(aveAmount);
            BigDecimal amount10 = independentRandCalc(aveAmount);
            BigDecimal amount11 = independentRandCalc(aveAmount);
            BigDecimal amount12 = independentRandCalc(aveAmount);
            BigDecimal amount13 = independentRandCalc(aveAmount);
            BigDecimal amount14 = independentRandCalc(aveAmount);
            BigDecimal amount15 = independentRandCalc(aveAmount);
            BigDecimal amount16 = independentRandCalc(aveAmount);
            BigDecimal amount17 = independentRandCalc(aveAmount);
            BigDecimal amount18 = independentRandCalc(aveAmount);
            BigDecimal amount19 = independentRandCalc(aveAmount);
            BigDecimal amount20 = independentRandCalc(aveAmount);
            BigDecimal amount21 =
                    applyCoin.subtract(amount1).subtract(amount2).subtract(amount3).subtract(amount4).subtract(amount5)
                    .subtract(amount6).subtract(amount7).subtract(amount8).subtract(amount9).subtract(amount10)
                    .subtract(amount11).subtract(amount12).subtract(amount13).subtract(amount14).subtract(amount15)
                    .subtract(amount16).subtract(amount17).subtract(amount18).subtract(amount19).subtract(amount20);

            TTeachainCoinCalc tTCC2 = new TTeachainCoinCalc();
            tTCC2.setUuid(uuid);
            tTCC2.setTeaCoin1(amount1);
            tTCC2.setTeaCoin2(amount2);
            tTCC2.setTeaCoin3(amount3);
            tTCC2.setTeaCoin4(amount4);
            tTCC2.setTeaCoin5(amount5);
            tTCC2.setTeaCoin6(amount6);
            tTCC2.setTeaCoin7(amount7);
            tTCC2.setTeaCoin8(amount8);
            tTCC2.setTeaCoin9(amount9);
            tTCC2.setTeaCoin10(amount10);
            tTCC2.setTeaCoin11(amount11);
            tTCC2.setTeaCoin12(amount12);
            tTCC2.setTeaCoin13(amount13);
            tTCC2.setTeaCoin14(amount14);
            tTCC2.setTeaCoin15(amount15);
            tTCC2.setTeaCoin16(amount16);
            tTCC2.setTeaCoin17(amount17);
            tTCC2.setTeaCoin18(amount18);
            tTCC2.setTeaCoin19(amount19);
            tTCC2.setTeaCoin20(amount20);
            tTCC2.setTeaCoin21(amount21);
            tTCC2.setTodayCoin(applyCoin);
            tTCC2.setEndTime(Integer.parseInt(date));
            coinCalcService.updateTCC(tTCC2);

//            改算法 每日结算不管这个钱了
//            if (applyCoin.compareTo(BigDecimal.ZERO) != 0) {
//                Boolean aa = walletService.updateBalanceByUser(uuid, applyCoin, 0);       // 0.价钱 1.减钱
//            }
        }
        log.info("每日按算力结算茶币完成" + new Date());
    }

    private BigDecimal independentRandCalc(BigDecimal aveAmount) {
        BigDecimal amount = BigDecimal.ZERO;
        if (new Random().nextBoolean()) {
            // 第二次 = 平均值 + 平均值 * 0.1 * （0~1）
            amount = aveAmount.add(aveAmount.multiply(new BigDecimal(0.001).multiply(new BigDecimal(new Random().nextInt(10)))).setScale(8, BigDecimal.ROUND_HALF_UP));
        } else {
            // 第二次 = 平均值 - 平均值 * 0.1 * （0~1）
            amount = aveAmount.subtract(aveAmount.multiply(new BigDecimal(0.001).multiply(new BigDecimal(new Random().nextInt(10)))).setScale(8, BigDecimal.ROUND_HALF_UP));
        }
        return amount;
    }
}

