package site.easy.to.build.crm.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerApiController {

    private final CustomerServiceImpl customerService;

    @GetMapping
//    @JsonView({POV.Customer.class})
    public List<Customer> findAll() {
        return customerService.findAll();
    }
}
