package cn.lzgabel.converter.processing.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import cn.lzgabel.converter.bean.task.UserTaskDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import com.google.common.collect.Maps;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.builder.UserTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

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
  public String onComplete(AbstractFlowNodeBuilder flowNodeBuilder, UserTaskDefinition definition)
      throws InvocationTargetException, IllegalAccessException {

    String nodeType = definition.getNodeType();
    String nodeName = definition.getNodeName();
    String assignee = definition.getAssignee();
    String candidateGroups = definition.getCandidateGroups();
    String userTaskForm = definition.getUserTaskForm();
    // 创建 UserTask
    // 自动生成id
    Method createTarget = getDeclaredMethod(flowNodeBuilder, "createTarget", Class.class);
    createTarget.setAccessible(true);
    Class<? extends ModelElementInstance> clazz =
        BpmnElementType.bpmnElementTypeFor(nodeType)
            .getElementTypeClass()
            .orElseThrow(
                () -> new RuntimeException("Unsupported BPMN element of type " + nodeType));
    Object target = createTarget.invoke(flowNodeBuilder, clazz);

    UserTask userTask = (UserTask) target;
    userTask.setName(nodeName);
    // set assignee and candidateGroups
    UserTaskBuilder userTaskBuilder = userTask.builder();
    if (StringUtils.isNotBlank(assignee)) {
      if (StringUtils.startsWith(assignee, ZEEBE_EXPRESSION_PREFIX)) {
        userTaskBuilder.zeebeAssigneeExpression(assignee);
      } else {
        userTaskBuilder.zeebeAssignee(assignee);
      }
    }

    if (StringUtils.isNotBlank(candidateGroups)) {
      if (StringUtils.startsWith(candidateGroups, ZEEBE_EXPRESSION_PREFIX)) {
        userTaskBuilder.zeebeCandidateGroupsExpression(candidateGroups);
      } else {
        userTaskBuilder.zeebeCandidateGroups(candidateGroups);
      }
    }

    // 补充 header
    Map<String, String> taskHeaders =
        Optional.ofNullable(definition.getTaskHeaders()).orElse(Maps.newHashMap());
    taskHeaders.entrySet().stream()
        .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
        .forEach(entry -> userTaskBuilder.zeebeTaskHeader(entry.getKey(), entry.getValue()));

    // set userTaskForm
    if (StringUtils.isNotBlank(userTaskForm)) {
      userTaskBuilder.zeebeUserTaskForm(userTaskForm);
    }
    String id = userTask.getId();

    // 如果当前任务还有后续任务，则遍历创建后续任务
    BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(flowNodeBuilder, id), nextNode);
    } else {
      return id;
    }
  }
}
