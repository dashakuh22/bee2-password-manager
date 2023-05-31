package by.bsu.bee2.bee2passwordmanager.controller;

import by.bsu.bee2.bee2passwordmanager.config.OnAuthSuccessHandler;
import by.bsu.bee2.bee2passwordmanager.entity.OAuthToken;
import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.OauthTokenRepository;
import by.bsu.bee2.bee2passwordmanager.repository.UserRepository;
import by.bsu.bee2.bee2passwordmanager.services.AuthHandlerService;
import by.bsu.bee2.bee2passwordmanager.services.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(value = {"http://localhost:8080", "http://localhost:4200/"}, allowCredentials = "true", exposedHeaders = {"MASTER_COOKIE", "OAUTH_COOKIE"})
@RequestMapping("/p/rest/")
public class BackendRestController {

    @Autowired
    private AuthHandlerService authHandlerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthTokenRepository oauthTokenRepository;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private OnAuthSuccessHandler onAuthSuccessHandler;

    @SneakyThrows
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "username") String username, @RequestParam(name = "password") String password) {
        Authentication authenticate = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authenticate != null) {
            onAuthSuccessHandler.onFormLoginAuthSuccess().onAuthenticationSuccess(request, response, authenticate);
        } else {
            onAuthSuccessHandler.onFormLoginAuthFailure().onAuthenticationFailure(request, response, new AuthenticationCredentialsNotFoundException("Incorrect password or name"));
        }
    }

    @RequestMapping(value = "/getMasterKey", method = RequestMethod.GET)
    public ResponseEntity<MasterKeyLoadStatus> getMasterKey(@CookieValue(value = CookieService.MASTER_COOKIE_NAME, defaultValue = "undefined") String masterKey) {
        UsernamePasswordAuthenticationToken masterAuthToken = authHandlerService.getFormLoginAuth(masterKey);
        User user = userRepository.findById(masterAuthToken.getName()).get();
        return ResponseEntity.ok(new MasterKeyLoadStatus(true, user.getMasterPassword()));
    }

    @RequestMapping(value = "/otherAuth", method = RequestMethod.GET)
    public ResponseEntity<OtherAuthStatus> otherAuth(@CookieValue(value = CookieService.MASTER_COOKIE_NAME, defaultValue = "undefined")
                                                     String masterCookieValue) {
        UsernamePasswordAuthenticationToken masterAuthToken = authHandlerService.getFormLoginAuth(masterCookieValue);
        String masterKey = userRepository.findById(masterAuthToken.getName()).map(User::getMasterPassword).get();

        List<OAuthToken> allByUserMasterPassword = oauthTokenRepository.findAllByUserMasterPassword(masterKey);

        List<String> services = Arrays.asList("local", "google");

        List<OtherAuth> list = services.stream().map(service -> {
            Optional<OAuthToken> any = allByUserMasterPassword.stream().filter(token -> token.getType().equals(service)).findAny();
            boolean isActive = any.map(OAuthToken::isActive).orElse(false);
            return new OtherAuth(service, any.isPresent(), isActive);
        }).toList();

        return ResponseEntity.ok(new OtherAuthStatus(true, list));
    }

    @RequestMapping(value = "/switch/{type}", method = RequestMethod.POST)
    public ResponseEntity<SwitchServiceResult> switchAuth(@CookieValue(value = CookieService.MASTER_COOKIE_NAME, defaultValue = "undefined")
                                                          String masterCookieValue, @PathVariable(name = "type") String type) {

        UsernamePasswordAuthenticationToken masterAuthToken = authHandlerService.getFormLoginAuth(masterCookieValue);
        User user = userRepository.findById(masterAuthToken.getName()).get();
        List<OAuthToken> allByUserMasterPassword = oauthTokenRepository.findAllByUserMasterPassword(user.getMasterPassword());

        Optional<OAuthToken> any = allByUserMasterPassword.stream().filter(oAuthToken -> oAuthToken.getType().equals(type)).findAny();

        boolean needAuth = false;
        String authLocation = null;

        if (any.isEmpty()) {
            if (type.equals("google")) {
                needAuth = true;
                authLocation = "http://localhost:8081/oauth2/authorization/google";
            } else if (type.equals("local")) {
                oauthTokenRepository.save(new OAuthToken(null, type, "", user, true));
            }
        } else {
            OAuthToken oAuthToken = any.get();
            oAuthToken.setActive(!oAuthToken.isActive());
            oauthTokenRepository.save(oAuthToken);
        }

        return ResponseEntity.ok(new SwitchServiceResult(
            true,
            needAuth,
            authLocation,
            otherAuth(masterCookieValue).getBody()
        ));
    }

    public record MasterKeyLoadStatus(boolean status, String masterKey) {
    }

    public record OtherAuth(String name, boolean presented, boolean active) {
    }

    public record OtherAuthStatus(boolean status, List<OtherAuth> otherAuths) {
    }

    public record SwitchServiceResult(
        boolean status,
        boolean needAuth,
        String authLocation,
        OtherAuthStatus otherAuthStatus
    ) {
    }

}
