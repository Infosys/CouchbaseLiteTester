package io.amrishraje.cblitetester;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GenerateSgSessionController {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSgSessionController.class);
    public TextField sgTokenUser;
    public TextField sgSessionTokenText;
    public Label statusLabel;
    Properties properties = new Properties();
    String sgUrl, sgAdminAuth, sgDB;

    @FXML
    void initialize() {
        logger.info("GenerateSgSessionController called");
        try {
            properties.loadFromXML(new FileInputStream("config.xml"));
            sgUrl = properties.getProperty("sgAdminURL");
            sgDB = properties.getProperty("sgDB");
            sgAdminAuth = properties.getProperty("sgAdminAuth");
            logger.info("SG URL is: {}", sgUrl);
        } catch (IOException e) {
            logger.error("Error reading config file", e);
        }
    }

    public void callSgSessionApi(ActionEvent event) {
        statusLabel.setText("");
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"name\": \"" + sgTokenUser.getText() + "\"\n}");
        Request request;

        if (sgAdminAuth == null || sgAdminAuth.isBlank() || sgAdminAuth.isEmpty()) {
            request = new Request.Builder()
                    .url(sgUrl + "/" + sgDB + "/_session")
                    .method("POST", body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();
        } else {
            request = new Request.Builder()
                    .url(sgUrl + "/" + sgDB + "/_session")
                    .method("POST", body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", sgAdminAuth)
                    .build();
        }
        Gson gson = new Gson();
        try {
            Response response = client.newCall(request).execute();
            JsonObject responseJson;
            if (response.isSuccessful()) {
                responseJson = gson.fromJson(response.body().string(), JsonObject.class);
                sgSessionTokenText.setText(responseJson.get("session_id").getAsString());
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(responseJson.get("session_id").getAsString()), null);
                statusLabel.setText("Copied to clipboard");
            } else {
                sgSessionTokenText.setText(response.message());
                logger.info("Sg Response: {}", response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
