package uk.ac.kcl.it;

import javax.annotation.PostConstruct;

/**
 * Created by rich on 03/06/16.
 */
public interface DbmsTestUtils {

    @PostConstruct
    void init();

    void initTikaTable();

    void initTextualGateTable();

    void createBasicInputTable();

    void createBasicOutputTable();

    void initMultiLineTextTable();

    void initJobRepository();
}
