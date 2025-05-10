package cn.lzgabel.converter.bean.listener;

import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListenerEventType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @date 2022/10/26
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@Builder
public class ExecutionListener {
  private ZeebeExecutionListenerEventType eventType;
  private String jobType;
  private String jobRetries;
}
