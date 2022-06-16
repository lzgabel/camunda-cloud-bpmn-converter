package cn.lzgabel.converter.bean;

import cn.lzgabel.converter.bean.event.intermediate.IntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.start.EndEventDefinition;
import cn.lzgabel.converter.bean.event.start.StartEventDefinition;
import cn.lzgabel.converter.bean.gateway.ExclusiveGatewayDefinition;
import cn.lzgabel.converter.bean.gateway.ParallelGatewayDefinition;
import cn.lzgabel.converter.bean.subprocess.CallActivityDefinition;
import cn.lzgabel.converter.bean.subprocess.SubProcessDefinition;
import cn.lzgabel.converter.bean.task.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈基础元素定义〉
 *
 * @author lizhi
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "nodeType",
    visible = true)
@JsonSubTypes({
  // event
  @JsonSubTypes.Type(
      value = StartEventDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.START_EVENT),
  @JsonSubTypes.Type(
      value = EndEventDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.END_EVENT),

  // task
  @JsonSubTypes.Type(
      value = UserTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.USER_TASK),
  @JsonSubTypes.Type(
      value = ServiceTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.SERVICE_TASK),
  @JsonSubTypes.Type(
      value = SendTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.SEND_TASK),
  @JsonSubTypes.Type(
      value = ScriptTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.SCRIPT_TASK),
  @JsonSubTypes.Type(
      value = ReceiveTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.RECEIVE_TASK),
  @JsonSubTypes.Type(
      value = ManualTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.MANUAL_TASK),
  @JsonSubTypes.Type(
      value = BusinessRuleTaskDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.BUSINESS_RULE_TASK),

  // sub process
  @JsonSubTypes.Type(
      value = CallActivityDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.CALL_ACTIVITY),
  @JsonSubTypes.Type(
      value = SubProcessDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.SUB_PROCESS),

  // gateway
  @JsonSubTypes.Type(
      value = ParallelGatewayDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.PARALLEL_GATEWAY),
  @JsonSubTypes.Type(
      value = ExclusiveGatewayDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.EXCLUSIVE_GATEWAY),

  // catch event
  @JsonSubTypes.Type(
      value = IntermediateCatchEventDefinition.class,
      name = BpmnElementType.BpmnElementTypeName.INTERMEDIATE_CATCH_EVENT)
})
public abstract class BaseDefinition implements Serializable {

  /** 节点名称 */
  private String nodeName;

  /** 节点类型 */
  private String nodeType;

  /** 入度节点 */
  private List<String> incoming;

  /** 后继节点 */
  private BaseDefinition nextNode;

  public abstract String getNodeType();

  public abstract static class BaseDefinitionBuilder<
      C extends BaseDefinition, B extends BaseDefinition.BaseDefinitionBuilder<C, B>> {
    public B nodeNode(String nodeName) {
      this.nodeName = nodeName;
      return self();
    }

    public B nextNode(BaseDefinition nextNode) {
      this.nextNode = nextNode;
      return self();
    }
  }
}
