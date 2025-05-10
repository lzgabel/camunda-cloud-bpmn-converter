package cn.lzgabel.converter.bean;

import cn.lzgabel.converter.bean.listener.ExecutionListener;
import java.util.List;
import lombok.Data;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
public class Process {

  /** 执行监听器 */
  List<ExecutionListener> executionListeners;

  /** 流程id */
  private String processId;

  /** 流程名称 */
  private String name;
}
