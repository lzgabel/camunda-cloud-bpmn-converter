package cn.lzgabel.converter.processing.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.bean.gateway.InclusiveGatewayDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.InclusiveGatewayBuilder;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 * 〈功能简述〉<br>
 * 〈InclusiveGateway节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class InclusiveGatewayProcessor
    extends AbstractGatewayProcessor<InclusiveGatewayDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final InclusiveGatewayDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final InclusiveGatewayBuilder inclusiveGatewayBuilder =
        (InclusiveGatewayBuilder) createInstance(flowNodeBuilder, definition);

    // 添加监听器
    createExecutionListener(inclusiveGatewayBuilder, definition);

    final String id = definition.getNodeId();
    final List<BranchDefinition> branchDefinitions = definition.getBranchDefinitions();
    if (CollectionUtils.isNotEmpty(definition.getBranchDefinitions())) {
      for (final BranchDefinition branchDefinition : branchDefinitions) {
        final BaseDefinition nextNode = branchDefinition.getNextNode();
        onCreate(moveToNode(inclusiveGatewayBuilder, id), nextNode);
        inclusiveGatewayBuilder
            .getElement()
            .getOutgoing()
            .forEach(
                sequenceFlow ->
                    conditionExpression(sequenceFlow, inclusiveGatewayBuilder, branchDefinition));
      }
    }
    return id;
  }

  private void conditionExpression(
      final SequenceFlow sequenceFlow,
      final InclusiveGatewayBuilder inclusiveGatewayBuilder,
      final BranchDefinition condition) {
    if (condition.isDefault()) {
      inclusiveGatewayBuilder.defaultFlow(sequenceFlow);
    }

    createConditionExpression(sequenceFlow, inclusiveGatewayBuilder, condition);
  }
}
