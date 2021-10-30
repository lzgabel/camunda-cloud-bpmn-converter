package cn.lzgabel.converter.bean.event;

import cn.lzgabel.converter.bean.BaseDefinition;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@SuperBuilder
public abstract class EventDefinition extends BaseDefinition {

    private String eventType;

    public abstract String getEventType();
}
