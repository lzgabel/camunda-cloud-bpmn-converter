package cn.lzgabel.converter.processing.container;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.subprocess.CallActivityDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.CallActivityBuilder;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 〈功能简述〉<br>
 * 〈CallActivity节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class CallActivityProcessor
    implements BpmnElementProcessor<CallActivityDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final CallActivityDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final CallActivityBuilder callActivityBuilder =
        (CallActivityBuilder) createInstance(flowNodeBuilder, definition);
    callActivityBuilder.getElement().setName(definition.getNodeName());
    callActivityBuilder.addExtensionElement(
        ZeebeCalledElement.class,
        (final ZeebeCalledElement zeebeCalledElement) -> {
          zeebeCalledElement.setProcessId(definition.getProcessId());
          zeebeCalledElement.setPropagateAllChildVariablesEnabled(
              definition.isPropagateAllChildVariablesEnabled());
          callActivityBuilder.addExtensionElement(zeebeCalledElement);
        });
    final String id = definition.getNodeId();
    final BaseDefinition childNode = definition.getNextNode();

    if (Objects.nonNull(childNode)) {
      return onCreate(moveToNode(callActivityBuilder, id), childNode);
    }

    return id;
  }
}
