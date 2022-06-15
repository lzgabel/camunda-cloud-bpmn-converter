package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.task.ReceiveTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.instance.ReceiveTask;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈ReceiveTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class ReceiveTaskProcessor
    implements BpmnElementProcessor<ReceiveTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      AbstractFlowNodeBuilder flowNodeBuilder, ReceiveTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    String nodeType = definition.getNodeType();
    String nodeName = definition.getNodeName();
    String messageName = definition.getMessageName();
    String messageCorrelationKey = definition.getCorrelationKey();

    // 创建 ReceiveTask
    ReceiveTask receiveTask = (ReceiveTask) createInstance(flowNodeBuilder, nodeType);
    receiveTask.setName(nodeName);
    String id = receiveTask.getId();
    // set messageName and correlationKey
    receiveTask
        .builder()
        .message(
            messageBuilder -> {
              if (StringUtils.isNotBlank(messageName)) {
                messageBuilder.name(messageName);
              }
              if (StringUtils.isNotBlank(messageCorrelationKey)) {
                // The correlationKey is an expression that usually accesses a variable of the
                // process instance
                // that holds the correlation key of the message
                // 默认如果没有 '=' 则自动拼上
                if (StringUtils.startsWith(messageCorrelationKey, ZEEBE_EXPRESSION_PREFIX)) {
                  messageBuilder.zeebeCorrelationKey(messageCorrelationKey);
                } else {
                  messageBuilder.zeebeCorrelationKeyExpression(messageCorrelationKey);
                }
              }
            });

    // 如果当前任务还有后续任务，则遍历创建后续任务
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(flowNodeBuilder, id), nextNode);
    } else {
      return id;
    }
  }
}
