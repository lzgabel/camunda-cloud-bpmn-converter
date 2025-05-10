package cn.lzgabel.converter.bean.event.end;

import cn.lzgabel.converter.bean.event.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class TerminateEndEventDefinition extends EndEventDefinition {

  @Override
  public String getEventType() {
    return EventType.TERMINATE;
  }
}
