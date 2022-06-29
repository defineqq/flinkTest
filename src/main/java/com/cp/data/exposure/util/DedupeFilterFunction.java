//package com.cp.data.exposure.util;
//
////通过实时的缓存来去重使用
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.LoadingCache;
//import com.cp.data.exposure.bean.ImFollow;
//import org.apache.flink.api.common.functions.RichFilterFunction;
//
//import java.util.concurrent.TimeUnit;
//
////expireAfterAccess: 当缓存项在指定的时间段内没有被读或写就会被回收。
////
////        expireAfterWrite：当缓存项在指定的时间段内没有更新就会被回收。
////
////        refreshAfterWrite：当缓存项上一次更新操作之后的多久会被刷新。
//
//public class DedupeFilterFunction extends RichFilterFunction {
//    LoadingCache<ImFollow, Boolean> cache;
//    public void open() throws Exception {
//        cache = CacheBuilder.newBuilder()
//// 设置过期时间
//                .expireAfterWrite(1, TimeUnit.MINUTES);
//
//    }
//
//}
//
//
//
//    public boolean filter(T value) throws Exception {
//        ID key = value.getID();
//        boolean seen = cache.get(key);
//        if (!seen) {
//            cache.put(key, true);
//            return true;
//        } else {
//            return false;
//        }
//    }