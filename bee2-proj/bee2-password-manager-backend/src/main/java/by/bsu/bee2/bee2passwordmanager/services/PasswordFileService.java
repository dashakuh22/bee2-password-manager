package by.bsu.bee2.bee2passwordmanager.services;

import by.bsu.bee2.bee2passwordmanager.controller.PluginController;
import by.bsu.bee2.bee2passwordmanager.entity.PasswordFile;
import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.PasswordFileRepository;
import by.bsu.bee2.bee2passwordmanager.services.client.GoogleDriveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PasswordFileService {

    @Autowired
    private GoogleDriveService googleDriveService;

    @Autowired
    private SerializationTokenServes serializationTokenServes;

    @Autowired
    private PasswordFileRepository passwordFileRepository;

    public void savePasswordFile(PluginController.SaveFileData saveFileData, String type, User user) {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = serializationTokenServes.loadOAuth2FromUserAndType(user, "google").get();
        String passwordFileId = generatePasswordFileId(saveFileData, user.getMasterPassword());
        if (saveFileData.data() != null) {
            GoogleDriveClient.FileRecord upload = googleDriveService.upload(saveFileData.filename(), saveFileData.data(), oAuth2AuthenticationToken);
            passwordFileRepository.save(new PasswordFile(passwordFileId, type, upload.id(), user));
            return;
        }
        passwordFileRepository.save(new PasswordFile(passwordFileId, type, null, user));
    }

    public PluginController.LoadFileResult loadFiles(User user) {
        List<PasswordFile> fileForUser = passwordFileRepository.getFileForUser(user);
        OAuth2AuthenticationToken oAuth2AuthenticationToken = serializationTokenServes.loadOAuth2FromUserAndType(user, "google").get();
        List<String> seedKeys = getAllSeedKeys(fileForUser);
        List<PluginController.SaveFileData> saveFileData = getSaveFileData(fileForUser, oAuth2AuthenticationToken);
        return new PluginController.LoadFileResult(seedKeys, saveFileData);
    }

    private List<PluginController.SaveFileData> getSaveFileData(List<PasswordFile> fileForUser, OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        return fileForUser.stream().map(file -> {
            byte[] bytes = googleDriveService.download(oAuth2AuthenticationToken, file.getFileId());
            String index = file.getKey().substring(5, 6);
            return new PluginController.SaveFileData(file.getKey().substring(0, 4), Integer.parseInt(index), file.getType(), bytes);
        }).toList();
    }

    private List<String> getAllSeedKeys(List<PasswordFile> fileForUser) {
        return fileForUser.stream().map(file -> file.getKey().substring(0, 4)).distinct().toList();
    }

    private String generatePasswordFileId(PluginController.SaveFileData saveFileData, String masterPassword) {
        String seedKey = saveFileData.filename().substring(0, 4);
        Integer index = saveFileData.index();
        return "%s-%d-%s".formatted(seedKey, index, masterPassword);
    }
}
