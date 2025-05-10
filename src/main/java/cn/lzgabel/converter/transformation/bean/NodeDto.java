package cn.lzgabel.converter.transformation.bean;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeDto {
  /** 节点 */
  private String id;

  /** 节点名称 */
  private String name;

  /** 节点类型 */
  private String type;

  /** 节点属性配置 */
  private Map<String, Object> properties;

  /** 监听器 */
  private List<ExecutionListenerDto> executionListeners;
}
