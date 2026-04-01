package de.creditreform.crefoteam.activiti;

/**
 * Created by CavdarK on 19.07.2017.
 */
public class ActivitProcessConstants {
    public static String UT_TASK_PARAM_NAME_MEIN_KEY = "MEIN_KEY";
    public static String UT_TASK_PARAM_NAME_TEST_TYPE = "TEST_TYPE";
    public static String UT_TASK_PARAM_NAME_EMAIL_FROM = "EMAIL_FROM";
    public static String UT_TASK_PARAM_NAME_SUCCESS_EMAIL_TO = "SUCCESS_MAIL_TO";
    public static String UT_TASK_PARAM_NAME_FAILURE_EMAIL_TO = "FAILURE_MAIL_TO";

    enum TEST_TYPES {
        PHASE2_ONLY("Nur PHASE-2"),
        PHASE1_AND_PHASE2("PHASE-1 und PHASE-2");

        private final String description;

        TEST_TYPES(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
