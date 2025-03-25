package site.easy.to.build.crm.service.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.GenericCsvService;
import site.easy.to.build.crm.csv.dto.CustomerCsvDto;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.service.user.UserProfileServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    GenericCsvService<CustomerCsvDto, Customer> genericCsvService;
    @Autowired
    CustomerLoginInfoServiceImpl customerLoginInfoService;
    @Autowired
    AuthenticationUtils authenticationUtils;
    @Autowired
    Environment environment;
    @Autowired
    GoogleGmailApiService googleGmailApiService;
    @Autowired
    UserProfileServiceImpl userProfileService;

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer findByCustomerId(int customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public List<Customer> findByUserId(int userId) {
        return customerRepository.findByUserId(userId);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void delete(Customer customer) {
        customerRepository.delete(customer);
    }

    @Override
    public List<Customer> getRecentCustomers(int userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return customerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByUserId(int userId) {
        return customerRepository.countByUserId(userId);
    }


    // csv
    @Transactional
    public List<Customer> importCsv(MultipartFile file, User user, Authentication authentication, boolean sendEmail) throws IOException, CsvValidationException {
        List<Customer> entities = new ArrayList<>();
        String filename = file.getOriginalFilename();

        for (CustomerCsvDto dto : genericCsvService.getDtosFromCsv(file, CustomerCsvDto.class, filename)) {
            entities.add(convertToEntity(dto, user, authentication, sendEmail));
        }
        this.customerRepository.saveAll(entities);
        return entities;
    }

    @Transactional
    public Customer convertToEntity(
            CustomerCsvDto csvDto,
            User user,
            Authentication authentication,
            boolean sendEmail
    ) {
        Customer customer = new Customer();
        customer.setEmail(csvDto.getCustomer_email());
        customer.setName(csvDto.getCustomer_name());
        customer.setUser(user);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCountry("Madagascar");

        String token = EmailTokenUtils.generateToken();
        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
        customerLoginInfo.setEmail(csvDto.getCustomer_email());
        customerLoginInfo.setToken(token);
        customerLoginInfo.setPasswordSet(false);

        customerLoginInfo.setCustomer(customer);
        customerLoginInfoService.save(customerLoginInfo);

        // email
        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && sendEmail && googleGmailApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            String baseUrl = environment.getProperty("app.base-url");

            String url = baseUrl + "set-password?token=" + customerLoginInfo.getToken();
            EmailTokenUtils.sendRegistrationEmail(
                    customer.getEmail(), customer.getName(), url,
                    oAuthUser, googleGmailApiService
            );
        }

        customer.setCustomerLoginInfo(customerLoginInfo);
        return customer;
    }
}
