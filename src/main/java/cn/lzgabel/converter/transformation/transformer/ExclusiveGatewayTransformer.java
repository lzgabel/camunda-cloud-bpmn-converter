package cn.lzgabel.converter.transformation.transformer;

import cn.lzgabel.converter.bean.gateway.ExclusiveGatewayDefinition;
import cn.lzgabel.converter.transformation.ElementTransformer;
import cn.lzgabel.converter.transformation.TransformContext;
import cn.lzgabel.converter.transformation.bean.NodeDto;

public class ExclusiveGatewayTransformer implements ElementTransformer {

  @Override
  public void transform(final NodeDto element, final TransformContext context) {
    final var definition =
        ExclusiveGatewayDefinition.builder()
            .nodeName(element.getName())
            .nodeId(element.getId())
            .executionListeners(transformExecutionListeners(element))
            .build();
    context.addDefinition(definition);
  }
}
