package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.testutils_cte.wisermailrule.WiserMailRule;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.WiserMessage;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;

public class CteActivitiRuleTest {

    protected final static Logger LOGGER = LoggerFactory.getLogger(CteActivitiRuleTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti-test.inmemory-cfg.xml");
    @Rule
    public WiserMailRule wiserMailRule = new WiserMailRule(3025);
    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected FormService formService;
    protected IdentityService identityService;
    protected RepositoryService repositoryService;
    protected HistoryService historyService;
    protected User testUser;

    @Before
    public void setUp() {
        try {
            identityService = activitiRule.getIdentityService();
            Group newGroup = identityService.newGroup(JUNIT_ACTIVITI_GROUP);
            identityService.saveGroup(newGroup);
            testUser = identityService.newUser(JUNIT_ACTIVITI_USER);
            testUser.setFirstName("JUNIT");
            testUser.setLastName("CTE-TESTER");
            testUser.setEmail("k.cavdar@verband.creditreform.de");
            identityService.saveUser(testUser);
            identityService.createMembership(testUser.getId(), JUNIT_ACTIVITI_GROUP);
            identityService.setAuthenticatedUserId(testUser.getId());

            processEngine = activitiRule.getProcessEngine();
            runtimeService = activitiRule.getRuntimeService();
            taskService = activitiRule.getTaskService();
            formService = activitiRule.getFormService();
            repositoryService = activitiRule.getRepositoryService();
            historyService = activitiRule.getHistoryService();
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            List<Group> groupList = identityService.createGroupQuery().list();
            for (Group theGroup : groupList) {
                identityService.deleteGroup(theGroup.getId());
            }
            List<User> userList = identityService.createUserQuery().list();
            for (User theUser : userList) {
                identityService.deleteUser(theUser.getId());
            }
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    protected ProcessInstance startProcess(String processDefinitionKey, String meinKey, String activityId) {
        LOGGER.info("\nCteActivitiRuleTest::startProcess({})::", meinKey);
        // Porozess-Definition holen...
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
        Assert.assertNotNull(processDefinition);
        LOGGER.info("\tProzessDefinition:\tResourceName = {}, Name = {}, Key = {}", processDefinition.getResourceName(), processDefinition.getName(), processDefinition.getKey());
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), paramsMap);
        Assert.assertNotNull(processInstance);
        LOGGER.info("\tID = {}, ProcessInstance:\tBusinessKey = {}, ProcessDefinitionId = {}", processInstance.getId(), processInstance.getBusinessKey(), processInstance.getProcessDefinitionId());
        Assert.assertEquals(processInstance.getActivityId(), activityId);
        return processInstance;
    }

    protected Task claimAndCompleteUserTask(String meinKey, String taskDefinitionKey, Map<String, Object> parametersMap) throws Exception {
        Set<Map.Entry<String, Object>> entries = parametersMap.entrySet();
        LOGGER.info("\nCteActivitiRuleTest::claimAndCompleteUserTask({}, {}, {})::", meinKey, taskDefinitionKey, entries);
        Task nextUserTask = selectTaskForBusinessKey(meinKey);
        if (nextUserTask != null) {
            Assert.assertEquals("Falscher Task gefunden!", taskDefinitionKey, nextUserTask.getTaskDefinitionKey());
            taskService.claim(nextUserTask.getId(), testUser.getId());
            taskService.complete(nextUserTask.getId(), parametersMap);
            return selectTaskForBusinessKey(meinKey);
        }
        return null;
    }

    private Task selectTaskForBusinessKey(String meinKey) {
        LOGGER.info("\n\tCteActivitiRuleTest::selectTaskForBusinessKey({})::", meinKey);
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> tasksList = taskQuery.includeProcessVariables().list();
        for (Task theUserTask : tasksList) {
            Map<String, Object> processVariables = theUserTask.getProcessVariables();
            LOGGER.info("\t\tTask:\tID = {}, ProcessInstanceId = {}, ExecutionId = {}, TaskDefinitionKey = {}\n\t\t\tParameter = {}",
                    theUserTask.getId(), theUserTask.getProcessInstanceId(), theUserTask.getExecutionId(),
                    theUserTask.getTaskDefinitionKey(), processVariables.toString());
        }
        for (Task theUserTask : tasksList) {
            Map<String, Object> processVariables = theUserTask.getProcessVariables();
            Object businessKeyX = processVariables.get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY);
            if (businessKeyX != null && businessKeyX.equals(meinKey)) {
                LOGGER.info("\t==>Task gefunden:\n\tTask:\tID = {}, ProcessInstanceId = {}, ExecutionId = {}, TaskDefinitionKey = {}\n\t\tParameter ={}",
                        theUserTask.getId(), theUserTask.getProcessInstanceId(), theUserTask.getExecutionId(),
                        theUserTask.getTaskDefinitionKey(), processVariables);
                return theUserTask;
            }
        }
        LOGGER.info("\t==>Kein weiterer Task mehr gefunden!");
        return null;
    }

    //   @Test  weil: org.activiti.engine.ActivitiException: Could not send e-mail in execution 6
    @Deployment(resources = JUNIT_DEPLOYMENT_NAME)
    public void testProcessTwoExecutionsParallelSameSignal() throws Exception {
        String businessKey0 = "BUSINESS-KEY-0";
        String businessKey1 = "BUSINESS-KEY-1";

        // prozess with businessKey = "JUNIT-BUSINESS-KEY-0"
        ProcessInstance processInstance0 = startProcess(JUNIT_PROCESS_DEFINITION_KEY, businessKey0, "UserTask0");

        // prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        ProcessInstance processInstance1 = startProcess(JUNIT_PROCESS_DEFINITION_KEY, businessKey1, "UserTask0");

        Task nextUserTask;
        Map<String, Object> parametersMap = new HashMap<>();

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-0", Path for signal 0
        parametersMap.put("signalType", "signalType0");
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTask0", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1", Path for signal 0
        parametersMap.put("signalType", "signalType0");
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTask0", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTask0.0", parametersMap);
        Assert.assertNotNull(nextUserTask);
        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTask0.1", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTask0.0", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTask0.2", parametersMap);
        Assert.assertNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTask0.1", parametersMap);
        Assert.assertNotNull(nextUserTask);
        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTask0.2", parametersMap);
        Assert.assertNull(nextUserTask);

        List<WiserMessage> messagesList = wiserMailRule.getMessages();
        Assert.assertEquals(2, messagesList.size()); // für processInstance0 und processInstance1
        WiserMessage wiserMessage = messagesList.get(0);
        MimeMessage mimeMessage = wiserMessage.getMimeMessage();
        Assert.assertEquals("JunitTest", mimeMessage.getSubject());
        Assert.assertTrue(mimeMessage.getContent().toString().startsWith("Mail from Signal signalType0"));
        Assert.assertEquals("k.cavdar@activiti-tester.de", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    //   @Test  weil: ActivitiException: No outgoing sequence flow of the exclusive gateway 'exclusiveGatewayTestType' could be selected for continuing the process
    @Deployment(resources = {AUTOMATED_MAIN_DEPLOYMENT_NAME, AUTOMATED_SUB_RT2_DEPLOYMENT_NAME, AUTOMATED_SUB_RT_DEPLOYMENT_NAME})
    public void testProcessTwoExecutionsParallelDifferentSignal() throws Exception {
        // prozess with businessKey = "KEY-0"
        String businessKey0 = "KEY-0";
        ProcessInstance processInstance0 = startProcess(AUTOMATED_MAIN_PROCESS_DEFINITION_KEY, businessKey0, "UserTaskPauseAllJobs");
        // prozess with businessKey = "BUSINESS-KEY-1"
        String businessKey1 = "KEY-1";
        startProcess(AUTOMATED_MAIN_PROCESS_DEFINITION_KEY, businessKey1, "UserTaskPauseAllJobs");

        Task nextUserTask;
        Map<String, Object> parametersMap = new HashMap<>();

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskPauseAllJobs", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskPauseAllJobs", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0", Path for testType : << INITIAL_TEST >>
        parametersMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_TEST_TYPE, "IntialTest");
        parametersMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_EMAIL_FROM, "junit-test@creditreform.de");
        parametersMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_SUCCESS_EMAIL_TO, "erfolg.junit-test@creditreform.de");
        parametersMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_FAILURE_EMAIL_TO, "fehler.junit-test@creditreform.de");
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskEnterTestData", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1", Path for testType : << INITIAL_TEST >>
        parametersMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_TEST_TYPE, ActivitProcessConstants.TEST_TYPES.PHASE2_ONLY.name());
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskEnterTestData", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // processInstance0
        parametersMap.clear();
        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskWaitForTestSystem", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskCpyTestProps2DL", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskStartDownloads", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskCpyDLsProps2GEN", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskCpyStgABDocs2Output", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskStartCollect", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskStartGenerates", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskCheckCollects", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskStartUploads", parametersMap);
        Assert.assertNotNull(nextUserTask);
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskCheckRefExports", parametersMap);
        Assert.assertNotNull(nextUserTask);
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskAnalyseChecks", parametersMap);
        Assert.assertNotNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-0"
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskSaveCOLsAsRefExp", parametersMap);
        Assert.assertNotNull(nextUserTask); // Prozess 0 zu Ende
        nextUserTask = claimAndCompleteUserTask(businessKey0, "UserTaskResumeAllJobs", parametersMap);
        Assert.assertNull(nextUserTask); // Prozess 0 zu Ende

        // claim und complete next UserTask for prozess with businessKey = "BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(businessKey1, "UserTaskResumeAllJobs", parametersMap);
        Assert.assertNull(nextUserTask); // Prozess 1 zu Ende

    }

}
