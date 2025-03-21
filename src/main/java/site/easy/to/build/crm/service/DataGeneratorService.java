package site.easy.to.build.crm.service;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
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

    private final JdbcTemplate jdbcTemplate;
    private static final Faker faker = new Faker();
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

    public Map<String, Object> generateData(String tableName, Map<String, Object> overrides) {
        List<ColumnMeta> columns = getTableSchema(tableName);
        Map<String, Object> data = new LinkedHashMap<>();

        columns.forEach(column -> {
            if (overrides.containsKey(column.name())) {
                data.put(column.name(), overrides.get(column.name()));
            } else if (!column.isAutoIncrement()) {
                data.put(column.name(), generateColumnValue(column));
            }
        });

        return data;
    }

    public void saveGeneratedData(String tableName, Map<String, Object> data) {
        String columns = String.join(", ", data.keySet());
        String values = data.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

        jdbcTemplate.update(
                "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")",
                data.values().toArray()
        );
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

    private static Object handleString(ColumnMeta column) {
        int maxLength = Math.min(column.columnSize(), 255);
        return switch (column.name().toLowerCase()) {
            case "email" -> faker.internet().emailAddress();
            case "firstname" -> faker.name().firstName();
            case "lastname" -> faker.name().lastName();
            case "phone" -> faker.phoneNumber().phoneNumber();
            case "address" -> faker.address().streetAddress();
            case "city" -> faker.address().city();
            case "country" -> faker.address().country();
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
