package cn.lzgabel.converter.processing;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import cn.lzgabel.converter.bean.BranchDefinition;
import cn.lzgabel.converter.bean.gateway.GatewayDefinition;
import cn.lzgabel.converter.bean.listener.ExecutionListener;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.camunda.zeebe.model.bpmn.instance.BpmnModelElementInstance;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.collections.CollectionUtils;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * 〈功能简述〉<br>
 * 〈完成基于 JSON 格式转 BPMN 元素业务逻辑转换〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public interface BpmnElementProcessor<E extends BaseDefinition, T extends AbstractFlowNodeBuilder> {

  String ZEEBE_EXPRESSION_PREFIX = "=";

  /**
   * 创建新的节点
   *
   * @param flowNodeBuilder builder
   * @param definition 流程节点参数
   * @return 最后一个节点id
   * @throws InvocationTargetException invocationTargetException
   * @throws IllegalAccessException illegalAccessException
   */
  default String onCreate(
      final AbstractFlowNodeBuilder flowNodeBuilder, final BaseDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final BpmnModelElementInstance element = flowNodeBuilder.getElement();
    final ModelInstance modelInstance = element.getModelInstance();
    final ModelElementInstance model = modelInstance.getModelElementById(definition.getNodeId());

    if (Objects.nonNull(model)) {
      flowNodeBuilder.connectTo(definition.getNodeId());
      return definition.getNodeId();
    }

    final String nodeType = definition.getNodeType();
    final BpmnElementType elementType = BpmnElementType.bpmnElementTypeFor(nodeType);
    final BpmnElementProcessor<BaseDefinition, AbstractFlowNodeBuilder> processor =
        BpmnElementProcessors.getProcessor(elementType);
    processor.onComplete(flowNodeBuilder, definition);

    return finalizeCompletion(flowNodeBuilder, definition);
  }

  /**
   * 完成当前节点详情设置
   *
   * @param flowNodeBuilder builder
   * @param definition 流程节点参数
   * @return 最后一个节点id
   * @throws InvocationTargetException invocationTargetException
   * @throws IllegalAccessException illegalAccessException
   */
  String onComplete(T flowNodeBuilder, E definition)
      throws InvocationTargetException, IllegalAccessException;

  /**
   * 完成后继节点创建
   *
   * @param flowNodeBuilder builder
   * @param definition 流程节点参数
   * @throws InvocationTargetException invocationTargetException
   * @throws IllegalAccessException illegalAccessException
   */
  default String finalizeCompletion(
      final AbstractFlowNodeBuilder flowNodeBuilder, final BaseDefinition definition)
      throws InvocationTargetException, IllegalAccessException {
    final String id = definition.getNodeId();

    // 如果还有后续任务，则遍历创建后续任务
    final BaseDefinition nextNode = definition.getNextNode();
    if (Objects.nonNull(nextNode)) {
      return onCreate(moveToNode(flowNodeBuilder, id), nextNode);
    }

    // 非网关节点，且存在多分支出度情况
    final List<BranchDefinition> branchDefinitions = definition.getBranchDefinitions();
    if (!(definition instanceof GatewayDefinition)
        && CollectionUtils.isNotEmpty(branchDefinitions)) {
      for (final BranchDefinition branchDefinition : branchDefinitions) {
        onCreate(moveToNode(flowNodeBuilder, id), branchDefinition.getNextNode());
      }
      return id;
    }
    return id;
  }

  /**
   * 循环向上转型, 获取对象的 DeclaredMethod
   *
   * @param object : 子类对象
   * @param methodName : 父类中的方法名
   * @param parameterTypes : 父类中的方法参数类型
   * @return 父类中的方法对象
   */
  default Method getDeclaredMethod(
      final Object object, final String methodName, final Class<?>... parameterTypes) {
    Method method;
    for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
      try {
        method = clazz.getDeclaredMethod(methodName, parameterTypes);
        return method;
      } catch (final Exception ignore) {
      }
    }
    return null;
  }

  /**
   * 移动到指定节点
   *
   * @param flowNodeBuilder builder
   * @param id 目标节点位移标识
   * @return 目标节点类型 builder
   */
  default AbstractFlowNodeBuilder<?, ?> moveToNode(
      final AbstractFlowNodeBuilder<?, ?> flowNodeBuilder, final String id) {
    return flowNodeBuilder.moveToNode(id);
  }

  /**
   * 创建指定类型实例
   *
   * @param flowNodeBuilder builder
   * @param definition 节点
   * @return 指定类型实例 builder
   */
  default AbstractFlowNodeBuilder createInstance(
      final AbstractFlowNodeBuilder<?, ?> flowNodeBuilder, final BaseDefinition definition) {
    final Method createTarget =
        getDeclaredMethod(flowNodeBuilder, "createTarget", Class.class, String.class);
    try {
      final var nodeType = definition.getNodeType();
      createTarget.setAccessible(true);
      final Class<? extends FlowNode> clazz =
          BpmnElementType.bpmnElementTypeFor(nodeType)
              .getElementTypeClass()
              .orElseThrow(
                  () -> new RuntimeException("Unsupported BPMN element of type: " + nodeType));

      final var instance =
          clazz.cast(createTarget.invoke(flowNodeBuilder, clazz, definition.getNodeId()));
      instance.setName(definition.getNodeName());
      return instance.builder();
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(
          String.format("创建指定流程实施失败: %s", definition.getNodeId()), e);
    }
  }

  default void createExecutionListener(
      final Consumer<ExecutionListener> consumer, final BaseDefinition definition) {
    if (Objects.isNull(definition.getExecutionListeners())) {
      return;
    }
    definition.getExecutionListeners().stream().filter(Objects::nonNull).forEach(consumer::accept);
  }
}
