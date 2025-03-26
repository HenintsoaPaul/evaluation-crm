package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;
import site.easy.to.build.crm.service.DuplicationService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;

@RequestMapping("/api/duplication")
@RestController
@RequiredArgsConstructor
public class DuplicationController {

    private final CustomerRepository customerRepository;
    private final CustomerServiceImpl customerService;
    private final DuplicationService duplicationService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> duplication(
            @PathVariable(name = "id") int customerId
    ) throws JsonProcessingException {
        Customer c = customerRepository.findByCustomerId(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "export.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        byte[] csvBytes = mapper.writeValueAsString(c).getBytes();

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> duplicationProcess(
            @RequestBody String jsonData
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Customer customer = mapper.readValue(jsonData, Customer.class);

            System.out.println(customer);
            duplicationService.duplicate(customer);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(jsonData);
    }
}
