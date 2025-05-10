package cn.lzgabel.converter.processing.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.bean.gateway.ParallelGatewayDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.ParallelGatewayBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 * 〈功能简述〉<br>
 * 〈ParallelGateway节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class ParallelGatewayProcessor
    extends AbstractGatewayProcessor<ParallelGatewayDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final ParallelGatewayDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final ParallelGatewayBuilder parallelGatewayBuilder =
        (ParallelGatewayBuilder) createInstance(flowNodeBuilder, definition);

    // 添加监听器
    createExecutionListener(parallelGatewayBuilder, definition);

    final String id = definition.getNodeId();
    final List<BranchDefinition> branchDefinitions = definition.getBranchDefinitions();
    if (CollectionUtils.isNotEmpty(branchDefinitions)) {
      for (final BranchDefinition branchDefinition : branchDefinitions) {
        final BaseDefinition nextNode = branchDefinition.getNextNode();
        onCreate(moveToNode(parallelGatewayBuilder, id), nextNode);
      }
    }

    return id;
  }
}
