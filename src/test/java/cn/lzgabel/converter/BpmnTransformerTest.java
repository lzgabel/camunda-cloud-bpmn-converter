package cn.lzgabel.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import cn.lzgabel.converter.transformation.BpmnTransformer;
import cn.lzgabel.converter.transformation.bean.ExecutionListenerDto;
import cn.lzgabel.converter.transformation.bean.FlowDto;
import cn.lzgabel.converter.transformation.bean.NodeDto;
import cn.lzgabel.converter.transformation.bean.ProcessDefinitionDto;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.BpmnModelElementInstance;
import io.camunda.zeebe.model.bpmn.instance.EndEvent;
import io.camunda.zeebe.model.bpmn.instance.EventDefinition;
import io.camunda.zeebe.model.bpmn.instance.ExclusiveGateway;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.camunda.zeebe.model.bpmn.instance.InclusiveGateway;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.ParallelGateway;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import io.camunda.zeebe.model.bpmn.instance.StartEvent;
import io.camunda.zeebe.model.bpmn.instance.TimerEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListener;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListenerEventType;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListeners;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class BpmnTransformerTest {
  private static final String ELEMENT_KEY = "element_key";
  private static final String TASK_START_EXECUTION_LISTENER_JOB_TYPE = "task_el_start";
  private static final String TASK_END_EXECUTION_LISTENER_JOB_TYPE = "task_el_end";
  private static final String INCIDENT_JOB_TYPE = "task_el_incident";
  ProcessDefinitionDto process;

  @Before
  public void setUp() {
    process = ProcessDefinitionDto.builder().name("小铚测试").processId("test").build();
  }

  private void initExecutionListener(final List<NodeDto> nodes) {
    nodes.forEach(
        node ->
            node.setExecutionListeners(
                List.of(
                    ExecutionListenerDto.builder()
                        .eventType("start")
                        .jobType(TASK_START_EXECUTION_LISTENER_JOB_TYPE)
                        .jobRetries("3")
                        .build(),
                    ExecutionListenerDto.builder()
                        .eventType("end")
                        .jobType(TASK_END_EXECUTION_LISTENER_JOB_TYPE)
                        .jobRetries("3")
                        .build(),
                    ExecutionListenerDto.builder()
                        .eventType("incident")
                        .jobType(INCIDENT_JOB_TYPE)
                        .jobRetries("3")
                        .build())));
  }

  @Test
  public void shouldCreateStartEvent() {
    // given
    final var nodes =
        List.of(NodeDto.builder().id(ELEMENT_KEY).name("测试").type("startEvent").build());
    process.setNodes(nodes);
    initExecutionListener(nodes);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final StartEvent startEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(startEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(startEvent.getName()).isEqualTo("测试");
    assertExecutionListeners(startEvent);
  }

  @Test
  public void shouldCreateEndEvent() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder()
                .id(ELEMENT_KEY)
                .name("测试")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder()
                .id("flow")
                .source("start")
                .target(ELEMENT_KEY)
                .executionListeners(
                    List.of(ExecutionListenerDto.builder().eventType("start").build()))
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);
    process.setExecutionListeners(
        List.of(
            ExecutionListenerDto.builder().eventType("start").jobType("el-start").build(),
            ExecutionListenerDto.builder().eventType("end").jobType("el-end").build()));

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final EndEvent endEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(endEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(endEvent.getName()).isEqualTo("测试");
    assertExecutionListeners(endEvent);
  }

  @Test
  public void shouldCreateReviewTask() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("测试").type("userTask").build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final UserTask userTask = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(userTask.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(userTask.getName()).isEqualTo("测试");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(userTask, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  @Test
  public void shouldCreateTimerIntermediateCatchEventWithDuration() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder()
                .id(ELEMENT_KEY)
                .name("测试")
                .type("intermediateCatchEvent")
                .properties(Map.of("eventType", "timer", "type", "duration", "expression", "PT10S"))
                .build(),
            NodeDto.builder()
                .id("end")
                .name("结束")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder().source(ELEMENT_KEY).target("end").build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final IntermediateCatchEvent catchEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(catchEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(catchEvent.getName()).isEqualTo("测试");

    assertThat(modelInstance.getModelElementsByType(EndEvent.class)).hasSize(1);

    final Collection<EventDefinition> eventDefinitions = catchEvent.getEventDefinitions();
    assertThat(catchEvent.getEventDefinitions()).hasSize(1);

    final EventDefinition eventDefinition = eventDefinitions.iterator().next();
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    final TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerEventDefinition.getTimeDuration().getTextContent()).isEqualTo("PT10S");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(catchEvent, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  @Test
  public void shouldCreateTimerIntermediateCatchEventWithDurationExpression() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder()
                .id(ELEMENT_KEY)
                .name("测试")
                .type("intermediateCatchEvent")
                .properties(
                    Map.of("eventType", "timer", "type", "duration", "expression", "=duration"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final IntermediateCatchEvent catchEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(catchEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(catchEvent.getName()).isEqualTo("测试");

    final Collection<EventDefinition> eventDefinitions = catchEvent.getEventDefinitions();
    assertThat(eventDefinitions).hasSize(1);

    final EventDefinition eventDefinition = eventDefinitions.iterator().next();
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    final TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerEventDefinition.getTimeDuration().getTextContent()).isEqualTo("=duration");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(catchEvent, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  @Test
  public void shouldCreateTimerIntermediateCatchEventWithDate() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder()
                .id(ELEMENT_KEY)
                .name("测试")
                .type("intermediateCatchEvent")
                .properties(
                    Map.of(
                        "eventType", "timer", "type", "date", "expression", "2019-10-01T12:00:00Z"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final IntermediateCatchEvent catchEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(catchEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(catchEvent.getName()).isEqualTo("测试");

    final Collection<EventDefinition> eventDefinitions = catchEvent.getEventDefinitions();
    assertThat(eventDefinitions).hasSize(1);

    final EventDefinition eventDefinition = eventDefinitions.iterator().next();
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    final TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerEventDefinition.getTimeDate().getTextContent())
        .isEqualTo("2019-10-01T12:00:00Z");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(catchEvent, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  @Test
  public void shouldCreateTimerIntermediateCatchEventWithDateExpression() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder()
                .id(ELEMENT_KEY)
                .name("测试")
                .type("intermediateCatchEvent")
                .properties(Map.of("eventType", "timer", "type", "date", "expression", "=date"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final IntermediateCatchEvent catchEvent = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(catchEvent.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(catchEvent.getName()).isEqualTo("测试");

    final Collection<EventDefinition> eventDefinitions = catchEvent.getEventDefinitions();
    assertThat(eventDefinitions).hasSize(1);

    final EventDefinition eventDefinition = eventDefinitions.iterator().next();
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    final TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerEventDefinition.getTimeDate().getTextContent()).isEqualTo("=date");

    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(catchEvent, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  @Test
  public void shouldCreateExclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("exclusiveGateway").build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(exclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(exclusiveGateway.getName()).isEqualTo("fork");
    assertExecutionListeners(exclusiveGateway);
  }

  @Test
  public void shouldCreateExclusiveGatewayWithDefaultConditionIsTrue() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("exclusiveGateway").build(),
            NodeDto.builder()
                .id("end")
                .name("结束")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder().source(ELEMENT_KEY).target("end").defaultFlow(true).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(exclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(exclusiveGateway.getName()).isEqualTo("fork");

    final SequenceFlow defaultSequenceFlow = exclusiveGateway.getDefault();
    assertThat(defaultSequenceFlow).isNotNull();
    assertExecutionListeners(exclusiveGateway);
  }

  @Test
  public void shouldCreateExclusiveGatewayWithConditionExpression() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("exclusiveGateway").build(),
            NodeDto.builder()
                .id("end")
                .name("结束")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("end")
                .conditionExpression("= true")
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(exclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(exclusiveGateway.getName()).isEqualTo("fork");

    final SequenceFlow sequenceFlow = exclusiveGateway.getOutgoing().iterator().next();
    assertThat(sequenceFlow.getConditionExpression().getTextContent()).isEqualTo("= true");
    assertExecutionListeners(exclusiveGateway);
  }

  @Test
  public void shouldCreateInclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("inclusiveGateway").build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(inclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(inclusiveGateway.getName()).isEqualTo("fork");
    assertExecutionListeners(inclusiveGateway);
  }

  @Test
  public void shouldCreateInclusiveGatewayWithDefaultConditionIsTrue() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("inclusiveGateway").build(),
            NodeDto.builder()
                .id("end")
                .name("结束")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder().source(ELEMENT_KEY).target("end").defaultFlow(true).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(inclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(inclusiveGateway.getName()).isEqualTo("fork");

    final SequenceFlow defaultSequenceFlow = inclusiveGateway.getDefault();
    assertThat(defaultSequenceFlow).isNotNull();
    assertExecutionListeners(inclusiveGateway);
  }

  @Test
  public void shouldCreateInclusiveGatewayWithConditionExpression() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("inclusiveGateway").build(),
            NodeDto.builder()
                .id("end")
                .name("结束")
                .type("endEvent")
                .properties(Map.of("eventType", "terminate"))
                .build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("end")
                .conditionExpression("= true")
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(inclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(inclusiveGateway.getName()).isEqualTo("fork");

    final SequenceFlow sequenceFlow = inclusiveGateway.getOutgoing().iterator().next();
    assertThat(sequenceFlow.getConditionExpression().getTextContent()).isEqualTo("= true");
    assertExecutionListeners(inclusiveGateway);
  }

  @Test
  public void shouldCreateParallelGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("parallelGateway").build());

    final List<FlowDto> flowDtos =
        List.of(FlowDto.builder().source("start").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(parallelGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(parallelGateway.getName()).isEqualTo("fork");
    assertExecutionListeners(parallelGateway);
  }

  @Test
  public void shouldSplitOnExclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("exclusiveGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-1")
                .conditionExpression("= a > 0")
                .build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-2")
                .conditionExpression("= a <= 0")
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(exclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(exclusiveGateway.getName()).isEqualTo("fork");

    final Collection<SequenceFlow> outgoing = exclusiveGateway.getOutgoing();
    assertThat(outgoing).hasSize(2);
    assertThat(outgoing)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple(ELEMENT_KEY, "task-1"), tuple(ELEMENT_KEY, "task-2"));
  }

  @Test
  public void shouldJoinOnExclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id("fork").name("fork").type("exclusiveGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("join").type("exclusiveGateway").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target("fork").build(),
            FlowDto.builder().source("fork").target("task-1").build(),
            FlowDto.builder().source("fork").target("task-2").build(),
            FlowDto.builder().source("task-1").target(ELEMENT_KEY).build(),
            FlowDto.builder().source("task-2").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ExclusiveGateway exclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(exclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(exclusiveGateway.getName()).isEqualTo("join");

    final Collection<SequenceFlow> incoming = exclusiveGateway.getIncoming();
    assertThat(incoming).hasSize(2);
    assertThat(incoming)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple("task-1", ELEMENT_KEY), tuple("task-2", ELEMENT_KEY));
  }

  @Test
  public void shouldSplitOnInclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("fork").type("inclusiveGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-1")
                .conditionExpression("= a > 0")
                .build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-2")
                .conditionExpression("= a <= 0")
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(inclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(inclusiveGateway.getName()).isEqualTo("fork");

    final Collection<SequenceFlow> outgoing = inclusiveGateway.getOutgoing();
    assertThat(outgoing).hasSize(2);
    assertThat(outgoing)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple(ELEMENT_KEY, "task-1"), tuple(ELEMENT_KEY, "task-2"));
  }

  @Test
  public void shouldJoinOnInclusiveGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id("fork").name("fork").type("inclusiveGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("join").type("inclusiveGateway").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target("fork").build(),
            FlowDto.builder().source("fork").target("task-1").build(),
            FlowDto.builder().source("fork").target("task-2").build(),
            FlowDto.builder().source("task-1").target(ELEMENT_KEY).build(),
            FlowDto.builder().source("task-2").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final InclusiveGateway inclusiveGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(inclusiveGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(inclusiveGateway.getName()).isEqualTo("join");

    final Collection<SequenceFlow> incoming = inclusiveGateway.getIncoming();
    assertThat(incoming).hasSize(2);
    assertThat(incoming)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple("task-1", ELEMENT_KEY), tuple("task-2", ELEMENT_KEY));
  }

  @Test
  public void shouldSplitOnParallelGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("测试").type("parallelGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target(ELEMENT_KEY).build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-1")
                .conditionExpression("= a > 0")
                .build(),
            FlowDto.builder()
                .source(ELEMENT_KEY)
                .target("task-2")
                .conditionExpression("= a <= 0")
                .build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(parallelGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(parallelGateway.getName()).isEqualTo("测试");

    final Collection<SequenceFlow> outgoing = parallelGateway.getOutgoing();
    assertThat(outgoing).hasSize(2);
    assertThat(outgoing)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple(ELEMENT_KEY, "task-1"), tuple(ELEMENT_KEY, "task-2"));
  }

  @Test
  public void shouldJoinOnParallelGateway() {
    // given
    final var nodes =
        List.of(
            NodeDto.builder().id("start").name("开始").type("startEvent").build(),
            NodeDto.builder().id("fork").name("fork").type("parallelGateway").build(),
            NodeDto.builder().id("task-1").name("任务1").type("userTask").build(),
            NodeDto.builder().id("task-2").name("任务2").type("userTask").build(),
            NodeDto.builder().id(ELEMENT_KEY).name("join").type("parallelGateway").build());

    final List<FlowDto> flowDtos =
        List.of(
            FlowDto.builder().source("start").target("fork").build(),
            FlowDto.builder().source("fork").target("task-1").build(),
            FlowDto.builder().source("fork").target("task-2").build(),
            FlowDto.builder().source("task-1").target(ELEMENT_KEY).build(),
            FlowDto.builder().source("task-2").target(ELEMENT_KEY).build());

    initExecutionListener(nodes);

    process.setNodes(nodes);
    process.setFlows(flowDtos);

    // when
    final var transformer = new BpmnTransformer();
    final var modelInstance = transformer.transformDefinitions(process);

    // then
    final ParallelGateway parallelGateway = modelInstance.getModelElementById(ELEMENT_KEY);
    assertThat(parallelGateway.getId()).isEqualTo(ELEMENT_KEY);
    assertThat(parallelGateway.getName()).isEqualTo("join");

    final Collection<SequenceFlow> incoming = parallelGateway.getIncoming();
    assertThat(incoming).hasSize(2);
    assertThat(incoming)
        .extracting(r -> tuple(r.getSource().getId(), r.getTarget().getId()))
        .containsExactlyInAnyOrder(tuple("task-1", ELEMENT_KEY), tuple("task-2", ELEMENT_KEY));
  }

  public void assertExecutionListeners(final FlowElement element) {
    final ZeebeExecutionListeners zeebeExecutionListeners =
        getExtensionElement(element, ZeebeExecutionListeners.class);

    final Collection<ZeebeExecutionListener> executionListeners =
        zeebeExecutionListeners.getExecutionListeners();
    assertThat(executionListeners).hasSize(3);

    assertThat(executionListeners)
        .extracting(ZeebeExecutionListener::getEventType, ZeebeExecutionListener::getType)
        .containsSubsequence(
            tuple(ZeebeExecutionListenerEventType.start, TASK_START_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.end, TASK_END_EXECUTION_LISTENER_JOB_TYPE),
            tuple(ZeebeExecutionListenerEventType.incident, INCIDENT_JOB_TYPE));
  }

  private <T extends BpmnModelElementInstance> T getExtensionElement(
      final BaseElement element, final Class<T> typeClass) {
    final T extensionElement =
        (T) element.getExtensionElements().getUniqueChildElementByType(typeClass);
    assertThat(element).isNotNull();
    return extensionElement;
  }
}
