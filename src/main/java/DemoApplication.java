/**
 gradle dependencies

 compile 'com.squareup.okhttp3:okhttp:3.6.0'
 compile 'commons-codec:commons-codec:1.10'
 compile 'commons-io:commons-io:2.5'
 compile 'org.json:json:20160810'
 */

import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


public class DemoApplication {
    // YOUR CONFIGURATION
    private static final String CLIENT_ID = "demo";
    private static final String CLIENT_SECRET = "demotestsecret";
    private static final String APPLICATION_ID = "fashion";
    private static final String IMAGE_URL = "http://goo.gl/8fgVc4";

    private static final String SEARCH_URL = "https://api.scopemedia.com/search-service/api/v1/"
            + "search/similar?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void main(String[] args) {
        searchUrl(IMAGE_URL);

        String encodedMediaFile = encodeImage(IMAGE_URL);
        searchBase64(encodedMediaFile);
    }

    private static void searchUrl(String url) {
        JSONObject params = new JSONObject();
        params.put("appId", APPLICATION_ID);
        params.put("mediaUrl", url);

        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(SEARCH_URL)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject result = new JSONObject(response.body().string());
            JSONArray media = result.getJSONArray("medias");
            for (int i = 0; i < media.length(); i++) {
                System.out.println(media.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void searchBase64(String encodedMediaFile) {
        JSONObject params = new JSONObject();
        params.put("appId", APPLICATION_ID);
        params.put("encodedMediaFile", encodedMediaFile);

        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(SEARCH_URL)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject result = new JSONObject(response.body().string());
            JSONArray media = result.getJSONArray("medias");
            for (int i = 0; i < media.length(); i++) {
                System.out.println(media.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String encodeImage(String mediaUrl) {
        try {
            byte[] imageBytes = IOUtils.toByteArray(new URL(mediaUrl));
            return Base64.encodeBase64String(imageBytes);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return null;
    }
}