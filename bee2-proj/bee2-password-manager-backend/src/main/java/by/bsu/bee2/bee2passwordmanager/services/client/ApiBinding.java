package by.bsu.bee2.bee2passwordmanager.services.client;

import com.google.gson.Gson;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ApiBinding {

    protected String accessToken;

    protected Gson gson;

    public ApiBinding(String accessToken) {
        this.accessToken = accessToken;
        this.gson = new Gson();
    }

}
