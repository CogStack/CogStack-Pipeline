package uk.ac.uk.it.TestExecutionListeners;


import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import uk.ac.kcl.it.PostGresTestUtils;
import uk.ac.kcl.it.SqlServerTestUtils;
import uk.ac.kcl.it.TestUtils;

/**
 * Created by rich on 03/06/16.
 */
public class PostgresTikaTestExecutionListener extends AbstractTestExecutionListener {

    public PostgresTikaTestExecutionListener(){};

    @Override
    public void beforeTestClass(TestContext testContext) {
        PostGresTestUtils sqlServerTestUtils =
                testContext.getApplicationContext().getBean(PostGresTestUtils.class);
        sqlServerTestUtils.initJobRepository();
        sqlServerTestUtils.initTikaTable();
        sqlServerTestUtils.createBasicOutputTable();
        TestUtils testUtils =
                testContext.getApplicationContext().getBean(TestUtils.class);
        testUtils.insertTestBinariesForTika("tblInputDocs");
    }

}
