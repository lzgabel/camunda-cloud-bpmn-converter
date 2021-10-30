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
public class TimerStartEventDefinition extends StartEventDefinition {

    /**
     * {@link TimerDefinitionType}
     */
    private String timerDefinitionType;


    private String timerDefinition;


    @Override
    public String getEventType() {
        return "timer";
    }
}
