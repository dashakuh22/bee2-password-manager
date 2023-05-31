package by.bsu.bee2.bee2passwordmanager.controller;

import by.bsu.bee2.bee2passwordmanager.services.AuthHandlerService;
import by.bsu.bee2.bee2passwordmanager.services.CookieService;
import by.bsu.bee2.bee2passwordmanager.services.GoogleDriveService;
import by.bsu.bee2.bee2passwordmanager.services.client.GoogleDriveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/p/login/")
public class FormLoginAthController {

    @Autowired
    private AuthHandlerService authHandlerService;

    @Autowired
    private GoogleDriveService googleDriveService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<InfoResp> loginFormLogin(
        @CookieValue(value = CookieService.MASTER_COOKIE_NAME, defaultValue = "undefined") String masterCookie,
        @CookieValue(value = CookieService.OAUTH_COOKIE_NAME, defaultValue = "undefined") String oauth2Cookie) {
        Authentication masterAuth = authHandlerService.getFormLoginAuth(masterCookie);
        Authentication oauthAuth = authHandlerService.getOAuth2Auth(oauth2Cookie);
        return ResponseEntity.ok(new InfoResp(masterAuth, oauthAuth));
    }

    @RequestMapping(value = "/files", method = RequestMethod.GET)
    public ResponseEntity<GoogleDriveClient.FileRecordList> files(@CookieValue(value = "OAUTH_COOKIE", defaultValue = "undefined") String oauth2Cookie) {
        return ResponseEntity.ok(googleDriveService.getFileList(oauth2Cookie));
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public ResponseEntity<GoogleDriveClient.FileRecord> upload(@CookieValue(value = "OAUTH_COOKIE", defaultValue = "undefined") String oauth2Cookie) {
        return ResponseEntity.ok(googleDriveService.upload("file.txt", "123".getBytes(), oauth2Cookie));
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> upload(@CookieValue(value = "OAUTH_COOKIE", defaultValue = "undefined") String oauth2Cookie,
                                         @RequestParam(value = "fileID") String fileId) {
        return ResponseEntity.ok(googleDriveService.download(oauth2Cookie, fileId));
    }

    private record InfoResp(Authentication master, Authentication oauth) {

    }

}
