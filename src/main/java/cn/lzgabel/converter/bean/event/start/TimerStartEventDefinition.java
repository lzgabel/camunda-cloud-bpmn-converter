package cn.lzgabel.converter.bean.event.start;

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
public class TimerStartEventDefinition extends StartEventDefinition {

    /**
     * {@link TimerDefinitionType}
     */
    @NonNull
    private String timerDefinitionType;

    @NonNull
    private String timerDefinition;


    @Override
    public String getEventType() {
        return "timer";
    }
}
