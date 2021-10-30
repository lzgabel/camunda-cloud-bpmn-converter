package cn.lzgabel.converter.bean.event.intermediate;

import cn.lzgabel.converter.bean.event.EventDefinition;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @date 2021/11/10
 * @since 1.0.0
 */

@Data
@SuperBuilder
public abstract class IntermediateCatchEventDefinition extends EventDefinition {

    @Override
    public String getNodeType() {
        return "intermediateCatchEvent";
    }
}
