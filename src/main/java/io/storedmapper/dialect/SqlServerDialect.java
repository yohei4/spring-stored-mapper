package io.storedmapper.dialect;

import java.util.List;

/**
 * SQL Server方言。
 *
 * <p>識別子を {@code [schema].[name]} 形式でクォートします。</p>
 *
 * @since 1.0.0
 */
public class SqlServerDialect implements DbDialect {

    @Override
    public String formatFullName(String schema, String name) {
        return "[" + schema + "].[" + name + "]";
    }

    @Override
    public String createTableFunctionQuery(String fullName, List<String> parameters, String orderByExpression) {
        var sql = "SELECT * FROM " + fullName + "(" + String.join(",", parameters) + ")";
        if (orderByExpression != null && !orderByExpression.isBlank()) {
            sql += " ORDER BY " + orderByExpression;
        }
        return sql;
    }

    @Override
    public String createScalarFunctionQuery(String fullName, List<String> parameters) {
        return "SELECT " + fullName + "(" + String.join(",", parameters) + ")";
    }

    @Override
    public String createStoredProcedureCall(String fullName, List<String> parameters) {
        return "{call " + fullName + "(" + String.join(",", parameters) + ")}";
    }
}
