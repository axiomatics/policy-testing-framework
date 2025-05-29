package com.myorg.alfa;

import com.axiomatics.cr.alfa.test.junit.AlfaExtension;
import com.axiomatics.cr.alfa.test.junit.TestRequest;
import com.axiomatics.cr.alfa.test.junit.TestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.axiomatics.cr.alfa.test.junit.matchers.AlfaMatchers.permit;
import static com.axiomatics.cr.alfa.test.junit.matchers.AttributeAssignmentMatcher.withText;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MySystemTest {

    @RegisterExtension
    public AlfaExtension alfa = new AlfaExtension().withAttributeConnectors().withEvaluationTrace();

    @Test
    public void shouldGiveMartinAccessToResource1() {
        TestRequest target = alfa.newTestRequest()
                .with("user.identity", "martin")
                .with("resource.identity", "1");

        TestResponse result = target.evaluate();

        assertThat(result, is(permit()));
        assertThat(result.getAdvice(), hasItem(withText("Permit since user is manager")));
    }

    @Test
    public void shouldGiveCeciliaAccessToResource1() {
        TestRequest target = alfa.newTestRequest()
                .with("user.identity", "cecilia")
                .with("resource.identity", "1");

        TestResponse result = target.evaluate();

        assertThat(result, is(permit()));
        assertThat(result.getAdvice(), hasItem(withText("Permit since user is consultant and resource and user is located both in london")));
    }

    @Test
    public void shouldNotGiveCeciliaAccessToResource2() {
        TestRequest target = alfa.newTestRequest()
                .with("user.identity", "cecilia")
                .with("resource.identity", "2");

        TestResponse result = target.evaluate();

        assertThat(result, is(not(permit())));
    }
}
