package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BpmnElementType.BpmnElementTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈业务规则定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class BusinessRuleTaskDefinition extends JobWorkerDefinition {

  @Override
  public String getNodeType() {
    return BpmnElementTypeName.BUSINESS_RULE_TASK;
  }
}
