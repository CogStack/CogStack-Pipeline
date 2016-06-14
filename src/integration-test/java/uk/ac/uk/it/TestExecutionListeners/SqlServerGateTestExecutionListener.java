package uk.ac.uk.it.TestExecutionListeners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.SqlServerTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class SqlServerGateTestExecutionListener extends AbstractTestExecutionListener {

    public SqlServerGateTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        SqlServerTestUtils sqlServerTestUtils =
                testContext.getApplicationContext().getBean(SqlServerTestUtils.class);
        sqlServerTestUtils.createJobRepository();
        sqlServerTestUtils.createTextualGateTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestXHTMLForGate("dbo.tblInputDocs",false);
    }

}
