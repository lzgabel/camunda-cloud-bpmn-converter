package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈用户任务定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class UserTaskDefinition extends BaseDefinition {

    private String assignee;

    private String candidateGroups;

    private String userTaskForm;

    public JSONObject taskHeaders;

    @Override
    public String getNodeType() {
        return "userTask";
    }
}
