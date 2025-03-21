package site.easy.to.build.crm.csv.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserCsvDto {
    @NotBlank(message = "'email' cannot be blank")
    @Email(message = "Please enter a valid email format")
    private String email;

    @NotBlank(message = "'status' cannot be blank")
    @Pattern(regexp = "^(active|inactive|suspended)$", message = "Invalid status")
    private String status;

    @Min(value = 1, message = "Min value for roleId is 1")
    @Max(value = 3, message = "Max value for roleId is 3")
    private int roleId;
}
