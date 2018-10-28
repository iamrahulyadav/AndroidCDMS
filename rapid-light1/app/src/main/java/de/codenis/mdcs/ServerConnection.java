package de.codenis.mdcs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class ServerConnection { 
	
    public String sendPostRequest(String requestURL, List<NameValuePair> params, Context c) {

        String response = "";
              	
        	try {
                HttpClient client = new DefaultHttpClient();  
                String postURL = Config.SERVER_URL;
                HttpPost post = new HttpPost(postURL); 
                    
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params);
                    post.setEntity(ent);

					//Toast.makeText(c,params.toString()+" s ", Toast.LENGTH_LONG).show();
                    //Log.d("my", "sssssccccc  "+post.toString());
					try {
						HttpResponse responsePOST = client.execute(post);
						//HttpEntity resEntity = responsePOST.getEntity();
						InputStream inputStream = responsePOST.getEntity().getContent();

						InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

						StringBuilder stringBuilder = new StringBuilder();

						String bufferedStrChunk = null;

						while((bufferedStrChunk = bufferedReader.readLine()) != null){
							stringBuilder.append(bufferedStrChunk);
						}
						response = stringBuilder.toString();

					}catch (HttpHostConnectException e){
						Toast.makeText(c," Server is down "+e, Toast.LENGTH_LONG).show();
					}


				//Toast.makeText(c, "response1111 "+response,Toast.LENGTH_LONG).show();
                    
            } catch (Exception e) {
            	Toast.makeText(c,"error :"+e, Toast.LENGTH_LONG).show();
            	e.printStackTrace();
            }
        	return response;
    }
    
    
    public Bitmap downloadBitmap(String url) {
		// initilize the default HTTP client object
		final DefaultHttpClient client = new DefaultHttpClient();

		//forming a HttoGet request 
		final HttpGet getRequest = new HttpGet(url);
		try {

			HttpResponse response = client.execute(getRequest);


			//check 200 OK for success
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("my", "Error " + statusCode + 
						" while retrieving bitmap from " + url);
				return null;

			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					// getting contents from the stream 
					inputStream = entity.getContent();

					// decoding stream data back into image Bitmap that android understands
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// You Could provide a more explicit error message for IOException
			getRequest.abort();
			Log.e("my", "Something went wrong while" +
					" retrieving bitmap from " + url + e.toString());
		} 

		return null;
	}
    
    public String DownloadFile(String fileURL, String name) {
    	File directory, pdfFile = null;
    	try {
        	
        	 directory = new File(Config.pdfDir);
        	 
        	    if (!directory.exists()) {
        	    	 Log.d("my", "creating Pdf directory"+directory);
        	        if (!directory.mkdirs()) {
        	        	 Log.d("my", "problem creating pdf File "+directory);
        	        }
        	    }
        	    
        	    
        	    if(directory.exists()){
        	    	if(name.equals(""))
        	    		name = System.currentTimeMillis()+"";
        	    	 pdfFile = new File(directory,name+".jpg");
        	    	 FileOutputStream file = new FileOutputStream(pdfFile);
	   	             URL url = new URL(fileURL);
	   	             HttpURLConnection connection = (HttpURLConnection) url .openConnection();
	   	             connection .setRequestMethod("GET");
	   	             connection .setDoOutput(true);
	   	             connection .connect();
	   	             InputStream input = connection .getInputStream();
	   	             byte[] buffer = new byte[2024000];
	   	             int len = 0;
	   	              while ((len = input .read(buffer)) > 0) {
	   	               file .write(buffer, 0, len );
	   	             }
	   	            file .close();
	   	            Log.d("my", pdfFile.getPath()+" path PDF "+pdfFile.getAbsolutePath());
	   	            return pdfFile.getAbsolutePath();
        	    }else{
        	    	 Log.d("my", "fileNot found "+pdfFile);
        	    }
        	    
      } catch (Exception e) {
    	  Log.e("my", "Something went wrong while downloading pdf" + e.toString());
    	  e.printStackTrace();
       }
		return pdfFile.getAbsolutePath();
    }

    public void downloadToxicSubstance(Context mContext, DBAdapter dbAdapter) throws JSONException {
		
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "getSubstances"));

        String result = sendPostRequest(Config.SERVER_URL,params,mContext);
        JSONObject jsonResponse = new JSONObject(result);
		String s = jsonResponse.getString("success");  
        Log.d("my","substance result :"+s);
        if(s.equalsIgnoreCase("true")){
        	for(int i1=0;i1<jsonResponse.length()-1;i1++){
        		JSONObject js = jsonResponse.getJSONObject(""+i1);
        		ContentValues initialValues = new ContentValues();
    			initialValues.put("toxic_substance ", js.getString("toxic_substance"));
    			
    			Boolean exist = dbAdapter.checkExist("toxic_substance", "toxic_substance", js.getString("toxic_substance"));
    			if(!exist){
	                long i = dbAdapter.addSubstance(initialValues);
	                if(i == -1)
	    			{
	    				Toast.makeText(mContext, "problem in adding substance",Toast.LENGTH_LONG).show();
	    			}
    			}
        	}
        }else{
        	Toast.makeText(mContext, "No toxicSubstance to update" ,Toast.LENGTH_LONG).show();
        }
	}
	
	public void downloadDescriptionTopic(Context mContext, DBAdapter dbAdapter) throws JSONException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "getTopic"));

        String result = sendPostRequest(Config.SERVER_URL,params,mContext);
        JSONObject jsonResponse = new JSONObject(result);
		String s = jsonResponse.getString("success");  
        Log.d("my","topic result :"+s);
        if(s.equalsIgnoreCase("true")){
        	for(int i1=0;i1<jsonResponse.length()-1;i1++){
        		JSONObject js = jsonResponse.getJSONObject(""+i1);
        		ContentValues initialValues = new ContentValues();
    			initialValues.put("preselect_description ", js.getString("preselect_description"));
				initialValues.put("priority", js.getString("priority"));

    			Boolean exist = dbAdapter.checkExist("preset_preselect_description", "preselect_description", js.getString("preselect_description"));
    			if(!exist){
	                long i = dbAdapter.addDescriptionTopic(initialValues);
	                if(i == -1)
	    			{
	    				Toast.makeText(mContext, "problem in adding descriptionTopic",Toast.LENGTH_LONG).show();
	    			}
    			}
        	}
        }else{
        	Toast.makeText(mContext, "No descriptionTopic to update" ,Toast.LENGTH_LONG).show();
        }
	}
	
	public void downloadVisitor(Context mContext, DBAdapter dbAdapter) throws JSONException {
	       
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "getVisitor"));

        String result = sendPostRequest(Config.SERVER_URL,params,mContext);
        JSONObject jsonResponse = new JSONObject(result);
		String s = jsonResponse.getString("success");  
        Log.d("my","topic result :"+s);
        
        if(s.equalsIgnoreCase("true")){
        	for(int i1=0;i1<jsonResponse.length()-1;i1++){
        		JSONObject js = jsonResponse.getJSONObject(""+i1);
        		ContentValues initialValues = new ContentValues();
    			initialValues.put("member", js.getString("member"));
    			initialValues.put("loginname", js.getString("loginname"));
    			initialValues.put("password", js.getString("password"));
    			
    			Boolean exist = dbAdapter.checkExist("team", "member", js.getString("member"));
    			if(!exist){
    				long i = dbAdapter.addMember(initialValues);
                    if(i == -1)
        			{
        				Toast.makeText(mContext, "problem in adding team members",Toast.LENGTH_LONG).show();
        			}
    			}
                
        	}
        }else{
        	Toast.makeText(mContext, "No Visitor to update" ,Toast.LENGTH_LONG).show();
        }
	}
   
}