package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType.BpmnElementTypeName;
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

  public Map<String, String> taskHeaders;
  private String assignee;
  private String candidateGroups;
  private String candidateUsers;
  private String userTaskForm;

  @Override
  public String getNodeType() {
    return BpmnElementTypeName.USER_TASK;
  }
}
