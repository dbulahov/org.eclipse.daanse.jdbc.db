/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.jdbc.db.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.StructureInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableMetaData;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.CatalogReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class DatabaseServiceImplH2Test {

    private DatabaseService databaseService = new DatabaseServiceImpl();

    private String catalogName = UUID.randomUUID().toString().toUpperCase();

    private DataSource ds() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:memFS:" + catalogName);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    private void setupData(Connection connection) throws SQLException {
        String sql = "Create table test (ID int primary key, name varchar(50), val numeric(10,3) NOT NULL, birthday date NOT NULL, t time NOT NULL)";

        Statement statement = connection.createStatement();

        statement.execute(sql);

        System.out.println("Created test table.");

        sql = "Insert into test (ID, name, val, birthday, t) values (1, 'name', 13.3, '1973-01-07', '18:20:59')";

        int rows = statement.executeUpdate(sql);

        if (rows > 0) {
            System.out.println("Inserted a new row.");
        }
        connection.commit();
    }

    @Test
    void createMetaDataTest() throws SQLException {
        DataSource ds = ds();
        MetaInfo metaInfo = databaseService.createMetaInfo(ds);
        assertThat(metaInfo).isNotNull();
    }

    @Test
    void getTypeInfoTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<TypeInfo> types = databaseService.getTypeInfo(databaseMetaData);
        assertThat(types).isNotNull().isNotEmpty();
    }

    @Test
    void getCatalogsTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<CatalogReference> catalogs = databaseService.getCatalogs(databaseMetaData);

        assertThat(catalogs).isNotNull().isNotEmpty();
    }

    @Test
    void getSchemasTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<SchemaReference> schemas = databaseService.getSchemas(databaseMetaData);
        assertThat(schemas).isNotNull().isNotEmpty();
    }

    @Test
    void getTablesTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<TableDefinition> tables = databaseService.getTableDefinitions(databaseMetaData);
        assertThat(tables).isNotNull().isNotEmpty();
    }

    @Test
    void getTableTypesTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<String> tableTypes = databaseService.getTableTypes(databaseMetaData);
        assertThat(tableTypes).isNotNull().isNotEmpty();
    }

    @Test
    void tableExistsTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();

        //
        boolean constantsTableExists = databaseService.tableExists(databaseMetaData, new TableReferenceR("CONSTANTS"));
        assertThat(constantsTableExists).isTrue();

        //
        constantsTableExists = databaseService.tableExists(databaseMetaData, new TableReferenceR("CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR("CONSTANTS", "BASE TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        Optional<SchemaReference> oSchema = Optional.of(new SchemaReferenceR("INFORMATION_SCHEMA"));
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR(oSchema, "CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        Optional<CatalogReference> oCatalog = Optional.of(new CatalogReferenceR(catalogName));
        oSchema = Optional.of(new SchemaReferenceR(oCatalog, "INFORMATION_SCHEMA"));
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR(oSchema, "CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();
    }

    @Test
    void tableColumnTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();

        //
        boolean exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR("USER_NAME"));
        assertThat(exists).isTrue();

        //
        Optional<TableReference> oTable = Optional.of(new TableReferenceR("USERS"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        oTable = Optional.of(new TableReferenceR("USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        oTable = Optional.of(new TableReferenceR("USERS", "BASE TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        Optional<SchemaReference> oSchema = Optional.of(new SchemaReferenceR("INFORMATION_SCHEMA"));
        oTable = Optional.of(new TableReferenceR(oSchema, "USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //

        Optional<CatalogReference> oCatalog = Optional.of(new CatalogReferenceR(catalogName));
        oSchema = Optional.of(new SchemaReferenceR(oCatalog, "INFORMATION_SCHEMA"));
        oTable = Optional.of(new TableReferenceR(oSchema, "USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();
    }

    @Test
    void tableColumnTestWithCustomerTables() throws SQLException {
        DataSource ds = ds();
        try (Connection connection = ds.getConnection()) {
            setupData(connection);
            DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
            Optional<CatalogReference> oCatalog = Optional.of(new CatalogReferenceR(catalogName));
            Optional<SchemaReference> oSchema = Optional.of(new SchemaReferenceR(oCatalog, "PUBLIC"));
            Optional<TableReference> oTable = Optional.of(new TableReferenceR(oSchema, "TEST", "BASE TABLE"));
            List<TableDefinition> tableDefinitions = databaseService.getTableDefinitions(databaseMetaData, oSchema.get());
            assertThat(tableDefinitions).isNotNull().hasSize(1);
            boolean exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "ID"));
            assertThat(exists).isTrue();
            exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "NAME"));
            assertThat(exists).isTrue();
            exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "VAL"));
            assertThat(exists).isTrue();
            exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "T"));
            assertThat(exists).isTrue();
            List<ColumnDefinition> columnDefinitions = databaseService.getColumnDefinitions(databaseMetaData, oTable.get());
            assertThat(columnDefinitions).isNotNull().hasSize(5);
        }
    }

    @Test
    void testWithCustomerTables() throws SQLException {
        DataSource ds = ds();
        try (Connection connection = ds.getConnection()) {
            setupData(connection);
            MetaInfo metaInfo = databaseService.createMetaInfo(connection);

            StructureInfo structureInfo = metaInfo.structureInfo();
            assertThat(structureInfo).isNotNull();

            //check catalog
            assertThat(structureInfo.catalogs()).isNotNull().hasSize(1);
            assertThat(structureInfo.catalogs().get(0)).isNotNull();
            assertThat(structureInfo.catalogs().get(0).name()).isNotNull();

            //check schemas
            assertThat(structureInfo.schemas()).isNotNull().hasSize(2);
            assertThat(structureInfo.schemas().stream().anyMatch(s -> "PUBLIC".equals(s.name()))).isTrue();
            assertThat(structureInfo.schemas().stream().anyMatch(s -> "INFORMATION_SCHEMA".equals(s.name()))).isTrue();
            Optional<SchemaReference> oSchemaReference = structureInfo.schemas().stream().filter(s -> "PUBLIC".equals(s.name())).findAny();
            assertThat(oSchemaReference).isNotNull().isPresent();
            assertThat(oSchemaReference.get()).isNotNull();
            assertThat(oSchemaReference.get().name()).isNotNull();
            assertThat(oSchemaReference.get().catalog()).isNotNull().isPresent();

            //check tables
            assertThat(structureInfo.tables()).isNotNull().hasSize(36);
            assertThat(structureInfo.tables().stream().anyMatch(t -> "TEST".equals(t.table().name()))).isTrue();
            Optional<TableDefinition> oTableDefinition = structureInfo.tables().stream().filter(t -> "TEST".equals(t.table().name())).findAny();
            assertThat(oTableDefinition).isNotNull().isPresent();
            assertThat(oTableDefinition.get()).isNotNull();
            TableReference tableReference = oTableDefinition.get().table();
            assertThat(tableReference).isNotNull();
            assertThat(tableReference.name()).isNotNull();
            assertThat(tableReference.schema()).isNotNull().isPresent();
            assertThat(tableReference.schema().get()).isNotNull();
            assertThat(tableReference.schema().get().name()).isNotNull();
            TableMetaData tableMetaData = oTableDefinition.get().tableMetaData();
            assertThat(tableMetaData).isNotNull();

            //check columns
            assertThat(structureInfo.columns()).isNotNull().isNotEmpty();
            assertThat(structureInfo.columns().get(0)).isNotNull();
            ColumnDefinition columnDefinition = structureInfo.columns().get(0);
            assertThat(columnDefinition.column()).isNotNull();
            assertThat(columnDefinition.column().name()).isNotNull();
            assertThat(columnDefinition.column().table()).isNotNull();
            Optional<TableReference> oTableReference = columnDefinition.column().table();
            assertThat(oTableReference).isNotNull().isPresent();
            assertThat(oTableReference.get()).isNotNull();
            assertThat(oTableReference.get().name()).isNotNull();
            assertThat(oTableReference.get().schema()).isNotNull().isPresent();
            assertThat(oTableReference.get().schema().get()).isNotNull();
            assertThat(oTableReference.get().schema().get().name()).isNotNull();
        }
    }

}
