package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import cn.lzgabel.converter.bean.event.start.*;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import cn.lzgabel.converter.processing.BpmnElementProcessors;
import io.camunda.zeebe.model.bpmn.builder.StartEventBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈StartEvent节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class StartEventProcessor
    implements BpmnElementProcessor<StartEventDefinition, StartEventBuilder> {

  private static final BiConsumer<StartEventBuilder, TimerStartEventDefinition> EMPTY =
      (start, definition) -> {};
  private static final EnumMap<
          TimerDefinitionType, BiConsumer<StartEventBuilder, TimerStartEventDefinition>>
      consumers = new EnumMap<>(TimerDefinitionType.class);

  static {
    consumers.put(
        TimerDefinitionType.DATE,
        (start, definition) -> {
          String timerDefinition = definition.getTimerDefinition();
          start.timerWithDate(timerDefinition);
        });
    consumers.put(
        TimerDefinitionType.DURATION,
        (start, definition) -> {
          String timerDefinition = definition.getTimerDefinition();
          start.timerWithDuration(timerDefinition);
        });
    consumers.put(
        TimerDefinitionType.CYCLE,
        (start, definition) -> {
          String timerDefinition = definition.getTimerDefinition();
          start.timerWithCycle(timerDefinition);
        });
  }

  @Override
  public String onComplete(StartEventBuilder start, StartEventDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    // 事件类型 timer/message 默认：none
    String eventType = definition.getEventType();
    String nodeName = definition.getNodeName();
    start.name(nodeName);
    if (StringUtils.isNotBlank(eventType)) {
      if (EventType.TIMER.isEqual(eventType)) {
        TimerStartEventDefinition timer = (TimerStartEventDefinition) definition;
        consumers
            .getOrDefault(
                TimerDefinitionType.timerDefinitionOf(timer.getTimerDefinitionType()), EMPTY)
            .accept(start, timer);
      } else if (EventType.MESSAGE.isEqual(eventType)) {
        MessageStartEventDefinition message = (MessageStartEventDefinition) definition;
        String messageName = message.getMessageName();
        start.message(messageName);
      }
    }

    String id = start.getElement().getId();
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.isNull(nextNode)) {
      return id;
    }

    BpmnElementType elementType = BpmnElementType.bpmnElementTypeFor(nextNode.getNodeType());
    return BpmnElementProcessors.getProcessor(elementType)
        .onComplete(moveToNode(start, id), nextNode);
  }
}
