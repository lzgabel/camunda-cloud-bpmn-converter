package cn.lzgabel.converter.bean.subprocess;

import cn.lzgabel.converter.bean.BaseDefinition;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈子流程定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class SubProcessDefinition extends BaseDefinition {

    @NonNull
    private BaseDefinition childNode;

    @Override
    public String getNodeType() {
        return "subProcess";
    }
}
