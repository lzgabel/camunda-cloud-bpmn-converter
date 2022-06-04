<div align="center">
<img src="./static/img/img.jpeg" width="400px"/>

<h1>Convert json to bpmn for Camunda Cloud. </h1>
</div>

## 技术方案

### 1. 节点类型（nodeType)

- serviceTask
  - 系统任务
- parallelGateway
  - 并行网关
- exclusiveGateway
  - 排他网关
- subProcess
  - 嵌入子流程
- callActivity
  - 调用活动
- sendTask
  - 发送任务
- receiveTask
  - 接收任务
- scriptTask
  - 脚本执行任务
- businessRuleTask
  - 业务规则任务

### 2. 数据结构

#### 2.0 `startEvent`

##### 2.0.0 `noneEvent`

> 默认空事件

##### 2.0.1 `timerEvent`

- `timerDefinitionType`
  - `date` 固定时间
  - `cycle` 循环时间
- `timerDefinition` 时间格式见 [ISO_8601](https://en.wikipedia.org/wiki/ISO_8601)
  - `date`
    - `2019-10-01T12:00:00+08:00`
  - `cycle`
    - `R/PT1M` 每分钟
    - `R5/PT1M` 每分钟，共执行5次

```json
{ 
  "nodeName":"开始",
  "nodeType":"startEvent",
  "eventType":"timer",
  "timerDefinitionType":"date/cycle",
  "timerDefinition":"2019-10-01T12:00:00+08:00"
}
```

##### 2.0.2 `messageEvent`

> [https://docs.camunda.io/docs/reference/bpmn-processes/message-events/message-events](https://docs.camunda.io/docs/reference/bpmn-processes/message-events/message-events)
>
> If the `correlationKey` of a message is empty then it will always create a new process instance and does not check if an instance is already active.

- `messageName`

```json
{ 
  "nodeName":"开始",
  "nodeType":"startEvent",
  "eventType":"message",
  "messageName":"test-message-name"
}
```

#### 2.1 `serviceTask`

> [https://docs.camunda.io/docs/reference/bpmn-processes/service-tasks/service-tasks](https://docs.camunda.io/docs/reference/bpmn-processes/service-tasks/service-tasks)

- `jobType` 若不带 `=` 前缀，则默认会拼接 `namespace`
  - 例如：
    - 入参： `jobType: abc` ， `namespace: mediax-media-manage`
    - 引擎最终会去调用 `mediax-media-manage.abc` 任务
- `jobType` 若带 `=` 前缀，则不会拼接 `namespace`
  - 场景分两种
    - 指定静态变量：`jobType: '="abc"'`
      - 引擎最终会去激活 `abc` 任务
    - 动态变量，由流程变量决定: `jobType: '=dynamic-job-type'`
      - 根据流程变量中 `dynamic-job-type` 值，引擎最终决定调用 `${dynamic-job-type}` 任务

```json
{
    "nodeName":"审核人1",
    "nodeType":"serviceTask",
    "jobType":"abc",
    "jobRetries": "3", // 非必填，默认：3
    "taskHeaders":{
        "a":"b",
        "e":"d"
    },
    "nextNode": null
}
```

#### 2.2 `exclusiveGateway`

> [https://docs.camunda.io/docs/reference/bpmn-processes/exclusive-gateways/exclusive-gateways](https://docs.camunda.io/docs/reference/bpmn-processes/exclusive-gateways/exclusive-gateways)
>
> 对于 parallelGateWay/exclusiveGateway 类型，目前建议设置 nextNode 的 nodeType 类型一一对应

```json
{
    "nodeName":"排他",
    "nodeType":"exclusiveGateway",
    "nextNode":{
        "nodeName":"",
        "nodeType":"exclusiveGateway",
        "nextNode":null
    },
    "branchNodes":[
        {
            "nodeName":"条件1",
            "conditionExpression":"=id>1",
            "nextNode":{
                "nodeName":"审核人2.1",
                "jobType":"abd",
                "nodeType":"serviceTask",
                "nextNode": null
            }
        },
        {
            "nodeName":"条件2",
            "conditionExpression":"=id<1",
            "nextNode":{
                "nodeName":"审核人2.2",
                "nodeType":"serviceTask",
                "jobType":"abc",
                "nextNode":null
            }
        },
        {...} // 建议分支条件不要超过3个，理论上来说，3个以上的条件都可以进行合并
    ]
}
```

#### 2.3 `parallelGateway`

> [https://docs.camunda.io/docs/reference/bpmn-processes/parallel-gateways/parallel-gateways](https://docs.camunda.io/docs/reference/bpmn-processes/parallel-gateways/parallel-gateways)

```json
{
    "nodeName":"并行任务",
    "nodeType":"parallelGateway",
    "nextNode":{
        "nodeName":"",
        "nodeType":"parallelGateway",
        "nextNode":null
    },
    "branchNodes":[
        {
            "nodeName":"分支1",
            "nextNode":{
                "nodeName":"审核人2.1",
                "jobType":"abd",
                "nodeType":"serviceTask",
                "nextNode": null
            }
        },
        {
            "nodeName":"分支2",
            "nextNode":{
                "nodeName":"审核人2.2",
                "nodeType":"serviceTask",
                "jobType":"abc",
                "nextNode":null
            }
        },
        {
            "nodeName":"分支3",
            "nextNode":{
                "nodeName":"审核人2.3",
                "nodeType":"serviceTask",
                "jobType":"abc",
                "nextNode":null
            }
        },
        {...}
    ]
}
```

#### 2.4 `subProcess`

> [https://docs.camunda.io/docs/reference/bpmn-processes/embedded-subprocesses/embedded-subprocesses](https://docs.camunda.io/docs/reference/bpmn-processes/embedded-subprocesses/embedded-subprocesses)

```json
{
    "nodeName":"审核人3.1",
    "nodeType":"subProcess",
    "childNode":{ // 子流程内部流程
        "nodeName":"审核人2.1",
        "jobType":"abd",
        "nodeType":"serviceTask",
        "nextNode": null
     },
    "nextNode":{
        "nodeName":"审核人2.1",
        "jobType":"abd",
        "nodeType":"serviceTask",
        "nextNode": null
     }
}
```

#### 2.5 `callActivity`

> [https://docs.camunda.io/docs/reference/bpmn-processes/call-activities/call-activities](https://docs.camunda.io/docs/reference/bpmn-processes/call-activities/call-activities)

- `processId` 若不带 `=` 前缀，则默认会拼接 `namespace`
  - 例如：
    - 入参： `processId: call-process-id` ， `namespace: mediax-media-manage`
    - 引擎最终会去调用 `mediax-media-manage.call-process-id` 这个流程
- `processId` 若带 `=` 前缀，则不会拼接 `namespace`
  - 场景分两种
    - 指定静态变量：`processId: '="call-process-id"'`
      - 引擎最终会去调用 `call-process-id` 这个流程
    - 动态变量，由流程变量决定: `processId: '=dynamic-process-id'`
      - 根据流程变量中 `dynamic-process-id` 值，引擎最终决定调用哪个流程

```json
{
    "nodeName":"审核人3.1",
    "processId":"call-process-id", 
    "propagateAllChildVariablesEnabled": "true/false",
    "nodeType":"callActivity",
    "nextNode":{
        "nodeName":"审核人2.1",
        "jobType":"abd",
        "nodeType":"serviceTask",
        "nextNode": null
        }
}
```

#### 2.6 `sendTask`

> [https://docs.camunda.io/docs/reference/bpmn-processes/send-tasks/send-tasks](https://docs.camunda.io/docs/reference/bpmn-processes/send-tasks/send-tasks)
>
> Jobs for send tasks are not processed by Zeebe itself. In order to process them, you need to provide a job worker.
>
> [https://github.com/camunda-community-hub/kafka-connect-zeebe](https://github.com/camunda-community-hub/kafka-connect-zeebe)

```json
{
    "nodeName":"发送任务",
    "nodeType":"sendTask",
    "jobType":"abc",
    "jobRetries": "3", // 非必填，默认：3
    "taskHeaders":{
        "a":"b",
        "e":"d"
    },
    "nextNode": null
}
```

#### 2.7 `receiveTask`

> [https://docs.camunda.io/docs/reference/bpmn-processes/receive-tasks/receive-tasks#messages](https://docs.camunda.io/docs/reference/bpmn-processes/receive-tasks/receive-tasks#messages)
>
> 当发布消息时， 消息名称 `messageName`和关联键 `correlationKey` 与订阅匹配时，该消息将关联到相应的流程实例。如果没有打开适当的订阅，则消息将被丢弃。

- `messageName` 关联消息 name， 类似MQ中的topic
- `correlationKey` 关联键
  - 静态变量 `="test-correlation-key"`
  - 动态变量 `=dynamic-test-correlation-key` ，`${dynamic-test-correlation-key}` 从全局变量中取， 若全局变量中不存在， 则报错。

```json
{
  "nodeName":"接收任务",
  "nodeType":"receiveTask",
  "messageName":"message-name",
  "correlationKey":"=test-correlationKey",
  "nextNode": null
}
```

#### 2.8 `scriptTask`

> [https://docs.camunda.io/docs/reference/bpmn-processes/script-tasks/script-tasks](https://docs.camunda.io/docs/reference/bpmn-processes/script-tasks/script-tasks)
>
> Jobs for script tasks are not processed by Zeebe itself. In order to process them, you need to provide a job worker.
>
> [https://github.com/camunda-community-hub/zeebe-script-worker](https://github.com/camunda-community-hub/zeebe-script-worker)

```json
{
    "nodeName":"脚本任务",
    "nodeType":"scriptTask",
    "jobType":"abc",
    "jobRetries": "3", // 非必填，默认：3
    "taskHeaders":{
        "language":"javascript/groovy/python...",  // Jobs for script tasks are not processed by Zeebe itself. In order to process them, you need to provide a job worker.
        "script":"a+b"
    },
    "nextNode": null
}
```

#### 2.9 businessRuleTask

> [https://docs.camunda.io/docs/reference/bpmn-processes/business-rule-tasks/business-rule-tasks](https://docs.camunda.io/docs/reference/bpmn-processes/business-rule-tasks/business-rule-tasks)
>
> Jobs for business rule tasks are not processed by Zeebe itself. In order to process them, you need to provide a job worker.
>
> [https://github.com/camunda-community-hub/zeebe-dmn-worker](https://github.com/camunda-community-hub/zeebe-dmn-worker)

```json
{
    "nodeName":"业务规则",
    "nodeType":"businessRuleTask",
    "jobType":"abc",
    "jobRetries": "3", // 非必填，默认：3
    "taskHeaders":{
        "decisionRef":"test-decision",  // 业务规则文件：test-decision.dmn
    },
    "nextNode": null
}
```

#### 2.10 intermediateCatchEvent

> [https://docs.camunda.io/docs/reference/bpmn-processes/message-events/message-events#intermediate-message-catch-events](https://docs.camunda.io/docs/reference/bpmn-processes/message-events/message-events#intermediate-message-catch-events)
>
> [https://docs.camunda.io/docs/reference/bpmn-processes/timer-events/timer-events#intermediate-timer-catch-events](https://docs.camunda.io/docs/reference/bpmn-processes/timer-events/timer-events#intermediate-timer-catch-events)

- `Intermediate timer catch events`

```json
{
    "nodeName":"延时器",
    "nodeType":"intermediateCatchEvent",
  	"eventType":"timer",
    "timerDefinition":"PT4M",
    "nextNode": null
}
```

- `Intermediate message catch events`[#](#tHnQD)

```json
{
    "nodeName":"延时器",
    "nodeType":"intermediateCatchEvent",
  	"eventType":"message",
    "messageName":"message-name",
    "correlationKey":"=test-correlationKey",
    "nextNode": null
}
```

### 3. 示例

#### 3.1 pom 依赖

```xml
<dependency>
  <groupId>cn.lzgabel.converter</groupId>
  <artifactId>camunda-cloud-bpmn-converter</artifactId>
  <version>1.0.1</version>
</dependency>
```

- `demo`: 更多细节，请查看测试类 `BpmnBuilderTest`
  - 通过传入 `json` 串

```java
public class Main {
  public static void main(String[] args) throws IOException {

    String json = "{\n" +
            "    \"process\":{\n" +
            "        \"processId\":\"process-id\",\n" +
            "        \"name\":\"process-name\"\n" +
            "    },\n" +
            "    \"processNode\":{\n" +
            "        \"nodeName\":\"Service Task A\",\n" +
            "        \"nodeType\":\"serviceTask\",\n" +
            "        \"jobType\":\"abc\",\n" +
            "        \"taskHeaders\":{\n" +
            "            \"a\":\"b\",\n" +
            "            \"e\":\"d\"\n" +
            "        },\n" +
            "        \"nextNode\":{\n" +
            "            \"nodeName\":\"Service Task B\",\n" +
            "            \"nodeType\":\"serviceTask\",\n" +
            "            \"jobType\":\"abc\",\n" +
            "            \"taskHeaders\":{\n" +
            "                \"a\":\"b\",\n" +
            "                \"e\":\"d\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";


    BpmnModelInstance bpmnModelInstance = BpmnBuilder.build(json);
    Path path = Paths.get(OUT_PATH + testName.getMethodName() + ".bpmn");
    if (path.toFile().exists()) {
      path.toFile().delete();
    }
    Files.createDirectories(path.getParent());
    Bpmn.writeModelToFile(Files.createFile(path).toFile(), bpmnModelInstance);

  }
}
```

- `sdk` 直接定义

```java
public class Main {
  public static void main(String[] args) throws IOException {

    JSONObject taskHeaders = new JSONObject();
    taskHeaders.fluentPut("a", "b").fluentPut("e", "d");

    ServiceTaskDefinition processNode = ServiceTaskDefinition.builder().nodeName("service task a")
            .jobType("abc")
            .taskHeaders(taskHeaders)
            .jobRetries("4")
            .build();

    ProcessDefinition processDefinition = ProcessDefinition.builder()
            .name("process-name")
            .processId("process-id")
            .processNode(processNode)
            .build();

    BpmnModelInstance bpmnModelInstance = BpmnBuilder.build(processDefinition);
    Path path = Paths.get(OUT_PATH + testName.getMethodName() + ".bpmn");
    if (path.toFile().exists()) {
      path.toFile().delete();
    }
    Files.createDirectories(path.getParent());
    Bpmn.writeModelToFile(Files.createFile(path).toFile(), bpmnModelInstance);

  }   
}
```

