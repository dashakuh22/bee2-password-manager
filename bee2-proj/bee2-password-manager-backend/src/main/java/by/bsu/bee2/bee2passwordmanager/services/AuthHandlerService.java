package by.bsu.bee2.bee2passwordmanager.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthHandlerService {

    private final Map<String, UsernamePasswordAuthenticationToken> uuidToUsernamePasswordAuthenticationToken = new HashMap<>();

    private final Map<String, OAuth2AuthenticationToken> uuidToOAuth2AuthenticationToken = new HashMap<>();

    public void saveFormLoginAuth(String uuid, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
        uuidToUsernamePasswordAuthenticationToken.put(uuid, usernamePasswordAuthenticationToken);
    }

    public UsernamePasswordAuthenticationToken getFormLoginAuth(String uuid) {
        return uuidToUsernamePasswordAuthenticationToken.get(uuid);
    }

    public void saveOAuth2Auth(String uuid, OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        uuidToOAuth2AuthenticationToken.put(uuid, oAuth2AuthenticationToken);
    }

    public OAuth2AuthenticationToken getOAuth2Auth(String uuid) {
        return uuidToOAuth2AuthenticationToken.get(uuid);
    }

}
