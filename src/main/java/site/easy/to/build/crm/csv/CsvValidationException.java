package site.easy.to.build.crm.csv;

import lombok.Getter;

import java.util.List;

@Getter
public class CsvValidationException extends RuntimeException {
    private final List<String> errors;

    public CsvValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
