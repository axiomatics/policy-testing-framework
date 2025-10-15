package com.myorg.alfa;


import com.axiomatics.cr.alfa.test.junit.AlfaExtension;
import com.axiomatics.cr.alfa.test.junit.AttributeConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class MyAttributeconnectorTest {

    @RegisterExtension
    public AlfaExtension alfa = new AlfaExtension();

    @Test
    public void shouldGetRoleConsultantForCecilia() {
        AttributeConnector target = alfa.newAttributeTest("ourConnector");

        List<String> result = target.lookup("user.role").by("user.identity", "cecilia");

        assertThat(result, hasItem("consultant"));
    }

    @Test
    public void shouldGetRoleManagerForMartin() {
        AttributeConnector target = alfa.newAttributeTest("ourConnector");

        List<String> result = target.lookup("user.role").by("user.identity", "martin");

        assertThat(result, hasItem("manager"));
    }
}
