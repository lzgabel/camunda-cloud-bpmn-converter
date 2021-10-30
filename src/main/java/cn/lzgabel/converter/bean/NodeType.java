package cn.lzgabel.converter.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.instance.*;

import java.util.List;
import java.util.Map;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @create 2021-11-13
 * @since 1.0.0
 */

public enum NodeType {
    /**
     * 开始事件
     */
    START_EVENT("startEvent", StartEvent.class),

    /**
     * 并行事件
     */
    PARALLEL_GATEWAY("parallelGateway", ParallelGateway.class),

    /**
     * 排他事件
     */
    EXCLUSIVE_GATEWAY("exclusiveGateway", ExclusiveGateway.class),

    /**
     * serviceTask
     */
    SERVICE_TASK("serviceTask", ServiceTask.class),

    /**
     * sendTask
     */
    SEND_TASK("sendTask", SendTask.class),

    /**
     * receiveTask
     */
    RECEIVE_TASK("receiveTask", ReceiveTask.class),

    /**
     * scriptTask
     */
    SCRIPT_TASK("scriptTask", ScriptTask.class),

    /**
     * businessRuleTask
     */
    BUSINESS_RULE_TASK("businessRuleTask", BusinessRuleTask.class),

    /**
     * userTask
     */
    USER_TASK("userTask", UserTask.class),

    /**
     * 子任务类型
     */
    SUB_PROCESS("subProcess", SubProcess.class),

    /**
     * 调用任务类型
     */
    CALL_ACTIVITY("callActivity", CallActivity.class),

    /**
     * 中级捕获事件
     */
    INTERMEDIATE_CATCH_EVENT("intermediateCatchEvent", IntermediateCatchEvent.class);

    private String typeName;

    private Class<?> typeClass;

    NodeType(String typeName, Class<?> typeClass) {
        this.typeName = typeName;
        this.typeClass = typeClass;
    }

    public final static Map<String, Class<?>> TYPE_CLASS_MAP = Maps.newHashMap();
    public final static List<String> JOB_WORKER_TASK = Lists.newArrayList(SERVICE_TASK.typeName,
            SEND_TASK.typeName, SCRIPT_TASK.typeName, BUSINESS_RULE_TASK.typeName);

    static {
        for (NodeType element : NodeType.values()) {
            TYPE_CLASS_MAP.put(element.typeName, element.typeClass);
        }
    }

    public boolean isEqual(String typeName) {
        return this.typeName.equals(typeName);
    }
}
