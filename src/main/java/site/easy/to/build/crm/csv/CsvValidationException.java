package site.easy.to.build.crm.csv;

import lombok.Getter;
import site.easy.to.build.crm.csv.dto.CsvErrorWrapper;

import java.util.List;

@Getter
public class CsvValidationException extends Exception {
    private final List<CsvErrorWrapper> errors;

    public CsvValidationException(String message, List<CsvErrorWrapper> errors) {
        super(message);
        this.errors = errors;
    }
}
