# Netty-MQTT

基于netty实现mqtt服务器，开发中:)

## TODO

- [x] 参考MQTT3.1.1规范实现
- [x] 完整的QoS服务质量等级实现
- [x] 遗嘱消息（仅支持主动关闭、会话过期等场景QoS0~1）
- [x] 保留消息及消息分发重试
- [x] 心跳机制
- [x] MQTT连接认证(可选择是否开启)
- [x] SSL方式连接(可选择是否开启)
- [x] 主题过滤(支持单主题订阅如 test_topic /mqtt/test --不能以/结尾, 通配符订阅 # /mqtt/# --以#结尾)
- [x] Websocket支持(可选择是否开启)
- [x] Redis-Cluster集群功能(可选择是否开启)
- [ ] Kafka消息转发功能(可选择是否开启)
- [ ] 启动后查看统计数据 http://127.0.0.1:8922/open/api/mqttwk/info

## 技术体系

* 使用 netty 实现通信及协议解析
* 使用 redis 实现消息缓存，集群
* 使用 kafka 实现消息代理(可选)