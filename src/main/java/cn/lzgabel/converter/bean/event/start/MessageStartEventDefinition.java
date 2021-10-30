package cn.lzgabel.converter.bean.event.start;

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
public class MessageStartEventDefinition extends StartEventDefinition {

    private String messageName;

    @Override
    public String getEventType() {
        return "message";
    }
}
