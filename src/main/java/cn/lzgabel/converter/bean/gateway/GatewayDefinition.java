package cn.lzgabel.converter.bean.gateway;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BranchDefinition;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈网关数据定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class GatewayDefinition extends BaseDefinition {

  /** 分支节点 */
  private List<BranchDefinition> branchDefinitions;

  public abstract static class GatewayDefinitionBuilder<
          C extends GatewayDefinition, B extends GatewayDefinition.GatewayDefinitionBuilder<C, B>>
      extends BaseDefinitionBuilder<C, B> {

    public GatewayDefinitionBuilder() {
      branchDefinitions = Lists.newArrayList();
    }

    public B branchDefinition(final BranchDefinition branchDefinition) {
      branchDefinitions.add(branchDefinition);
      return self();
    }
  }
}
