/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb.regressionsuites;

import java.io.IOException;

import org.voltdb.BackendTarget;
import org.voltdb.client.Client;
import org.voltdb.client.ProcCallException;
import org.voltdb.compiler.VoltProjectBuilder;

public class TestNewHsqldbFeatures extends RegressionSuite {

    public void test_ROW_NUMBER_OVER() throws IOException, ProcCallException {
        System.out.println("test xin...");
        Client client = getClient();
        String sql;

        sql = "insert into r1(ID, ratio) values(2, 2.0);";
        client.callProcedure("@AdHoc", sql);

        sql = "insert into r1(ID, ratio) values(3, 3.0);";
        client.callProcedure("@AdHoc", sql);

        sql = "SELECT * FROM (SELECT id, ratio, row_number() over() FROM R1) tb;";
        verifyAdHocFails(client, "VoltDB does not support the unknown operator from OpTypes.java with numeric cod", sql);

        sql = "SELECT * FROM (SELECT id, ratio, ROWNUM() FROM R1) tb;";
        verifyAdHocFails(client, "VoltDB does not support the unknown operator from OpTypes.java with numeric cod", sql);

        sql = "SELECT * FROM (SELECT id row_number() over(partition by ratio) FROM R1) tb;";
        verifyAdHocFails(client, "unexpected token", sql);
    }

    public TestNewHsqldbFeatures(String name) {
        super(name);
    }
    static public junit.framework.Test suite() {
        VoltServerConfig config = null;
        MultiConfigSuiteBuilder builder = new MultiConfigSuiteBuilder(
                TestNewHsqldbFeatures.class);
        VoltProjectBuilder project = new VoltProjectBuilder();
        final String literalSchema =
                "CREATE TABLE R1 ( "
                + "ID tinyint DEFAULT 0 NOT NULL, "
                + "ratio float"
                + "); " +
                ""
                ;
        try {
            project.addLiteralSchema(literalSchema);
        } catch (IOException e) {
            assertFalse(true);
        }
        boolean success;

        config = new LocalCluster("plansgroupby-onesite.jar", 1, 1, 0, BackendTarget.NATIVE_EE_JNI);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        config = new LocalCluster("plansgroupby-hsql.jar", 1, 1, 0, BackendTarget.HSQLDB_BACKEND);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        // Cluster
        config = new LocalCluster("plansgroupby-cluster.jar", 2, 3, 1, BackendTarget.NATIVE_EE_JNI);
        success = config.compile(project);
        assertTrue(success);
        builder.addServerConfig(config);

        return builder;
    }
}