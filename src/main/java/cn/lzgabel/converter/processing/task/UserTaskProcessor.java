package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.task.UserTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.UserTaskBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈UserTask节点类型详情设置〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class UserTaskProcessor
    implements BpmnElementProcessor<UserTaskDefinition, AbstractFlowNodeBuilder> {

  @Override
  public String onComplete(
      final AbstractFlowNodeBuilder flowNodeBuilder, final UserTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {

    final UserTaskBuilder userTaskBuilder =
        (UserTaskBuilder) createInstance(flowNodeBuilder, definition);

    final String assignee = definition.getAssignee();
    final String candidateGroups = definition.getCandidateGroups();
    final String candidateUsers = definition.getCandidateUsers();
    final String userTaskForm = definition.getUserTaskForm();

    if (StringUtils.isNotBlank(assignee)) {
      userTaskBuilder.zeebeAssigneeExpression(assignee);
    }

    if (StringUtils.isNotBlank(candidateGroups)) {
      userTaskBuilder.zeebeCandidateGroupsExpression(candidateGroups);
    }

    if (StringUtils.isNotBlank(candidateUsers)) {
      userTaskBuilder.zeebeCandidateUsersExpression(candidateUsers);
    }

    // set userTaskForm
    if (StringUtils.isNotBlank(userTaskForm)) {
      userTaskBuilder.zeebeUserTaskForm(userTaskForm);
    }

    // 补充 header
    final Map<String, String> taskHeaders =
        Optional.ofNullable(definition.getTaskHeaders()).orElse(Maps.newHashMap());
    taskHeaders.entrySet().stream()
        .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
        .forEach(entry -> userTaskBuilder.zeebeTaskHeader(entry.getKey(), entry.getValue()));

    // 添加监听器
    createExecutionListener(
        executionListener -> {
          final var executionListenerJobType = executionListener.getJobType();
          final var retries = executionListener.getJobRetries();
          userTaskBuilder.zeebeExecutionListener(
              (b) ->
                  b.eventType(executionListener.getEventType())
                      .type(executionListenerJobType)
                      .retries(retries));
        },
        definition);

    return definition.getNodeId();
  }
}
