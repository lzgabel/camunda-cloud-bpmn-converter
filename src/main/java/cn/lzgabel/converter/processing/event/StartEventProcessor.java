package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.TimerDefinitionType;
import cn.lzgabel.converter.bean.event.start.*;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.ExecutionListenerBuilder;
import io.camunda.zeebe.model.bpmn.builder.StartEventBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * 〈功能简述〉<br>
 * 〈StartEvent节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class StartEventProcessor
    implements BpmnElementProcessor<StartEventDefinition, StartEventBuilder> {

  @Override
  public String onComplete(
      final StartEventBuilder startEventBuilder, final StartEventDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    return createStartEvent(startEventBuilder, definition);
  }

  private String createStartEvent(
      final StartEventBuilder startEventBuilder, final StartEventDefinition definition) {
    startEventBuilder.id(definition.getNodeId()).name(definition.getNodeName());

    // 创建监听器
    createExecutionListener(
        executionListener -> {
          final var jobType = executionListener.getJobType();
          final var retries = executionListener.getJobRetries();
          final Consumer<ExecutionListenerBuilder> builder =
              (ExecutionListenerBuilder b) ->
                  b.eventType(executionListener.getEventType()).type(jobType).retries(retries);
          startEventBuilder.zeebeExecutionListener(builder);
        },
        definition);

    // 事件类型 timer/message 默认：none
    return switch (definition.getEventType()) {
      case EventType.TIMER ->
          createTimerStartEvent(startEventBuilder, (TimerStartEventDefinition) definition);
      case EventType.MESSAGE ->
          createMessageStartEvent(startEventBuilder, (MessageStartEventDefinition) definition);
      default -> definition.getNodeId();
    };
  }

  private String createMessageStartEvent(
      final StartEventBuilder startEventBuilder, final MessageStartEventDefinition definition) {
    startEventBuilder.message(definition.getMessageName());
    return definition.getNodeId();
  }

  private String createTimerStartEvent(
      final StartEventBuilder startEventBuilder, final TimerStartEventDefinition definition) {
    switch (definition.getTimerDefinitionType()) {
      case TimerDefinitionType.DATE -> {
        final String timerDefinition = definition.getTimerDefinitionExpression();
        startEventBuilder.timerWithDate(timerDefinition);
      }
      case TimerDefinitionType.CYCLE -> {
        final String timerDefinition = definition.getTimerDefinitionExpression();
        startEventBuilder.timerWithCycle(timerDefinition);
      }
      case TimerDefinitionType.DURATION -> {
        final String timerDefinition = definition.getTimerDefinitionExpression();
        startEventBuilder.timerWithDuration(timerDefinition);
      }
    }
    return definition.getNodeId();
  }
}
