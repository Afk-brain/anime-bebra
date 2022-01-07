package org.mo.bots.data;

import com.google.gson.Gson;
import org.mo.bots.data.objects.Category;
import org.mo.bots.data.response.ResponseCategory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class PosterProvider implements DataProvider {

    //region<HttpClient>
    private final String token = System.getenv("PosterToken");
    private final HttpClient client = HttpClient.newHttpClient();

    private String request(String method, Map<String, String> params) {
        String stringParams = "&";
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringParams += entry.getKey() + "=" + entry.getValue() + "&";
            }
        }
        try {
            URI uri = new URI("https://joinposter.com/api/" + method + "?token=" + token + params);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
    //endregion

    Gson gson = new Gson();

    @Override
    public Category[] getCategories() {
        String result = request("menu.getCategories", null);
        return gson.fromJson(result, ResponseCategory.class).response;
    }


    public static void main(String[] args) {
        PosterProvider provider = new PosterProvider();
        Category[] categories = provider.getCategories();
        for(Category category : categories) {
            System.out.println(category.id + " " + category.name + " " + category.photo);
        }
    }

}
