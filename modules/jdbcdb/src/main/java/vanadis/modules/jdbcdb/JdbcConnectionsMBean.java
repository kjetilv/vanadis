/*
 * Copyright 2008 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanadis.modules.jdbcdb;

import vanadis.core.lang.Not;
import vanadis.services.db.ConnectionsMBean;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class JdbcConnectionsMBean implements ConnectionsMBean {

    private final JdbcConnections jdbcConnections;

    public JdbcConnectionsMBean(JdbcConnections jdbcConnections) {
        this.jdbcConnections = Not.nil(jdbcConnections, "jdbc connections");
    }

    @Override
    public String query(String sql) {
        CallableStatement statement;
        try {
            statement = statement(sql);
        } catch (SQLException e) {
            return e.toString();
        }
        try {
            return String.valueOf(statement.executeQuery());
        } catch (SQLException e) {
            return e.toString();
        }
    }

    @Override
    public String command(String sql) {
        CallableStatement statement;
        try {
            statement = statement(sql);
        } catch (SQLException e) {
            return e.toString();
        }
        try {
            return String.valueOf(statement.execute());
        } catch (SQLException e) {
            return e.toString();
        }
    }

    @Override
    public String update(String sql) {
        CallableStatement statement;
        try {
            statement = statement(sql);
        } catch (SQLException e) {
            return e.toString();
        }
        try {
            return String.valueOf(statement.executeUpdate());
        } catch (SQLException e) {
            return e.toString();
        }
    }

    private CallableStatement statement(String sql) throws SQLException {
        return jdbcConnections.get().prepareCall(sql);
    }
}
