package cn.lzgabel.converter.processing.event;

import cn.lzgabel.converter.bean.event.start.EndEventDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;

/**
 * 〈功能简述〉<br>
 * 〈EndEvent节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class EndEventProcessor
    implements BpmnElementProcessor<EndEventDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(AbstractFlowNodeBuilder builder, EndEventDefinition flowNode) {
    return builder.endEvent().getElement().getId();
  }
}
