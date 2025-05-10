package cn.lzgabel.converter.bean.event.end;

import cn.lzgabel.converter.bean.BpmnElementType.BpmnElementTypeName;
import cn.lzgabel.converter.bean.event.EventDefinition;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class EndEventDefinition extends EventDefinition {

  @Override
  public String getNodeType() {
    return BpmnElementTypeName.END_EVENT;
  }
}
