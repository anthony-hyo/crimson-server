package com.crimson.bakuretsu;

import com.crimson.bakuretsu.core.Model;
import com.crimson.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;

public class Database {

    private static HikariPoolMXBean poolProxy;

    public static volatile HikariDataSource hikariDataSource;

    public static void close() {

    }

    public static void open() {

    }

    public static void openTransaction() {

    }

    public static void rollback() {

    }

    public static void commit() {

    }

    public static HikariPoolMXBean monitor() {
        return poolProxy;
    }

    public static DataSource pool() {
        if (hikariDataSource == null) {
            setup();
        }
        return hikariDataSource;
    }

    private static void setup() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(Config.singleton().database().JDBC_URL());
        config.setUsername(Config.singleton().database().user());
        config.setPassword(Config.singleton().database().password());

        config.setMaximumPoolSize(30);
        config.setRegisterMbeans(true);
        config.setPoolName("Game");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        hikariDataSource = new HikariDataSource(config);

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        Model.setDataSource(hikariDataSource);

        try {
            ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (Game)");
            poolProxy = JMX.newMXBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

}
