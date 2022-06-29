//package com.cp.data.test;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.cp.data.dataland.common.util.JsonPathUtil;
//import com.cp.data.dataland.common.util.TemplateUtil;
//import com.cp.data.dataland.transform.JsonPathFlatMapFunction;
//import com.cp.data.dataland.udf.common.date.DateFormat;
//import com.cp.data.dataland.udf.engine.UdfParser;
//import com.cp.data.dataland.udf.engine.Udfs;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//
//public class UdfIT {
//
//  @Test
//  public void testDtFmt() {
//    Assert.assertEquals("2019062020284",
//        Udfs.eval("substr(dt_fmt(1561033720337, 'yyyyMMddHHmmss'),0, 13)"));
//  }
//
//  @Test
//  public void testDtFmt2() {
//    Assert.assertEquals("2019062020284",
//        Udfs.eval("substr(dt_fmt(1561033720337, 'yyyyMMddHHmmss'),0, 8)"));
//  }
//
//  @Test
//  public void eval() {
//    String str = "concat(dt_fmt('$.dt', 'yyyyMMddHH'), if(lte(dt_fmt('$.dt', 'mm'), 30), '00', '30'))";
//    String data = "{\"dt\":\"2019-11-22 23:44:32\"}";
//    JsonPathFlatMapFunction jsonPathFlatMapFunction = new JsonPathFlatMapFunction(null);
//    System.out.println(jsonPathFlatMapFunction.eval(new UdfParser(str).parse(), JsonPathUtil.parse(data)));
//  }
//
//  @Test
//  public void eval2() {
//    String str = "lte(eval('Math.round(Math.random() * 100)'), 30)";
//    System.out.println(Udfs.eval(str));
//  }
//
//  @Test
//  public void eval5() {
//    ArrayList list = Lists.newArrayList("2019-12-20 18:05:33", "HH");
//    System.out.println(new DateFormat().invoke(list));
//  }
//
//  @Test
//  public void eval4() {
//    String str =  "arith('$.Data.price', redis(concat('zscore ranking:diamond:gift:', concat(dt_fmt('$.Data.created_at', 'yyyyMMddHH'), if(lte(dt_fmt('$.Data.created_at', 'mm'), 30), '00', '30')), ':', '$.Data.receiver', '$.Data.sender'), 'kewldatacenter.ux4gcx.ng.0001.use1.cache.amazonaws.com:6379'), '+')";
//    String data = "{\"dt\":\"2019-11-22 23:44:32\"}";
//    JsonPathFlatMapFunction jsonPathFlatMapFunction = new JsonPathFlatMapFunction(null);
//    System.out.println(jsonPathFlatMapFunction.eval(new UdfParser(str).parse(), JsonPathUtil.parse(data)));
//  }
//
//  @Test
//  public void eval3() {
//    String cql = "MATCH(`n${edgeidfrom}`:${type})\n CREATE-[rel:Rel${data}]->(`n${edgeidto}`:${type})";
//    List<String> variables = TemplateUtil.getVariableNames(cql);
//    Map<String, Object> prepareMap = Maps.newHashMap();
//    variables.forEach(v -> prepareMap.put(v, String.format("%s", v)));
//    String prepareCql = TemplateUtil.processTemplate(cql, prepareMap);
//    System.out.println(prepareCql);
//
//  }
//
//  @Test
//  public void test() {
//    System.out.println(new UdfParser("and(\"'$.Data.start_time' - '$.Data.end_time'\"), 1800), eq('$.Data.area','A_US'), in('$.Data.type','1,4') ,eq('$.Type','UPDATE'),eq('$.OldData.end_time', 0), gt('$.Data.end_time', 0))").parse());
//  }
//
//}
