package cn.lzgabel.converter.bean.gateway;

import cn.lzgabel.converter.bean.BpmnElementType.BpmnElementTypeName;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈包容网关〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@SuperBuilder
@NoArgsConstructor
public class InclusiveGatewayDefinition extends GatewayDefinition {

  @Override
  public String getNodeType() {
    return BpmnElementTypeName.INCLUSIVE_GATEWAY;
  }
}
