package by.bsu.bee2.bee2passwordmanager.repository;

import by.bsu.bee2.bee2passwordmanager.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OauthTokenRepository extends JpaRepository<OAuthToken, Integer> {

    List<OAuthToken> findAllByUserMasterPassword(String masterPassword);

    Optional<OAuthToken> findByUserMasterPasswordAndType(String masterPassword, String type);

}
