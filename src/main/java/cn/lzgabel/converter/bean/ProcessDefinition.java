package cn.lzgabel.converter.bean;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈process流程定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
public class ProcessDefinition {

    private Process process;

    private BaseDefinition processNode;

    public abstract static class ProcessDefinitionBuilder<C extends ProcessDefinition, B extends ProcessDefinition.ProcessDefinitionBuilder> {

        public ProcessDefinitionBuilder() {
            process = new Process();
        }

        public B name(String name) {
            process.setName(name);
            return self();
        }

        public B processId(@NonNull String processId) {
            process.setProcessId(processId);
            return self();
        }

        public B processNode(@NonNull BaseDefinition processNode) {
            this.processNode = processNode;
            return self();
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
