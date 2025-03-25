package site.easy.to.build.crm.csv.dto;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerCsvDto {
    @NotBlank(message = "'customer_email' cannot be blank")
    @Email(message = "Invalid 'customer_email' format")
    @Size(max = 255, message = "'customer_email' must be less than 255 characters")
    @CsvBindByName(column = "customer_email")
    private String customer_email;

    @NotBlank(message = "'customer_name' cannot be blank")
    @Size(max = 255, message = "'customer_customer_nameemail' must be less than 255 characters")
    @CsvBindByName(column = "customer_name")
    private String customer_name;
}
