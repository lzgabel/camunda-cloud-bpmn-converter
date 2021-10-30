package cn.lzgabel.converter.bean.gateway;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈并行网关〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class ParallelGatewayDefinition extends GatewayDefinition {

  @Override
  public String getNodeType() {
    return "parallelGateway";
  }
}
