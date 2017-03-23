package uk.ac.kcl.utils;

import javax.annotation.PostConstruct;

/**
 * Created by rich on 03/06/16.
 */
public interface DbmsTestUtils {

    @PostConstruct
    void init();

    void createTikaTable();

    void createBasicInputTable();

    void createBasicOutputTable();

    void createMultiLineTextTable();

    void createJobRepository();

    void createDeIdInputTable();

    int countRowsInOutputTable();

    void createDocManInputTable();

}
