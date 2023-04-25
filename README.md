# calcite 项目 apache-calcite-tutorial
# codegen+javacc 项目 codegen
# connector 项目 connector-elasticsearch6
               connector-elasticsearch7
               connector-elasticsearch-base
               connector-kudu
               connector-redis
# 测试工程 srcgit 



TODO:
 1 项目中 做过一次调整 ，主要调整是 剔除了 子项目 依赖 父项目依赖度部分，所以 子项目会报错，只需要 修改子项目的pom 就可以具体可以参考 connector-kudu  这个项目中pom的情况。
 2 所有项目 替换了 log的使用包 如果报错 参考 KuduLookupFunction中的