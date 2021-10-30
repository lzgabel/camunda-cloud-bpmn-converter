<div align="center">
<img src="./static/img/img.jpeg" width="400px"/>

<h1>Convert json to bpmn for Camunda Cloud. </h1>
</div>


### 1. NodeType
- startEvent
  - timer event 
  - message event
- serviceTask
- receiveTask
- sendTask
- scriptTask
- businessRuleTask
- subProcess
- callActivity
- intermediateCatchEvent
- parallelGateway  
- exclusiveGateway

### 2. Run
> cn.lzgabel.converter.BpmnBuilderTest

```java
public class Main {
    public static void main(String[] args) {
        
          JSONObject taskHeaders = new JSONObject();
          taskHeaders.fluentPut("a", "b").fluentPut("e", "d");
          
          ServiceTaskDefinition processNode = ServiceTaskDefinition.builder()
                  .nodeName("service task a")
                  .jobType("abc")
                  .taskHeaders(taskHeaders)
                  .jobRetries("4")
                  .build();
          
          ProcessDefinition processDefinition = ProcessDefinition.builder()
                  .process(Process.builder()
                          .processId("process-id")
                          .name("process-name")
                          .build())
                  .processNode(processNode)
                  .build();
          
          BpmnModelInstance bpmnModelInstance = BpmnBuilder.build(processDefinition);     
    } 
}
        
```
