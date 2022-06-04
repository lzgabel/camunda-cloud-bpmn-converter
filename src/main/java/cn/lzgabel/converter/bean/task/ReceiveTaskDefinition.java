package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈接收任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ReceiveTaskDefinition extends BaseDefinition {

  private String messageName;

  private String correlationKey;

  @Override
  public String getNodeType() {
    return BpmnElementType.RECEIVE_TASK.getElementTypeName().get();
  }
}
