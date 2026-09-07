package com.titran.pingcortex.utils;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@AllArgsConstructor
public class ManagedConnection implements AutoCloseable {
    private final Connection connection;
    private final DataSource dataSource;

    public static ManagedConnection open(DataSource dataSource) {
        return new ManagedConnection(DataSourceUtils.getConnection(dataSource), dataSource);
    }

    public Connection get() {
        return connection;
    }

    @Override
    public void close() {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
