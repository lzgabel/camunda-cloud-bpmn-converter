package cn.lzgabel.converter.transformation.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionListenerDto {
  private String eventType;
  private String jobType;
  private String jobRetries;
}
