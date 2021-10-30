package cn.lzgabel.converter.bean.gateway;

import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈排他网关定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@SuperBuilder
public class ExclusiveGatewayDefinition extends GatewayDefinition {

    @Override
    public String getNodeType() {
        return "exclusiveGateway";
    }
}
