package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import de.creditreform.crefoteam.cte.testutils_cte.wisermailrule.WiserMailRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;

public class RestIntegrationTestBase {
    protected final Logger LOGGER = LoggerFactory.getLogger(RestIntegrationTestBase.class);
    @Rule
    public WiserMailRule wiserMailRule = new WiserMailRule(3025);
    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();
    protected RestInvokerConfig restInvokerConfig;
    protected CteActivitiServiceRestImpl cteActivitiServiceREST;

    @Before
    public void setUp() {
        restInvokerConfig = new RestInvokerConfig(JUNIT_ACTIVITI_URL, JUNIT_ACTIVITI_USER, JUNIT_ACTIVITI_PWD);
        cteActivitiServiceREST = new CteActivitiServiceRestImpl(restInvokerConfig);

        LOGGER.info(this.getClass().getSimpleName() + ":: new CteActivitiServiceRestImpl() mit " + restInvokerConfig.getServiceUser() + "@" + restInvokerConfig.getServiceURL());

        LOGGER.info(this.getClass().getSimpleName() + "#setUp:: Lösche Deployments JUnit*");
        cleanDeployments("Junit");

        LOGGER.info(this.getClass().getSimpleName() + "#setUp:: Lösche Deployments ENE-Junit*");
        cleanDeployments("ENE-Junit");

        LOGGER.info(this.getClass().getSimpleName() + "#setUp:: Lösche Deployments AUTOMATED*");
        cleanDeployments("AUTOMATED");
    }

    @After
    public void tearDown() {
        LOGGER.info(this.getClass().getSimpleName() + "#tearDown:: Lösche Deployments Junit*");
        cleanDeployments("Junit");

        LOGGER.info(this.getClass().getSimpleName() + "#tearDown:: Lösche Deployments ENE-Junit*");
        cleanDeployments("ENE-Junit");

        LOGGER.info(this.getClass().getSimpleName() + "#tearDown:: Lösche Deployments AUTOMATED*");
        cleanDeployments("AUTOMATED");
    }

    private void cleanDeployments(String nameLike) {
        try {
            for (; ; ) {
                List<CteActivitiDeployment> cteActivitiDeploymentList = cteActivitiServiceREST.listDeploymentsForNameLike(nameLike);
                if (cteActivitiDeploymentList.isEmpty()) {
                    break;
                }
                for (CteActivitiDeployment cteActivitiDeployment : cteActivitiDeploymentList) {
                    LOGGER.info("Lösche Deployment '" + cteActivitiDeployment.getName() + "' mit der ID " + cteActivitiDeployment.getId());
                    cteActivitiServiceREST.deleteCteActivitiDeployment(cteActivitiDeployment);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected CteActivitiTask claimAndCompleteUserTask(Integer processInstanceID, String meinKey, String taskDefinitionKey, Map<String, Object> paramsMap, boolean nextTaskExpected) throws Exception {
        Set<Map.Entry<String, Object>> entries = paramsMap.entrySet();
        LOGGER.info("\nRestIntegrationTestBase::claimAndCompleteUserTask({}, {}, {})::", meinKey, taskDefinitionKey, entries);
        CteActivitiTask nextUserTask = cteActivitiServiceREST.selectTaskForBusinessKey(processInstanceID, meinKey);
        if (nextUserTask != null) {
            Assert.assertEquals("Falscher Task gefunden!", taskDefinitionKey, nextUserTask.getTaskDefinitionKey());
            cteActivitiServiceREST.claimTask(nextUserTask, restInvokerConfig.getServiceUser());
            cteActivitiServiceREST.completeTask(nextUserTask, paramsMap);
            CteActivitiTask nextTask = null;
            if (nextTaskExpected) {
                nextTask = cteActivitiServiceREST.selectTaskForBusinessKey(processInstanceID, meinKey);
                LOGGER.info("\t====> Next Task ist '{}'", nextTask.getTaskDefinitionKey());
            }
            return nextTask;
        }
        return null;
    }

}
