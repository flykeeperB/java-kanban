package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final HttpClient client;
    private final String storageUrl;
    private final String API_TOKEN;

    private boolean showMessages = false;

    public boolean isShowMessages() {
        return showMessages;
    }

    private void msg(String msg) {
        if (showMessages) {
            System.out.println(msg);
        }
    }

    public void setShowMessages(boolean showMessages) {
        this.showMessages = showMessages;
    }

    private String doSimpleRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем, успешно ли обработан запрос
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                msg("KVTaskClient: Что-то пошло не так. Сервер вернул код состояния: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("KVTaskClient: Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, запрос и повторите попытку.");
            System.out.println(e.getMessage());
        }
        return null;
    }

    public KVTaskClient(String storageUrl) {
        client = HttpClient.newHttpClient();
        this.storageUrl = storageUrl;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.storageUrl + "/register"))
                .GET()
                .build();
        this.API_TOKEN = doSimpleRequest(request);
        if (API_TOKEN != null) {
            msg("KVTaskClient: получен API_TOKEN: " + API_TOKEN);
        }
    }

    void put(String key, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl + "/save/" + key + "?API_TOKEN=" + API_TOKEN))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        doSimpleRequest(request);
    }

    String load(String key) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl + "/load/" + key + "?API_TOKEN=" + API_TOKEN))
                .GET()
                .build();
        return doSimpleRequest(request);
    }
}
