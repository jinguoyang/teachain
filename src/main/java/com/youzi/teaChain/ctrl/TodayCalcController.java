package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TTeachainCoinCalc;
import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.bean.Wallet;
import com.youzi.teaChain.service.CoinCalcService;
import com.youzi.teaChain.service.ConfigService;
import com.youzi.teaChain.service.UserService;
import com.youzi.teaChain.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Scope(value="prototype")	//非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/calc")
public class TodayCalcController {

    Logger logger = LoggerFactory.getLogger(TodayCalcController.class);

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;

    @Resource
    private CoinCalcService coinCalcService;

    @Resource
    private WalletService walletService;

    /**
     * 计算登录用户首页的没有发放的茶币
     * @throws ParseException
     */
//    @RequestMapping(value = "/calcPersonTeaCoin", method = RequestMethod.POST)
//    @ResponseBody
    public Map calcPersonTeaCoin(String uuid) throws ParseException {
        String minProvideCalcPower = configService.selectConfig("minProvideCalcPower");
        String minProvideTeaCoin = configService.selectConfig("minProvideTeaCoin");
        TUser tUser = userService.getTUserByUuid(uuid);
        Wallet wallet = walletService.getWalletByUuid(uuid);
        if (minProvideCalcPower != null && minProvideTeaCoin != null && tUser != null && wallet != null) {
            BigDecimal minProvideCalcPowerB = new BigDecimal(minProvideCalcPower);
            BigDecimal minProvideTeaCoinB = new BigDecimal(minProvideTeaCoin);
            BigDecimal calcPower = tUser.getCalcPower();
            BigDecimal balance = wallet.getBalance();
            if (calcPower.compareTo(minProvideCalcPowerB) >= 0 && balance.compareTo(minProvideTeaCoinB) >= 0) {
                return getTeaCoin(uuid);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        BigDecimal [] coinRes = new BigDecimal[7];
        coinRes[0] = BigDecimal.ZERO;
        coinRes[1] = BigDecimal.ZERO;
        coinRes[2] = BigDecimal.ZERO;
        coinRes[3] = BigDecimal.ZERO;
        coinRes[4] = BigDecimal.ZERO;
        coinRes[5] = BigDecimal.ZERO;
        coinRes[6] = BigDecimal.ZERO;
        map.put("teaCoin", coinRes);
        return map;
    }

    /**
     * 获取所有符合条件的生成茶币数据
     * @param uuid
     * @return
     * @throws ParseException
     */
    public Map getTeaCoin(String uuid) {
        Map<String, Object> map = new HashMap<>();

        TTeachainCoinCalc tTeachainCoinCalc = coinCalcService.selectCoinCalc(uuid);
        BigDecimal teaCoin1 = tTeachainCoinCalc.getTeaCoin1();
        BigDecimal teaCoin2 = tTeachainCoinCalc.getTeaCoin2();
        BigDecimal teaCoin3 = tTeachainCoinCalc.getTeaCoin3();
        BigDecimal teaCoin4 = tTeachainCoinCalc.getTeaCoin4();
        BigDecimal teaCoin5 = tTeachainCoinCalc.getTeaCoin5();
        BigDecimal teaCoin6 = tTeachainCoinCalc.getTeaCoin6();
        BigDecimal teaCoin7 = tTeachainCoinCalc.getTeaCoin7();
        BigDecimal teaCoin8 = tTeachainCoinCalc.getTeaCoin8();
        BigDecimal teaCoin9 = tTeachainCoinCalc.getTeaCoin9();
        BigDecimal teaCoin10 = tTeachainCoinCalc.getTeaCoin10();
        BigDecimal teaCoin11 = tTeachainCoinCalc.getTeaCoin11();
        BigDecimal teaCoin12 = tTeachainCoinCalc.getTeaCoin12();
        BigDecimal teaCoin13 = tTeachainCoinCalc.getTeaCoin13();
        BigDecimal teaCoin14 = tTeachainCoinCalc.getTeaCoin14();
        BigDecimal teaCoin15 = tTeachainCoinCalc.getTeaCoin15();
        BigDecimal teaCoin16 = tTeachainCoinCalc.getTeaCoin16();
        BigDecimal teaCoin17 = tTeachainCoinCalc.getTeaCoin17();
        BigDecimal teaCoin18 = tTeachainCoinCalc.getTeaCoin18();
        BigDecimal teaCoin19 = tTeachainCoinCalc.getTeaCoin19();
        BigDecimal teaCoin20 = tTeachainCoinCalc.getTeaCoin20();
        BigDecimal teaCoin21 = tTeachainCoinCalc.getTeaCoin21();

        Integer checkTime = configService.distributeTCCTime();
        BigDecimal [] coinRes = new BigDecimal[7];
        if (checkTime==3) {
            map.put("code", 200);

            coinRes[0] = teaCoin15;
            coinRes[1] = teaCoin16;
            coinRes[2] = teaCoin17;
            coinRes[3] = teaCoin18;
            coinRes[4] = teaCoin19;
            coinRes[5] = teaCoin20;
            coinRes[6] = teaCoin21;
            map.put("teaCoin", coinRes);
        } else if (checkTime==2) {
            map.put("code", 200);

            coinRes[0] = teaCoin8;
            coinRes[1] = teaCoin9;
            coinRes[2] = teaCoin10;
            coinRes[3] = teaCoin11;
            coinRes[4] = teaCoin12;
            coinRes[5] = teaCoin13;
            coinRes[6] = teaCoin14;
            map.put("teaCoin", coinRes);
        } else if (checkTime==1) {
            map.put("code", 200);

            coinRes[0] = teaCoin1;
            coinRes[1] = teaCoin2;
            coinRes[2] = teaCoin3;
            coinRes[3] = teaCoin4;
            coinRes[4] = teaCoin5;
            coinRes[5] = teaCoin6;
            coinRes[6] = teaCoin7;
            map.put("teaCoin", coinRes);
        }
        return map;
    }
}