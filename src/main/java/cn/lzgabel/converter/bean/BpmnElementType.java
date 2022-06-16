package cn.lzgabel.converter.bean;

import io.camunda.zeebe.model.bpmn.instance.*;
import io.camunda.zeebe.model.bpmn.instance.Process;
import java.util.Arrays;
import java.util.Optional;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

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
  PROCESS(BpmnElementTypeName.PROCESS, Process.class),
  SUB_PROCESS(BpmnElementTypeName.SUB_PROCESS, SubProcess.class),
  EVENT_SUB_PROCESS(null, null),

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

  // Gateways
  EXCLUSIVE_GATEWAY(BpmnElementTypeName.EXCLUSIVE_GATEWAY, ExclusiveGateway.class),
  PARALLEL_GATEWAY(BpmnElementTypeName.PARALLEL_GATEWAY, ParallelGateway.class),
  EVENT_BASED_GATEWAY(BpmnElementTypeName.EVENT_BASED_GATEWAY, EventBasedGateway.class),

  // Other
  SEQUENCE_FLOW(BpmnElementTypeName.SEQUENCE_FLOW, SequenceFlow.class),
  MULTI_INSTANCE_BODY(null, null),
  CALL_ACTIVITY(BpmnElementTypeName.CALL_ACTIVITY, CallActivity.class),
  BUSINESS_RULE_TASK(BpmnElementTypeName.BUSINESS_RULE_TASK, BusinessRuleTask.class),
  SCRIPT_TASK(BpmnElementTypeName.SCRIPT_TASK, ScriptTask.class),
  SEND_TASK(BpmnElementTypeName.SEND_TASK, SendTask.class);

  private final String elementTypeName;
  private final Class<? extends ModelElementInstance> elementTypeClass;

  BpmnElementType(
      final String elementTypeName, final Class<? extends ModelElementInstance> elementTypeClass) {
    this.elementTypeName = elementTypeName;
    this.elementTypeClass = elementTypeClass;
  }

  public Optional<String> getElementTypeName() {
    return Optional.ofNullable(this.elementTypeName);
  }

  public Optional<Class<? extends ModelElementInstance>> getElementTypeClass() {
    return Optional.ofNullable(this.elementTypeClass);
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
    static final String PROCESS = "process";
    static final String SUB_PROCESS = "subProcess";
    static final String START_EVENT = "startEvent";
    static final String INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent";
    static final String INTERMEDIATE_THROW_EVENT = "intermediateThrowEvent";
    static final String BOUNDARY_EVENT = "boundaryEvent";
    static final String END_EVENT = "endEvent";
    static final String SERVICE_TASK = "serviceTask";
    static final String RECEIVE_TASK = "receiveTask";
    static final String USER_TASK = "userTask";
    static final String MANUAL_TASK = "manualTask";
    static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
    static final String PARALLEL_GATEWAY = "parallelGateway";
    static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
    static final String SEQUENCE_FLOW = "sequenceFlow";
    static final String CALL_ACTIVITY = "callActivity";
    static final String BUSINESS_RULE_TASK = "businessRuleTask";
    static final String SCRIPT_TASK = "scriptTask";
    static final String SEND_TASK = "sendTask";
  }
}
