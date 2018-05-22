package com.sap.cloud.lm.sl.cf.core.liquibase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration.DatabaseType;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

public class AlterOperationTableTimestampStoringColumns extends
    AbstractDataTransformationChange<List<AlterOperationTableTimestampStoringColumns.OriginalOperation>, List<AlterOperationTableTimestampStoringColumns.TransformedOperation>> {

    private static final String SELECT_STATEMENT = "SELECT PROCESS_ID, STARTED_AT, ENDED_AT FROM OPERATION";
    private static final String UPDATE_STATEMENT = "UPDATE OPERATION SET STARTED_AT=?, ENDED_AT=? WHERE PROCESS_ID=?";
    private static final String[] POSTGRESQL_ALTER_STATEMENTS = new String[] {
        // @formatter:off
        "ALTER TABLE OPERATION DROP STARTED_AT",
        "ALTER TABLE OPERATION DROP ENDED_AT",
        "ALTER TABLE OPERATION ADD STARTED_AT TIMESTAMP",
        "ALTER TABLE OPERATION ADD ENDED_AT TIMESTAMP",
        // @formatter:on
    };
    private static final String[] HANA_ALTER_STATEMENTS = new String[] {
        // @formatter:off
        "ALTER TABLE OPERATION DROP (STARTED_AT, ENDED_AT)",
        "ALTER TABLE OPERATION ADD (STARTED_AT TIMESTAMP, ENDED_AT TIMESTAMP)"
        // @formatter:on
    };

    private String[] alterStatements;

    @Override
    public void setUp() throws SetupException {
        DatabaseType databaseType = ApplicationConfiguration.getInstance().getDatabaseType();
        switch (databaseType) {
            case POSTGRESQL:
                alterStatements = POSTGRESQL_ALTER_STATEMENTS;
                break;
            case HANA:
                alterStatements = HANA_ALTER_STATEMENTS;
                break;
            default:
                // Normally, one would throw an exception here, but we can't do that because the AuditLogManagerTest will fail.
                logger.warn(MessageFormat.format(Messages.UNKNOWN_DB_TYPE_WILL_NOT_ALTER_OPERATION_TABLE_COLUMN_TYPES, databaseType));
        }
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        if (alterStatements == null) {
            return;
        }
        super.execute(database);
    }

    @Override
    public List<OriginalOperation> extractData(ResultSet resultSet) throws SQLException {
        List<OriginalOperation> result = new ArrayList<>();
        while (resultSet.next()) {
            OriginalOperation operation = extractOperation(resultSet);
            result.add(operation);
            logger.debug(String.format("Retrieved operation with ID '%s' that started at '%s' and ended at '%s'.", operation.processId,
                operation.startedAt, operation.endedAt));
        }
        return result;
    }

    private OriginalOperation extractOperation(ResultSet resultSet) throws SQLException {
        OriginalOperation operation = new OriginalOperation();
        operation.processId = resultSet.getString("PROCESS_ID");
        operation.startedAt = resultSet.getString("STARTED_AT");
        operation.endedAt = resultSet.getString("ENDED_AT");
        return operation;
    }

    @Override
    public List<TransformedOperation> transformData(List<OriginalOperation> retrievedData) {
        return retrievedData.stream().map(operation -> transformOperation(operation)).collect(Collectors.toList());
    }

    private TransformedOperation transformOperation(OriginalOperation originalOperation) {
        TransformedOperation result = new TransformedOperation();
        result.processId = originalOperation.processId;
        result.startedAt = toTimestamp(originalOperation.startedAt);
        result.endedAt = toTimestamp(originalOperation.endedAt);
        return result;
    }

    private Timestamp toTimestamp(String zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        ZonedDateTime parsedZonedDateTime = ZonedDateTime.parse(zonedDateTime, Operation.DATE_TIME_FORMATTER);
        return new Timestamp(parsedZonedDateTime.toInstant().toEpochMilli());
    }

    @Override
    public void setUpdateStatementParameters(PreparedStatement preparedStatement, List<TransformedOperation> transformedData)
        throws SQLException {
        for (TransformedOperation operation : transformedData) {
            preparedStatement.setTimestamp(1, operation.startedAt);
            preparedStatement.setTimestamp(2, operation.endedAt);
            preparedStatement.setString(3, operation.processId);
            preparedStatement.addBatch();
            logger.debug(String.format("Updated operation with ID '%s' with start time: '%s' and end time '%s'.", operation.processId,
                operation.startedAt, operation.endedAt));
        }
    }

    @Override
    public String getSelectStatement() {
        return SELECT_STATEMENT;
    }

    @Override
    public String getUpdateStatement() {
        return UPDATE_STATEMENT;
    }

    @Override
    public String[] getAlterStatements() {
        return alterStatements;
    }

    @Override
    public String getConfirmationMessage() {
        return Messages.ALTERED_DATA_TYPES_FOR_OPERATION_TABLE;
    }

    static class OriginalOperation {

        String processId;
        String startedAt;
        String endedAt;

    }

    static class TransformedOperation {

        String processId;
        Timestamp startedAt;
        Timestamp endedAt;

    }

}
