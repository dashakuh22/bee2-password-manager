package by.bsu.bee2.bee2passwordmanager.services.client;

import lombok.SneakyThrows;
import okhttp3.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDriveClient extends ApiBinding {

    private final String GOOGLE_DRIVE_API_URL = "https://www.googleapis.com/drive/v3/files";

    private final String UPLOAD_GOOGLE_DRIVE_API_URL = "https://www.googleapis.com/upload/drive/v3/files";

    public GoogleDriveClient(String accessToken) {
        super(accessToken);
    }

    @SneakyThrows
    public FileRecordList getFiles(String searchQuery) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        Request request = new Request.Builder()
            .url(GOOGLE_DRIVE_API_URL + "?q=%s".formatted(searchQuery))
            .method("GET", null)
            .addHeader("Authorization", "Bearer %s".formatted(accessToken))
            .build();
        Response execute = client.newCall(request).execute();
        return gson.fromJson(execute.body().string(), FileRecordList.class);
    }

    @SneakyThrows
    public FileRecord upload(String fileName, byte[] fileContent) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("metadata", "",
                RequestBody.create(createMetadata(fileName).getBytes(), okhttp3.MediaType.parse("application/json;charset=UTF-8")))
            .addFormDataPart("", "",
                RequestBody.create(fileContent, okhttp3.MediaType.parse("application/octet-stream")))
            .build();
        Request request = new Request.Builder()
            .url(UPLOAD_GOOGLE_DRIVE_API_URL + "?uploadType=multipart")
            .method("POST", body)
            .addHeader("Authorization", "Bearer %s".formatted(accessToken))
            .build();
        Response execute = client.newCall(request).execute();
        return gson.fromJson(execute.body().string(), FileRecord.class);
    }

    @SneakyThrows
    public byte[] download(String fileID) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        Request request = new Request.Builder()
            .url(GOOGLE_DRIVE_API_URL + "/" + fileID + "?alt=media")
            .method("GET", null)
            .addHeader("Authorization", "Bearer %s".formatted(accessToken))
            .build();
        Response execute = client.newCall(request).execute();
        return execute.body().bytes();
    }

    private String createMetadata(String filename) {
        String metadata = """
            {
                "name": "%s"
            }
            """.formatted(filename);
        return metadata;
    }

    private String url() {
        return GOOGLE_DRIVE_API_URL;
    }

    private String url(String path) {
        return url() + path;
    }

    private Map<String, String> toMap(String... vars) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < vars.length; i += 2) {
            map.put(vars[i], vars[i + 1]);
        }
        return map;
    }

    public record FileRecordList(String kind, boolean incompleteSearch, List<FileRecord> files) {

    }

    public record FileRecord(String kind, String id, String name, String mimeType) {

    }

}
