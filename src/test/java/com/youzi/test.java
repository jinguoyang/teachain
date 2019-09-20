package com.youzi;

import com.youzi.teaChain.common.SHA;

public class test {

    public static void main(String[] args) throws Exception {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = simpleDateFormat.parse("2018-12-26 00:00:00");
//        long ts = date.getTime();
//        long current = System.currentTimeMillis();
//        long create = 1545382157050L;
//        long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
//        String a = simpleDateFormat.format(create);
////        int days = (int) ((zero - ) / (1000*3600*24));
//        System.out.println(a);
//        System.out.println(zero);
//        System.out.println(days);
//        BigDecimal a = new BigDecimal(Integer.parseInt("10")).divide(new BigDecimal(100));
//        BigDecimal level = new BigDecimal(86);
//        level = level.pow(2).divide(new BigDecimal(500), 3, BigDecimal.ROUND_HALF_UP).add(level).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
//        System.out.println(level);
        String a = SHA.encryptSHA("123456");
        System.out.println(a);
//        DateFormat df = new SimpleDateFormat("yyyyMMdd");
//        Date validDate = df.parse("00000000");
//        System.out.println(validDate);
//        BigDecimal level = new BigDecimal(15);
//        BigDecimal gen = (level.pow(2).divide(new BigDecimal(500), 3, BigDecimal.ROUND_HALF_UP).add(level)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
//        System.out.println(gen.multiply(new BigDecimal(24)));
    }
}

