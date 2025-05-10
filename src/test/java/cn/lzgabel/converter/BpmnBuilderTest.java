package cn.lzgabel.converter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import cn.lzgabel.converter.bean.*;
import cn.lzgabel.converter.bean.event.end.NoneEndEventDefinition;
import cn.lzgabel.converter.bean.event.start.NoneStartEventDefinition;
import cn.lzgabel.converter.bean.gateway.*;
import cn.lzgabel.converter.bean.listener.ExecutionListener;
import cn.lzgabel.converter.bean.task.*;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.BpmnModelElementInstance;
import io.camunda.zeebe.model.bpmn.instance.EndEvent;
import io.camunda.zeebe.model.bpmn.instance.ExclusiveGateway;
import io.camunda.zeebe.model.bpmn.instance.InclusiveGateway;
import io.camunda.zeebe.model.bpmn.instance.ParallelGateway;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.StartEvent;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListener;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListenerEventType;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListeners;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;

public class BpmnBuilderTest {
  private final Function<List<ZeebeExecutionListenerEventType>, List<ExecutionListener>>
      EXECUTION_LISTENER =
          (List<ZeebeExecutionListenerEventType> eventTypes) ->
              eventTypes.stream()
                  .map(
                      eventType ->
                          ExecutionListener.builder()
                              .jobType("task_execution_listener")
                              .eventType(eventType)
                              .build())
                  .toList();

  @Test
  public void shouldBuildProcess() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(ServiceTaskDefinition.builder().jobType("test").build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final Process process = modelInstance.getModelElementById("process-id");
    assertThat(process).isNotNull();
    assertThat(process.getId()).isEqualTo("process-id");
    assertThat(process.getName()).isEqualTo("process-name");
  }

  @Test
  public void shouldBuildProcessWithStartExecutionListener() {
    // given
    final var listener =
        ExecutionListener.builder()
            .eventType(ZeebeExecutionListenerEventType.start)
            .jobType("task_execution_listener")
            .build();

    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(ServiceTaskDefinition.builder().jobType("test").build())
            .executionListener(listener)
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final Process process = modelInstance.getModelElementById("process-id");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(process, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildProcessWithEndExecutionListener() {
    // given
    final var listener =
        ExecutionListener.builder()
            .eventType(ZeebeExecutionListenerEventType.end)
            .jobType("task_execution_listener")
            .build();

    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(ServiceTaskDefinition.builder().jobType("test").build())
            .executionListener(listener)
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final Process process = modelInstance.getModelElementById("process-id");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(process, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildStartEvent() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder().nodeId("start").nodeName("start").build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final StartEvent startEvent = modelInstance.getModelElementById("start");
    assertThat(startEvent).isNotNull();
    assertThat(startEvent.getId()).isEqualTo("start");
    assertThat(startEvent.getName()).isEqualTo("start");
  }

  @Test
  public void shouldBuildStartEventWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.start)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final StartEvent startEvent = modelInstance.getModelElementById("start");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(startEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildStartEventWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final StartEvent startEvent = modelInstance.getModelElementById("start");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(startEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildStartEventWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(
                                ZeebeExecutionListenerEventType.start,
                                ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final StartEvent startEvent = modelInstance.getModelElementById("start");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(startEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(2);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildEndEvent() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneEndEventDefinition.builder().nodeId("end").nodeName("end_name").build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final EndEvent endEvent = modelInstance.getModelElementById("end");
    assertThat(endEvent).isNotNull();
    assertThat(endEvent.getId()).isEqualTo("end");
    assertThat(endEvent.getName()).isEqualTo("end_name");
  }

  @Test
  public void shouldBuildEndEventWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneEndEventDefinition.builder()
                    .nodeId("end")
                    .nodeName("name")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.start)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final EndEvent endEvent = modelInstance.getModelElementById("end");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(endEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildEndEventWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneEndEventDefinition.builder()
                    .nodeId("end")
                    .nodeName("name")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final EndEvent endEvent = modelInstance.getModelElementById("end");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(endEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildEndEventWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneEndEventDefinition.builder()
                    .nodeId("end")
                    .nodeName("name")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(
                                ZeebeExecutionListenerEventType.start,
                                ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final EndEvent endEvent = modelInstance.getModelElementById("end");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(endEvent, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(2);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildServiceTask() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        ServiceTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .jobType("xflow_user_task")
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ServiceTask serviceTask = modelInstance.getModelElementById("task");

    final ZeebeTaskDefinition taskDefinition =
        getExtensionElement(serviceTask, ZeebeTaskDefinition.class);
    assertThat(taskDefinition.getType()).isEqualTo("xflow_user_task");
    assertThat(taskDefinition.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildServiceTaskWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        ServiceTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .jobType("xflow_user_task")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(ZeebeExecutionListenerEventType.start)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ServiceTask serviceTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(serviceTask, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildServiceTaskWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        ServiceTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .jobType("xflow_user_task")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(ZeebeExecutionListenerEventType.end)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ServiceTask serviceTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(serviceTask, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildServiceTaskWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        ServiceTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .jobType("xflow_user_task")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(
                                        ZeebeExecutionListenerEventType.start,
                                        ZeebeExecutionListenerEventType.end)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ServiceTask serviceTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(serviceTask, ZeebeExecutionListeners.class);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildUserTaskWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        UserTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(ZeebeExecutionListenerEventType.start)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final UserTask userTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(userTask, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildUserTaskWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        UserTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(ZeebeExecutionListenerEventType.end)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final UserTask userTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(userTask, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildUserTaskWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                NoneStartEventDefinition.builder()
                    .nodeId("start")
                    .nodeName("start")
                    .nextNode(
                        UserTaskDefinition.builder()
                            .nodeId("task")
                            .nodeName("task_name")
                            .executionListeners(
                                EXECUTION_LISTENER.apply(
                                    List.of(
                                        ZeebeExecutionListenerEventType.start,
                                        ZeebeExecutionListenerEventType.end)))
                            .nextNode(NoneEndEventDefinition.builder().nodeId("end").build())
                            .build())
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final UserTask userTask = modelInstance.getModelElementById("task");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(userTask, ZeebeExecutionListeners.class);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildExclusiveGateway() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ExclusiveGatewayDefinition.builder()
                    .nodeId("exclusive")
                    .nodeName("exclusive_gateway")
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById("exclusive");
    assertThat(exclusiveGateway).isNotNull();
    assertThat(exclusiveGateway.getId()).isEqualTo("exclusive");
    assertThat(exclusiveGateway.getName()).isEqualTo("exclusive_gateway");
  }

  @Test
  public void shouldBuildExclusiveGatewayWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ExclusiveGatewayDefinition.builder()
                    .nodeId("exclusive")
                    .nodeName("exclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.start)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById("exclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(exclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildExclusiveGatewayWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ExclusiveGatewayDefinition.builder()
                    .nodeId("exclusive")
                    .nodeName("exclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById("exclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(exclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildExclusiveGatewayWithTerminateExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ExclusiveGatewayDefinition.builder()
                    .nodeId("exclusive")
                    .nodeName("exclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(ZeebeExecutionListenerEventType.terminate)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById("exclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(exclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.terminate);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildExclusiveGatewayWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ExclusiveGatewayDefinition.builder()
                    .nodeId("exclusive")
                    .nodeName("exclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(
                                ZeebeExecutionListenerEventType.start,
                                ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById("exclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(exclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(2);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildInclusiveGateway() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                InclusiveGatewayDefinition.builder()
                    .nodeId("inclusive")
                    .nodeName("inclusive_gateway")
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById("inclusive");
    assertThat(inclusiveGateway).isNotNull();
    assertThat(inclusiveGateway.getId()).isEqualTo("inclusive");
    assertThat(inclusiveGateway.getName()).isEqualTo("inclusive_gateway");
  }

  @Test
  public void shouldBuildInclusiveGatewayWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                InclusiveGatewayDefinition.builder()
                    .nodeId("inclusive")
                    .nodeName("inclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.start)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById("inclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(inclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildInclusiveGatewayWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                InclusiveGatewayDefinition.builder()
                    .nodeId("inclusive")
                    .nodeName("inclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById("inclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(inclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildInclusiveGatewayWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                InclusiveGatewayDefinition.builder()
                    .nodeId("inclusive")
                    .nodeName("inclusive_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(
                                ZeebeExecutionListenerEventType.start,
                                ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById("inclusive");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(inclusiveGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(2);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildParallelGateway() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ParallelGatewayDefinition.builder()
                    .nodeId("parallel")
                    .nodeName("parallel_gateway")
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById("parallel");
    assertThat(parallelGateway).isNotNull();
    assertThat(parallelGateway.getId()).isEqualTo("parallel");
    assertThat(parallelGateway.getName()).isEqualTo("parallel_gateway");
  }

  @Test
  public void shouldBuildParallelGatewayWithStartExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ParallelGatewayDefinition.builder()
                    .nodeId("parallel")
                    .nodeName("parallel_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.start)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById("parallel");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(parallelGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildParallelGatewayWithEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ParallelGatewayDefinition.builder()
                    .nodeId("parallel")
                    .nodeName("parallel_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(List.of(ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById("parallel");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(parallelGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(1);

    final ZeebeExecutionListener executionListener =
        zeebeExecutionListeners.getExecutionListeners().iterator().next();
    assertThat(executionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(executionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(executionListener.getRetries()).isEqualTo("3");
  }

  @Test
  public void shouldBuildParallelGatewayWithStartAndEndExecutionListener() {
    // given
    final ProcessDefinition processDefinition =
        ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(
                ParallelGatewayDefinition.builder()
                    .nodeId("parallel")
                    .nodeName("parallel_gateway")
                    .executionListeners(
                        EXECUTION_LISTENER.apply(
                            List.of(
                                ZeebeExecutionListenerEventType.start,
                                ZeebeExecutionListenerEventType.end)))
                    .build())
            .build();

    // when
    final BpmnModelInstance modelInstance = BpmnBuilder.build(processDefinition);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById("parallel");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(parallelGateway, ZeebeExecutionListeners.class);
    assertThat(zeebeExecutionListeners.getExecutionListeners()).hasSize(2);

    final var iterator = zeebeExecutionListeners.getExecutionListeners().iterator();
    final ZeebeExecutionListener startExecutionListener = iterator.next();
    assertThat(startExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(startExecutionListener.getEventType())
        .isEqualTo(ZeebeExecutionListenerEventType.start);
    assertThat(startExecutionListener.getRetries()).isEqualTo("3");

    final ZeebeExecutionListener endExecutionListener = iterator.next();
    assertThat(endExecutionListener.getType()).isEqualTo("task_execution_listener");
    assertThat(endExecutionListener.getEventType()).isEqualTo(ZeebeExecutionListenerEventType.end);
    assertThat(endExecutionListener.getRetries()).isEqualTo("3");
  }

  private <T extends BpmnModelElementInstance> T getExtensionElement(
      final BaseElement element, final Class<T> typeClass) {
    final T extensionElement =
        (T) element.getExtensionElements().getUniqueChildElementByType(typeClass);
    assertThat(element).isNotNull();
    return extensionElement;
  }
}
