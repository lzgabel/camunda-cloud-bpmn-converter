package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.ServiceTaskDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractJobWorkerTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;

/**
 * 〈功能简述〉<br>
 * 〈ServiceTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class ServiceTaskProcessor
    extends JobWorkerTaskProcessor<ServiceTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  public AbstractJobWorkerTaskBuilder getJobWorkerTaskBuilder(Object target) {
    return ((ServiceTask) target).builder();
  }
}
