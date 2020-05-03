# BlobGenius
A simple distributed file system using blob storage in mysql or fs

一个简单的分布式文件存储系统，大文件切分成数据块存储，底层存储引擎采用Mysql或文件系统。

**介绍文档**
[用JAVA从零开始写一个分布式文件存储系统](https://www.jianshu.com/p/7807eb8e7d7b)

**功能特点**
- 数据块大小：大文件分成256KB、512KB、1024KB、2048KB，小数据对象切成256B、512B、1024B、4096B等；
- 数据校验，数据块默认采用SHA-256算法生成校验码；
- 数据块支持编码、压缩、加密处理；
- 数据存储支持主从备份与对等备份机制，最大备份数量可以设置，默认没有上限；
- 数据对象描述信息与数据块存储独立，不同数据对象可引用相同校验码的数据块对象，避免内容重复存储；
- 数据对象全文搜索采用ElasticSearch支持；
- 数据存储节点可动态加入或退出，数据复制自动进行；
- 系统三要素:DataObject-数据对象，DataBlob-数据块，DataBlobIds-数据块索引，每个数据对象由多个数据块构成，每个数据块得以是若干个数据对象的组成部分，采用引用计数机制；
