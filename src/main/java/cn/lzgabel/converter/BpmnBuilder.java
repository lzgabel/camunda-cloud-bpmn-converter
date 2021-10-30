
package cn.lzgabel.converter;

import cn.lzgabel.converter.bean.NodeType;
import cn.lzgabel.converter.bean.ProcessDefinition;
import cn.lzgabel.converter.bean.event.start.EventType;
import cn.lzgabel.converter.bean.event.start.TimerDefinitionType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.builder.ProcessBuilder;
import io.camunda.zeebe.model.bpmn.builder.*;
import io.camunda.zeebe.model.bpmn.instance.*;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉<br>
 * 〈基于 json 格式自动生成 bpmn 文件〉
 *
 * @author lizhi
 * @since 1.0.0
 */

public class BpmnBuilder {

    private static final String ZEEBE_EXPRESSION_PREFIX = "=";

    public static BpmnModelInstance build(ProcessDefinition processDefinition) {
        return build(processDefinition.toString());
    }

    public static BpmnModelInstance build(String json) {
        try {
            JSONObject object = JSON.parseObject(json, JSONObject.class);
            ProcessBuilder executableProcess = Bpmn.createExecutableProcess();
            JSONObject process = object.getJSONObject("process");
            Optional.ofNullable(process.getString("name"))
                    .filter(StringUtils::isNotBlank)
                    .ifPresent(executableProcess::name);
            Optional.ofNullable(process.getString("processId"))
                    .filter(StringUtils::isNotBlank)
                    .ifPresent(executableProcess::id);

            StartEventBuilder startEventBuilder = executableProcess.startEvent();
            JSONObject flowNode = object.getJSONObject("processNode");
            if (NodeType.START_EVENT.isEqual(flowNode.getString("nodeType"))) {
                createStartEvent(startEventBuilder, flowNode);
                flowNode = flowNode.getJSONObject("nextNode");
            }
            String lastNode = create(startEventBuilder, startEventBuilder.getElement().getId(), flowNode);

            moveToNode(startEventBuilder, lastNode).endEvent();
            BpmnModelInstance modelInstance = startEventBuilder.done();
            Bpmn.validateModel(modelInstance);

            return modelInstance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建失败: e=" + e.getMessage());
        }
    }

    private static String create(AbstractFlowNodeBuilder startFlowNodeBuilder, String fromId, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        String nodeType = flowNode.getString("nodeType");
        if (NodeType.PARALLEL_GATEWAY.isEqual(nodeType)) {
            return createParallelGateway(startFlowNodeBuilder, flowNode);
        } else if (NodeType.EXCLUSIVE_GATEWAY.isEqual(nodeType)) {
            return createExclusiveGateway(startFlowNodeBuilder, flowNode);
        } else if (NodeType.JOB_WORKER_TASK.contains(nodeType)) {
            flowNode.put("incoming", Collections.singletonList(fromId));
            return createJobWorkerTask(startFlowNodeBuilder, flowNode);
        } else if (NodeType.RECEIVE_TASK.isEqual(nodeType)) {
            flowNode.put("incoming", Collections.singletonList(fromId));
            return createReceiveTask(startFlowNodeBuilder, flowNode);
        } else if (NodeType.USER_TASK.isEqual(nodeType)) {
            flowNode.put("incoming", Collections.singletonList(fromId));
            return createUserTask(startFlowNodeBuilder, flowNode);
        } else if (NodeType.SUB_PROCESS.isEqual(nodeType)) {
            return createSubProcess(startFlowNodeBuilder, flowNode);
        } else if (NodeType.CALL_ACTIVITY.isEqual(nodeType)) {
            return createCallActivity(startFlowNodeBuilder, flowNode);
        } else if (NodeType.INTERMEDIATE_CATCH_EVENT.isEqual(nodeType)) {
            return createIntermediateCatchEvent(startFlowNodeBuilder, flowNode);
        } else {
            throw new RuntimeException("未知节点类型: nodeType=" + nodeType);
        }
    }

    private static void createStartEvent(StartEventBuilder startEventBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        // 事件类型 timer/message 默认：none
        String eventType = flowNode.getString("eventType");
        String nodeName = flowNode.getString("nodeName");
        if (StringUtils.isNotBlank(eventType)) {
            startEventBuilder.name(nodeName);
            if (EventType.TIMER.isEqual(eventType)) {
                // timer 定义类型： date/cycle/duration
                String timerDefinitionType = flowNode.getString("timerDefinitionType");
                if (TimerDefinitionType.DATE.isEqual(timerDefinitionType)) {
                    String timerDefinition = flowNode.getString("timerDefinition");
                    startEventBuilder.timerWithDate(timerDefinition);
                } else if (TimerDefinitionType.DURATION.isEqual(timerDefinitionType)) {
                    String timerDefinition = flowNode.getString("timerDefinition");
                    startEventBuilder.timerWithDuration(timerDefinition);
                } else if (TimerDefinitionType.CYCLE.isEqual(timerDefinitionType)) {
                    String timerDefinition = flowNode.getString("timerDefinition");
                    startEventBuilder.timerWithCycle(timerDefinition);
                }
            } else if (EventType.MESSAGE.isEqual(eventType)) {
                String messageName = flowNode.getString("messageName");
                startEventBuilder.message(messageName);
            }
        }
    }

    private static String createExclusiveGateway(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        String name = flowNode.getString("nodeName");
        ExclusiveGatewayBuilder exclusiveGatewayBuilder = startFlowNodeBuilder.exclusiveGateway().name(name);
        if (Objects.isNull(flowNode.getJSONArray("branchNodes")) && Objects.isNull(flowNode.getJSONObject("nextNode"))) {
            return exclusiveGatewayBuilder.getElement().getId();
        }
        List<JSONObject> flowNodes = Optional.ofNullable(flowNode.getJSONArray("branchNodes")).map(e -> e.toJavaList(JSONObject.class)).orElse(Collections.emptyList());
        List<String> incoming = Lists.newArrayListWithCapacity(flowNodes.size());

        List<JSONObject> conditions = Lists.newCopyOnWriteArrayList();
        for (JSONObject element : flowNodes) {
            JSONObject childNode = element.getJSONObject("nextNode");

            String nodeName = element.getString("nodeName");
            String expression = element.getString("conditionExpression");

            // 记录分支条件中不存在任务节点的情况（即空分支）
            if (Objects.isNull(childNode)) {
                incoming.add(exclusiveGatewayBuilder.getElement().getId());
                JSONObject condition = new JSONObject();
                condition.fluentPut("nodeName", nodeName)
                        .fluentPut("expression", expression);
                conditions.add(condition);
                continue;
            }
            // 只生成一个任务，同时设置当前任务的条件
            childNode.put("incoming", Collections.singletonList(exclusiveGatewayBuilder.getElement().getId()));
            String id = create(exclusiveGatewayBuilder, exclusiveGatewayBuilder.getElement().getId(), childNode);
            exclusiveGatewayBuilder.getElement().getOutgoing().stream().forEach(
                    sequenceFlow -> conditionExpress(sequenceFlow, exclusiveGatewayBuilder, element)
            );
            if (Objects.nonNull(id)) {
                incoming.add(id);
            }
        }

        JSONObject childNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(childNode)) {
            if (incoming == null || incoming.isEmpty()) {
                return create(exclusiveGatewayBuilder, exclusiveGatewayBuilder.getElement().getId(), childNode);
            } else {
                // 所有 service task 连接 end exclusive gateway
                childNode.put("incoming", incoming);
                AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(exclusiveGatewayBuilder, incoming.get(0));
                // 1.0 先进行边连接, 暂存 nextNode
                JSONObject nextNode = childNode.getJSONObject("nextNode");
                childNode.put("nextNode", null);
                String id = create(abstractFlowNodeBuilder, abstractFlowNodeBuilder.getElement().getId(), childNode);
                for (int i = 1; i < incoming.size(); i++) {
                    moveToNode(exclusiveGatewayBuilder, incoming.get(i)).connectTo(id);
                }

                //  针对分支条件中空分支场景 添加条件表达式
                if (!conditions.isEmpty()) {
                    List<SequenceFlow> sequenceFlows = moveToNode(exclusiveGatewayBuilder, id)
                            .getElement().getIncoming().stream()
                            // 获取从源 gateway 到目标节点 未设置条件表达式的节点
                            .filter(e -> StringUtils.equals(e.getSource().getId(), exclusiveGatewayBuilder.getElement().getId()))
                            .collect(Collectors.toList());

                    sequenceFlows.stream().forEach(sequenceFlow -> {
                        if (!conditions.isEmpty()) {
                            JSONObject condition = conditions.get(0);
                            conditionExpress(sequenceFlow, exclusiveGatewayBuilder, condition);
                            conditions.remove(0);
                        }
                    });

                }

                // 1.1 边连接完成后，在进行 nextNode 创建
                if (Objects.nonNull(nextNode)) {
                    return create(moveToNode(exclusiveGatewayBuilder, id), id, nextNode);
                } else {
                    return id;
                }
            }
        }
        return exclusiveGatewayBuilder.getElement().getId();
    }

    private static void conditionExpress(SequenceFlow sequenceFlow, ExclusiveGatewayBuilder exclusiveGatewayBuilder, JSONObject condition) {
        String nodeName = condition.getString("nodeName");
        String expression = condition.getString("conditionExpression");
        if (StringUtils.isBlank(sequenceFlow.getName()) && StringUtils.isNotBlank(nodeName)) {
            sequenceFlow.setName(nodeName);
        }
        // 设置条件表达式
        if (Objects.isNull(sequenceFlow.getConditionExpression()) && StringUtils.isNotBlank(expression)) {
            ConditionExpression conditionExpression = createInstance(exclusiveGatewayBuilder, ConditionExpression.class);
            conditionExpression.setTextContent(!expression.isEmpty() && !expression.startsWith("=") ? String.format("=%s", expression) : expression);
            sequenceFlow.setConditionExpression(conditionExpression);
        }
    }

    private static String createParallelGateway(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        String name = flowNode.getString("nodeName");
        ParallelGatewayBuilder parallelGatewayBuilder = startFlowNodeBuilder.parallelGateway().name(name);
        if (Objects.isNull(flowNode.getJSONArray("branchNodes"))
                && Objects.isNull(flowNode.getJSONObject("nextNode"))) {
            return parallelGatewayBuilder.getElement().getId();
        }

        List<JSONObject> flowNodes = Optional.ofNullable(flowNode.getJSONArray("branchNodes")).map(e -> e.toJavaList(JSONObject.class)).orElse(Collections.emptyList());
        List<String> incoming = Lists.newArrayListWithCapacity(flowNodes.size());
        for (JSONObject element : flowNodes) {
            JSONObject childNode = element.getJSONObject("nextNode");
            if (Objects.isNull(childNode)) {
                incoming.add(parallelGatewayBuilder.getElement().getId());
                continue;
            }
            String id = create(parallelGatewayBuilder, parallelGatewayBuilder.getElement().getId(), childNode);
            if (StringUtils.isNotBlank(id)) {
                incoming.add(id);
            }
        }

        JSONObject childNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(childNode)) {
            // 普通结束网关
            if (incoming == null || incoming.size() == 0) {
                return create(parallelGatewayBuilder, parallelGatewayBuilder.getElement().getId(), childNode);
            } else {
                // 所有 service task 连接 end parallel gateway
                childNode.put("incoming", incoming);
                AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(parallelGatewayBuilder, incoming.get(0));
                // 1.0 先进行边连接, 暂存 nextNode
                JSONObject nextNode = childNode.getJSONObject("nextNode");
                childNode.put("nextNode", null);
                String id = create(abstractFlowNodeBuilder, abstractFlowNodeBuilder.getElement().getId(), childNode);
                for (int i = 1; i < incoming.size(); i++) {
                    moveToNode(parallelGatewayBuilder, incoming.get(i)).connectTo(id);
                }
                // 1.1 边连接完成后，在进行 nextNode 创建
                if (Objects.nonNull(nextNode)) {
                    return create(moveToNode(parallelGatewayBuilder, id), id, nextNode);
                } else {
                    return id;
                }
            }
        }
        return parallelGatewayBuilder.getElement().getId();
    }

    private static String createIntermediateCatchEvent(AbstractFlowNodeBuilder abstractFlowNodeBuilder, JSONObject flowNode) {
        String nodeName = flowNode.getString("nodeName");
        String eventType = flowNode.getString("eventType");
        if (EventType.TIMER.isEqual(eventType)) {
            String timerDefinition = flowNode.getString("timerDefinition");
            return abstractFlowNodeBuilder.intermediateCatchEvent()
                    .timerWithDuration(timerDefinition).getElement().getId();
        } else if (EventType.MESSAGE.isEqual(eventType)) {
            String messageName = flowNode.getString("messageName");
            String messageCorrelationKey = flowNode.getString("correlationKey");
            if (StringUtils.isBlank(messageName) || StringUtils.isBlank(messageCorrelationKey)) {
                throw new RuntimeException("messageName/correlationKey 不能为空");
            }
            return abstractFlowNodeBuilder.intermediateCatchEvent().name(nodeName)
                    .message(messageBuilder -> {
                        if (StringUtils.isNotBlank(messageName)) {
                            messageBuilder.name(messageName);
                        }
                        if (StringUtils.isNotBlank(messageCorrelationKey)) {
                            // The correlationKey is an expression that usually accesses a variable of the process instance
                            // that holds the correlation key of the message
                            // 默认如果没有 '=' 则自动拼上
                            if (StringUtils.startsWith(messageCorrelationKey, ZEEBE_EXPRESSION_PREFIX)) {
                                messageBuilder.zeebeCorrelationKey(messageCorrelationKey);
                            } else {
                                messageBuilder.zeebeCorrelationKeyExpression(messageCorrelationKey);
                            }
                        }
                    }).getElement().getId();
        }
        return null;
    }

    private static String createJobWorkerTask(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        Map<String, AbstractFlowNodeBuilder> map = Maps.newHashMap();
        String nodeType = flowNode.getString("nodeType");
        String nodeName = flowNode.getString("nodeName");
        String jobType = flowNode.getString("jobType");
        String jobRetries = flowNode.getString("jobRetries");
        List<String> incoming = flowNode.getJSONArray("incoming").toJavaList(String.class);
        String id = null;
        if (incoming != null && !incoming.isEmpty()) {
            // 创建 Task
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(0));
            // 自动生成id
            Method createTarget = getDeclaredMethod(abstractFlowNodeBuilder, "createTarget", Class.class);
            // 手动传入id
            //Method createTarget = getDeclaredMethod(abstractFlowNodeBuilder, "createTarget", Class.class, String.class);

            createTarget.setAccessible(true);
            Object target = createTarget.invoke(abstractFlowNodeBuilder, NodeType.TYPE_CLASS_MAP.get(nodeType));
            AbstractJobWorkerTaskBuilder<?, Task> jobWorkerTaskBuilder = jobWorkerTaskBuilder(target);
            id = jobWorkerTaskBuilder.getElement().getId();

            // 补充 header
            Map<String, String> taskHeaders = null;
            if (flowNode.containsKey("taskHeaders")) {
                taskHeaders = flowNode.getObject("taskHeaders", new TypeReference<Map<String, String>>() {
                });
            }
            handleJobWorkerTask(jobWorkerTaskBuilder, nodeName, jobType, jobRetries, taskHeaders);

            // 连接所有入度节点
            for (int i = 1; i < incoming.size(); i++) {
                abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(i));
                abstractFlowNodeBuilder.connectTo(id);
            }
        }

        // 如果当前任务还有后续任务，则遍历创建后续任务
        JSONObject nextNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(nextNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, id);
            return create(abstractFlowNodeBuilder, id, nextNode);
        } else {
            return id;
        }
    }

    private static String createReceiveTask(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        Map<String, AbstractFlowNodeBuilder> map = Maps.newHashMap();
        String nodeType = flowNode.getString("nodeType");
        String nodeName = flowNode.getString("nodeName");
        String messageName = flowNode.getString("messageName");
        String messageCorrelationKey = flowNode.getString("correlationKey");
        List<String> incoming = flowNode.getJSONArray("incoming").toJavaList(String.class);
        String id = null;
        if (incoming != null && !incoming.isEmpty()) {
            // 创建 ReceiveTask
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(0));
            // 自动生成id
            Method createTarget = getDeclaredMethod(abstractFlowNodeBuilder, "createTarget", Class.class);
            createTarget.setAccessible(true);
            Object target = createTarget.invoke(abstractFlowNodeBuilder, NodeType.TYPE_CLASS_MAP.get(nodeType));

            if (target instanceof ReceiveTask) {
                ReceiveTask receiveTask = (ReceiveTask) target;
                receiveTask.setName(nodeName);
                // set messageName and correlationKey
                receiveTask.builder().message(messageBuilder -> {
                    if (StringUtils.isNotBlank(messageName)) {
                        messageBuilder.name(messageName);
                    }
                    if (StringUtils.isNotBlank(messageCorrelationKey)) {
                        // The correlationKey is an expression that usually accesses a variable of the process instance
                        // that holds the correlation key of the message
                        // 默认如果没有 '=' 则自动拼上
                        if (StringUtils.startsWith(messageCorrelationKey, ZEEBE_EXPRESSION_PREFIX)) {
                            messageBuilder.zeebeCorrelationKey(messageCorrelationKey);
                        } else {
                            messageBuilder.zeebeCorrelationKeyExpression(messageCorrelationKey);
                        }
                    }
                });
                id = receiveTask.getId();
            }
            // 连接所有入度节点
            for (int i = 1; i < incoming.size(); i++) {
                abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(i));
                abstractFlowNodeBuilder.connectTo(id);
            }
        }

        // 如果当前任务还有后续任务，则遍历创建后续任务
        JSONObject nextNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(nextNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, id);
            return create(abstractFlowNodeBuilder, id, nextNode);
        } else {
            return id;
        }
    }

    private static String createUserTask(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        Map<String, AbstractFlowNodeBuilder> map = Maps.newHashMap();
        String nodeType = flowNode.getString("nodeType");
        String nodeName = flowNode.getString("nodeName");
        String assignee = flowNode.getString("assignee");
        String candidateGroups = flowNode.getString("candidateGroups");
        String userTaskForm = flowNode.getString("userTaskForm");
        List<String> incoming = flowNode.getJSONArray("incoming").toJavaList(String.class);
        String id = null;
        if (incoming != null && !incoming.isEmpty()) {
            // 创建 ReceiveTask
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(0));
            // 自动生成id
            Method createTarget = getDeclaredMethod(abstractFlowNodeBuilder, "createTarget", Class.class);
            createTarget.setAccessible(true);
            Object target = createTarget.invoke(abstractFlowNodeBuilder, NodeType.TYPE_CLASS_MAP.get(nodeType));

            if (target instanceof UserTask) {
                UserTask userTask = (UserTask) target;
                userTask.getId();
                userTask.setName(nodeName);
                // set assignee and candidateGroups
                UserTaskBuilder userTaskBuilder = userTask.builder();
                if (StringUtils.isNotBlank(assignee)) {
                    if (StringUtils.startsWith(assignee, ZEEBE_EXPRESSION_PREFIX)) {
                        userTaskBuilder.zeebeAssigneeExpression(assignee);
                    } else {
                        userTaskBuilder.zeebeAssignee(assignee);
                    }
                }

                if (StringUtils.isNotBlank(candidateGroups)) {
                    if (StringUtils.startsWith(candidateGroups, ZEEBE_EXPRESSION_PREFIX)) {
                        userTaskBuilder.zeebeCandidateGroupsExpression(candidateGroups);
                    } else {
                        userTaskBuilder.zeebeCandidateGroups(candidateGroups);
                    }
                }

                // 补充 header
                Map<String, String> taskHeaders = null;
                if (flowNode.containsKey("taskHeaders")) {
                    taskHeaders = flowNode.getObject("taskHeaders", new TypeReference<Map<String, String>>() {
                    });
                    taskHeaders.entrySet().stream().filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                            .forEach(entry -> userTaskBuilder.zeebeTaskHeader(entry.getKey(), entry.getValue()));
                }

                // set userTaskForm
                if (StringUtils.isNotBlank(userTaskForm)) {
                    userTaskBuilder.zeebeUserTaskForm(userTaskForm);
                }
                id = userTask.getId();
            }
            // 连接所有入度节点
            for (int i = 1; i < incoming.size(); i++) {
                abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, incoming.get(i));
                abstractFlowNodeBuilder.connectTo(id);
            }
        }

        // 如果当前任务还有后续任务，则遍历创建后续任务
        JSONObject nextNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(nextNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(startFlowNodeBuilder, id);
            return create(abstractFlowNodeBuilder, id, nextNode);
        } else {
            return id;
        }
    }

    private static String createSubProcess(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        SubProcessBuilder subProcessBuilder = startFlowNodeBuilder.subProcess();
        EmbeddedSubProcessBuilder embeddedSubProcessBuilder = subProcessBuilder.embeddedSubProcess();
        StartEventBuilder startEventBuilder = embeddedSubProcessBuilder.startEvent();
        subProcessBuilder.getElement().setName(flowNode.getString("nodeName"));
        // 遍历创建子任务
        JSONObject childNode = flowNode.getJSONObject("childNode");
        String lastNode = startEventBuilder.getElement().getId();
        if (Objects.nonNull(childNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(subProcessBuilder, startEventBuilder.getElement().getId());
            lastNode = create(abstractFlowNodeBuilder, startEventBuilder.getElement().getId(), childNode);
        }
        moveToNode(startEventBuilder, lastNode).endEvent();

        // 如果当前任务还有后续任务，则遍历创建后续任务
        String id = subProcessBuilder.getElement().getId();
        JSONObject nextNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(nextNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(subProcessBuilder, id);
            return create(abstractFlowNodeBuilder, id, nextNode);
        }
        return subProcessBuilder.getElement().getId();
    }

    private static String createCallActivity(AbstractFlowNodeBuilder startFlowNodeBuilder, JSONObject flowNode) throws InvocationTargetException, IllegalAccessException {
        CallActivityBuilder callActivityBuilder = startFlowNodeBuilder.callActivity();
        callActivityBuilder.getElement().setName(flowNode.getString("nodeName"));
        callActivityBuilder.addExtensionElement(ZeebeCalledElement.class, (ZeebeCalledElement zeebeCalledElement) -> {
            zeebeCalledElement.setProcessId(flowNode.getString("processId"));
            Boolean propagateAllChildVariablesEnabled = Optional.ofNullable(flowNode.getBoolean("propagateAllChildVariablesEnabled")).orElse(false);
            zeebeCalledElement.setPropagateAllChildVariablesEnabled(propagateAllChildVariablesEnabled);
            callActivityBuilder.addExtensionElement(zeebeCalledElement);
        });
        String id = callActivityBuilder.getElement().getId();
        JSONObject childNode = flowNode.getJSONObject("nextNode");
        if (Objects.nonNull(childNode)) {
            AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder = moveToNode(callActivityBuilder, id);
            return create(abstractFlowNodeBuilder, id, childNode);
        }
        return id;
    }

    /**
     * 获取 jobWorkerTaskBuilder
     *
     * @param target
     * @return
     */
    private static AbstractJobWorkerTaskBuilder jobWorkerTaskBuilder(Object target) {
        if (target instanceof ServiceTask) {
            ServiceTask serviceTask = (ServiceTask) target;
            return serviceTask.builder();
        } else if (target instanceof ScriptTask) {
            ScriptTask scriptTask = (ScriptTask) target;
            return scriptTask.builder();
        } else if (target instanceof BusinessRuleTask) {
            BusinessRuleTask businessRuleTask = (BusinessRuleTask) target;
            return businessRuleTask.builder();
        } else if (target instanceof SendTask) {
            SendTask sendTask = (SendTask) target;
            return sendTask.builder();
        }
        return null;
    }

    private static <B extends AbstractJobWorkerTaskBuilder<B, E>, E extends Task>
    void handleJobWorkerTask(AbstractJobWorkerTaskBuilder<B, E> jobWorkerTaskBuilder,
                             String nodeName,
                             String jobType,
                             String jobRetries,
                             Map<String, String> taskHeaders) {

        // set name
        jobWorkerTaskBuilder.name(nodeName);

        // set job type
        if (StringUtils.isNotBlank(jobType)) {
            jobWorkerTaskBuilder.zeebeJobType(jobType);
        }
        // set job retries
        if (StringUtils.isNotBlank(jobRetries)) {
            jobWorkerTaskBuilder.zeebeJobRetries(jobRetries);
        }

        // set task header
        if (Objects.nonNull(taskHeaders) && !taskHeaders.isEmpty()) {
            taskHeaders.entrySet().stream().filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                    .forEach(entry -> jobWorkerTaskBuilder.zeebeTaskHeader(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object         : 子类对象
     * @param methodName     : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    private static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (Exception ignore) {
            }
        }
        return null;
    }


    private static AbstractFlowNodeBuilder<?, ?> moveToNode(AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder, String id) {
        return abstractFlowNodeBuilder.moveToNode(id);
    }

    private static <T extends ModelElementInstance>
    T createInstance(AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder, Class<T> clazz) {
        return abstractFlowNodeBuilder.getElement().getModelInstance().newInstance(clazz);
    }
}
