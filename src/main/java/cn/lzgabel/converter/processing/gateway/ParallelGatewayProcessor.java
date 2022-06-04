package cn.lzgabel.converter.processing.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.gateway.BranchNode;
import cn.lzgabel.converter.bean.gateway.ParallelGatewayDefinition;
import com.google.common.collect.Lists;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.ParallelGatewayBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
      AbstractFlowNodeBuilder flowNodeBuilder, ParallelGatewayDefinition flowNode)
      throws InvocationTargetException, IllegalAccessException {
    String name = flowNode.getNodeName();
    ParallelGatewayBuilder parallelGatewayBuilder = flowNodeBuilder.parallelGateway().name(name);
    List<BranchNode> branchNodes = flowNode.getBranchNodes();
    if (CollectionUtils.isEmpty(branchNodes) && Objects.isNull(flowNode.getNextNode())) {
      return parallelGatewayBuilder.getElement().getId();
    }

    List<String> incoming = Lists.newArrayListWithCapacity(branchNodes.size());
    for (BranchNode branchNode : branchNodes) {
      BaseDefinition childNode = branchNode.getNextNode();
      if (Objects.isNull(childNode)) {
        incoming.add(parallelGatewayBuilder.getElement().getId());
        continue;
      }
      String id =
          onCreate(
              moveToNode(parallelGatewayBuilder, parallelGatewayBuilder.getElement().getId()),
              childNode);
      if (StringUtils.isNotBlank(id)) {
        incoming.add(id);
      }
    }

    String id = parallelGatewayBuilder.getElement().getId();
    BaseDefinition nextNode = flowNode.getNextNode();
    if (Objects.nonNull(nextNode)) {
      nextNode.setIncoming(incoming);
      return merge(parallelGatewayBuilder, id, Collections.emptyList(), nextNode);
    }
    return id;
  }
}
