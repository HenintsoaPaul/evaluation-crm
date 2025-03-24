package site.easy.to.build.crm.api;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ApiServerException extends Exception {
    private final List<String> errors;

    public ApiServerException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public ApiServerException(String message) {
        super(message);
        this.errors = null;
    }
}
