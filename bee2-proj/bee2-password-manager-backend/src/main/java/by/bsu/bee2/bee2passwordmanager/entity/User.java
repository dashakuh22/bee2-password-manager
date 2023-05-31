package by.bsu.bee2.bee2passwordmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "login")
    @Getter
    private String login;

    @Getter
    @Column(name = "password")
    private String password;

    @Getter
    @Column(name = "master_key")
    private String masterPassword;

}
