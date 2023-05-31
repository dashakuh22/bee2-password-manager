package by.bsu.bee2.bee2passwordmanager.services;

import by.bsu.bee2.bee2passwordmanager.services.client.GoogleDriveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveService {

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private AuthHandlerService authHandlerService;

    public GoogleDriveClient.FileRecordList getFileList(String OAuthCookie) {
        return getClient(OAuthCookie).getFiles("name contains 'ОИО'");
    }

    public GoogleDriveClient.FileRecord upload(String filename, byte[] content, String OAuthCookie) {
        return getClient(OAuthCookie).upload(filename, content);
    }

    public GoogleDriveClient.FileRecord upload(String filename, byte[] content, OAuth2AuthenticationToken token) {
        return getClient(token).upload(filename, content);
    }

    public byte[] download(String OAuthCookie, String fileID) {
        return getClient(OAuthCookie).download(fileID);
    }

    public byte[] download(OAuth2AuthenticationToken token, String fileID) {
        return getClient(token).download(fileID);
    }

    private GoogleDriveClient getClient(String OAuthCookie) {
        return new GoogleDriveClient(getAccessToken(getOAuthToken(OAuthCookie)));
    }

    private GoogleDriveClient getClient(OAuth2AuthenticationToken oauthAuth) {
        return new GoogleDriveClient(getAccessToken(oauthAuth));
    }

    public OAuth2AuthenticationToken getOAuthToken(String OAuthCookie) {
        return authHandlerService.getOAuth2Auth(OAuthCookie);
    }

    private String getAccessToken(OAuth2AuthenticationToken oauthAuth) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(oauthAuth.getAuthorizedClientRegistrationId(), oauthAuth.getName());
        return oAuth2AuthorizedClient.getAccessToken().getTokenValue();
    }

}
