package com.youzi.teaChain.common;

import com.youzi.teaChain.common.wxpay.sdk.WXPayConstants;
import com.youzi.teaChain.common.wxpay.sdk.WXPayXmlUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.youzi.teaChain.common.wxpay.sdk.WXPayUtil.HMACSHA256;
import static com.youzi.teaChain.common.wxpay.sdk.WXPayUtil.MD5;

public class StringUtil {

    // 默认头像blob
//    public static String icon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAAH7klEQVR4Xu2cB0/sOhCFs/TeBPz/v4dE752nL08HzXod9wSuRKQVXJId28dnqid39vX19dX9XUkIzP7ASsKpf%2BgPrHSs/sDKwGoasHxm8fPzs%2BPv%2Bsmk%2BV3XbDb7n/qz2fdnaWnp%2B3f3uZxFlz47uhoKKH7yeXt76z8CKeRfuOcDbXl5uVtfX%2B8Aj89U1yhguQC9v793r6%2Bv3cfHRw9YiwsQAWplZaVbXV3tAFAsbCHfJ2M0sAQQP8UisaTlYgBfoAEcjJP6thynqTcUY2DP8/Nzr2r6GyBZlWq5CFeuAINtrUGrZpYAgT0ABFA%2BQ90SoCFZdnPW1ta6jY2NBZtWw%2B4mYAHSy8tLh8r9pgv1RC0BznrW0jlWgQWDMNywSd6uZudKFxH7nmVZzfyKwcI2wSY%2Bv/1iIwFsc3OzV8tSwLLBUiAJm2DVWIZ7jA0gvAAwnECJ8S8C6/HxsQfqX7pkJgAKwPCWuQxLBkuDwah/QfVCGwnDtre3vz1lKmhJYFmgACtV%2BG9mnhimyD9lrslgoXZPT099DNUaLG2GJuyT33pMxrJGvxosG3De39/3uV3tpG1uKGfBRhCrId8CprwP%2B2IT5to5aAzGRx0BLUVmkFna8YeHh34xKQJjO4RMglfsnq1AhGSjKgDGxxeVx8YM3WcTAEweMvRsFCxslILOUrAsm5CFN1W0b1OU2KIZHxbYxZXOyY6FzK2trSgZBsGSiqB%2BNteLLch3H1moGDYPoGovANrZ2elZVluW0Wbt7u727AqB7wVLAsSqmsVJ7e7u7nq1a3WxKNhQG5VrrQClcGIIsEGwYFOtURc7b29vexvVQmWsccbeAJhUqEY%2Bc0UOiXc2WCwOtam5BHitnKDRnc06VEgqWTpfwIqxy8ssLRIjXLJbojYgoX5jXoyFl9zf3/8uLZeMhxzrGb2xnj1k1SKxLYQLpUmyDPrNzc1kNS4MfopHiwEJQ4dYOscsxVU2US5l1hSssguHFUdHRz27ai7koNa%2BU6MFNbSGvXRQZMCq1kY9NB82mkW2YJfCCHe8BbBIPWCWDSRzQUONr6%2Bvm6RHOWNjoA8PD6tjLzwiIYmrVQtqWOsFARkZMGvqi8UdHBwU1aqYqwiCKvtUcQEsWFWjPgyIBxwzXAhtAov0sSJl4wSW7JZr/xbAwguiiiWGXbtzdXXVNFpPWaieIQrnUzp/5PBdZOjsUbLnwGoRX7E75%2BfnVTYvBxz3WQw8YUQNWMhUNG/lL4BFalJa4FPocXFxUZ18lwKGCqKKtWAhh3grCFatYQboy8vLuUJe6cJLvteKWQAFYINgUUaBWaWXDORP2qxWYBE%2BIGt0sHASfKa%2BUL1WSTXMmgQsvCnskmeZCjRcPnFWrIgXmw8akgxWaQKtSVCtAKwWBxyxhdn7uHoi%2BBabFFVD5XS1YCGHwJRK61QXc97b2ysOSN15Rg08i6wJHTQgEwcoAKvJMXOAJtqm6tCqxzQpdFApOWei7rMqJ5NMj92zJS2g%2BBcqCeeuR%2BeJg96QgWvTHYSLTRh64jadDtUGir4FMxYgoYI17URzoKSkOwxMAtyqn0HydJw2BlgABFCpp8opDEMmKVM0ka4t0biTgVWwtcV5oSsb8AGqpfoxhko00XoWNkb195RdiD0j%2B4XMlmUbFsTutwRK5gPj7qY6fTjiHljwhRZG3oKoBBv1BrSS%2BEsLYbcJOonUSxrSQpsrZwFbfbX8BbAQpgJgi%2BDOBY2SMyGF3rawIFjn4FsUtoT4RyWYMWygGJt0YMEk8WI1R2Gx3VOXMzGdziZ9k5MKi5lE5%2BRrrWIp3zxRvyHVHuWQNQYW92EWKZE2BUCs67dvi%2Bl1E4LO0pJxzLZyX4esQ92Ag70OOuVJGaTkGcAiBlM7kwVH8uwrcyygdeDpzhsbGCpJD7YcMXkdto5hGwArJ39kDmOCpa6ckNMI9meNyS6Bldr5PFZMJXapKSTU7xXs/GtxOu16Q/1bSXtqP70FSzJaMF7hAqFIrHM52iapJpESu6TvKDzgJ4zCA%2BpVu5xOHb20xKJggrxiLWi%2BcoxvvdHWbuV3qepiAdKuwR413Lqv%2B%2BbWzgQMQAk0Flv6ignfIxxJaSiJgiU3r8g7xjCxCFAAyDbbtlQfdx62MdcybYh12iQdpqawMwksFo7aAFjoTFFBpJLxsWtZvo1TOTjlTdZQL1aRGioFAQjZL98u2ITZvs2asmMxtube17s5odZJQM3tdk5ils3ZhvribSkm1w7lghF7XtnAUPkmx07ZsZLBsi4fNVPkrb%2Br0%2B%2BngbIbCygqDGqeqGdpO3g2WEpq7YtPyvNk3GM7P%2BV9VA3AMAd6/TfF8xXbLN8X7fvR5HgtC3utwBTLj4%2BPezaJUaXys5nlBpoY/bOzsx85rk9ZNGp3cnISfXsiRVY1WOweIQLHXpRcStuVUiab%2BwxMOj09nWsdqvHOxWDZiduwgd4sHIBbAc1daMnzUjtsEj0PFAtj%2BV7OOE3AsqqpsjF1/KntGCpHydmGDDVMcoFsDpYYhWqS6lCzsq3i1mO2WogOWcnx8HhjvPUKcE3BcndCSTMMw2Pa13trgNIJD8DApJz8LkftRmWWK1w2xCbXBLOAp3eiVa4hVnNfAgUUbI6qC3r9V6mK24dVswEpII7KLN8EbG3LlmsU7M6lF%2Ba/sFMFs1U/Qwo4kzKrZEK/%2BTuTM%2Bs3gxGb2x9YMYTM/f8AEwgKieG2n1AAAAAASUVORK5CYII=";
    public static String icon = "http://thirdwx.qlogo.cn/mmopen/vi_32/1W8mHTFW9ibk7KKbx6QJ5wm5yBB02UmMgn88CKic25Zr5XqywByvQOB6wVhl1aYwbalLFpjAPI8v4RaCmS9pU0icw/132";

    /**
     * 非空非NULL判定
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 生成最大数到最小数之间的随机数
     *
     * @param max
     * @param min
     * @return
     */
    public static String getNumberRandom(int max, int min) {
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
//        System.out.println(s);
        return String.valueOf(s);
    }

    /**
     * 生成随机26字母之一(大小写需设置)
     *
     * @param temp 生成对应位数的随机字母
     * @return
     */
    public static String randomA(int temp) {
        String str = "";
        for (int i = 0; i < temp; i++) {
            str = str + (char) (Math.random() * 26 + 'A');
        }
        return str;
    }

    /**
     * 生成含单字母Uuid(非Uuid类)
     *
     * @param id 唯一标识ID
     * @return 生成唯一标识+随机A+时间戳(10001A1533542558)
     */
    public static String getOneAUuid(String id) {
        long startTime = new Date().getTime() / 1000;
        return id + randomA(1) + String.valueOf(startTime);
    }

    /**
     * 用Uuid生成十六位唯一订单号
     *
     * @return
     */
    public static String getOrderIdByUUId() {
        int machineId = 1;//最大支持1-9个集群机器部署
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
//         0 代表前面补充0
//         15 代表长度为15
//         d 代表参数为正数型
        return machineId + String.format("%015d", hashCodeV);
    }

    /**
     * @Description: 生成商品订单号 日期时间加四位随机数(18位)
     * @param
     * @return java.lang.String
     * @Date 2018-11-15 09:58:37
     */
    public static String getOrderIdByTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = sdf.format(new Date());
        String result = "";
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            result += random.nextInt(10);
        }
        return newDate + result;
    }

    /**
     * 生成本地图片
     * 2018-8-24 17:06:36
     *
     * @param path      本地保存路径
     * @param buildName 图片名称
     * @param file      base64file文件
     * @return 图片名
     */
    public static String createImg(String path, String buildName, MultipartFile file) {
        //创建文件
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = file.getOriginalFilename();

        String imgName = buildName + fileName.substring(fileName.lastIndexOf("."));
//        String img=user.getId()+fileName.substring(fileName.lastIndexOf("."));//zhao.jpg
        FileOutputStream imgOut = null;//根据 dir 抽象路径名和 img 路径名字符串创建一个新 File 实例。
        try {
            imgOut = new FileOutputStream(new File(dir, imgName));
            /* System.out.println(file.getBytes());*/
            imgOut.write(file.getBytes());//返回一个字节数组文件的内容
            imgOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgName;
    }

    /**
     * @Description: 生成格式为 0x.... 42位钱包地址
     * @param uuid 用户uuid
     * @return java.lang.String
     * @Date 2018-11-15 09:47:09
     */
    public static String createCoinAddress(String uuid) throws Exception {
        String data = uuid + System.currentTimeMillis();
        String uuid40 = SHA.encryptSHA(data);
        String uuid42 = "0x" + uuid40;
        return uuid42;
    }

    private static final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new SecureRandom();

    /**
     * 获取随机字符串 Nonce Str
     *
     * @return String 随机字符串
     */
    public static String generateNonceStr() {
        char[] nonceChars = new char[32];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> data) throws Exception {
        org.w3c.dom.Document document = WXPayXmlUtil.newDocument();
        org.w3c.dom.Element root = document.createElement("xml");
        document.appendChild(root);
        for (String key : data.keySet()) {
            String value = data.get(key);
            if (value == null) {
                value = "";
            }
            value = value.trim();
            org.w3c.dom.Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
        try {
            writer.close();
        } catch (Exception ex) {
        }
        return output;
    }

    /**
     * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
     *
     * @param data     待签名数据
     * @param key      API密钥
     * @param signType 签名方式
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key, WXPayConstants.SignType signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.FIELD_SIGN)) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
        }
        sb.append("key=").append(key);
        if (WXPayConstants.SignType.MD5.equals(signType)) {
            return MD5(sb.toString()).toUpperCase();
        } else if (WXPayConstants.SignType.HMACSHA256.equals(signType)) {
            return HMACSHA256(sb.toString(), key);
        } else {
            throw new Exception(String.format("Invalid sign_type: %s", signType));
        }
    }


    /**
     * @Description: 身份证号格式合法化验证
     * @param idCardNum 身份证号
     * @return boolean
     * @Date 2018-11-14 03:40:07
     */ 
    public static boolean isIdNum(String idCardNum) {
        // 中国公民身份证格式：长度为15或18位，最后一位可以为字母
        Pattern idNumPattern = Pattern.compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");
        // 格式验证
        if (!idNumPattern.matcher(idCardNum).matches())
            return false;
        // 合法性验证
        int year = 0;
        int month = 0;
        int day = 0;
        if (idCardNum.length() == 15) {
            // 一代身份证
//            System.out.println("一代身份证：" + idCardNum);

            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{2})(\\d{2})(\\d{2}).*");
            Matcher birthDateMather = birthDatePattern.matcher(idCardNum);
            if (birthDateMather.find()) {
                year = Integer.valueOf("19" + birthDateMather.group(1));
                month = Integer.valueOf(birthDateMather.group(2));
                day = Integer.valueOf(birthDateMather.group(3));
            }
        } else if (idCardNum.length() == 18) {
            // 二代身份证
//            System.out.println("二代身份证：" + idCardNum);
            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");
            Matcher birthDateMather = birthDatePattern.matcher(idCardNum);
            if (birthDateMather.find()) {
                year = Integer.valueOf(birthDateMather.group(1));
                month = Integer.valueOf(birthDateMather.group(2));
                day = Integer.valueOf(birthDateMather.group(3));
            }
        }
        // 年份判断，100年前至今
        Calendar cal = Calendar.getInstance();
        // 当前年份
        int currentYear = cal.get(Calendar.YEAR);
        if (year <= currentYear - 100 || year > currentYear)
            return false;
        // 月份判断
        if (month < 1 || month > 12)
            return false;
        // 日期判断
        // 计算月份天数
        int dayCount = 31;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                dayCount = 31;
                break;
            case 2:
                // 2月份判断是否为闰年
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    dayCount = 29;
                    break;
                } else {
                    dayCount = 28;
                    break;
                }
            case 4:
            case 6:
            case 9:
            case 11:
                dayCount = 30;
                break;
        }
//        System.out.println(String.format("生日：%d年%d月%d日", year, month, day));
//        System.out.println(month + "月份有：" + dayCount + "天");
        return day >= 1 && day <= dayCount;
    }

    public static BigInteger checkCellPhone(String phone) {
        String PHONE_NUMBER_REG = "((13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$)";
        if (phone == null || phone.length()<11) {
            return null;
        }
        String result = "";
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REG);
        Matcher matcher = pattern.matcher(phone);
        while (matcher.find()){
            result = matcher.group(1);
        }
        if (result.isEmpty()){
            return new BigInteger("0");
        }
        return BigInteger.valueOf(Long.valueOf(result));
    }

    public static Boolean checkPassword(String password){
        if (password.length()<6){
            return false;
        }
        String reg = "^(\\d+[A-Za-z]+[A-Za-z0-9]*)|([A-Za-z]+\\d+[A-Za-z0-9]*)$";
//        String reg = "^(?![0-9_-]+$)(?![a-zA-Z_-]+$)[0-9A-Za-z-_]{8,16}$";
        return password.matches(reg);
    }

    /**
     * 自动补齐固定位数为0
     * 例： 606,6 => 000606
     * @param index     基础数
     * @param z         对齐位
     * @return
     */
    public static String autoComplementZero(String index, int z) {
        int temp;
        try {
            temp = Integer.parseInt(index);
        } catch (NumberFormatException e) {
            // 非数字转换
            return index;
        }

        for (int i=1;i<z;i++) {
            int x = (int) Math.pow(10, i);
            if (temp < x) {
                StringBuffer sb = new StringBuffer();
                for (int j=0;j<z-i;j++) {
                    sb.append("0");
                }
                return sb.append(temp).toString();
            }
        }
        return index;
    }
}
