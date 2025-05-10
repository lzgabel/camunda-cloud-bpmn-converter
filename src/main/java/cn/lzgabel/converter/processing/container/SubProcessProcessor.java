package cn.lzgabel.converter.processing.container;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.subprocess.SubProcessDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.EmbeddedSubProcessBuilder;
import io.camunda.zeebe.model.bpmn.builder.SubProcessBuilder;
import java.lang.reflect.InvocationTargetException;

/**
 * 〈功能简述〉<br>
 * 〈SubProcess节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class SubProcessProcessor
    implements BpmnElementProcessor<SubProcessDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final SubProcessDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final SubProcessBuilder subProcessBuilder =
        (SubProcessBuilder) createInstance(flowNodeBuilder, definition);
    final EmbeddedSubProcessBuilder embeddedSubProcessBuilder =
        subProcessBuilder.embeddedSubProcess();
    final BaseDefinition childNode = definition.getChildNode();
    // 创建默认开始节点
    embeddedSubProcessBuilder.startEvent().id(childNode.getNodeId()).name(childNode.getNodeName());
    onCreate(moveToNode(subProcessBuilder, childNode.getNodeId()), childNode.getNextNode());
    return definition.getNodeId();
  }
}
