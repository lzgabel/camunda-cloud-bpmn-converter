package cn.lzgabel.converter.processing.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractGatewayBuilder;
import io.camunda.zeebe.model.bpmn.builder.ExecutionListenerBuilder;
import io.camunda.zeebe.model.bpmn.instance.ConditionExpression;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public abstract class AbstractGatewayProcessor<
        E extends BaseDefinition, T extends AbstractFlowNodeBuilder>
    implements BpmnElementProcessor<E, T> {

  protected void createConditionExpression(
      final SequenceFlow sequenceFlow,
      final AbstractFlowNodeBuilder flowNodeBuilder,
      final BranchDefinition condition) {
    final String nodeName = condition.getNodeName();
    final String expression = condition.getConditionExpression();
    if (StringUtils.isBlank(sequenceFlow.getName()) && StringUtils.isNotBlank(nodeName)) {
      sequenceFlow.setName(nodeName);
    }
    // 设置条件表达式
    if (Objects.isNull(sequenceFlow.getConditionExpression())
        && StringUtils.isNotBlank(expression)) {
      final ConditionExpression conditionExpression =
          createInstance(flowNodeBuilder, ConditionExpression.class);
      conditionExpression.setTextContent(expression);
      sequenceFlow.setConditionExpression(conditionExpression);
    }
  }

  @SuppressWarnings("unchecked")
  public void createExecutionListener(
      final AbstractGatewayBuilder gatewayBuilder, final BaseDefinition definition) {
    if (Objects.isNull(definition.getExecutionListeners())) {
      return;
    }

    createExecutionListener(
        executionListener -> {
          final var jobType = executionListener.getJobType();
          final var retries = executionListener.getJobRetries();
          final Consumer<ExecutionListenerBuilder> builder =
              (ExecutionListenerBuilder b) ->
                  b.eventType(executionListener.getEventType()).type(jobType).retries(retries);
          gatewayBuilder.zeebeExecutionListener(builder);
        },
        definition);
  }

  private <T extends ModelElementInstance> T createInstance(
      final AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder, final Class<T> clazz) {
    return abstractFlowNodeBuilder.getElement().getModelInstance().newInstance(clazz);
  }
}
