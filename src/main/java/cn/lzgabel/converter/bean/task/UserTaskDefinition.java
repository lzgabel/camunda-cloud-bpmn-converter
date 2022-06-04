package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈用户任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class UserTaskDefinition extends BaseDefinition {

  private String assignee;

  private String candidateGroups;

  private String userTaskForm;

  public Map<String, String> taskHeaders;

  @Override
  public String getNodeType() {
    return BpmnElementType.USER_TASK.getElementTypeName().get();
  }
}
