package cn.lzgabel.converter.bean.event.intermediate;

import lombok.Data;
import lombok.NonNull;
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
public class MessageIntermediateCatchEventDefinition extends IntermediateCatchEventDefinition {

    @NonNull
    private String messageName;

    @NonNull
    private String correlationKey;

    @Override
    public String getEventType() {
        return "message";
    }
}
