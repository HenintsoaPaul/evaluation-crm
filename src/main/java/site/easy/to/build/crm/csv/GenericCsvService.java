package site.easy.to.build.crm.csv;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.dto.CsvErrorWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenericCsvService<T, E> {

    private final Validator validator;

//    private void validateBatch(List<T> rows) throws CsvValidationException {
//        List<String> errors = new ArrayList<>();
//
//        for (int i = 0; i < rows.size(); i++) {
//            T row = rows.get(i);
//            Set<ConstraintViolation<T>> violations = validator.validate(row);
//            for (ConstraintViolation<T> violation : violations) {
//                errors.add("Row " + (i + 1) + ": " + violation.getMessage());
//            }
//        }
//
//        if (!errors.isEmpty()) {
//            throw new CsvValidationException("CSV validation failed", null);
//        }
//    }

    private List<CsvErrorWrapper> validateBatch(List<T> rows, String filename)  {
        List<CsvErrorWrapper> csvErrorWrappers = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            T row = rows.get(i);
            Set<ConstraintViolation<T>> violations = validator.validate(row);
            for (ConstraintViolation<T> violation : violations) {
                csvErrorWrappers.add(new CsvErrorWrapper(
                        filename,
                        i + 1,
                        violation.getMessage(),
                        row.toString()
                ));
            }
        }
        return csvErrorWrappers;
    }

//    public List<T> getDtosFromCsv(MultipartFile file, Class<T> clazz) throws IOException, CsvValidationException {
//        try (
//                InputStreamReader isr = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
//                BufferedReader br = new BufferedReader(isr)
//        ) {
//            if (file.isEmpty()) {
//                throw new CsvValidationException("File vide rangah", null);
//            }
//
//            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(br)
//                    .withType(clazz)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .withQuoteChar('"') // regrouper les valeurs entre "..." [plusieurs valeurs, sur plusieurs ligne, double "..." pour echapper des "..."]
////                    .withSeparator(';')
////                    .withThrowExceptions(false) // pour éviter une erreur fatale en cas de colonne maquant
//                    .build();
//
//            List<T> uploads = csvToBean.parse();
//
//            validateBatch(uploads);
//
//            return uploads;
//        }
//    }

    public List<T> getDtosFromCsv(MultipartFile file, Class<T> clazz, String filename) throws IOException, CsvValidationException {
        try (
                InputStreamReader isr = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)
        ) {
            if (file.isEmpty()) {
                throw new CsvValidationException("File vide rangah", null);
            }

            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(br)
                    .withType(clazz)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withQuoteChar('"') // regrouper les valeurs entre "..." [plusieurs valeurs, sur plusieurs ligne, double "..." pour echapper des "..."]
//                    .withSeparator(';')
//                    .withThrowExceptions(false) // pour éviter une erreur fatale en cas de colonne maquant
                    .build();

            List<T> uploads = csvToBean.parse();

            List<CsvErrorWrapper> errors = validateBatch(uploads, filename);
            if (!errors.isEmpty()) {
                throw new CsvValidationException("Validation misy erreur ralh", errors);
            }

            return uploads;
        }
    }

    /*
    * 1-csv ->conversion-> csvDto (+ validation 😁)
    * 2-csvDto ->insertion-> tempTable
    * 3-tempTable ->select distinct des lignes fk a inserer-> . ->insertion des Fk dans les tables des Fk-> void
    * 4-tempTable ->insertion dans la table final
    * */
}
