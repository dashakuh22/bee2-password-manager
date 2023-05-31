package by.bsu.bee2.bee2passwordmanager.repository;

import by.bsu.bee2.bee2passwordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "select u.masterPassword from User u where u.login = ?1")
    String getMasterPasswordByLogin(String login);

    @Query(value = "select u.login from User u where u.masterPassword = ?1")
    String getLoginByMasterPassword(String masterPassword);

    @Query(value = "select u from User u where u.masterPassword = ?1")
    User getUserByMasterPassword(String masterPassword);
}
