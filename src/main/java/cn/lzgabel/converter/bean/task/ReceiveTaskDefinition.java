package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈接收任务定义〉
 *
 * @author lizhi
 * @date 2021/11/10
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class ReceiveTaskDefinition extends BaseDefinition {

    private String messageName;

    private String correlationKey;

    @Override
    public String getNodeType() {
        return "receiveTask";
    }
}
