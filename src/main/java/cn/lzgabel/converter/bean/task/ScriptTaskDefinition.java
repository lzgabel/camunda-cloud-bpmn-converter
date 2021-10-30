package cn.lzgabel.converter.bean.task;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈脚本任务定义〉
 *
 * @author lizhi
 * @date 2021-10-30
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class ScriptTaskDefinition extends JobWorkerDefinition {

    @Override
    public String getNodeType() {
        return "scriptTask";
    }
}
