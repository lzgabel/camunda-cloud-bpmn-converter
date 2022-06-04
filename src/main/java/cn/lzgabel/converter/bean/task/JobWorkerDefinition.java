package cn.lzgabel.converter.bean.task;

import cn.lzgabel.converter.bean.BaseDefinition;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈JobWorker定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class JobWorkerDefinition extends BaseDefinition {

  @NonNull private String jobType;

  private String jobRetries = "3";

  public Map<String, String> taskHeaders;
}
