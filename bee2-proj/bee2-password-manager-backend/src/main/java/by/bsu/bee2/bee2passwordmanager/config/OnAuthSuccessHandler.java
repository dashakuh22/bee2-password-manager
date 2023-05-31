package by.bsu.bee2.bee2passwordmanager.config;

import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.UserRepository;
import by.bsu.bee2.bee2passwordmanager.services.AuthHandlerService;
import by.bsu.bee2.bee2passwordmanager.services.CookieService;
import by.bsu.bee2.bee2passwordmanager.services.SerializationTokenServes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OnAuthSuccessHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private AuthHandlerService authHandlerService;

    @Autowired
    private SerializationTokenServes serializationTokenServes;

    public AuthenticationSuccessHandler onFormLoginAuthSuccess() {
        return (request, response, authentication) -> {
            Cookie cookie = proccessCookie(CookieService.MASTER_COOKIE_NAME, request, response);

            String uuid = cookie.getValue();
            authHandlerService.saveFormLoginAuth(uuid, (UsernamePasswordAuthenticationToken) authentication);

            User user = userRepository.findById(authentication.getName()).get();
            List<OAuth2AuthenticationToken> oAuth2AuthenticationTokens = serializationTokenServes.loadOAuth2FromUser(user);

            oAuth2AuthenticationTokens.forEach(oAuth2AuthenticationToken -> {
                Cookie oauth2Cookie = proccessCookie(CookieService.OAUTH_COOKIE_NAME, request, response);
                String oAuthUuid = oauth2Cookie.getValue();
                authHandlerService.saveOAuth2Auth(oAuthUuid, oAuth2AuthenticationToken);
            });

            sendAuthResponse(response);
        };
    }


    public AuthenticationSuccessHandler onOAuthAuthSuccess() {
        return (request, response, authentication) -> {
            Cookie masterCookie = proccessCookie(CookieService.MASTER_COOKIE_NAME, request, response);
            UsernamePasswordAuthenticationToken formLoginAuth = authHandlerService.getFormLoginAuth(masterCookie.getValue());
            if (formLoginAuth == null) {
                sendUnAuthResponse(response, new AuthenticationCredentialsNotFoundException("Use /login before OAuth2"));
                return;
            }

            Cookie oathCookie = proccessCookie(CookieService.OAUTH_COOKIE_NAME, request, response);
            authHandlerService.saveOAuth2Auth(oathCookie.getValue(), (OAuth2AuthenticationToken) authentication);

            User user = userRepository.findById(formLoginAuth.getName()).get();
            serializationTokenServes.attachOAuth2ToUser((OAuth2AuthenticationToken) authentication, "google", user);

            sendAuthResponse(response);
        };
    }

    Cookie proccessCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = cookieService.getCookie(name, request);
        System.out.printf("Куки с именем %s %s\n", name, cookie.isEmpty() ? "не найдена" : "найдена");
        return cookie.orElseGet(() -> cookieService.addCookie(name, UUID.randomUUID().toString(), "/", response));
    }

    public AuthenticationFailureHandler onOAuthAuthFailure() {
        return (request, response, exception) -> {
            sendUnAuthResponse(response, exception);
        };
    }

    public AuthenticationFailureHandler onFormLoginAuthFailure() {
        return (request, response, exception) -> {
            sendUnAuthResponse(response, exception);
        };
    }

    @SneakyThrows
    private void sendUnAuthResponse(HttpServletResponse response, AuthenticationException exception) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(toResp(false, exception));
    }

    @SneakyThrows
    private void sendAuthResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(toResp(true, null));
    }

    @SneakyThrows
    private String toResp(boolean isOk, Object o) {
        return objectMapper.writeValueAsString(new AuthResult(isOk, Optional.ofNullable(o).orElse("").toString()));
    }
    public record AuthResult(boolean status, String message) {

    }

}
