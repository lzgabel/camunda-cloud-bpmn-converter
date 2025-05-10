package cn.lzgabel.converter;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import cn.lzgabel.converter.bean.Process;
import cn.lzgabel.converter.bean.ProcessDefinition;
import cn.lzgabel.converter.bean.listener.ExecutionListener;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import cn.lzgabel.converter.processing.BpmnElementProcessors;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.builder.ProcessBuilder;
import io.camunda.zeebe.model.bpmn.builder.StartEventBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈基于 json 格式自动生成 bpmn 文件〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class BpmnBuilder {

  private static final BiConsumer<ProcessBuilder, List<ExecutionListener>>
      EXECUTION_LISTENER_CONSUMER =
          (builder, executionListeners) ->
              executionListeners.forEach(
                  executionListener ->
                      builder.zeebeExecutionListener(
                          c ->
                              c.eventType(executionListener.getEventType())
                                  .retries(executionListener.getJobRetries())
                                  .type(executionListener.getJobType())));

  public static BpmnModelInstance build(final ProcessDefinition processDefinition) {
    if (processDefinition == null) {
      throw new IllegalArgumentException("processDefinition must not be null");
    }

    try {
      final ProcessBuilder executableProcess = Bpmn.createExecutableProcess();

      final Process process = processDefinition.getProcess();
      Optional.ofNullable(process.getName())
          .filter(StringUtils::isNotBlank)
          .ifPresent(executableProcess::name);

      Optional.ofNullable(process.getProcessId())
          .filter(StringUtils::isNotBlank)
          .ifPresent(executableProcess::id);

      // 添加监听器
      EXECUTION_LISTENER_CONSUMER.accept(executableProcess, process.getExecutionListeners());

      final StartEventBuilder startEventBuilder = executableProcess.startEvent();
      final BaseDefinition processNode = processDefinition.getProcessNode();
      final BpmnElementProcessor<BaseDefinition, ?> processor =
          BpmnElementProcessors.getProcessor(BpmnElementType.START_EVENT);

      processor.onCreate(startEventBuilder, processNode);
      final BpmnModelInstance modelInstance = startEventBuilder.done();
      Bpmn.validateModel(modelInstance);
      return modelInstance;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException("创建失败: e=" + e.getMessage());
    }
  }
}
