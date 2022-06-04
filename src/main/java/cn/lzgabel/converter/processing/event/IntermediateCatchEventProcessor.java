package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.event.intermediate.IntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.MessageIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.TimerIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.start.EventType;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈IntermediateCatchEvent节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class IntermediateCatchEventProcessor
    implements BpmnElementProcessor<IntermediateCatchEventDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      AbstractFlowNodeBuilder flowNodeBuilder, IntermediateCatchEventDefinition flowNode) {
    String nodeName = flowNode.getNodeName();
    String eventType = flowNode.getEventType();
    if (EventType.TIMER.isEqual(eventType)) {
      TimerIntermediateCatchEventDefinition timer =
          (TimerIntermediateCatchEventDefinition) flowNode;
      String timerDefinition = timer.getTimerDefinition();
      return flowNodeBuilder
          .intermediateCatchEvent()
          .timerWithDuration(timerDefinition)
          .getElement()
          .getId();
    } else if (EventType.MESSAGE.isEqual(eventType)) {
      MessageIntermediateCatchEventDefinition message =
          (MessageIntermediateCatchEventDefinition) flowNode;
      String messageName = message.getMessageName();
      String messageCorrelationKey = message.getCorrelationKey();
      if (StringUtils.isBlank(messageName) || StringUtils.isBlank(messageCorrelationKey)) {
        throw new RuntimeException("messageName/correlationKey 不能为空");
      }
      return flowNodeBuilder
          .intermediateCatchEvent()
          .name(nodeName)
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
              })
          .getElement()
          .getId();
    }
    return null;
  }
}
