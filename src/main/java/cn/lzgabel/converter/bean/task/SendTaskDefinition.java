package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BpmnElementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈发送任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class SendTaskDefinition extends JobWorkerDefinition {

  @Override
  public String getNodeType() {
    return BpmnElementType.SEND_TASK.getElementTypeName().get();
  }
}
