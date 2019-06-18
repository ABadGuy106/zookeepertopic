# Zookeeper分布式专题与Dubbo微服入门

## 分布式系统概念与zookeeper简介

#### 什么是分布式系统？

- 很多台计算机组成一个整体，一个整体一致对外并且处理同一请求
- 内部的每台计算机都可以相互通信（rest/rpc）
- 客户端到服务端的一次请求到响应结束会经历多台计算机

#### zookeeper的特性

- 一致性：数据一致性，数据按照顺序分批入库
- 原子性：事务要么成功要么失败，不会局部化
- 单一视图：客户端连接集群的任一zk节点，数据都是一致的
- 可靠性：每次对zk的操作状态都会保存在服务端
- 实时性：客户端可以读取到zk服务端的最新数据


#### zookeeper主要目录结构

- bin:主要的一些运行命令
- conf:存放配置文件，其中我们需要修改zk.cfg
- contrib:附加的一些功能
- dist-mavne:mvn编译后的目录
- doc:文档
- lib:依赖jar包
- recipes:案例demo代码
- src:源码

#### zoo.cfg配置

- tickTime:用于计算的时间单元。比如session超时：N*tickTime
- initLimit:用于集群，允许从节点连接并同步到master节点的初始化连接时间，以tickTIme的倍数来表示
- syncLimit:用于集群，master主节点与从节点之间发送消息请求和应答时间长度。（心跳机制）
- dataDir:必须配置
- dataLogDir:日志目录，如果不配置会和dataDir共用同一个目录
- clientPort:连接服务器端口，默认2181

## zookeeper基本数据模型介绍

- 是一个树形结构，类似于前端开发中的tree.js组件
- 每个节点都称之为znode,它可以有子节点，也可以有数据
- 每个节点分为临时节点和永久节点，临时节点在客户端断开后消失
- 每个zk节点都有各自的版本号，可以通过命令行来显示节点信息
- 每当节点数据发生变化，那么该节点的版本号会累加(乐观锁)
- 删除/修改过时节点，版本号不匹配会报错
- 每个zk节点存储的数据不宜过大，几k即可
- 节点可以设置权限acl,可以通过权限来限制用户的访问

#### zk的作用体现

- master节点选举，主节点挂了以后，从节点就会接手主节点的工作，并且保证这个节点是唯一的，这也是所谓首脑模式，从而保证我们的集群高可用的
- 统一配置文件管理，即只需要部署一台服务器，则可以把相同的配置文件同步更新到其他所有的服务器，此操作在云计算中用的特别多（假设修改了redis统一配置）
- 发布与订阅，类似消息队列MQ(amq,rmq...)，dubbo发布者把数据存在znode上，订阅者会读取这个数据
- 提供分布式锁，分布式环境中不同进程之间争夺资源，类似于多线程中的锁
- 集群管理，集群中保证数据的强一致性

## ZK基于特性于基于Linux的ZK客户端命令学习

### 基本命令

通过./zkCli.sh 打开zk的客户端进入命令行后台

ls与ls2命令

​	ls2=ls+stat

get与stat命令

#### create 命令

```properties
[zk: localhost:2181(CONNECTED) 0] create /imooc imooc-data
Created /imooc
[zk: localhost:2181(CONNECTED) 2] get /imooc
imooc-data
cZxid = 0x52
ctime = Mon Jun 17 16:39:47 CST 2019
mZxid = 0x52
mtime = Mon Jun 17 16:39:47 CST 2019
pZxid = 0x52
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 10
numChildren = 0
```

使用-e参数，创建临时节点(当断开创建这个节点的连接时，该节点自动删除)：

```properties
[zk: localhost:2181(CONNECTED) 3] create -e /imooc/tmp imooc-data
Created /imooc/tmp
[zk: localhost:2181(CONNECTED) 4] get /imooc/tmp
imooc-data
cZxid = 0x53
ctime = Mon Jun 17 16:49:23 CST 2019
mZxid = 0x53
mtime = Mon Jun 17 16:49:23 CST 2019
pZxid = 0x53
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x16b64597d790004
dataLength = 10
numChildren = 0
```

使用 -s 参数创建顺序节点(当执行同一条创建命令时，zk会根据执行顺序创建带有顺序的节点)

```properties
[zk: localhost:2181(CONNECTED) 0] create -s /imooc/sec seq
Created /imooc/sec0000000001
[zk: localhost:2181(CONNECTED) 1] create -s /imooc/sec seq
Created /imooc/sec0000000002
[zk: localhost:2181(CONNECTED) 2] create -s /imooc/sec seq
Created /imooc/sec0000000003
[zk: localhost:2181(CONNECTED) 3] create -s /imooc/sec seq
Created /imooc/sec0000000004
```

#### set命令

```properties
[zk: localhost:2181(CONNECTED) 4] get /imooc
imooc-data
cZxid = 0x52
ctime = Mon Jun 17 16:39:47 CST 2019
mZxid = 0x52
mtime = Mon Jun 17 16:39:47 CST 2019
pZxid = 0x59
cversion = 6
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 10
numChildren = 4
[zk: localhost:2181(CONNECTED) 5] set /imooc new-data
cZxid = 0x52
ctime = Mon Jun 17 16:39:47 CST 2019
mZxid = 0x5a
mtime = Mon Jun 17 17:44:07 CST 2019
pZxid = 0x59
cversion = 6
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 4
[zk: localhost:2181(CONNECTED) 6] get /imooc
new-data
cZxid = 0x52
ctime = Mon Jun 17 16:39:47 CST 2019
mZxid = 0x5a
mtime = Mon Jun 17 17:44:07 CST 2019
pZxid = 0x59
cversion = 6
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 4
```

#### delete命令

```properties
[zk: localhost:2181(CONNECTED) 7] ls /imooc
[sec0000000003, sec0000000004, sec0000000001, sec0000000002]
[zk: localhost:2181(CONNECTED) 8] delete /imooc/sec0000000003
[zk: localhost:2181(CONNECTED) 9] ls /imooc
[sec0000000004, sec0000000001, sec0000000002]
```

### watcher机制

- 针对每个节点的操作，都会有一个监督者 -> watcher
- 当监控的某个对象(znode)发生了变化，则出发watcher事件
- zk中的watcher时一次性的，触发后立即销毁
- 父节点、子节点增删改都能触发其watcher
- 针对不同类型的操作，触发的watcher事件不同：
  - (子)节点创建事件
  - (子)节点删除事件
  - (子)节点数据变化事件

#### watcher命令学习

通过get path [watch]设置watcher

父节点 增 删 改操作触发watcher

子节点增 删 改操作触发watcher

#### watcher事件类型一

创建父节点触发：NodeCreated

修改父节点数据触发：NodeDataChanged

删除父节点触发：NodeDeleted

#### watcher事件类型二

ls为父节点设置watcher,创建子节点触发：NodeChildrenChanged

```properties
[zk: localhost:2181(CONNECTED) 13] ls /imooc watch
[sec0000000004, test2, test, sec0000000001, sec0000000002]
[zk: localhost:2181(CONNECTED) 14] create /imooc/abd 999

WATCHER::Created /imooc/abd


WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/imooc
```

ls为父节点设置watcher,删除子节点触发：NodeChildrenChaged

```properties
[zk: localhost:2181(CONNECTED) 0] ls /imooc watch
[sec0000000004, test2, abd, sec0000000001, sec0000000002]
[zk: localhost:2181(CONNECTED) 1] delete /imooc/abd

WATCHER::
[zk: localhost:2181(CONNECTED) 2]
WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/imooc
```

ls为父节点设置watcher,修改子节点不触发事件

#### watcher使用场景

统一资源配置

### ACL(access control lists)权限控制

针对节点可以设置相关读写等权限，目的为了保障数据安全性

权限permissions可以指定不同的权限范围以及角色

#### ACL命令行

getAcl：获取某个节点的acl权限信息

```properties
[zk: localhost:2181(CONNECTED) 8] create /imooc/abc 123
Created /imooc/abc
[zk: localhost:2181(CONNECTED) 9] getAcl /imooc/abc
'world,'anyone
: cdrwa
```

setAcl：设置某个节点的acl权限信息

addauth：输入认证授权信息，注册时输入明文密码（登录）但在zk的系统里，密码时以加密的形式存在的

#### ACL的构成一

zk的acl通过[scheme : id : permissions]来构成权限列表

scheme:代表采用的某种权限机制

id:代表允许访问的用户

permissions:权限组合字符串

#### ACL的构成二 - scheme

world:world下只有一个id,即只有一个用户，也就是anyone,那么组合的写法就是 world:anyone:[permissions]

auth:代表认证登录，需要注册用户有权限就可以，形式为 auth:user:passowrd:[permissions]

digest:需要密码加密才能访问，组合形式为 ： digest:username:BASE64(SHA1(password)):[permissions]

​    简而言之，auth与digest的区别就是，前者明文，后者密文

​	setAcl /path auth:lee:lee:cdrwa

​	与

​	setAcl /path digest:lee:BASE64(SHA1(password)) cdrwa

​	是等价的,在通过

​	addauth digest lee:lee 后都能操作指定节点的权限

ip:当设置ip指定的ip地址,此时限制ip进行访问，比如ip:192.168.1.1:[permissions]

super:代表超级管理员，拥有所有的权限

#### ACL的构成三 - permissions

权限字符串缩写 crdwa

​	CREATE:创建子节点

​	READ: 获取节点/子节点

​	DELETE:删除子节点

​	WRITE:设置节点数据

​	ADMIN:设置权限

#### ACL命令行学习

world:anyone:cdrwa

```properties
[zk: localhost:2181(CONNECTED) 11] getAcl /imooc/abc
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 12] setAcl /imooc/abc world:anyone:crwa
cZxid = 0x65
ctime = Mon Jun 17 18:42:41 CST 2019
mZxid = 0x65
mtime = Mon Jun 17 18:42:41 CST 2019
pZxid = 0x65
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
[zk: localhost:2181(CONNECTED) 13] getAcl /imooc/abc
'world,'anyone
: crwa
[zk: localhost:2181(CONNECTED) 14] create /imooc/abc/xyz 123
Created /imooc/abc/xyz
[zk: localhost:2181(CONNECTED) 15] delete /imooc/abc/xyz
Authentication is not valid : /imooc/abc/xyz
```

auth: user: pwd:cdrwa

digest: user:BASE64(SHA1(pwd)):cdrwa

addauth adigest user:pwd

```properties
[zk: localhost:2181(CONNECTED) 17] create /name name
Created /name
[zk: localhost:2181(CONNECTED) 18] create /name/imooc imooc
Created /name/imooc
[zk: localhost:2181(CONNECTED) 19] getAcl /name/imooc
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 20] setAcl /name/imooc auth:imooc:imooc:cdrwa
Acl is not valid : /names/imooc
[zk: localhost:2181(CONNECTED) 21] addauth digest imooc:imooc
[zk: localhost:2181(CONNECTED) 23] setAcl /name/imooc auth:imooc:imooc:cdrwa
cZxid = 0x6b
ctime = Tue Jun 18 10:51:27 CST 2019
mZxid = 0x6b
mtime = Tue Jun 18 10:51:27 CST 2019
pZxid = 0x6b
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 5
numChildren = 0
[zk: localhost:2181(CONNECTED) 24] getAcl /name/imooc
'digest,'imooc:XwEDaL3J0JQGkRQzM0DpO6zMzZs=
: cdrwa
[zk: localhost:2181(CONNECTED) 25] get /name/imooc
imooc
cZxid = 0x6b
ctime = Tue Jun 18 10:51:27 CST 2019
mZxid = 0x6b
mtime = Tue Jun 18 10:51:27 CST 2019
pZxid = 0x6b
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 5
numChildren = 0
```

ip:127.0.0.1:cdrwa

```properties
[zk: localhost:2181(CONNECTED) 5] create /name/ip ip
Created /name/ip
[zk: localhost:2181(CONNECTED) 6] getAcl /name/ip
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 7] setAcl /name/ip ip:127.0.0.1:cdrwa
cZxid = 0x71
ctime = Tue Jun 18 11:05:31 CST 2019
mZxid = 0x71
mtime = Tue Jun 18 11:05:31 CST 2019
pZxid = 0x71
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0
[zk: localhost:2181(CONNECTED) 8] getAcl /name/ip
'ip,'127.0.0.1
: cdrwa
```

#### ACL命令行学习二

进入super超级管理员模式

​	Super:

​		1.修改zkServer.sh 增加super管理员

​		将内容：

```shell
 nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

​		修改为：

```shell
 nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" "-Dzookeeper.DigestAuthenticationProvider.superDigest=imooc:XwEDaL3J0JQGkRQzM0DpO6zMzZs="\
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

​		2.重启zkServer.sh

​		使用超级管理员登录

```properties
addauth digest imooc:imooc
```

#### ACL的常用使用场景

开发/测试环境分离，开发者无权操作测试库的节点，只能看

生产环境上控制指定ip的服务可以访问相关节点，防止混乱

#### zookeeper的四字命令 Four Letter Words

zk可以通过它自身提供的简写命令来和服务器进行交互

需要使用nc命令，安装：yum install nc

echo \[commond\]|nc \[ip\]  \[port\]

  \[stat\] 查看zk的状态信息，以及是否mode

```shell
echo stat | nc 192.168.1.1 2181
```

 \[ruok\] 查看当前zkServer是否启动，返回imok

 \[dump\] 列出未经处理的会话和临时节点

 \[conf\] 查看服务器配置

 \[cons\] 展示连接到服务器的客户端信息

 \[envi\] 环境变量

 \[mntr\] 监控zk健康信息

 \[wchs\] 展示watch的信息

 \[wchc\] 与\[wchp\] session与wach及path与watch信息，默认情况下这两个命令是不在白名单内所以无法执行，需要修改zoo.cnf配置文件

在文件末尾添加如下内容，重启

```properties
4lw.commands.whitelist=*
```

## 使用zookeeper原生Java API进行客户端开发

- 会话连接与恢复
- 节点的增删改查
- watch与acl的相关操作





















































































































































