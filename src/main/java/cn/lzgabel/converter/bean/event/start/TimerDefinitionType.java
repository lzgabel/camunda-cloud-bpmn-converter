package cn.lzgabel.converter.bean.event.start;

import java.util.Arrays;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public enum TimerDefinitionType {

  /** date */
  DATE("date"),

  /** cycle */
  CYCLE("cycle"),

  /** duration */
  DURATION("duration");

  private String value;

  TimerDefinitionType(String value) {
    this.value = value;
  }

  public boolean isEqual(String value) {
    return this.value.equals(value);
  }

  public String value() {
    return this.value;
  }

  public static TimerDefinitionType timerDefinitionOf(String value) {
    return Arrays.stream(values())
        .filter(type -> type.isEqual(value))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unsupported type " + value));
  }
}
