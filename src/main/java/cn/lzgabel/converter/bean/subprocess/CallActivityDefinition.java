package cn.lzgabel.converter.bean.subprocess;

import cn.lzgabel.converter.bean.BaseDefinition;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈调用活动定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class CallActivityDefinition extends BaseDefinition {

    /**
     * 子流程 id
     */
    @NonNull
    private String processId;

    /**
     * 子流程变量是否向上传播
     */
    private boolean propagateAllChildVariablesEnabled;

    @Override
    public String getNodeType() {
        return "callActivity";
    }
}
