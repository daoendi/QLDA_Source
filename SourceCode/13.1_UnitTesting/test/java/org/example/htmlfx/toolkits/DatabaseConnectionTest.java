package org.example.htmlfx.toolkits;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;
import org.mockito.MockedStatic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseConnectionTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @Test
    public void testGetConnectionReturnsExistingConnection() throws SQLException {
        Connection proxyConn = createConnectionProxy(false);

        // Simulate an existing open connection by assigning it to the static field
        DatabaseConnection.databaseLink = proxyConn;

        Connection c = DatabaseConnection.getConnection();
        assertThat(c).isNotNull();
        assertThat(c).isSameAs(proxyConn);
    }

    @Test
    public void testGetConnectionRecreatesWhenClosed() throws SQLException {
        Connection proxyConn = createConnectionProxy(true);

        DatabaseConnection.databaseLink = proxyConn;

        // Simulate that the previously stored connection is closed;
        // calling getConnection() should attempt to create/return a different connection (or null if real connection fails).
        Connection c = DatabaseConnection.getConnection();
        // At minimum, it should not return the old closed connection proxy
        assertThat(c).isNotSameAs(proxyConn);
    }

    // Helper: create a lightweight dynamic proxy implementing Connection that reports the specified closed state
    private Connection createConnectionProxy(boolean closed) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("isClosed".equals(name)) {
                    return closed;
                }
                // default return values for unhandled methods: false for boolean, 0 for numeric, null otherwise
                Class<?> ret = method.getReturnType();
                if (ret == boolean.class) return false;
                if (ret == int.class) return 0;
                if (ret == long.class) return 0L;
                return null;
            }
        };

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                handler);
    }
}
