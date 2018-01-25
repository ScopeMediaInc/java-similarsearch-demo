import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample Java code to use Scope API service to do similar search and image prediction
 * @author paultan
 *
 */
public class ScopeAPITestApp {
	//register and copy your client_id and client_secret here
	private static final String CLIENT_ID = "ukKxYOZL94oDmIiPOO5GfREQHLglY25gkttmhFurUmmHSNSW1srrIY0ErT6lB3Eo";
	private static final String CLIENT_SECRET = "eWq0bU8j80R5b96YZmqfWNIYVugMj89m4P79qSKl4FyYiLMBQ23TuHf56gF9RrWh"; 
    
    private static String TEST_IMAGE_URL = "https://cdn-images.farfetch-contents.com/11/60/37/69/11603769_7944724_1000.jpg"; //set query image url
    //
       
    private static final String SEARCH_BASE_URL = "https://api.scopemedia.com/search/v2";    
    private static final String TAG_BASE_URL = "https://api.scopemedia.com/tagging/v2";
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static 	Map<String, String> headers = new HashMap<String, String>();
     
    private static void initializeHeaders () {
    		headers.put("Content-Type", "application/json");
    		headers.put("Client-Id", CLIENT_ID);
    		headers.put("Client-Secret", CLIENT_SECRET);
    }
    
    public static void main(String[] args) {    		
    		if (CLIENT_ID == null || CLIENT_SECRET == null) {
    			System.out.println("Need to register a free account and get your Client Id and Clent Secret first.");
    			return;
    		}
    		//add client_id and Client_Secret to request headers
    		initializeHeaders();
    		
//    		//create an image collection by adding images with their urls
//    		List<String> myImageUrls = new ArrayList<String>();
//    		//myImageUrls.add("http://mydomain.com/image1.jpg");
//    		boolean ret = addImagesToImageCollection(myImageUrls);    
//    		if (!ret) {
//    			System.out.println("build image collection failed.");
//    			return;
//    		}
    		
    		//check the images indexed in my dataset
    		getImagesInDataCollection(0,20);
    		
        similarSearchByImageUrl(TEST_IMAGE_URL);

        String encodedMediaFile = Utils.encodeImage(TEST_IMAGE_URL);
        similarSearchByImageData(encodedMediaFile);
    		
    		getAvailablePredictionModels();
    		predictImageByUrl("fashion-v1", TEST_IMAGE_URL);
    }
  
    /**
     * Adding images to your image collection 
     * @param imageUrls
     * @return
     */
    private static boolean addImagesToImageCollection(List<String> imageUrls) {
    		System.out.println("-->addImagesToImageCollection");
    		if (imageUrls == null || imageUrls.size() == 0) {
    			System.out.println("empty image set.");
    			return false;
    		}
    		
    		List<JSONObject> mediaList = new ArrayList<JSONObject>(imageUrls.size());
    		for (String imageUrl : imageUrls) {
    			if (!Utils.exists(imageUrl))
    				return false;
    			mediaList.add(new JSONObject( new HashMap() {{ put("mediaUrl", imageUrl); }} ));
    		}
 
    		JSONArray mediaArray = new JSONArray(mediaList);

        JSONObject params = new JSONObject();    
        params.put("medias", mediaArray);
        
        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(SEARCH_BASE_URL + "/medias")
                    .headers(Headers.of(headers))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();      
            printMediaResponse(response);          
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Check images in your collection
     * @param pageNumber
     * @param pageSize
     */
	private static void getImagesInDataCollection(int pageNumber, int pageSize) {
		System.out.println("-->getImagesInDataCollection");
		OkHttpClient client = new OkHttpClient();
		try {
			Request request = new Request.Builder()
					.url(String.format("%s/medias?page=%d&size=%d", SEARCH_BASE_URL, pageNumber, pageSize))
					.headers(Headers.of(headers)).get().build();
			Response response = client.newCall(request).execute();
			printMediaResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Similar search by using image url
	 * @param url
	 */
    private static void similarSearchByImageUrl(String url) {
    		System.out.println("-->similarSearchByUrl");
		if (url == null || !Utils.exists(url)) {
			System.out.println("invalid input image path: " + url);
			return;
		}
		
        JSONObject params = new JSONObject();
        params.put("mediaUrl", url);

        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(SEARCH_BASE_URL + "/similar")
                    .headers(Headers.of(headers))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            printMediaResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Similar search by using base64 encoded bytes
     * @param base64EncodedMediaData
     */
    private static void similarSearchByImageData(String base64EncodedMediaData) {
    		System.out.println("-->similarSearchByImageData");
		if (base64EncodedMediaData == null) {
			System.out.println("invalid input image data: " + base64EncodedMediaData);
			return;
		}
        JSONObject params = new JSONObject();
        params.put("base64", base64EncodedMediaData);

        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(SEARCH_BASE_URL + "/similar")
                    .headers(Headers.of(headers))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            printMediaResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private static boolean printMediaResponse(Response response) throws JSONException, IOException {
		if (response == null)
			return false;
		if (response.code() != 200) {
			System.out.println(response.message());
			return false;
		}
		JSONObject result = new JSONObject(response.body().string());
		if (!result.getString("status").equals("OK")) {
			System.out.println(result.getString("error"));
			return false;
		}
		JSONArray media = result.getJSONArray("medias");
		System.out.println(String.format("There are %d images in the result.", media.length()));
		for (int i = 0; i < media.length(); i++) {
			System.out.println(media.get(i));
		}
		
	    return true;
	}
	
    /**
     * Check available prediction models
     */
	private static void getAvailablePredictionModels() {
		System.out.println("-->getAvailablePredictionModels");
		OkHttpClient client = new OkHttpClient();
		try {
			Request request = new Request.Builder()
					.url(TAG_BASE_URL + "/models")
					.headers(Headers.of(headers)).get().build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				System.out.println(response.message());
				return;
			}
			JSONObject result = new JSONObject(response.body().string());
            if (!result.getString("status").equals("OK")) {
	        		System.out.println(result.getString("error"));
	        		return;
	        }  
			JSONArray models = result.getJSONArray("models");
			for (int i = 0; i < models.length(); i++) {
				System.out.println(models.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * predict image by url
	 * @param modelId
	 * @param imageUrl
	 */
    private static void predictImageByUrl(String modelId, String imageUrl) {
    		System.out.println("-->predictImageByUrl");
        JSONObject params = new JSONObject();
        params.put("modelId", modelId);
        params.put("mediaUrl", imageUrl);
        
        OkHttpClient client = new OkHttpClient();
        try {
            RequestBody body = RequestBody.create(JSON, params.toString());
            Request request = new Request.Builder()
                    .url(TAG_BASE_URL + "/prediction")
                    .headers(Headers.of(headers))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.code() != 200) {
            		System.out.println(response.message());
            		return;
            }
            JSONObject result = new JSONObject(response.body().string()); 
            if (!result.getString("status").equals("OK")) {
            		System.out.println(result.getString("error"));
            		return;
            }            
            JSONArray media = result.getJSONArray("tags");
            for (int i = 0; i < media.length(); i++) {
                System.out.println(media.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}