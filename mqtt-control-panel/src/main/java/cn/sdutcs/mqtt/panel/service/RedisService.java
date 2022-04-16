package cn.sdutcs.mqtt.panel.service;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import cn.sdutcs.mqtt.panel.common.Constant;
import cn.sdutcs.mqtt.panel.model.Metrics;
import cn.sdutcs.mqtt.panel.model.Point;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取broker配置信息
     */
    public Map getBrokerConfig() {
        String broker_config = redisTemplate.opsForValue().get("broker_config");
        return JSON.parseObject(broker_config, Map.class);
    }

    /**
     * 获取broker配置信息
     */
    public Metrics getBrokerMetrics(ArrayList<String> dateList) {
        List<Map> result = new ArrayList<>();
        dateList.forEach((date) -> {
            String brokerCounter = redisTemplate.opsForValue().get("broker_counter:" + date);
            if (!StrUtil.isBlank(brokerCounter)) {
                Map map = JSON.parseObject(brokerCounter, Map.class);
                map.put("date", date);
                result.add(map);
            }
        });
        return this.processMetrics(result);
    }

    private Metrics processMetrics(List<Map> dataOfDay) {
        Metrics m = new Metrics();
        int size = dataOfDay.size();
        AtomicInteger sumReq = new AtomicInteger();
        AtomicInteger sumPacket = new AtomicInteger();

        ArrayList<Point> reqOfDays = new ArrayList<>();
        ArrayList<Point> packetOfDays = new ArrayList<>();
        dataOfDay.forEach((data) -> {
            String date = (String) data.get("date");
            Integer receiveNum = (Integer) data.get("receiveNum");
            Integer sentNum = (Integer) data.get("sentNum");
            Integer heartbeatNum = (Integer) data.get("heartbeatNum");

            int reqNum = receiveNum + heartbeatNum;
            sumReq.addAndGet(reqNum);
            reqOfDays.add(new Point(date, reqNum));

            int packetNum = receiveNum + sentNum + heartbeatNum;
            sumPacket.addAndGet(packetNum);
            packetOfDays.add(new Point(date, packetNum));

        });
        Map curDay = dataOfDay.get(0);
        Integer receiveNum = (Integer) curDay.get("receiveNum");
        Integer sentNum = (Integer) curDay.get("sentNum");
        Integer heartbeatNum = (Integer) curDay.get("heartbeatNum");

        DecimalFormat df2 = new DecimalFormat("#.00");
        BigDecimal cpu = (BigDecimal) curDay.get("cpu");
        m.setCpu(df2.format(cpu));
        m.setLastSentTime(new Date((Long) curDay.get("lastSentTime")));
        m.setLastReceiveTime(new Date((Long) curDay.get("lastReceiveTime")));
        m.setCurConnNum((Integer) curDay.get("curChannelNum"));
        m.setMaxConnNum((Integer) curDay.get("maxChannelNum"));
        m.setReqNum(receiveNum + heartbeatNum);
        m.setReqAvg(sumReq.get() / size);
        m.setReqNumOfDays(reqOfDays);
        m.setPacketNum(receiveNum + sentNum + heartbeatNum);
        m.setPacketAvg(sumPacket.get() / size);
        m.setPacketNumOfDays(packetOfDays);
        return m;
    }

    private Map<String, Long> getTopics(String topicType) {
        Map<String, Long> result = new HashMap<>();
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder()
                    .match(topicType + "*")
                    .count(10000).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return keys;
        });

        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        for (String key : keys) {
            ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
            Map<String, String> map2 = hash.entries(key);
            result.put(key.substring(topicType.length()), hash.size(key));
        }
        return result;
    }

    public Map<String, Map<String, Long>> getTopicList() {
        Map<String, Long> wild = this.getTopics(Constant.TOPIC_CACHE_PRE_WILD);
        Map<String, Long> notwild = this.getTopics(Constant.TOPIC_CACHE_PRE_NOT_WILD);
        Map<String, Map<String, Long>> result = new HashMap<>();
        result.put("wild", wild);
        result.put("notwild", notwild);
        return result;
    }

    public Set<String> getTopicListByClientId(String clientId) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        return set.members(Constant.CACHE_CLIENT_PRE + clientId);
    }

    public List<SubscribeStore> getTopicSubscriber(String topicType, String topic) {
        List<SubscribeStore> list = new ArrayList<>();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        Map<String, String> map = hash.entries(topicType + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add(JSONObject.parseObject(v, SubscribeStore.class));
            });
        }
        return list;
    }
}
