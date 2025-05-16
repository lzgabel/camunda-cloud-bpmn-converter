package cn.lzgabel.converter.transformation.transformer;

import cn.lzgabel.converter.bean.task.ServiceTaskDefinition;
import cn.lzgabel.converter.transformation.ElementTransformer;
import cn.lzgabel.converter.transformation.TransformContext;
import cn.lzgabel.converter.transformation.bean.NodeDto;

public class ServiceTaskTransformer implements ElementTransformer {

  @Override
  public void transform(final NodeDto element, final TransformContext context) {
    final var properties = element.getProperties();
    final var definition =
        ServiceTaskDefinition.builder()
            .nodeName(element.getName())
            .nodeId(element.getId())
            .jobRetries(properties.getOrDefault("jobRetries", "3"))
            .jobType(properties.getOrDefault("jobType", "<default>"))
            .executionListeners(transformExecutionListeners(element))
            .build();
    context.addDefinition(definition);
  }
}
