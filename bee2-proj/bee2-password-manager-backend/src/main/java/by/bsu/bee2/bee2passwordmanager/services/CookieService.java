package by.bsu.bee2.bee2passwordmanager.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CookieService {

    public static final String MASTER_COOKIE_NAME = "MASTER_COOKIE";

    public static final String OAUTH_COOKIE_NAME = "OAUTH_COOKIE";

    public Optional<Cookie> getCookie(String cookieName, HttpServletRequest request) {
        if (request.getCookies() == null) {
            System.out.println("Список куки пуст.");
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .findFirst();
    }

    public Cookie addCookie(String cookieName, String cookieValue, String cookiePath, HttpServletResponse response) {
        System.out.printf("Добавляем куки с именем %s значение %s путь %s\n", cookieName, cookieValue, cookiePath);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
        return cookie;
    }

}
