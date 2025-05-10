package cn.lzgabel.converter.bean;

import io.camunda.zeebe.model.bpmn.instance.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public enum BpmnElementType {
  // Default
  UNSPECIFIED(null, null),

  // Containers
  SUB_PROCESS(BpmnElementTypeName.SUB_PROCESS, SubProcess.class),
  CALL_ACTIVITY(BpmnElementTypeName.CALL_ACTIVITY, CallActivity.class),

  // Events
  START_EVENT(BpmnElementTypeName.START_EVENT, StartEvent.class),
  INTERMEDIATE_CATCH_EVENT(
      BpmnElementTypeName.INTERMEDIATE_CATCH_EVENT, IntermediateCatchEvent.class),
  INTERMEDIATE_THROW_EVENT(
      BpmnElementTypeName.INTERMEDIATE_THROW_EVENT, IntermediateThrowEvent.class),
  BOUNDARY_EVENT(BpmnElementTypeName.BOUNDARY_EVENT, BoundaryEvent.class),
  END_EVENT(BpmnElementTypeName.END_EVENT, EndEvent.class),

  // Tasks
  SERVICE_TASK(BpmnElementTypeName.SERVICE_TASK, ServiceTask.class),
  RECEIVE_TASK(BpmnElementTypeName.RECEIVE_TASK, ReceiveTask.class),
  USER_TASK(BpmnElementTypeName.USER_TASK, UserTask.class),
  MANUAL_TASK(BpmnElementTypeName.MANUAL_TASK, ManualTask.class),
  BUSINESS_RULE_TASK(BpmnElementTypeName.BUSINESS_RULE_TASK, BusinessRuleTask.class),
  SCRIPT_TASK(BpmnElementTypeName.SCRIPT_TASK, ScriptTask.class),
  SEND_TASK(BpmnElementTypeName.SEND_TASK, SendTask.class),

  // Gateways
  EXCLUSIVE_GATEWAY(BpmnElementTypeName.EXCLUSIVE_GATEWAY, ExclusiveGateway.class),
  PARALLEL_GATEWAY(BpmnElementTypeName.PARALLEL_GATEWAY, ParallelGateway.class),
  INCLUSIVE_GATEWAY(BpmnElementTypeName.INCLUSIVE_GATEWAY, InclusiveGateway.class),
  EVENT_BASED_GATEWAY(BpmnElementTypeName.EVENT_BASED_GATEWAY, EventBasedGateway.class);

  private final String elementTypeName;
  private final Class<? extends FlowNode> elementTypeClass;

  BpmnElementType(final String elementTypeName, final Class<? extends FlowNode> elementTypeClass) {
    this.elementTypeName = elementTypeName;
    this.elementTypeClass = elementTypeClass;
  }

  public Optional<String> getElementTypeName() {
    return Optional.ofNullable(elementTypeName);
  }

  public Optional<Class<? extends FlowNode>> getElementTypeClass() {
    return Optional.ofNullable(elementTypeClass);
  }

  public boolean isEquals(final String elementTypeName) {
    return this.elementTypeName.equals(elementTypeName);
  }

  public static BpmnElementType bpmnElementTypeFor(final String elementTypeName) {
    return Arrays.stream(values())
        .filter(
            bpmnElementType ->
                bpmnElementType.elementTypeName != null
                    && bpmnElementType.elementTypeName.equals(elementTypeName))
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException("Unsupported BPMN element of type " + elementTypeName));
  }

  public static class BpmnElementTypeName {
    public static final String SUB_PROCESS = "subProcess";
    public static final String START_EVENT = "startEvent";
    public static final String INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent";
    public static final String INTERMEDIATE_THROW_EVENT = "intermediateThrowEvent";
    public static final String BOUNDARY_EVENT = "boundaryEvent";
    public static final String END_EVENT = "endEvent";
    public static final String SERVICE_TASK = "serviceTask";
    public static final String RECEIVE_TASK = "receiveTask";
    public static final String USER_TASK = "userTask";
    public static final String MANUAL_TASK = "manualTask";
    public static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
    public static final String INCLUSIVE_GATEWAY = "inclusiveGateway";
    public static final String PARALLEL_GATEWAY = "parallelGateway";
    public static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
    public static final String CALL_ACTIVITY = "callActivity";
    public static final String BUSINESS_RULE_TASK = "businessRuleTask";
    public static final String SCRIPT_TASK = "scriptTask";
    public static final String SEND_TASK = "sendTask";
  }
}
