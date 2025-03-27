package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "customer_login_info")
public class CustomerLoginInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    private String token;

    @Column(name = "password_set")
    private Boolean passwordSet;

    @OneToOne(mappedBy = "customerLoginInfo", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("customerLoginInfo")
    @PrimaryKeyJoinColumn
    private Customer customer;


    public CustomerLoginInfo() {
    }

    public CustomerLoginInfo(String username, String password, String token, Boolean passwordSet, Customer customer) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.passwordSet = passwordSet;
        this.customer = customer;
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }

    public String getEmail() {
        return username;
    }

    public void setEmail(String email) {
        this.username = email;
    }
}
