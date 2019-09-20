package com.youzi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class redisTest {

    public static void main(String[] args) throws Exception {

////        new RedisClient().show();
//        Jedis jedis = new RedisClient().getResource();
//        jedis.set("key1", "1111");
//        System.out.println(jedis.get("key1"));
//        jedis.set("key1", "2222");
//        System.out.println(jedis.get("key1"));
//        jedis.append("key1", "+3333");
//        System.out.println(jedis.get("key1"));
//        jedis.del("key1");
//        System.out.println(jedis.get("key1"));
//        System.out.println("一次性新增key201,key202,key203,key204及其对应值："+jedis.mset("key201","value201",
//                "key202","value202","key203","value203","key204","value204"));
//        System.out.println("一次性获取key201,key202,key203,key204各自对应的值："+
//                jedis.mget("key201","key202","key203","key204"));
////        System.out.println("一次性删除key201,key202："+jedis.del(new String[]{"key201", "key202"}));
//        System.out.println("一次性删除key201,key202："+jedis.del("key201", "key202"));
//        System.out.println("一次性获取key201,key202,key203,key204各自对应的值："+
//                jedis.mget("key201","key202","key203","key204"));
//        Map map = new HashMap();
//        map.put("code", String.valueOf(200));
//        map.put("name", "qwer");
//        map.put("pwd", "1234");
//        map.put("type", String.valueOf(1));
//        map.put("status", String.valueOf(0));
//        map.put("amount", "200.33");
//        jedis.hmset("user1", map);
//        Map map111 = jedis.hgetAll("user1");
//        System.out.println(map111.toString());
//
//        jedis.setnx("nxg1", "1111");
//        System.out.println("nxg1 : " + jedis.get("nxg1"));
//        jedis.setnx("nxg2", "2222");
//        System.out.println("nxg2 : " + jedis.get("nxg2"));
//        jedis.set("nxg2", "2323");
//        System.out.println("nxg2 : " + jedis.get("nxg2"));
//        jedis.incr("nxg2");
//        System.out.println("nxg2 : " + jedis.get("nxg2"));
//        System.out.println(jedis.mget("user1"));
////        System.out.println(jedis.setex("nxg2", 3, "200"));
////        System.out.println("nxg2 : " + jedis.get("nxg2"));
//
//        System.out.println("key302原值：" + jedis.getSet("nxg2", "value302-after-getset"));
//        System.out.println("key302新值：" + jedis.get("nxg2"));
//        System.out.println("获取key302对应值中的子串：" + jedis.getrange("nxg2", 5, 7));
//        Jedis jedis = new RedisClient().getResource();
//        String a = jedis.get("user:1");
//        Iterator<String> iter = jedis.hkeys("user:1").iterator();
//        Map a = new HashMap();
//        while (iter.hasNext()){
//            String key = iter.next();
//            a.put(key,jedis.hmget("user:1", key));
//        }
//        Map b= JSON.parseObject(a,Map.class);
//        b.put("vip",10);
//        System.out.println(b);
//        String cc = JSON.toJSON(b).toString();
//        jedis.set("user:2",cc);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse("2019-1-24 00:00:01");
        if(date.getTime() > 1548259200000L){
            System.out.println("guole");
        }
        System.out.println("end");
    }
}
