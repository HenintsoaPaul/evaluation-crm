package site.easy.to.build.crm.service.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.GenericCsvService;
import site.easy.to.build.crm.csv.dto.CsvErrorWrapper;
import site.easy.to.build.crm.csv.dto.CustomerCsvDto;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private JdbcTemplate jdbcTemplate;

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

    // batch
    public void saveBatch(List<Customer> customers, Integer batchSize) {
        String sql = "INSERT INTO customer (name, email, country, user_id, profile_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());
        int userId = customers.get(0).getUser().getId();

        int listSize = customers.size();
        batchSize = (batchSize == null || batchSize > listSize) ? listSize : batchSize;

        jdbcTemplate.batchUpdate(sql, customers, batchSize,
                (PreparedStatement ps, Customer customer) -> {
                    ps.setString(1, customer.getName());
                    ps.setString(2, customer.getEmail());
                    ps.setString(3, "Madagascar");
                    ps.setInt(4, userId);
                    ps.setInt(5, customer.getCustomerLoginInfo().getId());
                    ps.setTimestamp(6, t);
                });
    }

    // csv
    @Transactional
    public List<Customer> importCsv(MultipartFile file, User user) throws IOException, CsvValidationException {
        List<Customer> entities = new ArrayList<>();
        List<CustomerCsvDto> dtos = new ArrayList<>();
        List<CsvErrorWrapper> errors = new ArrayList<>();

        String fileName = file.getOriginalFilename();
        try {
            dtos = genericCsvService.getDtosFromCsv(file, CustomerCsvDto.class, fileName);
        } catch (CsvValidationException e) {
            errors.addAll(e.getErrors());
        }

        for (int i = 0; i < dtos.size(); i++) {
            CustomerCsvDto dto = dtos.get(i);
            entities.add(convertToEntity(dto, user, errors, i + 1, fileName));
        }

        if (!errors.isEmpty()) {
            throw new CsvValidationException("dto->customer", errors);
        }

        return entities;
    }

    @Transactional
    public Customer convertToEntity(
            CustomerCsvDto csvDto,
            User user,
            List<CsvErrorWrapper> errors,
            int rowIndex,
            String fileName
    ) {
        Customer existing = customerRepository.findByEmail(csvDto.getCustomer_email());
        if (existing != null) {
            String msg = "Duplicate email '" + existing.getEmail() + "'!";
            errors.add(new CsvErrorWrapper(fileName, rowIndex, msg, csvDto.toString()));
        }

        Customer customer = new Customer();
        customer.setEmail(csvDto.getCustomer_email());
        customer.setName(csvDto.getCustomer_name());
        customer.setUser(user);
        customer.setCountry("Madagascar");

        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
        customerLoginInfo.setEmail(csvDto.getCustomer_email());
        customerLoginInfo.setToken(EmailTokenUtils.generateToken());
        customerLoginInfo.setPasswordSet(false);

        customerLoginInfo.setCustomer(customer);
        customerLoginInfoService.save(customerLoginInfo);

        customer.setCustomerLoginInfo(customerLoginInfo);
        return customer;
    }


//    // avec email
//    @Transactional
//    public List<Customer> importCsv(MultipartFile file, User user, Authentication authentication, boolean sendEmail) throws IOException, CsvValidationException {
//        List<Customer> entities = new ArrayList<>();
//        String filename = file.getOriginalFilename();
//        List<CustomerCsvDto> dtos = genericCsvService.getDtosFromCsv(file, CustomerCsvDto.class, filename);
//
//        for (CustomerCsvDto dto : dtos) {
//            entities.add(convertToEntity(dto, user, authentication, sendEmail));
//        }
//
//        return entities;
//    }
//
//    @Transactional
//    public Customer convertToEntity(
//            CustomerCsvDto csvDto,
//            User user,
//            Authentication authentication,
//            boolean sendEmail
//    ) {
//        Customer customer = new Customer();
//        customer.setEmail(csvDto.getCustomer_email());
//        customer.setName(csvDto.getCustomer_name());
//        customer.setUser(user);
//        customer.setCreatedAt(LocalDateTime.now());
//        customer.setCountry("Madagascar");
//
//        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
//        customerLoginInfo.setEmail(csvDto.getCustomer_email());
//        customerLoginInfo.setToken(EmailTokenUtils.generateToken());
//        customerLoginInfo.setPasswordSet(false);
//
//        customerLoginInfo.setCustomer(customer);
//        customerLoginInfoService.save(customerLoginInfo);
//
//        // email
//        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && sendEmail && googleGmailApiService != null) {
//            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
//            String baseUrl = environment.getProperty("app.base-url");
//
//            String url = baseUrl + "set-password?token=" + customerLoginInfo.getToken();
//            EmailTokenUtils.sendRegistrationEmail(
//                    customer.getEmail(), customer.getName(), url,
//                    oAuthUser, googleGmailApiService
//            );
//        }
//
//        customer.setCustomerLoginInfo(customerLoginInfo);
//        return customer;
//    }
}
