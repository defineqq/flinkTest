//package com.cp.data.test;
//
//import com.google.common.collect.Lists;
//import com.cp.data.dataland.udf.common.exp.Arithmetic;
//import redis.clients.jedis.Jedis;
//
//public class Test {
//
//  public static void main(String[] args) {
//    String sql = "select t.uid, group_concat(t.action) as action,effect_length_for_day_count,brokerage_company_id,area \n"
//        + "from (\n"
//        + "select \n"
//        + "distinct tb1.uid, tb2.action, effect_length_for_day_count,brokerage_company_id,tb1.area \n"
//        + "from cp_signeranchor tb1\n"
//        + "left join cp_signeranchor_contract tb2 \n"
//        + "on tb1.contract_id = tb2.id  and tb2.status=1 and tb2.is_delete='False' and tb2.action <>''\n"
//        + ")t  group by t.uid,effect_length_for_day_count,brokerage_company_id, area";
//    System.out.println(sql);
//    String command = "sadd xx 'sdsd'";
//    int idx = command.indexOf(" ");
//    String arg0 = command.substring(0, idx);
//    String arg1 = command.substring(idx + 1);
//    System.out.println("args0:" + arg0 + ",arg1=" + arg1);
//    System.out.println("setnx".trim().toUpperCase());
//  }
//}
