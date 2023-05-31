package by.bsu.bee2.bee2passwordmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_files")
@NoArgsConstructor
@AllArgsConstructor
public class PasswordFile {

    @Id
    @Column(name = "key")
    @Getter
    private String key;

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "file_id")
    private String fileId;

    @ManyToOne
    @Getter
    @JoinColumn(name = "login")
    private User user;
}
