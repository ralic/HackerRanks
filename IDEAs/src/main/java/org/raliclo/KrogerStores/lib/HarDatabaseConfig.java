/**
 * BenchLab: Internet Scale Benchmarking.
 * Copyright (C) 2010-2011 Emmanuel Cecchet.
 * Contact: cecchet@cs.umass.edu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Initial developer(s): karlholl (sf.net)
 * Contributor(s): Emmanuel Cecchet. , Ralic Lo
 */
package org.raliclo.KrogerStores.lib;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines a HarDatabaseConfig
 *
 * @author <a href="mailto:manu@frogthinker.org">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class HarDatabaseConfig {
    private String driverClassName;
    private String jdbcUrl;
    private String login;
    private String password;
    private String tablePrefix;
    private String dbAutoGeneratedId = "BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY";
    private String stringDbType = "CLOB";
    private String longDbType = "BIGINT";
    private String timestampDbType = "TIMESTAMP";
    private String smallIntDbType = "SMALLINT";

    // Booleans that are set when the table has been created
    // Note: inserted keys are uppercased
    private List<String> createdTables = new LinkedList<String>();

    // private List<Connection> connectionPool = new LinkedList<Connection>();
    private Connection con = null;

    /**
     * Creates a new <code>HarDatabaseConfig</code> object to store/retrieve HAR
     * representations in/from a database.
     *
     * @param driverClassName   Class name of the database driver
     * @param jdbcUrl           the JDBC URL to access the databsae
     * @param login             database user login
     * @param password          database user password
     * @param tablePrefix       prefix to use in front of table names
     * @param dbAutoGeneratedId database type to define an auto-generated key (if
     *                          null, "BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY" is
     *                          used)
     * @param stringDbType      database type to define a string type (if null,
     *                          "LONG VARCHAR" is used)
     * @param longDbType        database type to define a long type (if null, "BIGINT" is
     *                          used)
     * @param timestampDbType   type to define a timestamp type (if null,
     *                          "TIMESTAMP" is used)
     * @param smallIntDbType    type to defined a small int type (if null, "SMALLINT"
     *                          is used)
     * @throws SQLException if the database driver cannot be loaded
     */
    public HarDatabaseConfig(String driverClassName, String jdbcUrl,
                             String login, String password, String tablePrefix,
                             String dbAutoGeneratedId, String stringDbType, String longDbType,
                             String timestampDbType, String smallIntDbType) throws SQLException {
        try {
            Class.forName(driverClassName).newInstance();
        } catch (Exception e) {
            throw new SQLException("Failed to instantiate database driver "
                    + driverClassName + " (" + e + ")", e);
        }
        this.driverClassName = driverClassName;
        this.jdbcUrl = jdbcUrl;
        this.login = login;
        this.password = password;
        this.tablePrefix = tablePrefix;
        if (dbAutoGeneratedId != null)
            this.dbAutoGeneratedId = dbAutoGeneratedId;
        if (stringDbType != null)
            this.stringDbType = stringDbType;
        if (longDbType != null)
            this.longDbType = longDbType;
        if (timestampDbType != null)
            this.timestampDbType = timestampDbType;
        if (smallIntDbType != null)
            this.smallIntDbType = smallIntDbType;

        con = DriverManager.getConnection(jdbcUrl, login, password);
        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        this.initCreatedTables();

    }

    private void initCreatedTables() throws SQLException {
        Connection c = this.getConnection();

        DatabaseMetaData dmd = c.getMetaData();
        ResultSet res = dmd.getTables(null, null, null, new String[]{"TABLE"});
        while (res.next()) {
            this.addCreatedTable(res.getString("TABLE_NAME"));
        }
        res.close();
        this.closeConnection(c);
    }

    /**
     * Return the list of HarLog ids stored in the database. Use new
     * {@link HarLog#HarLog(HarDatabaseConfig, long)} to fetch HarLog entries.
     *
     * @return list of HarLog ids
     * @throws SQLException if a db error occurs
     */
    public List<Long> getHarLogIds() throws SQLException {
        Connection c = null;
        Statement s = null;
        ResultSet rs = null;
        List<Long> list = new ArrayList<Long>();
        try {
            c = getConnection();
            s = c.createStatement();
            rs = s.executeQuery("SELECT id FROM " + getTablePrefix()
                    + HarLog.TABLE_NAME);
            while (rs.next()) {
                list.add(rs.getLong(1));
            }
            return list;
        } catch (Exception e) {
            return list;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception ignore) {
            }
            try {
                if (s != null)
                    s.close();
            } catch (Exception ignore) {
            }
            closeConnection(c);
        }
    }

    /**
     * Return the number of entries stored in the database table
     *
     * @param tableName name of the table to COUNT(*)
     * @return number of entries in the given table
     * @throws SQLException if a db error occurs
     */
    public long getNbOfEntries(String tableName) throws SQLException {
        Connection c = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            s = c.createStatement();
            rs = s.executeQuery("SELECT COUNT(*) FROM " + getTablePrefix()
                    + tableName);
            rs.next();
            return rs.getLong(1);
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception ignore) {
            }
            try {
                if (s != null)
                    s.close();
            } catch (Exception ignore) {
            }
            closeConnection(c);
        }
    }

    /**
     * Retrieve a database connection using the current configuration. Note: this
     * returned Collection object is always the same since we want to disable
     * autocommit.
     *
     * @return a connection to the database
     * @throws SQLException if an error occured creating the database
     */

    public Connection getConnection() throws SQLException {
        return con;
    }

    /**
     * Close the given connection (actually put it back as is in the connection
     * pool)
     *
     * @param c connection to close
     */
    public void closeConnection(Connection c) {
        // Not needed for now
    }

    /**
     * Write this object in the given database referencing the specified logId.
     *
     * @param logId         the logId this object refers to
     * @param config        the database configuration
     * @param tableName     name of the table to insert data into (without the table
     *                      prefix)
     * @param nameColumn    name of the name column
     * @param nameValue     value of the name field
     * @param valueColumn   name of the value column
     * @param valueValue    value of the value field
     * @param commentColumn name of the comment column
     * @param commentValue  value of the comment field
     * @throws SQLException if a database access error occurs *
     */
    public long writeNameValueCommentJDBC(long logId, HarDatabaseConfig config,
                                          String tableName, String nameColumn, String nameValue,
                                          String valueColumn, String valueValue, String commentColumn,
                                          String commentValue) throws SQLException {
        long returnedId = -1;
        Connection c = config.getConnection();
        tableName = config.getTablePrefix() + tableName;
        if (!config.isCreatedTable(tableName)) {
            try {
                Statement s = c.createStatement();
                s.executeUpdate("CREATE TABLE " + tableName + " (id "
                        + config.getDbAutoGeneratedId() + "," + nameColumn + " "
                        + config.getStringDbType() + "," + valueColumn + " "
                        + config.getStringDbType() + "," + commentColumn + " "
                        + config.getStringDbType() + ", log_id " + config.getLongDbType()
                        + ")");
                s.close();
                config.addCreatedTable(tableName);
            } catch (Exception ignore) { // Database table probably already exists
            }
        }
        PreparedStatement ps = null;
        try {
            ps = c.prepareStatement("INSERT INTO " + tableName + "(" + nameColumn
                            + "," + valueColumn + "," + commentColumn
                            + ",log_id) VALUES (?,?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, nameValue);
            ps.setString(2, valueValue);
            if (commentValue == null)
                ps.setNull(3, Types.LONGVARCHAR);
            else
                ps.setString(3, commentValue);
            ps.setLong(4, logId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next())
                throw new SQLException(
                        "The database did not generate a key for an HarEntry row");
            returnedId = rs.getLong(1);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (Exception ignore) {
            }
            config.closeConnection(c);
        }
        return returnedId;
    }

    /**
     * Check if a table is empty with a COUNT(*) and drop it if it is.
     *
     * @param c         an open connection to the database
     * @param tableName the name of the table to check
     * @param config    database config to be updated if the table is dropped (cache
     *                  update)
     * @throws SQLException if an error occurs
     */
    public void dropTableIfEmpty(Connection c, String tableName,
                                 HarDatabaseConfig config) throws SQLException {
        Statement s = null;
        ResultSet rs = null;
        try {
            s = c.createStatement();
            rs = s.executeQuery("SELECT COUNT(*) FROM " + tableName);
            rs.next();
            if (rs.getLong(1) == 0) {
                s.executeUpdate("DROP TABLE " + tableName);
                config.removeTable(tableName);
            }
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception ignore) {
            }
            try {
                if (s != null)
                    s.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Delete objects in the given database table referencing the specified logId.
     * Drop the table if empty after deleting elements.
     *
     * @param logId     the logId this object refers to
     * @param config    the database configuration
     * @param tableName name of the table to insert data into (without the table
     *                  prefix)
     * @throws SQLException if a database access error occurs *
     */
    public void deleteFromTable(long logId, HarDatabaseConfig config,
                                String tableName) throws SQLException {
        Connection c = config.getConnection();
        tableName = config.getTablePrefix() + tableName;
        PreparedStatement ps = null;
        try {
            ps = c.prepareStatement("DELETE FROM " + tableName + " WHERE log_id=?");
            ps.setLong(1, logId);
            ps.executeUpdate();
            config.dropTableIfEmpty(c, tableName, config);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (Exception ignore) {
            }
            config.closeConnection(c);
        }
    }

    /**
     * Add a table to the list of tables that have been created
     *
     * @param tableName the table that has been created (or already exists)
     */
    public void addCreatedTable(String tableName) {
        createdTables.add(tableName.toUpperCase());
    }

    /**
     * Returns true if the given table name is in the list of created tables
     *
     * @param tableName the table to look for
     * @return true if the table has been set as created
     */
    public boolean isCreatedTable(String tableName) {
        return createdTables.contains(tableName.toUpperCase());
    }

    /**
     * Remove a table to the list of tables that have been created (cache
     * invalidation)
     *
     * @param tableName the table that has been removed
     */
    public void removeTable(String tableName) {
        createdTables.remove(tableName.toUpperCase());
    }

    /**
     * Returns the driverClassName value.
     *
     * @return Returns the driverClassName.
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Sets the driverClassName value.
     *
     * @param driverClassName The driverClassName to set.
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Returns the jdbcUrl value.
     *
     * @return Returns the jdbcUrl.
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Sets the jdbcUrl value.
     *
     * @param jdbcUrl The jdbcUrl to set.
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Returns the login value.
     *
     * @return Returns the login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login value.
     *
     * @param login The login to set.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Returns the password value.
     *
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password value.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the tablePrefix value.
     *
     * @return Returns the tablePrefix.
     */
    public String getTablePrefix() {
        return tablePrefix;
    }

    /**
     * Sets the tablePrefix value.
     *
     * @param tablePrefix The tablePrefix to set.
     */
    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    /**
     * Returns the dbAutoGeneratedId value.
     *
     * @return Returns the dbAutoGeneratedId.
     */
    public String getDbAutoGeneratedId() {
        return dbAutoGeneratedId;
    }

    /**
     * Sets the dbAutoGeneratedId value.
     *
     * @param dbAutoGeneratedId The dbAutoGeneratedId to set.
     */
    public void setDbAutoGeneratedId(String dbAutoGeneratedId) {
        this.dbAutoGeneratedId = dbAutoGeneratedId;
    }

    /**
     * Returns the stringDbType value.
     *
     * @return Returns the stringDbType.
     */
    public String getStringDbType() {
        return stringDbType;
    }

    /**
     * Sets the stringDbType value.
     *
     * @param stringDbType The stringDbType to set.
     */
    public void setStringDbType(String stringDbType) {
        this.stringDbType = stringDbType;
    }

    /**
     * Returns the smallIntDbType value.
     *
     * @return Returns the smallIntDbType.
     */
    public String getSmallIntDbType() {
        return smallIntDbType;
    }

    /**
     * Sets the smallIntDbType value.
     *
     * @param smallIntDbType The smallIntDbType to set.
     */
    public void setSmallIntDbType(String smallIntDbType) {
        this.smallIntDbType = smallIntDbType;
    }

    /**
     * Returns the longDbType value.
     *
     * @return Returns the longDbType.
     */
    public String getLongDbType() {
        return longDbType;
    }

    /**
     * Sets the longDbType value.
     *
     * @param longDbType The longDbType to set.
     */
    public void setLongDbType(String longDbType) {
        this.longDbType = longDbType;
    }

    /**
     * Returns the timestampDbType value.
     *
     * @return Returns the timestampDbType.
     */
    public String getTimestampDbType() {
        return timestampDbType;
    }

    /**
     * Sets the timestampDbType value.
     *
     * @param timestampDbType The timestampDbType to set.
     */
    public void setTimestampDbType(String timestampDbType) {
        this.timestampDbType = timestampDbType;
    }

}