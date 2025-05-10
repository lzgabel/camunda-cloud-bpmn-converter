package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.end.EndEventDefinition;
import cn.lzgabel.converter.bean.event.end.MessageEndEventDefinition;
import cn.lzgabel.converter.bean.event.end.TerminateEndEventDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.EndEventBuilder;
import io.camunda.zeebe.model.bpmn.builder.ExecutionListenerBuilder;
import java.util.function.Consumer;

/**
 * 〈功能简述〉<br>
 * 〈EndEvent节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class EndEventProcessor
    implements BpmnElementProcessor<EndEventDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final EndEventDefinition definition) {
    return createEndEvent(flowNodeBuilder, definition);
  }

  private String createEndEvent(
      final AbstractFlowNodeBuilder flowNodeBuilder, final EndEventDefinition definition) {
    final EndEventBuilder endEventBuilder =
        (EndEventBuilder) createInstance(flowNodeBuilder, definition);

    // 创建监听器
    createExecutionListener(
        executionListener -> {
          final var jobType = executionListener.getJobType();
          final var retries = executionListener.getJobRetries();
          final Consumer<ExecutionListenerBuilder> builder =
              (ExecutionListenerBuilder b) ->
                  b.eventType(executionListener.getEventType()).type(jobType).retries(retries);
          endEventBuilder.zeebeExecutionListener(builder);
        },
        definition);

    // 事件类型 terminate 默认：none
    return switch (definition.getEventType()) {
      case EventType.TERMINATE ->
          createTerminateEndEvent(endEventBuilder, (TerminateEndEventDefinition) definition);
      case EventType.MESSAGE ->
          createMessageEndEvent(endEventBuilder, (MessageEndEventDefinition) definition);
      default -> definition.getNodeId();
    };
  }

  public String createTerminateEndEvent(
      final EndEventBuilder builder, final TerminateEndEventDefinition definition) {
    builder.terminate();
    return definition.getNodeId();
  }

  private String createMessageEndEvent(
      final EndEventBuilder endEventBuilder, final MessageEndEventDefinition definition) {
    endEventBuilder.message(
        consumer ->
            consumer
                .throwEventDefinitionDone()
                .zeebeJobRetries(definition.getJobRetries())
                .zeebeJobType(definition.getJobType()));
    return definition.getNodeId();
  }
}
