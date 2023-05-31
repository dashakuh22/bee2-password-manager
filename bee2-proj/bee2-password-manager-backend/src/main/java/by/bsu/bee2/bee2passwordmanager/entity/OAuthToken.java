package by.bsu.bee2.bee2passwordmanager.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "oauth2", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter
    private Integer id;

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Setter
    @Column(name = "auth_token_base64", length = 32768)
    private String authTokenBase64;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Setter
    @Column(name = "active")
    boolean active;

}
