package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BpmnElementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈脚本任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ScriptTaskDefinition extends JobWorkerDefinition {

  @Override
  public String getNodeType() {
    return BpmnElementType.SCRIPT_TASK.getElementTypeName().get();
  }
}
