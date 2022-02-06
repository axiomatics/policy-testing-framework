package com.myorg.alfa;

import com.axiomatics.cr.alfa.test.junit.AlfaTestRule;
import com.axiomatics.cr.alfa.test.junit.TestRequest;
import com.axiomatics.cr.alfa.test.junit.TestResponse;
import org.junit.Rule;
import org.junit.Test;

import static com.axiomatics.cr.alfa.test.junit.matchers.AlfaMatchers.permit;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class MyConsultantPolicyUnitTest {

    @Rule
    public AlfaTestRule rule = new AlfaTestRule().withMainPolicy("consultants.Main");

    @Test
    public void shouldPermitIfUserAndResourceAreInSameLocation() {
        TestRequest target = rule.newTestRequest()
                .with("user.role", "consultant")
                .with("user.location", "hawaii")
                .with("resource.location", "hawaii");

        TestResponse result = target.evaluate();

        assertThat(result, is(permit()));
    }

    @Test
    public void shouldNotPermitIfUserAndResourceAreInDifferentLocation() {
        TestRequest target = rule.newTestRequest()
                .with("user.role", "consultant")
                .with("user.location", "hawaii")
                .with("resource.location", "stockholm");

        TestResponse result = target.evaluate();

        assertThat(result, is(not(permit())));
    }
}
