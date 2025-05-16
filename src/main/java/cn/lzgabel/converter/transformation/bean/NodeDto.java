package cn.lzgabel.converter.transformation.bean;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDto {
  /** 节点 */
  private String id;

  /** 节点名称 */
  private String name;

  /** 节点类型 */
  private String type;

  /** 节点属性配置 */
  private Map<String, String> properties;

  /** 监听器 */
  private List<ExecutionListenerDto> executionListeners;
}
