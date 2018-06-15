package mingxin.wang.common.db;

import mingxin.wang.common.concurrent.ConcurrentInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public abstract class MultiDatabaseTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatabaseTransaction.class);

    private final DataSource dataSource;

    protected MultiDatabaseTransaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final void execute(Executor executor) throws InterruptedException {
        AtomicLong succeeded = new AtomicLong(), fail = new AtomicLong();
        Collection<String> dbNames;
        try (Connection conn = dataSource.getConnection()) {
            dbNames = getNames(conn, "SHOW DATABASES");
        } catch (Exception e) {
            LOGGER.error("Failed to create connection.", e);
            return;
        }
        ConcurrentInvoker invoker = new ConcurrentInvoker();
        for (String dbName : dbNames) {
            if (!isTargetDatabase(dbName)) {
                continue;
            }
            invoker.add(executor, () -> {
                try (Connection conn = dataSource.getConnection()) {
                    try (Statement stmt = conn.createStatement()) {
                        conn.setAutoCommit(false);
                        stmt.execute("USE " + dbName);
                    }
                    for (String tableName : getNames(conn, "SHOW TABLES")) {
                        try {
                            if (!isTargetTable(dbName, tableName)) {
                                continue;
                            }
                            execute(dbName, tableName, conn);
                            conn.commit();
                            LOGGER.info("Successful transaction, database='{}', table='{}'", dbName, tableName);
                            succeeded.getAndIncrement();
                        } catch (Throwable t) {
                            conn.rollback();
                            LOGGER.error("Abortive transaction, database='{}', table='{}'.", dbName, tableName, t);
                            fail.getAndIncrement();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Could not select database '{}'.", dbName, e);
                }
            });
        }
        invoker.syncInvoke();
        LOGGER.info("Transactions done, succeeded {}, failed {}.", succeeded.get(), fail.get());
    }

    protected abstract boolean isTargetDatabase(String dbName);

    protected abstract boolean isTargetTable(String dbName, String tableName);

    protected abstract void execute(String dbName, String tableName, Connection connection) throws Exception;

    private Collection<String> getNames(Connection connection, String sql) throws SQLException {
        ArrayList<String> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet tablesResultSet = stmt.executeQuery(sql);
            while (tablesResultSet.next()) {
                result.add(tablesResultSet.getString(1));
            }
        }
        return result;
    }
}
