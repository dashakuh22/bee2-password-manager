package by.bsu.bee2.bee2passwordmanager.repository;

import by.bsu.bee2.bee2passwordmanager.entity.PasswordFile;
import by.bsu.bee2.bee2passwordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordFileRepository extends JpaRepository<PasswordFile, String> {

    @Query(value = "select p from PasswordFile p where p.user = ?1 order by p.key")
    List<PasswordFile> getFileForUser(User user);
}
