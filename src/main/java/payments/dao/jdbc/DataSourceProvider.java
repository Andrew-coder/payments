package payments.dao.jdbc;

import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.sql.DataSource;
import java.util.ResourceBundle;

/**
 * this class give connection pool with apache tomcat implementation
 */
public class DataSourceProvider {
    private static final String DB_PROPERTIES = "db";
    private static final String JDBC_URL = "jdbc.url";
    private static final String JDBC_DRIVER = "jdbc.driver";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    private static class InstanceHolder{
        private static final DataSource instance = setupDataSource();
    }

    public static DataSource getInstance(){
        return InstanceHolder.instance;
    }

    public static DataSource setupDataSource() {
        ResourceBundle dbProperties = ResourceBundle.getBundle(DB_PROPERTIES);
        PoolProperties properties = new PoolProperties();
        properties.setDriverClassName(dbProperties.getString(JDBC_DRIVER));
        properties.setUrl(dbProperties.getString(JDBC_URL));
        properties.setUsername(dbProperties.getString(USER));
        properties.setPassword(dbProperties.getString(PASSWORD));
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }
}