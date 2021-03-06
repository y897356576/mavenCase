package com.stone.demo.redisCase;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Set;

/**
 * Created by 石头 on 2017/7/15.
 */
public class RedisInJava {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("106.14.200.149", 6379);
        jedis.auth("pwd");
        System.out.println("Redis连接状态: " + jedis.ping());

        try{
            doRedis_0(jedis);
//            doRedis_1(jedis);
//            doRedis_2(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Tuple> vals = jedis.zrangeByScoreWithScores("keyA_z0", 0, 10);
        for (Tuple val : vals) {
            System.out.println("val:" + val.getElement() + "; score:" + val.getScore());
        }
    }


    private static void doRedis_0(Jedis jedis) {
        System.out.println(jedis.keys("*"));
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            System.out.println(key + " : " + jedis.get(key));
        }
    }

    private static void doRedis_1(Jedis jedis) {
        Long l = jedis.setnx("key01", "val01");
        System.out.println(l==1L ? "保存成功" : "Key已存在");
        String s = jedis.set("key01", "val01");
        System.out.println(s.equals("OK") ? "保存成功" : "保存失败");
        System.out.println("key01 : " + jedis.get("key01"));
    }

    private static void doRedis_2(Jedis jedis) {
        jedis.lpush("java_list_1", "listVal0");
        jedis.lpush("java_list_1", "listVal1");
        jedis.lpush("java_list_1", "listVal1");
        System.out.println("Val_All : " + jedis.lrange("java_list_1", 0, -1));
        System.out.println("Val_NotFirst" + jedis.lrange("java_list_1", 1, 2));
        String popStr = jedis.rpop("java_list_1");
        System.out.println("PopString : " + popStr);
        System.out.println(jedis.lrange("java_list_1", 0, -1));
        jedis.del("java_list_1");
    }

}
