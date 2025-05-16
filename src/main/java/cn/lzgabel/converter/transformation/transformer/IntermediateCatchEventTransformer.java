package cn.lzgabel.converter.transformation.transformer;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.intermediate.MessageIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.TimerIntermediateCatchEventDefinition;
import cn.lzgabel.converter.transformation.ElementTransformer;
import cn.lzgabel.converter.transformation.TransformContext;
import cn.lzgabel.converter.transformation.bean.NodeDto;
import java.util.Map;

public class IntermediateCatchEventTransformer implements ElementTransformer {

  @Override
  public void transform(final NodeDto element, final TransformContext context) {
    final var properties = element.getProperties();
    if (properties == null) {
      throw new IllegalArgumentException("定时器节点属性配置不能为空");
    }

    switch (properties.get("eventType")) {
      case EventType.TIMER -> transformTimerIntermediateCatchEvent(element, properties, context);
      case EventType.MESSAGE ->
          transformMessageIntermediateCatchEvent(element, properties, context);
    }
  }

  private void transformTimerIntermediateCatchEvent(
      final NodeDto element, final Map<String, String> properties, final TransformContext context) {
    final var definition =
        TimerIntermediateCatchEventDefinition.builder()
            .nodeName(element.getName())
            .nodeId(element.getId())
            .timerDefinitionType(properties.get("type"))
            .timerDefinitionExpression(properties.get("expression"))
            .executionListeners(transformExecutionListeners(element))
            .build();
    context.addDefinition(definition);
  }

  private void transformMessageIntermediateCatchEvent(
      final NodeDto element, final Map<String, String> properties, final TransformContext context) {
    final var definition =
        MessageIntermediateCatchEventDefinition.builder()
            .nodeName(element.getName())
            .nodeId(element.getId())
            .messageName(properties.get("messageName"))
            .correlationKey(properties.get("correlationKey"))
            .executionListeners(transformExecutionListeners(element))
            .build();
    context.addDefinition(definition);
  }
}
