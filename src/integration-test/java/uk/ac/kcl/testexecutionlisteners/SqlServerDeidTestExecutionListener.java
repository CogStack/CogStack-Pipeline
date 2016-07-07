package uk.ac.kcl.testexecutionlisteners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.SqlServerTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class SqlServerDeidTestExecutionListener extends AbstractTestExecutionListener {

    public SqlServerDeidTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        SqlServerTestUtils sqlServerTestUtils =
                testContext.getApplicationContext().getBean(SqlServerTestUtils.class);
        sqlServerTestUtils.createJobRepository();
        sqlServerTestUtils.createBasicInputTable();
        sqlServerTestUtils.createBasicOutputTable();
        sqlServerTestUtils.createDeIdInputTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestDataForDeidentification("dbo.tblIdentifiers","dbo.tblInputDocs",1);


    }

}
