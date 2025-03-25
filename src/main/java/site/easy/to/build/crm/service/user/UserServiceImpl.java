package site.easy.to.build.crm.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.GenericCsvService;
import site.easy.to.build.crm.csv.dto.UserCsvDto;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.UserRepository;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.role.RoleServiceImpl;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    GenericCsvService<UserCsvDto, User> genericCsvService;
    @Autowired
    RoleServiceImpl roleService;
    @Autowired
    Environment environment;
    @Autowired
    GoogleGmailApiService googleGmailApiService;
    @Autowired
    UserProfileServiceImpl userProfileService;

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByToken(String token) {
        return userRepository.findByToken(token);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(int id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // csv
    public List<User> importCsv(MultipartFile file, OAuthUser oAuthUser) throws IOException, CsvValidationException {
        List<User> entities = new ArrayList<>();
        String filename = file.getOriginalFilename();

        for (UserCsvDto dto : genericCsvService.getDtosFromCsv(file, UserCsvDto.class, filename)) {
            entities.add(convertToEntity(dto, oAuthUser));
        }
        return entities;
    }


    public User convertToEntity(UserCsvDto csvDto, OAuthUser oAuthUser) throws CsvValidationException {
        User user = new User();
        // email + status
        user.setEmail(csvDto.getEmail());
        user.setStatus(csvDto.getStatus());

        // set role
        Optional<Role> role = roleService.findById(csvDto.getRoleId());
        if (role.isEmpty()){
            throw new CsvValidationException("Role not found", null);
        }
        role.ifPresent(value -> user.setRoles(List.of(value)));

        // set email token
        String token = EmailTokenUtils.generateToken();
        user.setToken(token);

        String baseUrl = environment.getProperty("app.base-url") + "set-employee-password?token=" + token;
        String name = user.getEmail().split("@")[0];

        // sent email for password confirmation
        if(googleGmailApiService != null) {
            EmailTokenUtils.sendRegistrationEmail(user.getEmail(), name, baseUrl, oAuthUser, googleGmailApiService);
        }

        user.setUsername(name);
        user.setPasswordSet(false);
        user.setCreatedAt(LocalDateTime.now());
        this.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setStatus(user.getStatus());
        userProfile.setFirstName(name);
        userProfile.setUser(user);
        userProfileService.save(userProfile);

        return user;
    }
}
