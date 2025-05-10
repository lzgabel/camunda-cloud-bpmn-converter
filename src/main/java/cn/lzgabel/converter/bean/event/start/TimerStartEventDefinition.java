package cn.lzgabel.converter.bean.event.start;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.TimerDefinitionType;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@SuperBuilder
public class TimerStartEventDefinition extends StartEventDefinition {

  /** {@link TimerDefinitionType} */
  @NonNull private String timerDefinitionType;

  @NonNull private String timerDefinitionExpression;

  @Override
  public String getEventType() {
    return EventType.TIMER;
  }
}
