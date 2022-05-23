package cn.sdutcs.mqtt.store.utils;

import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * Topic模糊匹配
 */
public class TopicMatcher {
    /**
     * 模糊匹配：用 / 分割后，若topic比filter路径短，不可能匹配；否则，遍历匹配。遍历规则如下
     * 如果是通配符[+|#]，保留通配符（若[#]直接跳出，#后不允许接后缀），否则从topic取出
     * 用于构造新的filter。遍历结束后，如果新的构造filter等于原topicFilter证明匹配，反之。
     * <p>
     * topic:  test/a/b/c
     * filter: test/a/+/#
     * 结果：   test/a/+/# == filter   match
     * <p>
     * topic:  test/aa/b/c
     * filter: test/a/+/#
     * 结果：   test/aa/+/# != filter  not match
     * <p>
     * topic:  test/aa/b/c
     * filter: test/a
     * 结果：   test/aa != filter   not match
     * <p>
     * topic:  test/a/b/c
     * filter: test/#
     * 结果：   test/# == filter   match
     * <p>
     * topic:  test/a
     * filter: test/a/#
     * 结果：   topicEle.size() < filterEle.size()  not match
     */
    public static boolean match(String topicFilter, String topic) {
        List<String> topicEle = StrUtil.split(topic, '/');
        List<String> filterEle = StrUtil.split(topicFilter, '/');
        if (topicEle.size() >= filterEle.size()) {
            // 对topic重置替换，如果和topicFilter相同，则证明match
            String expected = "";
            for (int i = 0; i < filterEle.size(); i++) {
                String value = filterEle.get(i);
                if (value.equals("+")) {
                    expected = expected + "+/";
                } else if (value.equals("#")) {
                    expected = expected + "#/";
                    break;
                } else {
                    expected = expected + topicEle.get(i) + "/";
                }
            }
            expected = StrUtil.removeSuffix(expected, "/");
            return topicFilter.equals(expected);
        }
        return false;
    }
}
