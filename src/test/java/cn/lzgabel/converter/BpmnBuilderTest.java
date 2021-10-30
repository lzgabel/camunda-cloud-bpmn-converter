package cn.lzgabel.converter;

import cn.lzgabel.converter.bean.ProcessDefinition;
import cn.lzgabel.converter.bean.event.intermediate.MessageIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.intermediate.TimerIntermediateCatchEventDefinition;
import cn.lzgabel.converter.bean.event.start.MessageStartEventDefinition;
import cn.lzgabel.converter.bean.event.start.TimerDefinitionType;
import cn.lzgabel.converter.bean.event.start.TimerStartEventDefinition;
import cn.lzgabel.converter.bean.gateway.BranchNode;
import cn.lzgabel.converter.bean.gateway.ExclusiveGatewayDefinition;
import cn.lzgabel.converter.bean.gateway.ParallelGatewayDefinition;
import cn.lzgabel.converter.bean.subprocess.CallActivityDefinition;
import cn.lzgabel.converter.bean.subprocess.SubProcessDefinition;
import cn.lzgabel.converter.bean.task.*;
import com.alibaba.fastjson.JSONObject;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class BpmnBuilderTest {

    @Rule
    public TestName testName = new TestName();

    private static final String OUT_PATH = "target/out/";


    @Test
    public void timer_date_start_event_from_json() throws IOException {
        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Timer Start\",\n" +
                "        \"nodeType\":\"startEvent\",\n" +
                "        \"eventType\":\"timer\",\n" +
                "        \"timerDefinitionType\":\"date\",\n" +
                "        \"timerDefinition\":\"2019-10-01T12:00:00+08:00\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Service Task A\",\n" +
                "            \"nodeType\":\"serviceTask\",\n" +
                "            \"jobType\":\"abc\",\n" +
                "            \"jobRetries\":\"2\",\n" +
                "            \"taskHeaders\":{\n" +
                "                \"a\":\"b\",\n" +
                "                \"e\":\"d\"\n" +
                "            },\n" +
                "            \"nextNode\":null\n" +
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


    @Test
    public void timer_cycle_start_event_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Timer Start\",\n" +
                "        \"nodeType\":\"startEvent\",\n" +
                "        \"eventType\":\"timer\",\n" +
                "        \"timerDefinitionType\":\"cycle\",\n" +
                "        \"timerDefinition\":\"R1/PT2M\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Service Task A\",\n" +
                "            \"nodeType\":\"serviceTask\",\n" +
                "            \"jobType\":\"abc\",\n" +
                "            \"jobRetries\":\"3\",\n" +
                "            \"taskHeaders\":{\n" +
                "                \"a\":\"b\",\n" +
                "                \"e\":\"d\"\n" +
                "            },\n" +
                "            \"nextNode\":null\n" +
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

    @Test
    public void message_start_event_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Message Start\",\n" +
                "        \"nodeType\":\"startEvent\",\n" +
                "        \"eventType\":\"message\",\n" +
                "        \"messageName\":\"test-message-name\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Service Task A\",\n" +
                "            \"nodeType\":\"serviceTask\",\n" +
                "            \"jobType\":\"abc\",\n" +
                "            \"jobRetries\":\"3\",\n" +
                "            \"taskHeaders\":{\n" +
                "                \"a\":\"b\",\n" +
                "                \"e\":\"d\"\n" +
                "            },\n" +
                "            \"nextNode\":null\n" +
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


    @Test
    public void intermediate_timer_catch_event_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Service Task A\",\n" +
                "        \"nodeType\":\"serviceTask\",\n" +
                "        \"jobType\":\"abc\",\n" +
                "        \"jobRetries\":\"3\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"a\":\"b\",\n" +
                "            \"e\":\"d\"\n" +
                "        },\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Intermediate Timer Catch Event\",\n" +
                "            \"nodeType\":\"intermediateCatchEvent\",\n" +
                "            \"eventType\":\"timer\",\n" +
                "            \"timerDefinition\":\"PT4M\",\n" +
                "            \"nextNode\":null\n" +
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


    @Test
    public void intermediate_message_catch_event_from_json() throws IOException {
        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Service Task A\",\n" +
                "        \"nodeType\":\"serviceTask\",\n" +
                "        \"jobType\":\"abc\",\n" +
                "        \"jobRetries\":\"3\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"a\":\"b\",\n" +
                "            \"e\":\"d\"\n" +
                "        },\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Intermediate Message Catch Event\",\n" +
                "            \"nodeType\":\"intermediateCatchEvent\",\n" +
                "            \"eventType\":\"message\",\n" +
                "            \"messageName\":\"message-name\",\n" +
                "            \"correlationKey\":\"=test-correlationKey\",\n" +
                "            \"nextNode\":null\n" +
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

    @Test
    public void service_task_from_json() throws IOException {

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


    @Test
    public void send_task_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Send Task A\",\n" +
                "        \"nodeType\":\"sendTask\",\n" +
                "        \"jobType\":\"abc\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"a\":\"b\",\n" +
                "            \"e\":\"d\"\n" +
                "        },\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Send Task B\",\n" +
                "            \"nodeType\":\"sendTask\",\n" +
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


    @Test
    public void receive_task_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Receive Task A\",\n" +
                "        \"nodeType\":\"receiveTask\",\n" +
                "        \"messageName\":\"message-name\",\n" +
                "        \"correlationKey\":\"=test-correlationKey\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Receive Task B\",\n" +
                "            \"nodeType\":\"receiveTask\",\n" +
                "            \"messageName\":\"message-name\",\n" +
                "            \"correlationKey\":\"=test-correlationKey\",\n" +
                "            \"nextNode\":null\n" +
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


    @Test
    public void script_task_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Script Task A\",\n" +
                "        \"nodeType\":\"scriptTask\",\n" +
                "        \"jobType\":\"abc\",\n" +
                "        \"jobRetries\":\"3\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"language\":\"javascript\",\n" +
                "            \"script\":\"a+b\"\n" +
                "        },\n" +
                "        \"nextNode\":null\n" +
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

    @Test
    public void user_task_from_json() throws IOException {

        BpmnModelInstance done = Bpmn.createProcess("user-task-process").startEvent("start").userTask("user-task-id-1")
                .zeebeUserTaskForm("{}").userTask("user-task-id-2")
                .zeebeUserTaskForm("{}").endEvent("end-id").done();


        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"User Task A\",\n" +
                "        \"nodeType\":\"userTask\",\n" +
                "        \"assignee\":\"lizhi\",\n" +
                "        \"candidateGroups\":\"lizhi\",\n" +
                "        \"userTaskForm\":\"{}\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"key1\":\"b\",\n" +
                "            \"key2\":\"a+b\"\n" +
                "        },\n" +
                "        \"nextNode\":null\n" +
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

    @Test
    public void business_rule_task_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Business Rule Task A\",\n" +
                "        \"nodeType\":\"businessRuleTask\",\n" +
                "        \"jobType\":\"abc\",\n" +
                "        \"jobRetries\":\"3\",\n" +
                "        \"taskHeaders\":{\n" +
                "            \"decisionRef\":\"test-decision\"\n" +
                "        },\n" +
                "        \"nextNode\":null\n" +
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


    @Test
    public void sub_process_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Sub Process A\",\n" +
                "        \"nodeType\":\"subProcess\",\n" +
                "        \"childNode\":{\n" +                     // sub process nested process
                "            \"nodeName\":\"Service Task A\",\n" +
                "            \"jobType\":\"a\",\n" +
                "            \"nodeType\":\"serviceTask\",\n" +
                "            \"nextNode\":null\n" +
                "        },\n" +
                "        \"nextNode\":{\n" +         // Node after sub process
                "            \"nodeName\":\"Service Task B\",\n" +
                "            \"jobType\":\"b\",\n" +
                "            \"nodeType\":\"serviceTask\",\n" +
                "            \"nextNode\":null\n" +
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

    @Test
    public void call_activity_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"智能媒资组\",\n" +
                "        \"processId\":\"call-mediax-process-id\",\n" +
                "        \"propagateAllChildVariablesEnabled\":\"true\",\n" +
                "        \"nodeType\":\"callActivity\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"magic应用组\",\n" +
                "            \"processId\":\"call-magic-process-id\",\n" +
                "            \"propagateAllChildVariablesEnabled\":\"true\",\n" +
                "            \"nodeType\":\"callActivity\",\n" +
                "            \"nextNode\":null\n" +
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

    @Test
    public void exclusive_gateway_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Exclusive Gateway Start\",\n" +
                "        \"nodeType\":\"exclusiveGateway\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Exclusive Gateway End\",\n" +
                "            \"nodeType\":\"exclusiveGateway\",\n" +
                "            \"nextNode\":{\n" +
                "                \"nodeName\":\"Service Task C\",\n" +
                "                \"jobType\":\"abd\",\n" +
                "                \"nodeType\":\"serviceTask\",\n" +
                "                \"nextNode\":null\n" +
                "            }\n" +
                "        },\n" +
                "        \"branchNodes\":[\n" +
                "            {\n" +
                "                \"nodeName\":\"condition A\",\n" +
                "                \"conditionExpression\":\"=id>1\",\n" +
                "                \"nextNode\":{\n" +
                "                    \"nodeName\":\"Service Task A\",\n" +
                "                    \"jobType\":\"abd\",\n" +
                "                    \"nodeType\":\"serviceTask\",\n" +
                "                    \"nextNode\":null\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"nodeName\":\"condition B\",\n" +
                "                \"conditionExpression\":\"=id<1\",\n" +
                "                \"nextNode\":{\n" +
                "                    \"nodeName\":\"Service Task B\",\n" +
                "                    \"nodeType\":\"serviceTask\",\n" +
                "                    \"jobType\":\"abc\",\n" +
                "                    \"nextNode\":null\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
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


    @Test
    public void parallel_gateway_from_json() throws IOException {

        String json = "{\n" +
                "    \"process\":{\n" +
                "        \"processId\":\"process-id\",\n" +
                "        \"name\":\"process-name\"\n" +
                "    },\n" +
                "    \"processNode\":{\n" +
                "        \"nodeName\":\"Parallel Gateway Start\",\n" +
                "        \"nodeType\":\"parallelGateway\",\n" +
                "        \"nextNode\":{\n" +
                "            \"nodeName\":\"Parallel Gateway End\",\n" +
                "            \"nodeType\":\"parallelGateway\",\n" +
                "            \"nextNode\":null\n" +
                "        },\n" +
                "        \"branchNodes\":[\n" +
                "            {\n" +
                "                \"nodeName\":\"branch A\",\n" +
                "                \"nextNode\":{\n" +
                "                    \"nodeName\":\"Service Task A\",\n" +
                "                    \"jobType\":\"abd\",\n" +
                "                    \"nodeType\":\"serviceTask\",\n" +
                "                    \"nextNode\":null\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"nodeName\":\"branch B\",\n" +
                "                \"nextNode\":{\n" +
                "                    \"nodeName\":\"Service Task B\",\n" +
                "                    \"nodeType\":\"serviceTask\",\n" +
                "                    \"jobType\":\"abc\",\n" +
                "                    \"nextNode\":null\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"nodeName\":\"branch C\",\n" +
                "                \"nextNode\":{\n" +
                "                    \"nodeName\":\"Service Task C\",\n" +
                "                    \"nodeType\":\"serviceTask\",\n" +
                "                    \"jobType\":\"abc\",\n" +
                "                    \"nextNode\":null\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
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

    //------  from processDefinition --------
    @Test
    public void timer_date_start_event_from_process_definition() throws IOException {

        TimerStartEventDefinition processNode = TimerStartEventDefinition.builder()
                .timerDefinitionType(TimerDefinitionType.DATE.value())
                .timerDefinition("2019-10-01T12:00:00+08:00")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("lizhi")
                        .jobType("abc")
                        .jobRetries("4")
                        .build())
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


    @Test
    public void timer_cycle_start_event_from_process_definition() throws IOException {

        TimerStartEventDefinition processNode = TimerStartEventDefinition.builder()
                .timerDefinitionType(TimerDefinitionType.CYCLE.value())
                .timerDefinition("R1/PT5M")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("lizhi")
                        .jobType("abc")
                        .jobRetries("4")
                        .build())
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

    @Test
    public void message_start_event_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("a", "b").fluentPut("e", "d");
        MessageStartEventDefinition processNode = MessageStartEventDefinition.builder()
                .nodeName("message start")
                .messageName("test-message-name")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("lizhi")
                        .jobType("abc")
                        .jobRetries("4")
                        .taskHeaders(taskHeaders)
                        .build())
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


    @Test
    public void intermediate_timer_catch_event_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("a", "b").fluentPut("e", "d");

        ServiceTaskDefinition processNode = ServiceTaskDefinition.builder().nodeName("service task a")
                .jobType("abc")
                .taskHeaders(taskHeaders)
                .jobRetries("4")
                .nextNode(TimerIntermediateCatchEventDefinition.builder()
                        .nodeName("timer catch a")
                        .timerDefinition("PT4M")
                        .build())
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


    @Test
    public void intermediate_message_catch_event_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("a", "b").fluentPut("e", "d");

        ServiceTaskDefinition processNode = ServiceTaskDefinition.builder().nodeName("service task a")
                .jobType("abc")
                .taskHeaders(taskHeaders)
                .jobRetries("4")
                .nextNode(MessageIntermediateCatchEventDefinition.builder()
                        .nodeName("catch message a")
                        .messageName("test-message-name")
                        .correlationKey("test-correlation-key")
                        .build())
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

    @Test
    public void service_task_from_process_definition() throws IOException {

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


    @Test
    public void send_task_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("a", "b").fluentPut("e", "d");

        SendTaskDefinition processNode = SendTaskDefinition.builder().nodeName("send task a")
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


    @Test
    public void receive_task_from_process_definition() throws IOException {

        ReceiveTaskDefinition processNode = ReceiveTaskDefinition.builder().nodeName("receive task a")
                .messageName("test-receive-message-name")
                .correlationKey("=test-receive-correlation-key")
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


    @Test
    public void script_task_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("language", "javascript").fluentPut("script", "a+d");

        ScriptTaskDefinition processNode = ScriptTaskDefinition.builder().nodeName("script task a")
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

    @Test
    public void user_task_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("a", "b").fluentPut("c", "c");

        UserTaskDefinition processNode = UserTaskDefinition.builder().nodeName("user task a")
                .assignee("lizhi")
                .candidateGroups("lizhi,shuwen")
                .userTaskForm("{}")
                .taskHeaders(taskHeaders)
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

    @Test
    public void business_rule_task_from_process_definition() throws IOException {

        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("decisionRef", "test-decision");

        BusinessRuleTaskDefinition processNode = BusinessRuleTaskDefinition.builder().nodeName("business rule task a")
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


    @Test
    public void sub_process_from_process_definition() throws IOException {
        JSONObject taskHeaders = new JSONObject();
        taskHeaders.fluentPut("decisionRef", "test-decision");

        ServiceTaskDefinition processNode = ServiceTaskDefinition.builder().nodeName("service task a")
                .jobType("abc")
                .taskHeaders(taskHeaders)
                .jobRetries("4")
                .nextNode(SubProcessDefinition.builder().nodeName("sub-process")
                        .childNode(ServiceTaskDefinition.builder().nodeName("inner service task")
                                .jobType("inner-service-task")
                                .build())
                        .nextNode(ServiceTaskDefinition.builder().nodeName("service task b")
                                .jobType("abd")
                                .build())
                        .build())
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

    @Test
    public void call_activity_from_process_definition() throws IOException {

        CallActivityDefinition processNode = CallActivityDefinition.builder()
                .nodeName("call mediax process")
                .processId("call-mediax-id")
                .propagateAllChildVariablesEnabled(true)
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

    @Test
    public void exclusive_gateway_from_process_definition() throws IOException {

        BranchNode branchNode1 = BranchNode.builder()
                .nodeName("分支1")
                .conditionExpression("id>1")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("service a")
                        .jobType("service-a")
                        .build())
                .build();

        BranchNode branchNode2 = BranchNode.builder()
                .nodeName("分支2")
                .conditionExpression("id<=1")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("service a")
                        .jobType("service-a")
                        .build())
                .build();

        ExclusiveGatewayDefinition processNode = ExclusiveGatewayDefinition.builder()
                .nodeName("exclusive gateway start")
                .branchNode(branchNode1)
                .branchNode(branchNode2)
                .nextNode(ExclusiveGatewayDefinition.builder()
                        .nodeName("exclusive gateway end")
                        .nextNode(ServiceTaskDefinition.builder()
                                .nodeName("service c")
                                .jobType("service-c")
                                .build())
                        .build())
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


    @Test
    public void parallel_gateway_from_process_definition() throws IOException {

        BranchNode branchNode1 = BranchNode.builder()
                .nodeName("分支1")
                .conditionExpression("id>1")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("service a")
                        .jobType("service-a")
                        .build())
                .build();

        BranchNode branchNode2 = BranchNode.builder()
                .nodeName("分支2")
                .conditionExpression("id>1")
                .nextNode(ServiceTaskDefinition.builder()
                        .nodeName("service a")
                        .jobType("service-a")
                        .build())
                .build();

        ParallelGatewayDefinition processNode = ParallelGatewayDefinition.builder()
                .nodeName("parallel gateway start")
                .branchNode(branchNode1)
                .branchNode(branchNode2)
                .nextNode(ParallelGatewayDefinition.builder()
                        .nodeName("parallel gateway end")
                        .nextNode(ServiceTaskDefinition.builder()
                                .nodeName("service c")
                                .jobType("service-c")
                                .build())
                        .build())
                .build();
        List<BranchNode> branchNodes = processNode.getBranchNodes();

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
