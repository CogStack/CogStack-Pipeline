package uk.ac.kcl.testexecutionlisteners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.PostGresTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class PostgresDeidTestExecutionListener extends AbstractTestExecutionListener {

    public PostgresDeidTestExecutionListener(){}

    @Override
    public void beforeTestClass(TestContext testContext) {
        PostGresTestUtils postgresTestUtils =
                testContext.getApplicationContext().getBean(PostGresTestUtils.class);
        postgresTestUtils.createJobRepository();
        postgresTestUtils.createBasicInputTable();
        postgresTestUtils.createBasicOutputTable();
        postgresTestUtils.createDeIdInputTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestDataForDeidentification("tblIdentifiers","tblInputDocs");
        //testUtils.insertTestDataForDeidentificationMemoryLeak("tblIdentifiers","tblInputDocs");

    }

}
