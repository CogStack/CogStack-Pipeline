package uk.ac.kcl.testexecutionlisteners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.PostGresTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class PostgresGateTestExecutionListener extends AbstractTestExecutionListener {

    public PostgresGateTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        PostGresTestUtils sqlServerTestUtils =
                testContext.getApplicationContext().getBean(PostGresTestUtils.class);
        sqlServerTestUtils.createJobRepository();
        sqlServerTestUtils.createTikaTable();
        //sqlServerTestUtils.createTextualGateTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestBinariesForTika("tblInputDocs");
    }

}
