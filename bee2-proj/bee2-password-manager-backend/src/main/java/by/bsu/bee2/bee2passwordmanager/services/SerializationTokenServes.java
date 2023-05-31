package by.bsu.bee2.bee2passwordmanager.services;

import by.bsu.bee2.bee2passwordmanager.entity.OAuthToken;
import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.OauthTokenRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class SerializationTokenServes {

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private OauthTokenRepository oauthTokenRepository;

    public void attachOAuth2ToUser(OAuth2AuthenticationToken authentication, String type, User user) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        OAuth2Auth oAuth2Auth = new OAuth2Auth(authentication, oAuth2AuthorizedClient);

        System.out.printf("Сохранение токена %s для пользователя %s\n", type, user.getLogin());

        String serialize = serialize(oAuth2Auth);

        Optional<OAuthToken> byUserMasterPasswordAndType = oauthTokenRepository.findByUserMasterPasswordAndType(user.getMasterPassword(), type);
        OAuthToken oAuthToken = byUserMasterPasswordAndType.orElse(new OAuthToken(null, type, null, user, true));
        oAuthToken.setAuthTokenBase64(serialize);

        oauthTokenRepository.save(oAuthToken);
    }

    public List<OAuth2AuthenticationToken> loadOAuth2FromUser(User user) {
        System.out.printf("Поиск токенов для пользлателя %s\n", user.getLogin());
        List<OAuthToken> allByUserMasterPassword = oauthTokenRepository.findAllByUserMasterPassword(user.getMasterPassword());
        List<OAuth2AuthenticationToken> oAuth2AuthenticationTokens = new ArrayList<>();
        for (OAuthToken oAuthToken : allByUserMasterPassword) {
            System.out.printf("Найден токен для пользлателя %s -> %s\n", user.getLogin(), oAuthToken.getType());
            if (oAuthToken.getType().equals("local")) {
                continue;
            }
            OAuth2Auth deserialize = deserialize(oAuthToken.getAuthTokenBase64());
            oAuth2AuthenticationTokens.add(deserialize.authentication());
            System.out.println("Регистрация токена...");
            oAuth2AuthorizedClientService.saveAuthorizedClient(deserialize.oAuth2AuthorizedClient(), deserialize.authentication());
        }
        return oAuth2AuthenticationTokens;
    }

    public Optional<OAuth2AuthenticationToken> loadOAuth2FromUserAndType(User user, String type) {
        List<OAuthToken> allByUserMasterPassword = oauthTokenRepository.findAllByUserMasterPassword(user.getMasterPassword());
        Optional<OAuthToken> any = allByUserMasterPassword.stream().filter(oAuthToken -> oAuthToken.getType().equals(type)).findAny();
        if (any.isEmpty()) {
            return Optional.empty();
        }
        OAuth2Auth deserialize = deserialize(any.get().getAuthTokenBase64());
        return Optional.of(deserialize.authentication());
    }


    public record OAuth2Auth(OAuth2AuthenticationToken authentication, OAuth2AuthorizedClient oAuth2AuthorizedClient) implements Serializable {

    }

    @SneakyThrows
    private static String serialize(OAuth2Auth auth) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(auth);
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    @SneakyThrows
    private static OAuth2Auth deserialize(String base64) {
        byte[] decode = Base64.getDecoder().decode(base64.getBytes());
        ByteArrayInputStream bis = new ByteArrayInputStream(decode);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (OAuth2Auth) in.readObject();
    }

}
