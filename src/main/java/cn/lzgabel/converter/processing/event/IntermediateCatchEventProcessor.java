package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.TimerDefinitionType;
import cn.lzgabel.converter.bean.event.intermediate.IntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.MessageIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.TimerIntermediateCatchEventDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.ExecutionListenerBuilder;
import io.camunda.zeebe.model.bpmn.builder.IntermediateCatchEventBuilder;
import java.util.function.Consumer;

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
      final AbstractFlowNodeBuilder flowNodeBuilder,
      final IntermediateCatchEventDefinition definition) {
    return createIntermediateCatchEvent(flowNodeBuilder, definition);
  }

  private String createIntermediateCatchEvent(
      final AbstractFlowNodeBuilder flowNodeBuilder,
      final IntermediateCatchEventDefinition definition) {
    final String eventType = definition.getEventType();
    final IntermediateCatchEventBuilder intermediateCatchEventBuilder =
        (IntermediateCatchEventBuilder) createInstance(flowNodeBuilder, definition);

    // 创建监听器
    createExecutionListener(
        executionListener -> {
          final var jobType = executionListener.getJobType();
          final var retries = executionListener.getJobRetries();
          final Consumer<ExecutionListenerBuilder> builder =
              (ExecutionListenerBuilder b) ->
                  b.eventType(executionListener.getEventType()).type(jobType).retries(retries);
          intermediateCatchEventBuilder.zeebeExecutionListener(builder);
        },
        definition);

    return switch (eventType) {
      case EventType.TIMER ->
          createTimerIntermediateCatchEvent(intermediateCatchEventBuilder, definition);
      case EventType.MESSAGE ->
          createMessageIntermediateCatchEvent(intermediateCatchEventBuilder, definition);
      default -> throw new IllegalArgumentException(String.format("暂不支持: %s 类型", eventType));
    };
  }

  private String createMessageIntermediateCatchEvent(
      final IntermediateCatchEventBuilder intermediateCatchEventBuilder,
      final IntermediateCatchEventDefinition definition) {
    final MessageIntermediateCatchEventDefinition message =
        (MessageIntermediateCatchEventDefinition) definition;
    intermediateCatchEventBuilder.message(message.getMessageName());
    return definition.getNodeId();
  }

  private String createTimerIntermediateCatchEvent(
      final IntermediateCatchEventBuilder intermediateCatchEventBuilder,
      final IntermediateCatchEventDefinition definition) {

    final TimerIntermediateCatchEventDefinition timer =
        (TimerIntermediateCatchEventDefinition) definition;

    final String expression = timer.getTimerDefinitionExpression();
    switch (timer.getTimerDefinitionType()) {
      case TimerDefinitionType.DATE -> intermediateCatchEventBuilder.timerWithDate(expression);
      case TimerDefinitionType.DURATION ->
          intermediateCatchEventBuilder.timerWithDuration(expression);
      default ->
          throw new IllegalArgumentException(
              "未知 timer definition type: " + timer.getTimerDefinitionType());
    }

    return definition.getNodeId();
  }
}
