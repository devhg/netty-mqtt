# Netty-MQTT

基于netty实现mqtt服务器，开发中:)

## TODO

- [x] 参考MQTT3.1.1规范实现
- [x] 完整的QoS服务质量等级实现 ✅
- [x] 遗嘱消息  ✅（仅支持主动关闭、发送失败、失去心跳等场景QoS0~2）
- [x] 保留消息及消息分发重试 ✅
- [x] 心跳机制 PingReq PingRec 长连接保活 ✅
- [x] MQTT连接认证，用户名密码验证 ✅ (可选择是否开启)
- [x] SSL方式连接 ✅ (可选择是否开启)
- [x] 主题过滤 ✅ (支持单主题订阅如 test_topic /mqtt/test --不能以/结尾, 通配符订阅 # /mqtt/# --以#结尾)
- [x] Websocket支持 ✅ (可选择是否开启)
- [ ] Redis-Cluster集群功能(可选择是否开启)
- [x] IP黑名单接口 ✅
- [x] Kafka消息转发功能(可选择是否开启)
- [x] 控制面的监控功能，链路追踪功能
- [ ] 全链路的压力测试

## 目标

1. 心跳维护 ✅
2. 链路监控 ✅
3. 报文监控 ✅
4. 物联网卡流量监控
5. 在线维护 ✅
6. ip黑名单 ✅
7. 多规约支持（代码里就配置了645/698/376/104/二进制/MQTT/充电桩这几种规约，其实支持更多，个人感觉能支持所有规约，至少目前我见到的都能支持)
8. 服务端序列化传输优化（protobuf）

## 技术体系

* 使用 netty 实现通信及协议解析
* 使用 redis 实现消息缓存，集群
* 使用 kafka 实现消息代理(可选)

## 理想架构图
![](https://image-ihui.oss-cn-beijing.aliyuncs.com/img/20220403231318.png)
![](https://image-ihui.oss-cn-beijing.aliyuncs.com/img/20220403231819.png)
![](https://image-ihui.oss-cn-beijing.aliyuncs.com/img/20220403231833.png)
