package com.myorg.alfa;


import com.axiomatics.cr.alfa.test.junit.AlfaTestRule;
import com.axiomatics.cr.alfa.test.junit.AttributeConnector;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class MyAttributeconnectorTest {

    @Rule
    public AlfaTestRule rule = new AlfaTestRule();

    @Test
    public void shouldGetRoleConsultantForCecilia() {
        AttributeConnector target = rule.newAttributeTest("ourConnector");

        List<String> result = target.lookup("user.role").by("user.identity", "cecilia");

        assertThat(result, hasItem("consultant"));
    }

    @Test
    public void shouldGetRoleManagerForMartin() {
        AttributeConnector target = rule.newAttributeTest("ourConnector");

        List<String> result = target.lookup("user.role").by("user.identity", "martin");

        assertThat(result, hasItem("manager"));
    }
}
