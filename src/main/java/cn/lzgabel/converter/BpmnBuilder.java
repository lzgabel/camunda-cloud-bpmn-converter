package cn.lzgabel.converter;

import cn.lzgabel.converter.bean.BaseDefinition;
import cn.lzgabel.converter.bean.BpmnElementType;
import cn.lzgabel.converter.bean.Process;
import cn.lzgabel.converter.bean.ProcessDefinition;
import cn.lzgabel.converter.processing.BpmnElementProcessor;
import cn.lzgabel.converter.processing.BpmnElementProcessors;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.builder.AbstractBaseElementBuilder;
import io.camunda.zeebe.model.bpmn.builder.ProcessBuilder;
import io.camunda.zeebe.model.bpmn.builder.StartEventBuilder;
import io.camunda.zeebe.model.bpmn.instance.Gateway;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import io.camunda.zeebe.model.bpmn.instance.dc.Bounds;
import io.camunda.zeebe.model.bpmn.instance.di.Waypoint;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * 〈功能简述〉<br>
 * 〈基于 json 格式自动生成 bpmn 文件〉
 *
 * @author lizhi
 * @since 1.0.0
 */
public class BpmnBuilder {

  public static BpmnModelInstance build(String json) {
    return build(ProcessDefinition.of(json));
  }

  public static BpmnModelInstance build(ProcessDefinition processDefinition) {
    if (processDefinition == null) {
      return null;
    }

    try {
      ProcessBuilder executableProcess = Bpmn.createExecutableProcess();

      Process process = processDefinition.getProcess();
      Optional.ofNullable(process.getName())
          .filter(StringUtils::isNotBlank)
          .ifPresent(executableProcess::name);

      Optional.ofNullable(process.getProcessId())
          .filter(StringUtils::isNotBlank)
          .ifPresent(executableProcess::id);

      StartEventBuilder startEventBuilder = executableProcess.startEvent();
      BaseDefinition processNode = processDefinition.getProcessNode();
      BpmnElementProcessor<BaseDefinition, AbstractBaseElementBuilder> processor =
          BpmnElementProcessors.getProcessor(BpmnElementType.START_EVENT);
      String lastNode = processor.onCreate(startEventBuilder, processNode);
      processor.moveToNode(startEventBuilder, lastNode).endEvent();
      BpmnModelInstance modelInstance = correctWayPoints(startEventBuilder.done());
      Bpmn.validateModel(modelInstance);

      return modelInstance;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("创建失败: e=" + e.getMessage());
    }
  }

  private static BpmnModelInstance correctWayPoints(BpmnModelInstance model) {
    List<SequenceFlow> sequenceFlows =
        (List<SequenceFlow>) model.getModelElementsByType(SequenceFlow.class);

    for (SequenceFlow sf : sequenceFlows) {
      if (!(sf.getTarget() instanceof Gateway)) {
        continue;
      }

      // Get source and target's bounds.
      Bounds s_b =
          (Bounds) sf.getSource().getDiagramElement().getUniqueChildElementByType(Bounds.class);
      Bounds t_b =
          (Bounds) sf.getTarget().getDiagramElement().getUniqueChildElementByType(Bounds.class);

      // Calculate the new waypoints
      Waypoint wp;

      // Source and target are at the same y-coordinate
      if (s_b.getY() + s_b.getHeight() / 2 == t_b.getY() + t_b.getHeight() / 2) {
        // Clear the old waypoints
        sf.getDiagramElement().getWaypoints().clear();

        wp = model.newInstance(Waypoint.class);
        wp.setX(s_b.getX() + s_b.getWidth());
        wp.setY(s_b.getY() + s_b.getHeight() / 2);
        sf.getDiagramElement().getWaypoints().add(wp);

        wp = model.newInstance(Waypoint.class);
        wp.setX(t_b.getX());
        wp.setY(t_b.getY() + t_b.getHeight() / 2);
        sf.getDiagramElement().getWaypoints().add(wp);
      }

      // Source and target are not at the same y-coordinate
      if (s_b.getY() + s_b.getHeight() / 2 > t_b.getY() + t_b.getHeight() / 2) {
        // Clear the old waypoints
        sf.getDiagramElement().getWaypoints().clear();

        wp = model.newInstance(Waypoint.class);
        wp.setX(s_b.getX() + s_b.getWidth());
        wp.setY(s_b.getY() + s_b.getHeight() / 2);
        sf.getDiagramElement().getWaypoints().add(wp);

        wp = model.newInstance(Waypoint.class);
        wp.setX(t_b.getX() + t_b.getWidth() / 2);
        wp.setY(s_b.getY() + s_b.getHeight() / 2);
        sf.getDiagramElement().getWaypoints().add(wp);

        wp = model.newInstance(Waypoint.class);
        wp.setX(t_b.getX() + t_b.getWidth() / 2);
        wp.setY(t_b.getY() + t_b.getHeight());
        sf.getDiagramElement().getWaypoints().add(wp);
      }
    }

    return model;
  }
}
