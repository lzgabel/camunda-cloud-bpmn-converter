package cn.lzgabel.converter.transformation;

import cn.lzgabel.converter.bean.listener.ExecutionListener;
import cn.lzgabel.converter.transformation.bean.NodeDto;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListenerEventType;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public interface ElementTransformer {

  default List<ExecutionListener> transformExecutionListeners(final NodeDto element) {
    if (CollectionUtils.isEmpty(element.getExecutionListeners())) {
      return Collections.emptyList();
    }

    return element.getExecutionListeners().stream()
        .map(
            e ->
                ExecutionListener.builder()
                    .jobType(e.getJobType())
                    .jobRetries(e.getJobRetries())
                    .eventType(ZeebeExecutionListenerEventType.valueOf(e.getEventType()))
                    .build())
        .toList();
  }

  void transform(final NodeDto element, final TransformContext context);
}
