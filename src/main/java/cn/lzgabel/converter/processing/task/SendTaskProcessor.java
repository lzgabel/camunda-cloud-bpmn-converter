package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.SendTaskDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractJobWorkerTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.SendTask;

/**
 * 〈功能简述〉<br>
 * 〈SendTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class SendTaskProcessor
    extends JobWorkerTaskProcessor<SendTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  public AbstractJobWorkerTaskBuilder getJobWorkerTaskBuilder(Object target) {
    return ((SendTask) target).builder();
  }
}
