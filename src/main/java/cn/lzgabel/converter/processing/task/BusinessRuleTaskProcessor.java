package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.BusinessRuleTaskDefinition;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractJobWorkerTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.BusinessRuleTask;

/**
 * 〈功能简述〉<br>
 * 〈BusinessRuleTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class BusinessRuleTaskProcessor
    extends JobWorkerTaskProcessor<BusinessRuleTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  AbstractJobWorkerTaskBuilder getJobWorkerTaskBuilder(Object target) {
    return ((BusinessRuleTask) target).builder();
  }
}
