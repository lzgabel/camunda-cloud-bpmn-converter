package cn.lzgabel.converter.bean;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

/**
 * 〈功能简述〉<br>
 * 〈process流程定义〉
 *
 * @author lizhi
 * @date 2021-10-30
 * @since 1.0.0
 */
@Data
@Builder
public class ProcessDefinition {

    private Process process;

    private BaseDefinition processNode;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
