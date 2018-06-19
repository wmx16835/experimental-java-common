package mingxin.wang.common;

import com.google.common.collect.ImmutableList;
import com.mysql.cj.jdbc.MysqlDataSource;
import mingxin.wang.common.db.MultiDatabaseTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public class DataSourcesTest {
    private static DataSource makeMysqlDataSource(String url, String user, String password) {
        MysqlDataSource result = new MysqlDataSource();
        result.setUrl(url);
        result.setUser(user);
        result.setPassword(password);
        return result;
    }

    private static Iterable<String> makeOtaAddPosAreaSql(String tableName) {
        return ImmutableList.of(
                "ALTER TABLE " + tableName + " ADD `pos_area` varchar(50) NOT NULL DEFAULT '' COMMENT 'pos地区'",
                "DELETE FROM " + tableName + " WHERE `gds_type`<>1 AND `half_rt`=0",
                "UPDATE " + tableName + " SET `pos_area`='中国'");
    }

    private static Iterable<String> makeOtaSetPosAreaSql(String tableName) {
        return ImmutableList.of(
                "UPDATE " + tableName + " SET `pos_area`='中国'");
    }

    private static Iterable<String> makeOtaClearHalfRtPosAreaSql(String tableName) {
        return ImmutableList.of(
                "UPDATE " + tableName + " SET `pos_area`='' WHERE `half_rt`=1");
    }

    private static Iterable<String> setAmadeusPosAreaHanSql(String tableName) {
        return ImmutableList.of(
                "UPDATE " + tableName + " SET `pos_area`='韩国' WHERE `gds_type`=3");
    }

    private static Iterable<String> recoverTuanMaxAmount(String tableName) {
        return ImmutableList.of(
                "ALTER TABLE " + tableName + " ADD `tuan_max_amount` tinyint(2) NOT NULL DEFAULT '9' COMMENT '最大团体人数'",
                "ALTER TABLE " + tableName + " DROP COLUMN `tuan_max_amoun`");
    }

    private static void addCharacterType(String tableName, Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " add charter_type tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否包机，0非包机，1包机'");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
//
//        // 政策库
//        new MultiDatabaseTransaction(makeMysqlDataSource(
//                "jdbc:mysql://10.86.32.254:3309/?useUnicode=true&characterEncoding=utf8", "ttsuser", "HTDaBeJ81Nv2")) {
//
//            @Override
//            protected boolean isTargetDatabase(String dbName) {
//                return dbName.startsWith("ttsgj");
//            }
//
//            @Override
//            protected boolean isTargetTable(String dbName, String tableName) {
//                return tableName.contains("ota") && !tableName.contains("multitrans");
//            }
//
//            @Override
//            protected void execute(String dbName, String tableName, Connection connection) throws SQLException {
//                addCharacterType(tableName, connection);
//            }
//        }.execute(executorService);
//
//        // 搜索库 Master
//        new MultiDatabaseTransaction(makeMysqlDataSource(
//                "jdbc:mysql://10.86.33.29:3317?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", "ttsuser", "x6zitYxhpidSdur7")) {
//
//            @Override
//            protected boolean isTargetDatabase(String dbName) {
//                return dbName.startsWith("SearchDB_");
//            }
//
//            @Override
//            protected boolean isTargetTable(String dbName, String tableName) {
//                return tableName.startsWith("return_policy_") || tableName.startsWith("single_policy_") || tableName.startsWith("ota_");
//            }
//
//            @Override
//            protected void execute(String dbName, String tableName, Connection connection) throws SQLException {
//                addCharacterType(tableName, connection);
//            }
//        }.execute(executorService);

        // 搜索库 Master
        new MultiDatabaseTransaction(makeMysqlDataSource(
                "jdbc:mysql://10.86.33.29:3317?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", "ttsuser", "x6zitYxhpidSdur7")) {

            @Override
            protected boolean isTargetDatabase(String dbName) {
                return dbName.startsWith("SearchDB_");
            }

            @Override
            protected boolean isTargetTable(String dbName, String tableName) {
                return tableName.startsWith("return_policy_") || tableName.startsWith("single_policy_") || tableName.startsWith("ota_");
            }

            @Override
            protected void execute(String dbName, String tableName, Connection connection) throws SQLException {
                addCharacterType(tableName, connection);
            }
        }.execute(executorService);

        executorService.shutdown();
    }
}
