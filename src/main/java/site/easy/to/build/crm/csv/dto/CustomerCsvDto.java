package site.easy.to.build.crm.csv.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerCsvDto {
    @NotBlank(message = "'email' cannot be blank")
    private String customer_email;

    @NotBlank(message = "'status' cannot be blank")
    private String customer_name;
}
