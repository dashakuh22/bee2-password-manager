package by.bsu.bee2.bee2passwordmanager.controller;

import by.bsu.bee2.bee2passwordmanager.entity.PasswordFile;
import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.PasswordFileRepository;
import by.bsu.bee2.bee2passwordmanager.repository.UserRepository;
import by.bsu.bee2.bee2passwordmanager.services.GoogleDriveService;
import by.bsu.bee2.bee2passwordmanager.services.PasswordFileService;
import by.bsu.bee2.bee2passwordmanager.services.SerializationTokenServes;
import by.bsu.bee2.bee2passwordmanager.services.client.GoogleDriveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/plugin/")
public class PluginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleDriveService googleDriveService;

    @Autowired
    private SerializationTokenServes serializationTokenServes;

    @Autowired
    private PasswordFileRepository passwordFileRepository;

    @Autowired
    private PasswordFileService passwordFileService;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }

    @RequestMapping(value = "/master-key/{key}", method = RequestMethod.POST)
    public ResponseEntity<ActivationResult> validateMasterKey(@PathVariable String key) {
        String loginByMasterPassword = userRepository.getLoginByMasterPassword(key);
        String message = loginByMasterPassword != null ? null : "Incorrect secret key!";
        return ResponseEntity.ok(new ActivationResult(loginByMasterPassword != null, loginByMasterPassword, message));
    }

    @RequestMapping(value = "/save/google/{key}", method = RequestMethod.POST)
    public void saveGoogle(@RequestBody SaveFileData saveFileData, @PathVariable String key) {
        System.out.printf("Saving data [filename= %s ] to google drive for key %s\n", saveFileData.filename(), key);
        User user = userRepository.getUserByMasterPassword(key);
        passwordFileService.savePasswordFile(saveFileData, saveFileData.type(), user);
    }

    @RequestMapping(value = "load/{key}", method = RequestMethod.GET)
    public ResponseEntity<LoadFileResult> loadFiles(@PathVariable String key) {
        System.out.printf("Load data from google drive for key %s\n", key);
        User user = userRepository.getUserByMasterPassword(key);
        return ResponseEntity.ok(passwordFileService.loadFiles(user));
    }

    @RequestMapping(value = "/save-info/{key}", method = RequestMethod.POST)
    public void saveGoogle(@RequestBody SaveFileInfo saveFileInfo, @PathVariable String key) {
        System.out.printf("Saving file info %s key %s\n", saveFileInfo, key);
    }

    public record ActivationResult(boolean status, String login, String message) {

    }

    public record LoadFileResult(List<String> passwordsSeedKeys, List<SaveFileData> files) {

    }

    public record SaveFileInfo(List<String> files, String key) {

    }

    public record SaveFileData(String filename, Integer index, String type, byte[] data) {
        @Override
        public String toString() {
            return "SaveFileData{" +
                "filename='" + filename + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
        }
    }

}
