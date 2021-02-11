package com.scopemedia.api.sdkdemo;
/**
 * 
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.scopemedia.api.client.ScopeAIBuilder;
import com.scopemedia.api.client.ScopeAIClient;
import com.scopemedia.api.demo.Utils;
import com.scopemedia.api.dto.Media;
import com.scopemedia.api.dto.Model;
import com.scopemedia.api.dto.Tag;
import com.scopemedia.api.request.AddMediaRequest;
import com.scopemedia.api.request.PredictionRequest;
import com.scopemedia.api.request.SimilarImageRequest;
import com.scopemedia.api.response.MediaResponse;
import com.scopemedia.api.response.ModelResponse;
import com.scopemedia.api.response.PredictionResponse;

/**
 * Java example showing how to use the Scope Java SDK 
 * to do similar search and image prediction
 * @author paultan
 *
 */
public class ScopeSDKClientTestApp {
	//register and copy your client_id and client_secret here
    private static final String CLIENT_ID = "ukKxYOZL94oDmIiPOO5GfREQHLglY25gkttmhFurUmmHSNSW1srrIY0ErT6lB3Eo";
    private static final String CLIENT_SECRET = "eWq0bU8j80R5b96YZmqfWNIYVugMj89m4P79qSKl4FyYiLMBQ23TuHf56gF9RrWh"; 

    private static ScopeAIClient client;
    
	public static void main(String[] args) {
		if (CLIENT_ID == null || CLIENT_SECRET == null) {
			System.out.println("Need to register a free account and get your Client Id and Clent Secret first.");
			return;
		}
		client = new ScopeAIBuilder(CLIENT_ID, CLIENT_SECRET).build();
		ScopeSDKClientTestApp testApp = new ScopeSDKClientTestApp();
		
		//Before you can perform similar image search, 
		//you need to add images to your Application's image collection
//		List<String> myImageUrls = new ArrayList<String>();
//		//@TODO add image urls to list myImageUrls
//		testApp.addImagesToImageCollection(myImageUrls);
		
//		//check the images added to your collection
		testApp.getImagesInDataCollection(0,20);
 

		//@TODO set query image url
		String queryImageUrl = "https://cdn-images.farfetch-contents.com/11/60/37/69/11603769_7944724_1000.jpg"; 
		//try similar image search api
		testApp.similarSearchByUrl(queryImageUrl);

		//encode your image bytes with base64 
        String encodedMediaFile = Utils.encodeImage(queryImageUrl);
        testApp.similarSearchByImageData(encodedMediaFile);
		
		//Now let's try image prediction api
		testApp.predictImageByUrl("fashion-v1", queryImageUrl);
	}

	/**
	 * Adding images to your image collection 
	 * @param imageUrls List of image urls
	 * @return
	 */
	public boolean addImagesToImageCollection(List<String> imageUrls) {
		System.out.println("-->addImagesToImageCollection");
		if (imageUrls == null || imageUrls.size() == 0) {
			System.out.println("empty image set.");
			return false;
		}

		AddMediaRequest request = new AddMediaRequest();
		Media[] mediaArray = new Media[imageUrls.size()];
		int i = 0;
		for (String imageUrl : imageUrls) {
			if (!Utils.exists(imageUrl))
				return false;
			Media media = new Media();
			media.setUrl(imageUrl);
			mediaArray[i++] = media;
		}
		request.setMedias(mediaArray);
		try {
			MediaResponse response = client.addMedias(request).performSync();
			return printMediaResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	   
	/**
	 * Check images in your collection
	 * @param page
	 * @param size
	 */
	public void getImagesInDataCollection(int page, int size) {
		System.out.println("-->getImagesInDataCollection");
        try {
            MediaResponse response = client.getMedias(page, size).performSync();
            printMediaResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Similar search by using image url
	 * @param imageUrl
	 */
	public void similarSearchByUrl(String imageUrl) {
		System.out.println("-->similarSearchByUrl");
		if (imageUrl == null || !Utils.exists(imageUrl)) {
			System.out.println("invalid input image path: " + imageUrl);
			return;
		}
        SimilarImageRequest request = new SimilarImageRequest();
        request.setMediaAsUrl(imageUrl);

        try {
            MediaResponse response = client.getSimilarImages(request).performSync();
            printMediaResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Similar search by using base64 encoded bytes
	 * @param base64EncodedMediaData
	 */
	public void similarSearchByImageData(String base64EncodedMediaData) {
		System.out.println("-->similarSearchByImageData");
		if (base64EncodedMediaData == null) {
			System.out.println("invalid input image data: " + base64EncodedMediaData);
			return;
		}
		
        SimilarImageRequest request = new SimilarImageRequest();
        request.setMediaAsBase64(base64EncodedMediaData);

        try {
            MediaResponse response = client.getSimilarImages(request).performSync();
            printMediaResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	private boolean printMediaResponse(MediaResponse response) {
		if (response == null)
			return false;
		if (response.getCode() != 200) {
	    		System.out.println(response.getMessage());
	    		return false;
	    }
	    Media[] mediaList = response.getMedias();
	    System.out.println(String.format("There are %d images in the result.", mediaList.length));
	    for (Media media : mediaList) {
	        System.out.println(media.toString());
		}
	    return true;
	}
	
	/**
	 * Check available prediction models
	 */
	public void getAvailablePredictionModels() {
		System.out.println("-->getAvailablePredictionModels");
		try {
			ModelResponse response = client.getModels().performSync();
			if (response == null || response.getCode() != 200) {
				System.out.println(response.getMessage());
				return;
			}
			Model[] models = response.getModels();
			for (Model model : models) {
				System.out.println(model.toString());
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
	public void predictImageByUrl(String modelId, String imageUrl) {
		System.out.println("-->predictImageByUrl");
		PredictionRequest request = new PredictionRequest();
		request.setModelId(modelId);
		request.setMediaAsUrl(imageUrl);

		try {
			PredictionResponse response = client.getPrediction(request).performSync();
			if (response == null || response.getCode() != 200) {
				System.out.println(response.getMessage());
				return;
			}
			Tag[] tags = response.getTags();
			for (Tag tag : tags) {
				System.out.println(tag.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
