package cn.lzgabel.converter.processing.container;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.subprocess.SubProcessDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.EmbeddedSubProcessBuilder;
import io.camunda.zeebe.model.bpmn.builder.StartEventBuilder;
import io.camunda.zeebe.model.bpmn.builder.SubProcessBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

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
  public String onComplete(AbstractFlowNodeBuilder flowNodeBuilder, SubProcessDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    SubProcessBuilder subProcessBuilder = flowNodeBuilder.subProcess();
    EmbeddedSubProcessBuilder embeddedSubProcessBuilder = subProcessBuilder.embeddedSubProcess();

    // 子流程内部创建开始
    StartEventBuilder startEventBuilder = embeddedSubProcessBuilder.startEvent();
    subProcessBuilder.getElement().setName(definition.getNodeName());
    String lastNode = startEventBuilder.getElement().getId();
    // 创建子流程节点
    BaseDefinition childNode = definition.getChildNode();
    if (Objects.nonNull(childNode)) {
      lastNode =
          onCreate(
              moveToNode(subProcessBuilder, startEventBuilder.getElement().getId()), childNode);
    }
    // 子流程内部创建结束
    moveToNode(startEventBuilder, lastNode).endEvent();

    // 如果当前任务还有后续任务，则遍历创建后续任务
    String id = subProcessBuilder.getElement().getId();
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(subProcessBuilder, id), nextNode);
    }
    return subProcessBuilder.getElement().getId();
  }
}
