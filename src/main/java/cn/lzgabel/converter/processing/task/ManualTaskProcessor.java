package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.task.ManualTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.instance.ManualTask;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

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
  public String onComplete(AbstractFlowNodeBuilder flowNodeBuilder, ManualTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {

    String nodeType = definition.getNodeType();
    String nodeName = definition.getNodeName();
    ManualTask manualTask = (ManualTask) createInstance(flowNodeBuilder, nodeType);
    String id = manualTask.getId();
    manualTask.setName(nodeName);

    // 如果当前任务还有后续任务，则遍历创建后续任务
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(flowNodeBuilder, id), nextNode);
    } else {
      return id;
    }
  }
}
