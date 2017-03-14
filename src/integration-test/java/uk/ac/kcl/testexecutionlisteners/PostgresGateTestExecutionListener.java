package uk.ac.kcl.testexecutionlisteners;


import org.junit.Ignore;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.utils.TestUtils;
import uk.ac.kcl.utils.PostGresTestUtils;

/**
 * Created by rich on 03/06/16.
 */
@Ignore
public class PostgresGateTestExecutionListener extends AbstractTestExecutionListener {

    public PostgresGateTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        PostGresTestUtils postGresTestUtils =
                testContext.getApplicationContext().getBean(PostGresTestUtils.class);
        postGresTestUtils.createJobRepository();
        postGresTestUtils.createTikaTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestBinariesForTika("tblInputDocs");
    }

}
