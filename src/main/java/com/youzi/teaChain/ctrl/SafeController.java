package com.youzi.teaChain.ctrl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.youzi.teaChain.bean.*;
import com.youzi.teaChain.common.ErrorCode;
import com.youzi.teaChain.common.SHA;
import com.youzi.teaChain.common.StringUtil;
import com.youzi.teaChain.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youzi.teaChain.common.StringUtil.checkCellPhone;

@Controller
@Scope(value = "singleton")    //非scope=”singleton”单例模式注解，每次请求单独的Action，线程安全
@RequestMapping("/safe")
public class SafeController {
    private Logger log = LoggerFactory.getLogger(SafeController.class);

    @Resource
    private UserService userService;
    @Resource
    private WalletService walletService;
    @Resource
    private LoginService loginService;
    @Resource
    private CodeLogService codeLogService;
    @Resource
    private ConfigService configService;
    @Resource
    private TransLogService transLogService;
    @Resource
    private CommonController commonController;
    @Resource
    private PatronSaintService patronSaintService;

    @Value("${yp.apiKey}")
    private String apiKey;

    /**
     * @Description: 绑定身份证
     * @param uuid 用户uuid
     * @param idCardNum 身份证号
     * @param idCardName 身份证姓名
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-12 17:00:45
     */
    @RequestMapping(value = "/idCardAuthentic", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> idCardAuthentic(String uuid, String idCardNum, String idCardName) {
        Map<String, Object> map = new HashMap<>();
        String idCard = userService.getTUserByUuid(uuid).getIdCardNum();
        if (idCard == null) {
            if (StringUtil.isIdNum(idCardNum)) {
                Boolean hasIdCard = userService.checkIdCardNum(idCardNum);
                if (!hasIdCard) {
                    Boolean setIdCardSuccess = userService.setIdCard(uuid, idCardNum, idCardName);
                    if (setIdCardSuccess) {
                        map.put("code", 200);
                    } else {
                        map.put("code", 500);
                        map.put("msg", "身份验证失败！");
                    }
                } else {
                    map.put("code", 500);
                    map.put("msg", "该身份证已被注册！");
                }
            } else {
                map.put("code", 500);
                map.put("msg", "身份验证错误，请输入正确身份信息！");
            }
        }else{
            map.put("code", 500);
            map.put("msg", "已验证过身份,不可重复验证！");
        }
        return map;
    }

    /**
     * @Description: 绑定微信号(提现用)
     * @param uuid 用户uuid
     * @param wechatNum 微信号
     * @param payPassword 支付密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-12 17:05:32
     */
    @RequestMapping(value = "/WXAuthentic", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> WXAuthentic(String uuid, String wechatNum, String payPassword) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Wallet wallet = walletService.getWalletByUuid(uuid);
        if (SHA.encryptSHA(payPassword).equals(wallet.getPayPassword())) {
            Boolean setWechatNumSuccess = userService.setWechatNum(uuid, wechatNum);
            if (setWechatNumSuccess) {
                map.put("code", 200);
            } else {
                map.put("code", 500);
                map.put("msg", "微信绑定失败！");
            }
        } else {
            map.put("code", 500);
            map.put("msg", "支付密码错误！");
        }
        return map;
    }

    /**
     * @Description: 找回登录密码(重置密码)
     * @param phone 接收短信手机号(账号绑定手机号)
     * @param code 验证码
     * @param password 新密码
     * @return java.util.Map
     * @Date 2018-11-13 10:01:08
     */
    @RequestMapping(value = "/resetLoginPassword", method = RequestMethod.POST)
    @ResponseBody
    public Map resetLoginPassword(String phone, int code ,String password) throws Exception {
        Map<String, Object> map = new HashMap<>();
        BigInteger phoneInt = checkCellPhone(phone);
        String phoneStr;
        if (phoneInt != null){
            phoneStr = phoneInt.toString();
        }else {
            phoneStr = phone;
        }
        int resCode = codeLogService.selectPhoneCode("-1", phoneInt);
        if (resCode == 0) {
            map.put("code", 500);
            map.put("msg", "验证码已失效，请重新获取验证码！");
            return map;
        }
        if (code == resCode) {
            TTeachainLogin tTeachainLogin = loginService.getLoginByPhone(phoneStr);
            if(tTeachainLogin!=null){
                loginService.setPasswordByUuid(SHA.encryptSHA(password),tTeachainLogin.getUuid());
                map.put("code", 200);
            }else {
                map.put("code", 500);
                map.put("msg", "手机号未绑定");
            }
        } else {
            map.put("code", 500);
            map.put("msg", "验证码输入有误，请仔细确认后输入！");
        }
        return map;
    }

    /**
     * @Description: 更改登录密码
     * @param uuid 用户uuid
     * @param oldPassword 原登录密码
     * @param newPassword 新登录密码
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-12 17:09:17
     */
    @RequestMapping(value = "/changeLoginPassword", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> changeLoginPassword(String uuid, String oldPassword, String newPassword) throws Exception {
        Map<String, Object> map = new HashMap<>();
        TTeachainLogin tTeachainLogin = loginService.getLoginInfoByUuid(uuid);
        if (tTeachainLogin != null) {
            if (!oldPassword.equals(newPassword)) {
                TUser tUser = userService.getTUserByUuid(uuid);
                int hasLoginPassword = tUser.getHasLoginPassword();
                if ((hasLoginPassword == 0 && oldPassword.equals("")) || tTeachainLogin.getPassword().equals(SHA.encryptSHA(oldPassword))) {
                    Boolean changePasswordSuccess = loginService.setPasswordByUuid(SHA.encryptSHA(newPassword), uuid);
                    if (changePasswordSuccess) {
                        map.put("code", 200);
                    } else {
                        map.put("code", 500);
                        map.put("msg", "更改密码失败！");
                    }
                } else {
                    map.put("code", 500);
                    map.put("msg", "原密码错误！");
                }
            }else {
                map.put("code", 500);
                map.put("msg", "新密码不可与原密码相同！");
            }
        } else {
            map.put("code", 500);
            map.put("msg", "请检查uuid是否正确");
        }
        return map;
    }

    /**
     * @Description: 更改支付密码
     * @param uuid 用户uuid
     * @param oldPassword 原支付密码
     * @param newPassword 新支付密码
     * @param phone 绑定手机号
     * @param code 验证码
     * @param flag 1.密码验证 2.短信验证
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018-11-12 17:11:49
     */
    @RequestMapping(value = "/changePayPassword", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> changePayPassword(String uuid, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,
                                                 @RequestParam("phone") String phone,@RequestParam("code") String code, int flag) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if(flag == 1){
            Wallet wallet = walletService.getWalletByUuid(uuid);
            if (!oldPassword.equals(newPassword)) {
                if (wallet != null) {
                    TUser tUser = userService.getTUserByUuid(uuid);
                    int hasPayPassword = tUser.getHasPayPassword();
                    if ((hasPayPassword == 0 && oldPassword.equals("")) || wallet.getPayPassword().equals(SHA.encryptSHA(oldPassword))) {
                        Boolean changePasswordSuccess = walletService.setPasswordByUuid(SHA.encryptSHA(newPassword), uuid);
                        if (changePasswordSuccess) {
                            map.put("code", 200);
                        } else {
                            map.put("code", 500);
                            map.put("msg", "更改密码失败！");
                        }
                    } else {
                        map.put("code", 500);
                        map.put("msg", "原密码错误！");
                    }
                } else {
                    map.put("code", 500);
                    map.put("msg", "请检查uuid是否正确！");
                }
            }else {
                map.put("code", 500);
                map.put("msg", "新密码不可与原密码相同！");
            }
        }else{
            BigInteger phoneInt = checkCellPhone(phone);
            int resCode = codeLogService.selectPhoneCode(uuid, phoneInt);
            if (resCode == 0) {
                map.put("code", 500);
                map.put("msg", "验证码已失效，请重新获取验证码！");
                return map;
            }
            if (Integer.parseInt(code) == resCode) {
//                newPassword = "";//清空
                walletService.setPasswordByUuid(SHA.encryptSHA(newPassword),uuid);//直接更换
                map.put("code", 200);
            } else {
                map.put("code", 500);
                map.put("msg", "验证码输入有误，请仔细确认后输入！");
            }
        }
        return map;
    }

    /**
     * @Description: 发送短信验证码
     * @param uuid 用户uuid
     * @param phone 接收短信手机号
     * @param flag 1.绑定手机号 2.重置支付密码 3.重置登录密码 4.手机验证码登录
     * @return java.util.Map
     * @Date 2018-11-12 17:12:08
     */
    @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
    @ResponseBody
    public Map sendCode(String uuid, String phone, int flag){
        Map<String, Object> map = new HashMap<>();
        BigInteger phoneInt = checkCellPhone(phone);
        if(phoneInt==null){
            map.put("code",500);
            map.put("msg","手机号不合法！");
            return map;
        }else if(phoneInt.compareTo(new BigInteger("0"))==0){
            map.put("code",500);
            map.put("msg","暂不支持该号段绑定，请联系客服！");
            return map;
        }
        String phoneStr = phoneInt.toString();
        if (flag == 1){
            // 檢查是否多次調用接口
            Boolean aa = codeLogService.checkApiCallCounts(uuid);
            if (aa) {
                map.put("code", 500);
                map.put("msg", "您的请求次数过多，请稍后重试！");
                return map;
            }
            Boolean hasPhone = loginService.checkPhone(phoneStr);
            if(hasPhone){
                map.put("code", 500);
                map.put("msg", "手机号已被绑定");
                return map;
            }
        }else if(flag == 2){
            Boolean hasPhone = loginService.checkPhone(phoneStr);
            if(!hasPhone){
                map.put("code", 500);
                map.put("msg", "手机号未绑定");
                return map;
            }
        }else if (flag == 3){
            Boolean hasPhone = loginService.checkPhone(phoneStr);
            if(!hasPhone){
                map.put("code", 500);
                map.put("msg", "手机号未绑定");
                return map;
            }
            uuid = "";
        }else if (flag == 4){
            Boolean hasPhone = loginService.checkPhone(phoneStr);
            if(!hasPhone){
                map.put("code",ErrorCode.NOTFOUNDPLAYER);
                map.put("msg", "手机号未绑定");
                return map;
            }
            uuid = "";
        }else {
            map.put("code",500);
            map.put("msg", "flag错误");
            return map;
        }

        String code = StringUtil.getNumberRandom(9999, 1000);
        Boolean bb = aLiSendCode(uuid, phoneInt, code);

        if (bb) {
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("msg", "验证码发送失败，请稍后重试");
        }
        return map;
    }

    /**
     * @Description: 短信验证码确认 绑定手机号
     * @param uuid 用户uuid
     * @param phone 电话号
     * @param code 验证码
     * @return java.util.Map
     * @Date 2018-11-12 17:14:21
     */
    @RequestMapping(value = "/sureCode", method = RequestMethod.POST)
    @ResponseBody
    public Map getCode(String uuid, String phone, int code) {
        Map<String, Object> map = new HashMap<>();
        BigInteger phoneInt = checkCellPhone(phone);
        int resCode = codeLogService.selectPhoneCode(uuid, phoneInt);
        if (resCode == 0) {
            map.put("code", 500);
            map.put("msg", "验证码已失效，请重新获取验证码！");
            return map;
        }
        if (code == resCode) {
            userService.updateUserPhoneByUuid(uuid, phoneInt);
            loginService.updateLoginPhoneByUuid(uuid, phoneInt);
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("msg", "验证码输入有误，请仔细确认后输入！");
        }
        return map;
    }

    public Boolean aLiSendCode(String uuid, BigInteger phone, String code) {
        //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
        //替换成你的AK
        final String accessKeyId = "VfnCSnrbLbitAQ5u";//你的accessKeyId,参考本文档步骤2
        final String accessKeySecret = "V6LJASbnr0Mk4UDyxV6IuRCmpBG7p9";//你的accessKeySecret，参考本文档步骤2
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        } catch (ClientException e) {
            e.printStackTrace();
            return false;
        }
        IAcsClient acsClient = new DefaultAcsClient(profile);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
        request.setPhoneNumbers(phone.toString());
        //必填:短信签名-可在短信控制台中找到
        request.setSignName("启鼎互娱");
        //必填:短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
        request.setTemplateCode("SMS_151765891");
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        request.setTemplateParam("{\"code\":\""+ code +"\"}");
        //可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");
        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");
        //请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            log.error("ali短信服务请求失败：" + e.getMessage());
            return false;
        }

        TTeachainCodeLog ttcl = new TTeachainCodeLog();
        ttcl.setUuid(uuid);
        ttcl.setPhone(phone);
        ttcl.setCode(code);
        ttcl.setRes(sendSmsResponse.getCode());
        Date date = new Date(new Date().getTime() + 300000);
        ttcl.setInvaildTime(date);
        if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
            //请求成功
            ttcl.setStatus(0);
            Boolean bb = codeLogService.insertCodeLog(ttcl);
            return true;
        } else {
            ttcl.setStatus(1);
            Boolean bb = codeLogService.insertCodeLog(ttcl);
            return false;
        }
    }

    /**
     * @Description:   云币转账
     * @param uuid      用户标识
     * @param address   目标地址
     * @param phone     目标电话
     * @param amount    转账数量
     * @param transPwd  交易密码
     * @return java.util.Map
     * @Date 2018-12-10 16:36:05
     */
    @RequestMapping(value = "/transTeaCoin", method = RequestMethod.POST)
    @ResponseBody
    public Map initLoadCheck(String uuid, String address, String phone, BigDecimal amount, String transPwd) throws Exception {

        synchronized (this) {
            Map<String, Object> map = new HashMap<>();

//            if (true) {
//                map.put("code", 500);
//                map.put("msg", "云庄园转账功能维护，预计开启事件2019年1月31日18点！");
//                return map;
//            }

            String aimUuid = walletService.selectUserByAddressPhone(address, phone);
            if (StringUtils.isEmpty(aimUuid)) {
                map.put("code", 500);
                map.put("msg", "信息确认失败，请核实转账的地址跟电话");
                return map;
            }

            Wallet wallet = walletService.getWalletByUuid(uuid);
            if(!SHA.encryptSHA(transPwd).equals(wallet.getPayPassword())){
                map.put("code", 500);
                map.put("msg", "交易密码错误！");
                return map;
            }

            Integer transLock= wallet.getTransLock();
            if (transLock==2){
                map.put("code", 500);
                map.put("msg", "交易失败，请联系客服进行人工审核！");
                return map;
            }

            TTeachainLogin ttl = loginService.getLoginInfoByUuid(uuid);
            if (ttl.getStatus() == 2) {
                map.put("code", 500);
                map.put("msg", "安全模式，您的账号存在被盗风险！\n" +
                        "进入官方QQ群869800795解除安全模式。");
                return map;
            }

            // 校验茶币
            String brokerageSel = configService.selectConfig("brokerageSell2");
            String transMinCounts = configService.selectConfig("transMinCounts");
            String transMinRate = configService.selectConfig("transMinRate");
            if (amount.compareTo(new BigDecimal(transMinCounts)) < 0) {
                map.put("code", 500);
                map.put("msg", "调用人家接口不太好吧！");
                return map;
            }

            Map patronSaintUser =patronSaintService.getPatronSaintUserByUuid(uuid);
            try{
                Integer maxLevel = (Integer) patronSaintUser.get("maxLevel");
                if (maxLevel<=5){
                    if (transLock == 1){
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String date = df.format(new Date());
                        int warnCount = transLogService.selectWarningTrans(uuid,aimUuid,date);
                        if(warnCount > 3){  //一天最多四笔高危转账
                            loginService.setStatusByUuid(uuid,2);
                            loginService.setStatusByUuid(aimUuid,2);
                            map.put("code", 500);
                            map.put("msg", "安全模式，您的账号存在被盗风险！\n" +
                                    "进入官方QQ群869800795解除安全模式。");
                            return map;
                        }
                    }else {
                        // 拒绝
                        walletService.setTransLock(uuid, 2);
                        map.put("code", 500);
                        map.put("msg", "交易失败，请联系客服进行人工审核！");
                        return map;
                    }
                }else if(transLock==0){
                    // 允许
                    walletService.setTransLock(uuid,1);
                }
            }catch (Exception e){
                map.put("code", 500);
                map.put("msg", "用户信息出错！");
                return map;
            }

            BigDecimal brokerageSelB = amount.multiply(new BigDecimal(brokerageSel));
            if (brokerageSelB.compareTo(new BigDecimal(transMinRate)) < 0) {
                brokerageSelB = new BigDecimal(transMinRate);
            }
            BigDecimal realCoin = amount.subtract(brokerageSelB);
            BigDecimal balance = wallet.getBalance();
            if (balance.compareTo(realCoin) < 0) {
                map.put("code", 500);
                map.put("msg", "您的余额不足，不足以转账");
                return map;
            }

            // 更新钱包
            Wallet aimWallet = walletService.getWalletByUuid(aimUuid);
            Boolean c1 = walletService.updateWalletBalanceFunc(uuid, amount, 1);
            Boolean c2 = walletService.updateWalletBalanceFunc(aimUuid, realCoin, 0);
            if (c1 && c2) {
                TTeachainTransLog ttrl = new TTeachainTransLog();
                ttrl.setUuid(uuid);
                ttrl.setAimUuid(aimUuid);
                ttrl.setAmount(amount);
                ttrl.setRate(brokerageSelB);
                ttrl.setRealCoin(realCoin);
                Integer maxLevel = (Integer) patronSaintUser.get("maxLevel");
                if (maxLevel<=5) {
                    ttrl.setWarnTag(1);
                }
                transLogService.insertTransLog(ttrl);

                map.put("code", 200);
                map.put("totalTCC", wallet.getBalance().subtract(realCoin));
                return map;
            } else {
                // 回滚
                walletService.updateRollBack(wallet);
                walletService.updateRollBack(aimWallet);
                map.put("code", 500);
                map.put("msg", "更新钱包余额出错，请稍后重试");
                return map;
            }
        }
    }

    /**
     * @Description:   云币转账记录
     * @param uuid      用户标识
     * @return java.util.Map
     * @Date 2018-12-11 12:04:41
     */
    @RequestMapping(value = "/transRecord", method = RequestMethod.POST)
    @ResponseBody
    public Map transRecord(String uuid) {
        Map<String, Object> map = new HashMap<>();
        List list =  transLogService.selectTransLogByUuid(uuid);
        if (list != null) {
            map.put("code", 200);
            map.put("transList", list);
        } else {
            map.put("code", 500);
            map.put("msg", "查询数据失败，请稍后重试");
        }
        return map;
    }

    /**
     * CNY充值钻石
     * @param uuid
     * @param money
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rechargeDiamond", method = RequestMethod.POST)
    @ResponseBody
    public Map orderQuery(String uuid, BigDecimal money) {
        synchronized (this) {
            Map<String, Object> map = new HashMap<>();
            try {
                TUser user = userService.getTUserByUuid(uuid);
                BigDecimal totalMoney = user.getTotalMoney();
                if (totalMoney.compareTo(money) < 0) {
                    map.put("code", 500);
                    map.put("msg", "充值钻石失败，您的账户CNY余额不足，请及时充值");
                }
//                userService.changeTotalMoney(uuid, money, 1);        // 0.增加 1.减少
                Boolean aa = userService.updateCNYFunc(uuid, money, 1);
                if (!aa) {
                    map.put("code", 500);
                    map.put("msg", "更新CNY出错！");
                    return map;
                }
                Boolean bb = userService.updateDiamondFunc(uuid, money.multiply(BigDecimal.TEN), 0);
                if (!bb) {
                    map.put("code", 500);
                    map.put("msg", "更新钻石出错！");
                    userService.updateCNYFunc(uuid, money, 2);
                    return map;
                }
                if (commonController.updateProfit(uuid,money,money.multiply(BigDecimal.TEN),0,"CNY充值钻石",map))
                    return map;
                map.put("code", 200);
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                map.put("code", 500);
                map.put("msg", "添加充值记录出错！");
                return map;
            }
        }
    }
}
