package cn.lzgabel.converter.transformation;

import cn.lzgabel.converter.BpmnBuilder;
import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType.BpmnElementTypeName;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.bean.ProcessDefinition;
import cn.lzgabel.converter.bean.gateway.GatewayDefinition;
import cn.lzgabel.converter.transformation.bean.FlowDto;
import cn.lzgabel.converter.transformation.bean.ProcessDefinitionDto;
import cn.lzgabel.converter.transformation.transformer.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.builder.SequenceFlowBuilder;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeExecutionListenerEventType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public final class BpmnTransformer {
  private final TransformationVisitor visitor;

  public BpmnTransformer() {
    visitor = new TransformationVisitor();
    visitor.registerHandler(BpmnElementTypeName.START_EVENT, new StartEventTransformer());
    visitor.registerHandler(BpmnElementTypeName.END_EVENT, new EndEventTransformer());
    visitor.registerHandler(BpmnElementTypeName.USER_TASK, new UserTaskTransformer());
    visitor.registerHandler(BpmnElementTypeName.SERVICE_TASK, new ServiceTaskTransformer());
    visitor.registerHandler(
        BpmnElementTypeName.EXCLUSIVE_GATEWAY, new ExclusiveGatewayTransformer());
    visitor.registerHandler(
        BpmnElementTypeName.INCLUSIVE_GATEWAY, new InclusiveGatewayTransformer());
    visitor.registerHandler(BpmnElementTypeName.PARALLEL_GATEWAY, new ParallelGatewayTransformer());
    visitor.registerHandler(
        BpmnElementTypeName.INTERMEDIATE_CATCH_EVENT, new IntermediateCatchEventTransformer());
    // todo add intermediate event
  }

  public BpmnModelInstance transformDefinitions(final ProcessDefinitionDto request) {
    final var context = new TransformContext();
    visitor.setContext(context);

    Optional.ofNullable(request.getNodes()).orElse(List.of()).forEach(visitor::visit);

    Optional.ofNullable(request.getFlows())
        .orElse(List.of())
        .forEach(flow -> handleFlow(flow, context));

    final var processDefinition =
        ProcessDefinition.builder()
            .name(request.getName())
            .processId(request.getProcessId())
            .processNode(context.start())
            .build();

    final var modelInstance = BpmnBuilder.build(processDefinition);
    handleExecutionListeners(modelInstance, request);

    return modelInstance;
  }

  private void handleExecutionListeners(
      final BpmnModelInstance modelInstance, final ProcessDefinitionDto request) {
    // Process 增加监听器
    final var process = modelInstance.getModelElementsByType(Process.class).iterator().next();
    Optional.ofNullable(request.getExecutionListeners())
        .orElse(List.of())
        .forEach(
            el ->
                process
                    .builder()
                    .zeebeExecutionListener(
                        c ->
                            c.eventType(ZeebeExecutionListenerEventType.valueOf(el.getEventType()))
                                .type(el.getJobType())
                                .retries(Optional.ofNullable(el.getJobRetries()).orElse("3"))));

    final Collection<SequenceFlow> sequenceFlows =
        modelInstance.getModelElementsByType(SequenceFlow.class);
    // sequence flow 增加监听器
    Optional.ofNullable(request.getFlows())
        .ifPresent(
            flows -> {
              final Table<String, String, FlowDto> sequenceFlowMap = HashBasedTable.create();
              flows.forEach(
                  flow -> {
                    final var source = flow.getSource();
                    final var target = flow.getTarget();
                    sequenceFlowMap.put(source, target, flow);
                  });

              sequenceFlows.forEach(
                  sequenceFlow -> {
                    final var source = sequenceFlow.getSource();
                    final var target = sequenceFlow.getTarget();
                    final var flowDto = sequenceFlowMap.get(source.getId(), target.getId());
                    if (flowDto != null) {
                      final SequenceFlowBuilder builder = sequenceFlow.builder();

                      Optional.ofNullable(flowDto.getExecutionListeners())
                          .orElse(List.of())
                          .forEach(
                              el ->
                                  builder.zeebeExecutionListener(
                                      c ->
                                          c.eventType(
                                                  ZeebeExecutionListenerEventType.valueOf(
                                                      el.getEventType()))
                                              .type(el.getJobType())
                                              .retries(
                                                  Optional.ofNullable(el.getJobRetries())
                                                      .orElse("3"))));
                    }
                  });
            });
  }

  private void handleFlow(final FlowDto flow, final TransformContext context) {
    final BaseDefinition source = context.definition(flow.getSource());
    final BaseDefinition target = context.definition(flow.getTarget());
    if (source instanceof final GatewayDefinition gatewayDefinition) {
      final List<BranchDefinition> branchDefinitions =
          Optional.ofNullable(gatewayDefinition.getBranchDefinitions())
              .orElse(Lists.newArrayList());

      final var builder = BranchDefinition.builder();
      final String conditionExpression = flow.getConditionExpression();
      if (StringUtils.isNotBlank(conditionExpression)) {
        builder.conditionExpression(conditionExpression);
      } else {
        builder.isDefault(flow.isDefaultFlow());
      }
      final BranchDefinition branchDefinition = builder.build();
      branchDefinition.setNextNode(target);

      branchDefinitions.add(branchDefinition);
      gatewayDefinition.setBranchDefinitions(branchDefinitions);
      return;
    }

    // 如果非网关节点，存在多条分支路径, 将下游节点清空
    if (Objects.nonNull(source.getNextNode())) {
      final List<BranchDefinition> branchDefinitions =
          Optional.ofNullable(source.getBranchDefinitions()).orElse(Lists.newArrayList());
      final BranchDefinition branchDefinition =
          BranchDefinition.builder().nextNode(source.getNextNode()).build();
      branchDefinitions.add(branchDefinition);
      source.setBranchDefinitions(branchDefinitions);
      source.setNextNode(null);
    }

    if (CollectionUtils.isNotEmpty(source.getBranchDefinitions())) {
      source.getBranchDefinitions().add(BranchDefinition.builder().nextNode(target).build());
      return;
    }

    // 设置下游节点
    source.setNextNode(target);
  }
}
