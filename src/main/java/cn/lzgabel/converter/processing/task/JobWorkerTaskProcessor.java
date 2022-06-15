package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.task.JobWorkerDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractJobWorkerTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.Task;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈所有JobWorkerTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public abstract class JobWorkerTaskProcessor<
        E extends JobWorkerDefinition, T extends AbstractFlowNodeBuilder>
    implements BpmnElementProcessor<E, T> {

  @Override
  public String onComplete(AbstractFlowNodeBuilder flowNodeBuilder, JobWorkerDefinition definition)
      throws InvocationTargetException, IllegalAccessException {

    String nodeType = definition.getNodeType();
    String nodeName = definition.getNodeName();
    String jobType = definition.getJobType();
    String jobRetries = definition.getJobRetries();

    // 创建 Task
    AbstractJobWorkerTaskBuilder<?, Task> jobWorkerTaskBuilder =
        getJobWorkerTaskBuilder(createInstance(flowNodeBuilder, nodeType));
    String id = jobWorkerTaskBuilder.getElement().getId();

    // 补充 header
    handleJobWorkerTask(
        jobWorkerTaskBuilder, nodeName, jobType, jobRetries, definition.getTaskHeaders());

    // 如果当前任务还有后续任务，则遍历创建后续任务
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(flowNodeBuilder, id), nextNode);
    } else {
      return id;
    }
  }

  /**
   * 通过实例获取对应 jobWorkerTaskBuilder
   *
   * @param target 实例对象
   * @return jobWorkerTaskBuilder
   */
  @SuppressWarnings("unchecked")
  abstract AbstractJobWorkerTaskBuilder getJobWorkerTaskBuilder(Object target);

  private <B extends AbstractJobWorkerTaskBuilder<B, E>, E extends Task> void handleJobWorkerTask(
      AbstractJobWorkerTaskBuilder<B, E> jobWorkerTaskBuilder,
      String nodeName,
      String jobType,
      String jobRetries,
      Map<String, String> taskHeaders) {

    // set name
    jobWorkerTaskBuilder.name(nodeName);

    // set job type
    if (StringUtils.isNotBlank(jobType)) {
      jobWorkerTaskBuilder.zeebeJobType(jobType);
    }
    // set job retries
    if (StringUtils.isNotBlank(jobRetries)) {
      jobWorkerTaskBuilder.zeebeJobRetries(jobRetries);
    }

    // set task header
    Optional.ofNullable(taskHeaders).orElse(Maps.newHashMap()).entrySet().stream()
        .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
        .forEach(entry -> jobWorkerTaskBuilder.zeebeTaskHeader(entry.getKey(), entry.getValue()));
  }
}
