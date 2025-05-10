package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.JobWorkerDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.AbstractJobWorkerTaskBuilder;
import io.camunda.zeebe.model.bpmn.builder.ExecutionListenerBuilder;
import io.camunda.zeebe.model.bpmn.instance.Task;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final JobWorkerDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    // 创建 Task
    final AbstractJobWorkerTaskBuilder<?, Task> jobWorkerTaskBuilder =
        (AbstractJobWorkerTaskBuilder<?, Task>) createInstance(flowNodeBuilder, definition);

    // 补充 header、监听器
    handleJobWorkerTask(jobWorkerTaskBuilder, definition);

    return definition.getNodeId();
  }

  private <B extends AbstractJobWorkerTaskBuilder<B, E>, E extends Task> void handleJobWorkerTask(
      final AbstractJobWorkerTaskBuilder<B, E> jobWorkerTaskBuilder,
      final JobWorkerDefinition definition) {

    final String jobType = definition.getJobType();
    final String jobRetries = definition.getJobRetries();
    final Map<String, String> taskHeaders = definition.getTaskHeaders();

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

    createExecutionListener(
        executionListener -> {
          final Consumer<ExecutionListenerBuilder> builder =
              (ExecutionListenerBuilder b) ->
                  b.eventType(executionListener.getEventType())
                      .type(executionListener.getJobType())
                      .retries(executionListener.getJobRetries());
          jobWorkerTaskBuilder.zeebeExecutionListener(builder);
        },
        definition);
  }
}
