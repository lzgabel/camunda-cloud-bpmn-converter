package cn.lzgabel.converter.bean.task;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈系统任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class ServiceTaskDefinition extends JobWorkerDefinition {

    @Override
    public String getNodeType() {
        return "serviceTask";
    }
}
