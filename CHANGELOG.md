## [2019.08.19]

### 配置化数据落地方案定型并开发完成

1. 数据源
   * 支持kafka source

2. 数据过滤、加工，实现完整udf 定义
   * 原始流字段获取使用json path 实现， 如： $.Data.uid
   * 数据过滤， 字段转换统一用udf 配置实现
   *  已实现udf：common（公共udf， 如日期、字符串）、exp(运算符，如if or and)、biz（业务udf ，已有签约主播udf）
   * 重写redis pool， 支持command 调用

3. Sink 已实现MySQL、Redis 存储

### 业务接入

+ 工会签约主播收钻统计