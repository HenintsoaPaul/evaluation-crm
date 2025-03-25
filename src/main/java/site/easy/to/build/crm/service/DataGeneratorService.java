package site.easy.to.build.crm.service;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DataGeneratorService {

    public record ColumnMeta(
            String name,
            String typeName,
            int columnSize,
            int numericPrecision,
            int decimalDigits,
            boolean isPrimaryKey,
            boolean isAutoIncrement,
            boolean isNullable
    ) {
    }

    public record FkColumn(
            String name,
            String refTableName,
            String refColumnName
    ) {
    }

    private final JdbcTemplate jdbcTemplate;
    private static final Faker faker = new Faker(new Locale("fr"));
    private static final Map<String, Function<ColumnMeta, Object>> TYPE_HANDLERS = new HashMap<>();
    private static final Set<String> SKIP_GENERATION_TYPES = Set.of("BLOB", "BINARY", "VARBINARY");

    static {
        TYPE_HANDLERS.put("VARCHAR", DataGeneratorService::handleString);
        TYPE_HANDLERS.put("CHAR", DataGeneratorService::handleString);
        TYPE_HANDLERS.put("TEXT", DataGeneratorService::handleString);
        TYPE_HANDLERS.put("INT", DataGeneratorService::handleInteger);
        TYPE_HANDLERS.put("BIGINT", DataGeneratorService::handleLong);
        TYPE_HANDLERS.put("DECIMAL", DataGeneratorService::handleDecimal);
        TYPE_HANDLERS.put("DATE", c -> LocalDate.now().plusDays(faker.number().numberBetween(0, 365)));
        TYPE_HANDLERS.put("DATETIME", c -> LocalDateTime.now().plusHours(faker.number().numberBetween(0, 8760)));
        TYPE_HANDLERS.put("TIMESTAMP", c -> LocalDateTime.now());
        TYPE_HANDLERS.put("BOOLEAN", c -> faker.bool().bool());
        TYPE_HANDLERS.put("TINYINT", c -> faker.bool().bool());
        TYPE_HANDLERS.put("TINYINT(1)", c -> faker.bool().bool());
        TYPE_HANDLERS.put("FLOAT", c -> faker.number().randomDouble(2, 0, 1000));
        TYPE_HANDLERS.put("DOUBLE", c -> faker.number().randomDouble(4, 0, 10000));
    }

    public void genData(String tableName, int rows) {
        for (int i = 0; i < rows; i++) {
            saveGeneratedData(tableName, generateData(tableName));
        }
    }

    public Map<String, Object> generateData(String tableName) {
        List<ColumnMeta> columns = getTableSchema(tableName);
        Map<String, Object> data = new LinkedHashMap<>();

        List<FkColumn> fkColumns = getForeignKeysColumns(tableName);
        List<String> fkColumnNames = fkColumns.stream().map(fk -> fk.name).toList();

        columns.forEach(column -> {
            if (!column.isAutoIncrement()) {

                // if fk column
                if (fkColumnNames.contains(column.name())) {

                    FkColumn fkColumnObj = null;
                    for (FkColumn fkColumn : fkColumns) {
                        if (column.name().equals(fkColumn.name())) {
                            fkColumnObj = fkColumn;
                            break;
                        }
                    }

                    // select refColumnName from refTableName limit 1
                    assert fkColumnObj != null;
                    Integer idKey;
                    try {
                        idKey = jdbcTemplate.queryForObject(
                                "select " + fkColumnObj.refColumnName + " as idKey from " + fkColumnObj.refTableName + " limit 1",
                                (rs, rownum) -> rs.getInt("idKey")
                        );
                    } catch (EmptyResultDataAccessException e) {
                        idKey = null;
                    }

                    // aucune ligne existe pour la table ref
                    if (idKey == null) {
                        // insertion de la nouvelle ligne pour la fk
                        String refTable = fkColumnObj.refTableName;
                        Map<String, Object> newData = generateData(refTable);
                        saveGeneratedData(refTable, newData);

                        // on selecte la nouvelle ligne
                        idKey = jdbcTemplate.queryForObject(
                                "select " + fkColumnObj.refColumnName + " as idKey from " + fkColumnObj.refTableName + " limit 1",
                                (rs, rownum) -> rs.getInt("idKey")
                        );
                        data.put(column.name(), idKey);
                    } else {
                        data.put(column.name(), idKey);
                    }

                    // if not fk column
                } else {
                    data.put(column.name(), generateColumnValue(column));
                }

            }
        });

        return data;
    }

    public void saveGeneratedData(String tableName, Map<String, Object> data) {
        String columns = String.join(", ", data.keySet());
        String values = data.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

        String k = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        jdbcTemplate.update(
                k,
                data.values().toArray()
        );

        System.out.println("insert into " + tableName);
    }

    private Object generateColumnValue(ColumnMeta column) {
        if (column.isPrimaryKey() && column.isAutoIncrement()) {
            return null; // Let DB handle auto-increment
        }

        String typeName = column.typeName().toUpperCase();
        if (SKIP_GENERATION_TYPES.contains(typeName)) {
            return null;
        }

        Function<ColumnMeta, Object> handler = TYPE_HANDLERS.getOrDefault(typeName, c -> {
            throw new IllegalArgumentException("Unsupported type: " + c.typeName());
        });

        return handler.apply(column);
    }

    private List<ColumnMeta> getTableSchema(String tableName) {
        return jdbcTemplate.query(
                """
                        SELECT 
                            COLUMN_NAME, 
                            DATA_TYPE, 
                            CHARACTER_MAXIMUM_LENGTH,
                            NUMERIC_PRECISION,
                            NUMERIC_SCALE,
                            COLUMN_KEY,
                            EXTRA,
                            IS_NULLABLE
                        FROM INFORMATION_SCHEMA.COLUMNS 
                        WHERE TABLE_SCHEMA = DATABASE() 
                            AND TABLE_NAME = ?
                        ORDER BY ORDINAL_POSITION
                        """,
                (rs, rowNum) -> new ColumnMeta(
                        rs.getString("COLUMN_NAME"),
                        rs.getString("DATA_TYPE"),
                        rs.getInt("CHARACTER_MAXIMUM_LENGTH"),
                        rs.getInt("NUMERIC_PRECISION"),
                        rs.getInt("NUMERIC_SCALE"),
                        rs.getString("COLUMN_KEY").equalsIgnoreCase("PRI"),
                        rs.getString("EXTRA").contains("auto_increment"),
                        rs.getString("IS_NULLABLE").equalsIgnoreCase("YES")
                ),
                tableName
        );
    }

    private List<FkColumn> getForeignKeysColumns(String tableName) {
        return jdbcTemplate.query(
                """
                        SELECT
                            COLUMN_NAME,
                            REFERENCED_TABLE_NAME,
                            REFERENCED_COLUMN_NAME
                        FROM
                            INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                        WHERE
                            TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND REFERENCED_TABLE_NAME IS NOT NULL
                          AND REFERENCED_COLUMN_NAME IS NOT NULL
                        ORDER BY ORDINAL_POSITION
                        """,
                (rs, rowNum) -> new FkColumn(
                        rs.getString("COLUMN_NAME"),
                        rs.getString("REFERENCED_TABLE_NAME"),
                        rs.getString("REFERENCED_COLUMN_NAME")
                ),
                tableName
        );
    }

    private static Object handleString(ColumnMeta column) {
        int maxLength = Math.min(column.columnSize(), 255);
        return switch (column.name().toLowerCase()) {
            case "email" -> faker.internet().emailAddress();
            case "firstname" -> faker.name().firstName();
            case "first_name" -> faker.name().firstName();
            case "lastname" -> faker.name().lastName();
            case "last_name" -> faker.name().lastName();
            case "username" -> faker.name().username();
            case "user_name" -> faker.name().username();
            case "phone" -> faker.phoneNumber().phoneNumber();
            case "address" -> faker.address().streetAddress();
            case "city" -> faker.address().city();
            case "country" -> faker.address().country();
            case "description" -> faker.lorem().sentence();
            case "position" -> faker.job().position();
            case "twitter" -> faker.internet().url();
            case "facebook" -> faker.internet().url();
            case "youtube" -> faker.internet().url();
            default -> faker.lorem().characters(1, maxLength);
        };
    }

    private static Object handleInteger(ColumnMeta column) {
        return faker.number().numberBetween(0, Integer.MAX_VALUE);
    }

    private static Object handleLong(ColumnMeta column) {
        return faker.number().randomNumber();
    }

    private static Object handleDecimal(ColumnMeta column) {
        int precision = column.decimalDigits();
        return faker.number().randomDouble(
                precision,
                0,
                (long) Math.pow(10, column.columnSize() - precision)
        );
    }
}
