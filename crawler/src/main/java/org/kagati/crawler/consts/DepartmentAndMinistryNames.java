package org.kagati.crawler.consts;

import java.util.HashMap;

// TEMPORARY
// This class will be replaced by a JSON file soon.
public class DepartmentAndMinistryNames {
    public static final HashMap<String, Ministry> ALL_NAMES = new HashMap<>() {{
        put("moha", new Ministry("Ministy of Home Affairs", "moha.gov.np"));
        put("mofa", new Ministry("Ministy of Foreign Affairs", "mofa.gov.np"));
        put("pass", new Ministry("Department of Passport", "nepalpassport.gov.np"));
        put("immi", new Ministry("Department of Immigration", "immigration.gov.np"));
        put("doe", new Ministry("Department of Education", "doe.gov.np"));
        put("moe", new Ministry("Ministry of Education", "moe.gov.np"));
        put("mopit", new Ministry("Ministry of Physical Infrastructure and Transport", "mopit.gov.np"));
        put("dorw", new Ministry("Department of Railways", "dorw.gov.np"));
        put("dor", new Ministry("Department of Roads", "dor.gov.np"));
        put("tourism", new Ministry("Ministry of Culture, Tourism and Civil Aviation", "tourism.gov.np"));
        put("opmcm", new Ministry("Office of Prime Minister and Council of Ministers", "opmcm.gov.np"));
        put("npc", new Ministry("National Planning Commission", "npc.gov.np"));
        put("nnrfc", new Ministry("National Natural Resources and Fiscal Commission", "nnrfc.gov.np"));
        put("nic", new Ministry("National Information Commission", "nic.gov.np"));
        put("nkp", new Ministry("Nepal Law Newspaper", "nkp.gov.np"));
        put("hr", new Ministry("House of Representatives", "hr.parliament.gov.np"));
        put("na", new Ministry("National Assembly", "na.parliament.gov.np"));
    }};
}