package com.youzi.teaChain.ctrl;

import com.youzi.teaChain.bean.TUser;
import com.youzi.teaChain.service.ConfigService;
import com.youzi.teaChain.service.RechargeAllLogService;
import com.youzi.teaChain.service.RechargeProfitLogService;
import com.youzi.teaChain.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CommonController {
    private Logger log = LoggerFactory.getLogger(CommonController.class);

    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;
    @Resource
    private RechargeAllLogService rechargeAllLogService;
    @Resource
    private RechargeProfitLogService rechargeProfitLogService;

    /**
     * 更新上下级分润
     * 2019-1-5 11:46:27
     * @param uuid      下级
     * @param money     下级充值人民币
     * @param amount    下级充值钻石数量
     * @param type      充值种类 0.平台充值 1.守护神充值
     * @param remark    备注
     * @return
     */
    public Boolean updateProfit(String uuid, BigDecimal money, BigDecimal amount, int type, String remark, Map<String,Object> map) {
        Map<String,Object> param = new HashMap<>();
        param.put("uuid", uuid);
        param.put("amount", amount);
        param.put("money", money);
        param.put("type", type);
        param.put("remark", remark);
        int allLogId = rechargeAllLogService.createRechargeAllLog(param);
        TUser tUser = userService.getTUserByUuid(uuid);
        // 以下可以提取到service， 等待diamond抽出再提取
        if (allLogId != 0) {
            // 判断有下级的才可以进行分润
            if (tUser.getSuperior() != 0) {
                String superUuid = userService.getUuidById(tUser.getSuperior());
                // 更新上级CNY、钻石
                String diamondUpDownRadio = configService.selectConfig("diamondUpDownRadio");
                String cnyUpDownRadio = configService.selectConfig("cnyUpDownRadio");
                // mysql更新CNY、钻石
                BigDecimal addDiamond = amount.multiply(new BigDecimal(diamondUpDownRadio)).setScale(0, BigDecimal.ROUND_DOWN);
                BigDecimal addCNY = money.multiply(new BigDecimal(cnyUpDownRadio)).setScale(2, BigDecimal.ROUND_DOWN);
                Boolean aa = userService.updateDiamondFunc(superUuid, addDiamond, 0);
                Boolean bb = userService.updateCNYFunc(superUuid, addCNY, 0);
                if (!aa || !bb) {
                    log.error("更新上级钻石或CNY出错。: uuid-" + superUuid + " diamond-" + addDiamond + " CNY-" + addCNY);
                }
                // 添加分润记录
                Map<String,Object> param2 = new HashMap<>();
                param2.put("uuid", uuid);
                param2.put("superior", superUuid);
                param2.put("logId", allLogId);
                param2.put("amount", amount);        // 钻石数量
                param2.put("profitDiamond", addDiamond);
                param2.put("profitCNY", addCNY);
                rechargeProfitLogService.createRechargeProfitLog(param2);
            }
            return false;
        }else {
            map.put("code", 500);
            map.put("msg", "添加充值记录出错！");
            return true;
        }
    }


    public Boolean testRao(String uuid, BigDecimal money, BigDecimal amount, int allLogId) {
        TUser tUser = userService.getTUserByUuid(uuid);
        // 以下可以提取到service， 等待diamond抽出再提取
        String superUuid = userService.getUuidById(tUser.getSuperior());
        // 更新上级CNY、钻石
        String diamondUpDownRadio = configService.selectConfig("diamondUpDownRadio");
        String cnyUpDownRadio = configService.selectConfig("cnyUpDownRadio");
        // mysql更新CNY、钻石
        BigDecimal addDiamond = amount.multiply(new BigDecimal(diamondUpDownRadio)).setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal addCNY = money.multiply(new BigDecimal(cnyUpDownRadio)).setScale(2, BigDecimal.ROUND_DOWN);
        Boolean aa = userService.updateDiamondFunc(superUuid, addDiamond, 0);
        Boolean bb = userService.updateCNYFunc(superUuid, addCNY, 0);
        if (!aa || !bb) {
            log.error("更新上级钻石或CNY出错。: uuid-" + superUuid + " diamond-" + addDiamond + " CNY-" + addCNY);
        }// 添加分润记录
        Map<String,Object> param2 = new HashMap<>();
        param2.put("uuid", uuid);
        param2.put("superior", superUuid);
        param2.put("logId", allLogId);
        param2.put("amount", amount);        // 钻石数量
        param2.put("profitDiamond", addDiamond);
        param2.put("profitCNY", addCNY);
        rechargeProfitLogService.createRechargeProfitLog(param2);
        return true;
    }
}
