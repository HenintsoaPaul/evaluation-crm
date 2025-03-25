package site.easy.to.build.crm.csv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvErrorWrapper {

    private String fileName;
    private int lineNumber;
    private String errorMessage;
    private String rawData;
}
