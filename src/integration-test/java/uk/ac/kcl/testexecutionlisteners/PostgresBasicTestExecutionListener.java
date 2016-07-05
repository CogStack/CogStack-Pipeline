package uk.ac.kcl.testexecutionlisteners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.PostGresTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class PostgresBasicTestExecutionListener extends AbstractTestExecutionListener {

    public PostgresBasicTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        PostGresTestUtils sqlServerTestUtils =
                testContext.getApplicationContext().getBean(PostGresTestUtils.class);
        sqlServerTestUtils.createJobRepository();
        sqlServerTestUtils.createBasicInputTable();
        sqlServerTestUtils.createBasicOutputTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertDataIntoBasicTable("tblInputDocs");
    }

}
