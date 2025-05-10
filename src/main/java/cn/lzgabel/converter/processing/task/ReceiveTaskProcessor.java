package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.ReceiveTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.ReceiveTaskBuilder;
import java.lang.reflect.InvocationTargetException;
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
      final AbstractFlowNodeBuilder flowNodeBuilder, final ReceiveTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final ReceiveTaskBuilder receiveTaskBuilder =
        (ReceiveTaskBuilder) createInstance(flowNodeBuilder, definition);

    final String messageName = definition.getMessageName();
    final String messageCorrelationKey = definition.getCorrelationKey();

    receiveTaskBuilder.message(
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

    return definition.getNodeId();
  }
}
