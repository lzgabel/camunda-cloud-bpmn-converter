package cn.lzgabel.converter.bean;

import cn.lzgabel.converter.bean.listener.ExecutionListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
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
@NoArgsConstructor
public class ProcessDefinition {

  private Process process;

  private BaseDefinition processNode;

  @SneakyThrows
  @Override
  public String toString() {
    return new ObjectMapper().writeValueAsString(this);
  }

  public abstract static class ProcessDefinitionBuilder<
      C extends ProcessDefinition, B extends ProcessDefinitionBuilder> {

    public ProcessDefinitionBuilder() {
      process = new Process();
      process.executionListeners = Lists.newArrayList();
    }

    public B name(final String name) {
      process.setName(name);
      return self();
    }

    public B processId(@NonNull final String processId) {
      process.setProcessId(processId);
      return self();
    }

    public B processNode(@NonNull final BaseDefinition processNode) {
      this.processNode = processNode;
      return self();
    }

    public B executionListener(@NonNull final ExecutionListener executionListener) {
      process.executionListeners.add(executionListener);
      return self();
    }
  }
}
