package cn.lzgabel.converter.transformation.transformer;

import cn.lzgabel.converter.bean.event.EventType;
import cn.lzgabel.converter.bean.event.end.NoneEndEventDefinition;
import cn.lzgabel.converter.bean.event.end.TerminateEndEventDefinition;
import cn.lzgabel.converter.transformation.ElementTransformer;
import cn.lzgabel.converter.transformation.TransformContext;
import cn.lzgabel.converter.transformation.bean.NodeDto;

public class EndEventTransformer implements ElementTransformer {
  @Override
  public void transform(final NodeDto element, final TransformContext context) {
    final var properties = element.getProperties();
    switch ((String) properties.getOrDefault("eventType", "none")) {
      case EventType.TERMINATE ->
          context.addDefinition(
              TerminateEndEventDefinition.builder()
                  .nodeName(element.getName())
                  .nodeId(element.getId())
                  .executionListeners(transformExecutionListeners(element))
                  .build());
      case EventType.NONE ->
          context.addDefinition(
              NoneEndEventDefinition.builder()
                  .nodeName(element.getName())
                  .nodeId(element.getId())
                  .executionListeners(transformExecutionListeners(element))
                  .build());
    }
  }
}
