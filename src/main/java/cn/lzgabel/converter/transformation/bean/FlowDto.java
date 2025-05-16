package cn.lzgabel.converter.transformation.bean;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowDto {
  private String id;

  private String source;
  private String target;

  private String conditionExpression;

  private boolean defaultFlow;

  private List<ExecutionListenerDto> executionListeners;
}
