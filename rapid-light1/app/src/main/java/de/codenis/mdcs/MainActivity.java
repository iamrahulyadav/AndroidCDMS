package de.codenis.mdcs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private DBAdapter dbAdapter;
	private ProjectListAdapter mAdapter;
	private ListView listView;
	private EditText inputSearch;
	private Context mContext;
	private ProgressDialog pDialog, processDialog;
	private SessionManager session;
	ServerConnection sc = new ServerConnection();
    public static final int progress_bar_type = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 // for network strict mode of android
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
				new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		if(Build.VERSION.SDK_INT>=24){
			try{
				Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
				m.invoke(null);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		// Progress dialog
        processDialog = new ProgressDialog(this);
        processDialog.setCancelable(false);
        processDialog.setIndeterminate(true);
		
		mContext = this;
	
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		mAdapter = new ProjectListAdapter(this, dbAdapter.getProjects());
		
		listView=(ListView)findViewById(R.id.list);
		listView.setAdapter(mAdapter);
		listView.setTextFilterEnabled(true);
		
		inputSearch = (EditText) findViewById(R.id.inputSearch);
		inputSearch.addTextChangedListener(new TextWatcher() {
		     
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		        // When user changed the Text
		    	mAdapter.getFilter().filter(cs);
		   }

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});	
	}

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.add_project) {

			Intent addProject = new Intent(this, AddProjectActivity.class);
	        addProject.putExtra("id", 0);
	    	startActivityForResult(addProject,0);
	        return true;
	    }else if(id == R.id.upload_project) {
        	new Upload().execute();
        	return true;
        }
	    else if(id == R.id.download_project) {
        	new Download().execute();
			return true;
        }
	    else if(id == R.id.clear_project) {
        	new ClearProjects().execute();
			return true;
        }
	    /*else if(id == R.id.export_project) {
        	export();
			return true;
        }*/
		else if(id == R.id.logout) {
        	logout();
			return true;
        }
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * showing progress dialog
	 */
	private void showDialog(String message) {
        if (!processDialog.isShowing()){
        	processDialog.setMessage(message);
        	processDialog.show();
            }
    }
 
    public void hideDialog() {
        if (processDialog.isShowing())
            processDialog.dismiss();
    }
	
	/**
     * Showing progress Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
    	pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        
     
        switch (id) {
        case 0: // we set this to 0
        	
            pDialog.setMessage("Downloading Projekte. Bitte warten...");
            pDialog.show();
            return pDialog;
        case 1: // we set this to 1
        	pDialog.setMessage("Uploading Projekte. Bitte warten...");
            pDialog.show();
            return pDialog;
            
        case 2: // we set this to 2
        	pDialog.setMessage("Deleting Projekte. Bitte warten...");
            pDialog.show();
            return pDialog;
        default:
            return null;
        }
    }
    
    /**
     * Background Async Task to download file
     * */
    class Download extends AsyncTask<String, String, String> {
 
		@SuppressWarnings("deprecation")
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(0);
            showDialog("Downloading Projekte ...");
        }
 
        @Override
        protected String doInBackground(String... f_url) {
            int count =0;
			while(count<100)
			{      
				   count = count +1;
			       try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			      // publishProgress(""+count);
			}
            return null;
        }
 
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
       }
 
       @SuppressWarnings("deprecation")
	@Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //dismissDialog(0);
            try {
            	download();
				hideDialog();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          
        }
  }

    class Upload extends AsyncTask<String, String, String> {
    	 
		@SuppressWarnings("deprecation")
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(1);
            showDialog("Uploading Projekte ...");
        }
 
        @Override
        protected String doInBackground(String... f_url) {
            try {
            	int count =0;
            	while(count<10)
                {      
            		   count = count +1;
                       Thread.sleep(5);
            	      // publishProgress(""+count);
            	}
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null;
        }
 
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
           // pDialog.setProgress(Integer.parseInt(progress[0]));
       }
 
       @SuppressWarnings("deprecation")
	@Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //dismissDialog(1);
            try {
				upload();
				hideDialog();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          
        }
 
    }
	

    class ClearProjects extends AsyncTask<String, String, String> {
    	 
		@SuppressWarnings("deprecation")
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(2);
            showDialog("Deleting Projekte ...");
        }
 
        @Override
        protected String doInBackground(String... f_url) {
            try {
            	int count =0;
            	while(count<100)
                {      
            		   count = count +1;
                       Thread.sleep(5);
            	       //publishProgress(""+count);
            	}
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null;
        }
 
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
           // pDialog.setProgress(Integer.parseInt(progress[0]));
       }
 
       @SuppressWarnings("deprecation")
	@Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //dismissDialog(2);
            try {
				clearProjects();
				hideDialog();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          
        }
    }
	
    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(!file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
	
	private void export(){
		SQLiteDatabase sqldb = dbAdapter.mDb;
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
             
            if (sd.canWrite()) {
                String currentDBPath = "//data//"+getPackageName()+"//databases//";
                String backupDBPath = "backup-";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                
                if (currentDB.exists()) {
                	
                	List<File> files = getListFiles(currentDB);
                    for(int i=0;i<files.size();i++){
                    	//Toast.makeText(getApplicationContext(), "file"+files.get(i), Toast.LENGTH_LONG).show();
	                    FileChannel src = new FileInputStream(files.get(i)).getChannel();
	                    FileChannel dst = new FileOutputStream(backupDB+""+files.get(i).getName()).getChannel();
	                    dst.transferFrom(src, 0, src.size());
	                    src.close();
	                    dst.close();
	                    //Toast.makeText(getApplicationContext(), "projects exported", Toast.LENGTH_LONG).show();
                    }
                }
                
            }
        } catch (Exception e) {
        	Toast.makeText(getApplicationContext(), "projects exported"+e, Toast.LENGTH_LONG).show();
        }
		/*try {
			Cursor c = sqldb.rawQuery("select * from project", null);
			int rowcount = 0;
			int colcount = 0;
			File sdCardDir = Environment.getExternalStorageDirectory();
			String filename = "MyBackUp.csv";
			// the name of the file to export with
			File saveFile = new File(sdCardDir, filename);
			FileWriter fw = new FileWriter(saveFile);
			BufferedWriter bw = new BufferedWriter(fw);
			rowcount = c.getCount();
			colcount = c.getColumnCount();
			if (rowcount > 0) {
				c.moveToFirst();
			    for (int i = 0; i < colcount; i++) {
			    	if (i != colcount - 1) {
				       bw.write(c.getColumnName(i) + ",");
			        } else {
			           bw.write(c.getColumnName(i));
			        }
			    }
			    bw.newLine();
			    for (int i = 0; i < rowcount; i++) {
			    	c.moveToPosition(i);
			        for (int j = 0; j < colcount; j++) {
			        	if (j != colcount - 1)
			        		bw.write(c.getString(j) + ",");
			        	else
			        		bw.write(c.getString(j));
			         }
			        bw.newLine();
			    }
			    bw.flush();
			    Toast.makeText(getApplicationContext(), "projects exported", Toast.LENGTH_LONG).show();
			}
	   } catch (Exception ex) {
			if (sqldb.isOpen()) {
				sqldb.close();
				Toast.makeText(getApplicationContext(), "Problem in exporting projects", Toast.LENGTH_LONG).show();
		     }
	} finally {
	
	}*/
	}
		private void clearProjects() throws JSONException{
		Log.d("my", "clear Project");
		ServerConnection sc = new ServerConnection();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "clearProjects"));
        String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);
        JSONObject jsonResponse = new JSONObject(result);

        String s = jsonResponse.getString("success");//jsonResponse.length();
		Log.d("my", result+" result json :"+s);
        if(s.equalsIgnoreCase("true")){
        	int deletedRecords = deleteProjects(jsonResponse);
        	if(deletedRecords > 0){
        		  mAdapter.setProjects(dbAdapter.getProjects());
				  mAdapter.notifyDataSetChanged();
				  //Toast.makeText(this," Projects Refreshed", Toast.LENGTH_LONG).show();
        	}
		}else{
            Toast.makeText(getApplicationContext(), "Problem in deleting projects", Toast.LENGTH_LONG).show();
        }
        updatePreselectData();
	}
	
	private void updatePreselectData() throws JSONException{
		dbAdapter.deleteDescriptionTopic();
		dbAdapter.deleteToxic();
		dbAdapter.deleteMember();
		
		ServerConnection sc = new ServerConnection();
		sc.downloadToxicSubstance(mContext, dbAdapter);
    	sc.downloadDescriptionTopic(mContext, dbAdapter);
    	sc.downloadVisitor(mContext, dbAdapter);
	}
	
	private int deleteProjects(JSONObject jsonResponse) throws JSONException{
		int count = 0;
		for(int i1=0;i1<jsonResponse.length()-1;i1++)
		{
			JSONObject jo = jsonResponse.getJSONObject(""+i1);

			//delete pdf file
			long project_id = dbAdapter.getProjectId(jo.getInt("id"));


			if(project_id != -1){
				ProjectModel ProjectDetails = dbAdapter.getProject(project_id);
				//deleteProjectsPdf(ProjectDetails);

				deleteProjectsImage(ProjectDetails);
			}

			int i = dbAdapter.deleteServerProject(jo.getInt("id"));
			if(i != -1)
			{
				JSONObject jr = new JSONObject(jo.getString("positions"));
				deletePositions(jr, project_id);
				JSONObject pl = new JSONObject(jo.getString("plans"));
				deletePlans(pl, project_id);
				count++;
			}else{
				Toast.makeText(getApplicationContext(), "Problem deleting project no:"+jo.getInt("id"), Toast.LENGTH_LONG).show();
			}
		}
		return count;
	}

	private void deletePositionsImage(long project_id, long position_id){

		if(project_id>0 && position_id>0){
			deleteProjectsFile(project_id+"_"+position_id+"_position1.jpg",Config.imageDir);
			deleteProjectsFile(project_id+"_"+position_id+"_position2.jpg",Config.imageDir);
		}
	}

	public void deleteProjectsImage(ProjectModel ProjectDetails){
		if(!ProjectDetails.server_project_id.equalsIgnoreCase("")){
			deleteProjectsFile(ProjectDetails.server_project_id+"_Project.jpg",Config.imageDir);
		}
	}

	public void deleteProjectsPdf(long project_id, int number){

		if(project_id > 0){
			deleteProjectsFile(project_id+"_"+"plan"+number+".pdf",Config.pdfDir);
		}
	}

	private void deleteProjectsFile(String name, String dirName){
		//Toast.makeText(getApplicationContext(), dirName+""+name, Toast.LENGTH_LONG).show();
		File directory, pdfFile = null;
		try {

			directory = new File(dirName);//Config.pdfDir

			if (!directory.exists()) {
				Log.d("my", "creating Pdf directory"+directory);
				if (!directory.mkdirs()) {
					Log.d("my", "problem creating pdf File "+directory);
				}
			}


			if(directory.exists() && !name.equalsIgnoreCase("")){

				pdfFile = new File(directory,name);
				if (pdfFile.exists()) {
					if (pdfFile.delete()) {
						//Toast.makeText(getApplicationContext(), "file Deleted :" + name, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "file not Deleted :" + name, Toast.LENGTH_LONG).show();
					}
				}
			}else{
				Toast.makeText(getApplicationContext(), "Directory Not found "+name, Toast.LENGTH_LONG).show();
				Log.d("my", "Directory Not found "+name);
			}

		} catch (Exception e) {
			Log.e("my", "Something went wrong while downloading pdf" + e.toString());
			e.printStackTrace();
		}
	}

	private void deletePositions(JSONObject jsonResponse, long project_id) throws JSONException{
		for(int i1=0;i1<jsonResponse.length();i1++)
		{
			JSONObject jo = jsonResponse.getJSONObject(""+i1);
			deletePositionsImage(project_id, jo.getInt("id"));
			int i = dbAdapter.deleteServerPosition(jo.getInt("id"));
			if(i == -1)
			{
				Toast.makeText(getApplicationContext(), "Problem deleting position no:"+jo.getInt("id"), Toast.LENGTH_LONG).show();
			}
		}
	}

	private void deletePlans(JSONObject jsonResponse, long project_id) throws JSONException{
		for(int i1=0;i1<jsonResponse.length();i1++)
		{
			JSONObject jo = jsonResponse.getJSONObject(""+i1);
			deleteProjectsPdf(project_id, jo.getInt("number"));
			int i = dbAdapter.deleteServerPlan(jo.getInt("id"));
			if(i == -1)
			{
				Toast.makeText(getApplicationContext(), "Problem deleting position no:"+jo.getInt("id"), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void logout() {
		session = new SessionManager(getApplicationContext());
		if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
			session.setLogin(false);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
	}
	
	 private byte[] convertToByteArray(InputStream inputStream) throws IOException{
		    
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                
         int next = inputStream.read();
         while (next > -1) {
               bos.write(next);
               next = inputStream.read();
          }
                
         bos.flush();
                
         return bos.toByteArray();
   }
   
	
	public void upload() throws JSONException, IOException {

		ServerConnection sc = new ServerConnection();
		List<ProjectModel> projectList = dbAdapter.getLocalProjects();
		Log.d("my", "uploading");
		if(projectList != null){
			//Toast.makeText(getApplicationContext(), " upload", Toast.LENGTH_LONG).show();
				for(int i=0; i<projectList.size(); i++){
					ProjectModel pm = projectList.get(i);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
			        params.add(new BasicNameValuePair("action", "upload"));
			        params.add(new BasicNameValuePair("date_create", pm.date_creation));
			        params.add(new BasicNameValuePair("name_station", pm.name_station));
			        params.add(new BasicNameValuePair("number_station", pm.number_station));
			        params.add(new BasicNameValuePair("adress", pm.address));
					params.add(new BasicNameValuePair("object", pm.object));
					params.add(new BasicNameValuePair("auftraggeber", pm.auftraggeber));
			        params.add(new BasicNameValuePair("date_visit", pm.date_visit));
			        params.add(new BasicNameValuePair("name_evaluator", pm.name_evaluator));
			        params.add(new BasicNameValuePair("server_project_id", pm.server_project_id));
			        Log.d("my", "uploading pdf");
			        /*if(pm.plan1 != null){
						File plan1 = new File(pm.plan1);
						if(plan1.exists()) {
							InputStream inputStream = new FileInputStream(plan1);
							byte[] data = convertToByteArray(inputStream);
							if (data != null) {
								String pdf_str = Base64.encodeToString(data, Base64.DEFAULT);
								params.add(new BasicNameValuePair("photo_plan1", pdf_str));
							}
						}
			        }*/

			        if(pm.image != null){
						File planImage = new File(pm.image);
						if(planImage.exists()) {
							InputStream inputStream = new FileInputStream(planImage);
							byte[] data = convertToByteArray(inputStream);
							if (data != null) {
								String image_str = Base64.encodeToString(data, Base64.DEFAULT);
								params.add(new BasicNameValuePair("photo_object", image_str));
							}
						}
			        }
			        
			        String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);

					try{

						JSONObject jsonResponse = new JSONObject(result);
						String s = jsonResponse.getString("success");//jsonResponse.length();
						Log.d("my", "success:"+s);
						if(s.equalsIgnoreCase("true")){
							int server_project_id = jsonResponse.getInt("project_id");
							Log.d("my", "server_pro_id:"+server_project_id);
							updateProjectStatus(pm.id, server_project_id, "uploaded");	  //Toast.makeText(this,pm.id+" Records Uploaded Succesfully", Toast.LENGTH_LONG).show();
							if(pm.id > 0 && server_project_id>0){
								uploadPlans(pm.id,server_project_id);
								uploadPositions(pm.id,server_project_id);
								mAdapter.setProjects(dbAdapter.getProjects());
								mAdapter.notifyDataSetChanged();
								Toast.makeText(getApplicationContext(), "projekt Uploaded", Toast.LENGTH_LONG).show();
							}
							//progressDoalog.dismiss();
						}else{
							//progressDoalog.dismiss();
							String eMsg = jsonResponse.getString("errorMsg");
							Log.d("my", "success:"+eMsg);
							Toast.makeText(getApplicationContext(), "Problem to upload", Toast.LENGTH_LONG).show();
						}
					}catch (JSONException e){
						hideDialog();
						Toast.makeText(getApplicationContext(), e+ " Fail Uploading"+result, Toast.LENGTH_LONG).show();
					}


				}
				
		}else{
			Toast.makeText(this,"Keine Projekte zum upload vorhanden!!", Toast.LENGTH_LONG).show();
		}
		
	}

	public void uploadPlans(long id, int server_project_id) throws JSONException, IOException {
		Log.d("my", "uploadingPlans");
		ServerConnection sc = new ServerConnection();
		List<PlanModel> planList = dbAdapter.getLocalPlans(id);

		if(planList != null){
			for(int i=0; i<planList.size(); i++){
				PlanModel pm = planList.get(i);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "uploadPlans"));
				params.add(new BasicNameValuePair("project_id", ""+server_project_id));
				params.add(new BasicNameValuePair("number", pm.number));
				params.add(new BasicNameValuePair("plan_name", ""+pm.plan_name));
				params.add(new BasicNameValuePair("server_plan_id", pm.server_plan_id));

				if(pm.plan_url != null){
					File plan1 = new File(pm.plan_url);
					if(plan1.exists()) {
						InputStream inputStream = new FileInputStream(plan1);
						byte[] data = convertToByteArray(inputStream);
						if (data != null) {
							String pdf_str = Base64.encodeToString(data, Base64.DEFAULT);
							params.add(new BasicNameValuePair("plan_url", pdf_str));
						}
					}
				}else{
					params.add(new BasicNameValuePair("plan_url", ""));
					Log.d("my", "plan Not Added:");
				}

				String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);
				JSONObject jsonResponse = new JSONObject(result);
				String s = jsonResponse.getString("success");//jsonResponse.length();
				Log.d("my", "Plan success:"+s);

				if(s.equalsIgnoreCase("true")){
					int server_plan_id = jsonResponse.getInt("plan_id");
					Log.d("my", "server_pro_id:"+server_plan_id);
					updatePlanStatus(pm.id, server_plan_id, "uploaded");	  //Toast.makeText(this,pm.id+" Records Uploaded Succesfully", Toast.LENGTH_LONG).show();
				}else{
					//Error in login. Get the error message
					Toast.makeText(getApplicationContext(), "problem uploading positions", Toast.LENGTH_LONG).show();
				}
			}
		}else{
			//Toast.makeText(this," No position to upload!!", Toast.LENGTH_LONG).show();
		}
	}

	public void uploadPositions(long id, int server_project_id) throws JSONException, IOException {
		Log.d("my", "uploadingPositions");
		ServerConnection sc = new ServerConnection();
		List<PositionModel> positionList = dbAdapter.getLocalPositions(id);
		if(positionList != null){
				for(int i=0; i<positionList.size(); i++){
					Log.d("my", "position des "+positionList.get(i).description);
					PositionModel pm = positionList.get(i);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
			        params.add(new BasicNameValuePair("action", "uploadPositions"));
			        params.add(new BasicNameValuePair("project_id", ""+server_project_id));
			        params.add(new BasicNameValuePair("toxic_substance", pm.toxic_substance));
			        params.add(new BasicNameValuePair("position_number", ""+pm.position_number));
			        params.add(new BasicNameValuePair("description_topic", pm.description_topic));
			        params.add(new BasicNameValuePair("description", pm.description));
			        params.add(new BasicNameValuePair("degree", pm.degree));
			        params.add(new BasicNameValuePair("comment", pm.comment));
			        params.add(new BasicNameValuePair("server_position_id", pm.server_position_id));
					params.add(new BasicNameValuePair("investigation", ""+pm.investigation));
					params.add(new BasicNameValuePair("priority", ""+pm.priority));
					//Toast.makeText(this,pm.priority+" aa upload!!"+pm.investigation, Toast.LENGTH_LONG).show();
			        if(pm.photo1 != null){
			        	InputStream inputStream = new FileInputStream(new File(pm.photo1));
				        byte[] data = convertToByteArray(inputStream);
				        if(data != null){
				        	String image_str = Base64.encodeToString(data,Base64.DEFAULT);
				        	params.add(new BasicNameValuePair("photo1", image_str));
				        }
			        }else{
			        	params.add(new BasicNameValuePair("photo1", ""));
			        	 Log.d("my", "Photo1 Not Added:");
			        }
			        
			        if(pm.photo2 != null){
			        	InputStream inputStream = new FileInputStream(new File(pm.photo2));
				        byte[] data = convertToByteArray(inputStream);
				        if(data != null){
				        	String image_str = Base64.encodeToString(data,Base64.DEFAULT);
				        	params.add(new BasicNameValuePair("photo2", image_str));
				        }
			        }else{
			        	params.add(new BasicNameValuePair("photo2", ""));
			        	 Log.d("my", "Photo2 Not Added:");
			        }
			        
			        String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);
			        JSONObject jsonResponse = new JSONObject(result);
					String s = jsonResponse.getString("success");//jsonResponse.length();  
			        Log.d("my", "Position success:"+s);
			        
			        if(s.equalsIgnoreCase("true")){
			        	int server_position_id = jsonResponse.getInt("position_id");
			        	Log.d("my", "server_pro_id:"+server_position_id);
			        	updatePositionStatus(pm.id, server_position_id, "uploaded");	  //Toast.makeText(this,pm.id+" Records Uploaded Succesfully", Toast.LENGTH_LONG).show();
			        }else{
			        	 //Error in login. Get the error message
			            Toast.makeText(getApplicationContext(), "problem uploading positions", Toast.LENGTH_LONG).show();
			        }	
				}
				
		}else{
			//Toast.makeText(this," No position to upload!!", Toast.LENGTH_LONG).show();
		}
	}
	
	public void download() throws JSONException, UnsupportedEncodingException {
		Log.d("my", "Downloading");
		ServerConnection sc = new ServerConnection();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "download"));
        String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);
		//Toast.makeText(this," Result"+result, Toast.LENGTH_LONG).show();
        try{
			JSONObject jsonResponse = new JSONObject(result);
			String s = jsonResponse.getString("success");//jsonResponse.length();
			Log.d("my", result+" result json :"+s);
			if(s.equalsIgnoreCase("true")){
				int downloadLenth = saveRecord(jsonResponse);
				if(downloadLenth > 0){
					Toast.makeText(this,downloadLenth+" Records Downloaded", Toast.LENGTH_LONG).show();
					mAdapter.setProjects(dbAdapter.getProjects());
					mAdapter.notifyDataSetChanged();
					if(downloadLenth == jsonResponse.length()-1){
						updateStatus();
					}
				}else{
					Toast.makeText(this,downloadLenth+" No records to download", Toast.LENGTH_LONG).show();
				}

			}else{
				Toast.makeText(getApplicationContext(), "No records to download", Toast.LENGTH_LONG).show();
			}
        }catch (JSONException e){
			Toast.makeText(getApplicationContext(), "JSONException "+e, Toast.LENGTH_LONG).show();
			hideDialog();
		}
	}
	
	public int saveRecord(JSONObject jsonResponse) throws JSONException, UnsupportedEncodingException {
    	int count = 0;
    	Log.d("my", "saving");
    	for(int i1=0;i1<jsonResponse.length()-1;i1++){
    			JSONObject jo = jsonResponse.getJSONObject(""+i1);
    		
    			ContentValues initialValues = new ContentValues();
    			initialValues.put("name_station", jo.getString("name_report"));
    			initialValues.put("number_station", jo.getString("project_number"));
    			initialValues.put("address", jo.getString("adress"));
				initialValues.put("object", jo.getString("objekt"));
				initialValues.put("auftraggeber", jo.getString("auftraggeber"));
    			initialValues.put("date_visit", jo.getString("date_visit"));
    			initialValues.put("date_creation", jo.getString("date_visit"));
    			initialValues.put("name_evaluator", jo.getString("name_evaluator"));
    			initialValues.put("status", "Downloaded");

				long project_id = dbAdapter.getProjectId(jo.getInt("id"));

				String projectIdPrefix=""+jo.getInt("id");
				if(projectIdPrefix.equalsIgnoreCase(""))
					projectIdPrefix = System.currentTimeMillis()+"";

    			if(!jo.getString("photo_object").equalsIgnoreCase("")){
					String imgUrl =jo.getString("photo_object");
					String imgName = imgUrl.replace("https://bwxpress.ch/mdcs/upload/photos/", "");
    				initialValues.put("image", getImage(Config.SERVER_FILES_ACCESS_URL+"&imgName="+imgName,projectIdPrefix+"_Project"));
    			}
    		/*	if(!jo.getString("photo_plan1").equalsIgnoreCase("")){
					String pdfUrl =jo.getString("photo_plan1");
					String pdfName = pdfUrl.replace("https://bwxpress.ch/mdcs/upload/plaene/", "");
					initialValues.put("plan1", sc.DownloadFile(Config.SERVER_FILES_ACCESS_URL+"&fileName="+pdfName, projectIdPrefix+"_plan1"));
    			}
			*/
    			initialValues.put("server_project_id", jo.getInt("id"));
            	
    			Log.d("my", "saving Project Locally");


    			long i;
				if(project_id != -1){
    				i = dbAdapter.update(initialValues, project_id);
    			}else{
					i = dbAdapter.save(initialValues);
					project_id = i;
    			}

				if(project_id != -1)
				{
					savePlan(jo.getString("plans"),project_id);
				    savePosition(jo.getString("positions"),project_id);
				    mAdapter.setProjects(dbAdapter.getProjects());
				    mAdapter.notifyDataSetChanged();
					count++;
				}else{
					Toast.makeText(this, "Some problem in saving Downloaded record",Toast.LENGTH_LONG).show();
				}
    	}
		return count;
	}

	public void savePlan(String plans,long project_id) throws JSONException
	{
		JSONObject jsonPlan = new JSONObject(plans);
		for(int j=0;j<jsonPlan.length();j++){
			int count= j+1;
			JSONObject jp = jsonPlan.getJSONObject(""+j);
			ContentValues initialValues = new ContentValues();

			initialValues.put("project_id", ""+project_id);
			initialValues.put("plan_name", jp.getString("plan_name"));
			initialValues.put("number", jp.getString("number"));

			String planIdPrefix=""+jp.getInt("id");
			if(planIdPrefix.equalsIgnoreCase(""))
				planIdPrefix = System.currentTimeMillis()+"";

			if(!jp.getString("plan_url").equalsIgnoreCase("")){
				String pdfUrl =jp.getString("plan_url");
				String pdfName = pdfUrl.replace("https://bwxpress.ch/mdcs/upload/plaene/", "");
				pdfName = pdfName.replace("./upload/plaene/", "");
				initialValues.put("plan_url", sc.DownloadFile(Config.SERVER_FILES_ACCESS_URL+"&fileName="+pdfName, project_id+"_plan"+jp.getString("number")));
			}

			long plan_id = dbAdapter.getPlanId(jp.getInt("id"));

			initialValues.put("status", "Downloaded");
			initialValues.put("server_plan_id", jp.getInt("id"));

			long i;
			if(plan_id != -1){
				Log.d("my", "plan updated");
				i = dbAdapter.updatePlan(initialValues, plan_id);
			}else{
				Log.d("my", "plan Inserted");
				i = dbAdapter.savePlan(initialValues);
			}

			if(i == -1)
			{
				Toast.makeText(this, "Some problem in saving Plans",Toast.LENGTH_LONG).show();
			}
		}
	}


	public void savePosition(String positions,long project_id) throws JSONException
	{
		JSONObject jsonPosition = new JSONObject(positions);
		Log.d("my", "saving Position length"+jsonPosition.length());
		for(int j=0;j<jsonPosition.length();j++){
			
			JSONObject jp = jsonPosition.getJSONObject(""+j);
			ContentValues initialValues = new ContentValues();
			initialValues.put("project_id", ""+project_id);
	      	initialValues.put("position_number", jp.getString("positon_number"));
			initialValues.put("toxic_substance", jp.getString("toxic_substance"));
			initialValues.put("description_topic", jp.getString("description1"));
			initialValues.put("description", jp.getString("description2"));
			initialValues.put("degree", jp.getString("dimension"));
			initialValues.put("comment", jp.getString("comment"));
			initialValues.put("investigation", jp.getString("investigation"));
			initialValues.put("priority", jp.getString("priority"));

			long position_id = dbAdapter.getPositonId(jp.getInt("id"));
			String prefixPositionID = jp.getInt("id")+"";
			if(prefixPositionID.equalsIgnoreCase(""))
				prefixPositionID = System.currentTimeMillis()+"";

			if(!jp.getString("photo1").equalsIgnoreCase("") && project_id>=0){
				Log.d("my", "photo1 download");
				String imgUrl =jp.getString("photo1");
				String imgName = imgUrl.replace("https://bwxpress.ch/mdcs/upload/photos/", "");
				initialValues.put("photo1", getImage(Config.SERVER_FILES_ACCESS_URL+"&imgName="+imgName,project_id+"_"+prefixPositionID+"_position1"));
			}
			if(!jp.getString("photo2").equalsIgnoreCase("") && project_id>=0){
				Log.d("my", "photo2 download");
				String imgUrl = jp.getString("photo2");
				String imgName = imgUrl.replace("https://bwxpress.ch/mdcs/upload/photos/", "");
				initialValues.put("photo2", getImage(Config.SERVER_FILES_ACCESS_URL+"&imgName="+imgName,project_id+"_"+prefixPositionID+"_position2"));
			}
			initialValues.put("status", "Downloaded");
			
        	initialValues.put("server_position_id", jp.getInt("id"));
			

			long i;
			if(position_id != -1){
				Log.d("my", "position updated");
				i = dbAdapter.updatePosition(initialValues, position_id);
			}else{
				Log.d("my", "position Inserted");
				i = dbAdapter.savePosition(initialValues);
			}
				
			if(i == -1)
			{
				Toast.makeText(this, "Some problem in saving Positions",Toast.LENGTH_LONG).show();
			}
		}
	}

	
	public void updateProjectStatus(long project_id, int server_project_id, String status) {
		ContentValues initialValues = new ContentValues();
			initialValues.put("server_project_id", ""+server_project_id);
			initialValues.put("status", status);
			if(project_id> 0){
				long index = dbAdapter.update(initialValues, project_id);
				if(index == -1)
				{
					Toast.makeText(this, "problem updating project status",Toast.LENGTH_LONG).show();
				}
			}
    	
	}

	public void updatePlanStatus(long plan_id, int server_plan_id, String status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put("server_plan_id", server_plan_id);
		initialValues.put("status", status);
		if(plan_id> 0){
			long index = dbAdapter.updatePlan(initialValues, plan_id);
			if(index == -1)
			{
				Toast.makeText(this, "problem updating Plans status",Toast.LENGTH_LONG).show();
			}
		}
	}

	public void updatePositionStatus(long position_id, int server_position_id, String status) {
		ContentValues initialValues = new ContentValues();
			initialValues.put("server_position_id", server_position_id);
			initialValues.put("status", status);
			if(position_id> 0){
				long index = dbAdapter.updatePosition(initialValues, position_id);
				if(index == -1)
				{
					Toast.makeText(this, "problem updating position status",Toast.LENGTH_LONG).show();
				}
			}
	}
	
	public void updateStatus() throws JSONException {
		ServerConnection sc = new ServerConnection();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "updateStatus"));
        String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);
        JSONObject jsonResponse = new JSONObject(result);
		String s = jsonResponse.getString("success");//jsonResponse.length();  
		if(!s.equalsIgnoreCase("true")){
			Toast.makeText(this,"This is problem updating status", Toast.LENGTH_LONG).show();
		}
	}
	
	public String getImage(String path, String name) {
		Bitmap bitmapimg = sc.downloadBitmap(path);
        /*ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if(bitmapimg != null){
        bitmapimg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        }
		return bytes.toByteArray();*/
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmapimg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
		//String DownloadImagePath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+System.currentTimeMillis() + ".jpg";
		if(name.equalsIgnoreCase(""))
			name = System.currentTimeMillis()+"";
		String DownloadImagePath = Environment.getExternalStorageDirectory()+"/Pictures/mdcsPhoto/"+name + ".jpg";
		File destination = new File(DownloadImagePath);
		
		
		FileOutputStream fo;
		try {
			destination.createNewFile();
			fo = new FileOutputStream(destination);
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("my", "Downloaded::"+DownloadImagePath);
		return DownloadImagePath;
	}
	
	public void startDetailActivity(long id) {
    	Intent intent = new Intent(this, AddProjectActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent,0);
	}
}
