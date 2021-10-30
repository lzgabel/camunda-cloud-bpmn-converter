package cn.lzgabel.converter.bean.event.intermediate;

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
public class MessageIntermediateCatchEventDefinition extends IntermediateCatchEventDefinition {

    private String messageName;

    private String correlationKey;

    @Override
    public String getEventType() {
        return "message";
    }
}
