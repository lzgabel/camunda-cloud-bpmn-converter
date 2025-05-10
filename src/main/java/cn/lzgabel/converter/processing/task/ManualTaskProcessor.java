package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.ManualTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import java.lang.reflect.InvocationTargetException;

/**
 * 〈功能简述〉<br>
 * 〈ManualTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class ManualTaskProcessor
    implements BpmnElementProcessor<ManualTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final ManualTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    createInstance(flowNodeBuilder, definition);
    return definition.getNodeId();
  }
}
