package cn.lzgabel.converter.processing.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.bean.gateway.ExclusiveGatewayDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.ExclusiveGatewayBuilder;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 * 〈功能简述〉<br>
 * 〈ExclusiveGateway节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class ExclusiveGatewayProcessor
    extends AbstractGatewayProcessor<ExclusiveGatewayDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final ExclusiveGatewayDefinition definition)
      throws InvocationTargetException, IllegalAccessException {

    final ExclusiveGatewayBuilder exclusiveGatewayBuilder =
        (ExclusiveGatewayBuilder) createInstance(flowNodeBuilder, definition);

    // 添加监听器
    createExecutionListener(exclusiveGatewayBuilder, definition);

    final String id = definition.getNodeId();
    final List<BranchDefinition> branchDefinitions = definition.getBranchDefinitions();
    if (CollectionUtils.isNotEmpty(definition.getBranchDefinitions())) {
      for (final BranchDefinition branchDefinition : branchDefinitions) {
        final BaseDefinition nextNode = branchDefinition.getNextNode();
        onCreate(moveToNode(exclusiveGatewayBuilder, id), nextNode);
        exclusiveGatewayBuilder
            .getElement()
            .getOutgoing()
            .forEach(
                sequenceFlow ->
                    conditionExpression(sequenceFlow, exclusiveGatewayBuilder, branchDefinition));
      }
    }
    return id;
  }

  private void conditionExpression(
      final SequenceFlow sequenceFlow,
      final ExclusiveGatewayBuilder exclusiveGatewayBuilder,
      final BranchDefinition condition) {
    if (condition.isDefault()) {
      exclusiveGatewayBuilder.defaultFlow(sequenceFlow);
      return;
    }
    createConditionExpression(sequenceFlow, exclusiveGatewayBuilder, condition);
  }
}
