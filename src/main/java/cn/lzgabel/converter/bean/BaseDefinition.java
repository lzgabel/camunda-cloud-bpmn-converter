package cn.lzgabel.converter.bean;

import cn.lzgabel.converter.bean.listener.ExecutionListener;
import cn.lzgabel.converter.bean.task.*;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈基础元素定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseDefinition implements Serializable {
  /** 节点标识 */
  private String nodeId;

  /** 节点名称 */
  private String nodeName;

  /** 节点类型 */
  private String nodeType;

  /** 入度节点 */
  private List<String> incoming;

  /** 后继节点 */
  private BaseDefinition nextNode;

  /** 分支节点 */
  private List<BranchDefinition> branchDefinitions;

  /** 执行监听器 */
  private List<ExecutionListener> executionListeners;

  public abstract String getNodeType();

  public abstract static class BaseDefinitionBuilder<
      C extends BaseDefinition, B extends BaseDefinition.BaseDefinitionBuilder<C, B>> {

    public BaseDefinitionBuilder() {
      executionListeners = Lists.newArrayList();
    }

    public B nodeNode(final String nodeName) {
      this.nodeName = nodeName;
      return self();
    }

    public B nextNode(final BaseDefinition nextNode) {
      this.nextNode = nextNode;
      return self();
    }

    public B executionlistener(final ExecutionListener listener) {
      executionListeners.add(listener);
      return self();
    }

    public B executionlistener(final Supplier<ExecutionListener> supplier) {
      executionListeners.add(supplier.get());
      return self();
    }
  }
}
