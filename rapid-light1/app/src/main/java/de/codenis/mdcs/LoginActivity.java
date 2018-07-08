package de.codenis.mdcs;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	 private Button button_login;
	 private EditText username ,password;
	 private ProgressDialog processDialog;
	 private Context mContext;
	 private SessionManager session;
	 private DBAdapter dbAdapter;
	 private ServerConnection sc;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        
        setContentView(R.layout.activity_login);

		// for network strict mode of android
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
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

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            int permissionCheckRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheckRead != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            int permissionCheckCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		mContext = this;

		session = new SessionManager(getApplicationContext());
		if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
		username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        button_login = (Button) findViewById(R.id.button_login);
              
     // Progress dialog
        processDialog = new ProgressDialog(this);
        processDialog.setMessage("Logging in ...");
        processDialog.setCancelable(false);
        processDialog.setIndeterminate(true);
 
        
     // Login button Click Event
        button_login.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
            	
                String uname = username.getText().toString().trim();
                String pass =  password.getText().toString().trim();
 
                // Check for empty data in the form
                if (!uname.isEmpty() && !pass.isEmpty()) {
                    try {
                    	showDialog();
						checkLogin(uname, pass);
					} catch (JSONException e) {
						hideDialog();
						Toast.makeText(getApplicationContext(),"Error"+ e, Toast.LENGTH_LONG).show();
					}
                } else {
                    Toast.makeText(getApplicationContext(),"Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }
 
        });
		
	}
	public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if(ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }
	
    private void checkLogin(final String uname, final String pass) throws JSONException {
    	if(isInternetAvailable()){
    		sc = new ServerConnection();
    	       
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("action", "checkLogin"));
            params.add(new BasicNameValuePair("username", uname));
            params.add(new BasicNameValuePair("password", pass));
         
            String result = sc.sendPostRequest(Config.SERVER_URL,params,mContext);


            //Toast.makeText(getApplicationContext(),result +" result 1", Toast.LENGTH_LONG).show();
            JSONObject jsonResponse = new JSONObject(result);
            String s = jsonResponse.getString("success");
            //Toast.makeText(getApplicationContext(),"result :"+s, Toast.LENGTH_LONG).show();
            
            if(s.equalsIgnoreCase("true")){
            	session.setLogin(true);
            	int check = dbAdapter.checkMember(uname, pass);
            	if(check == 0){
    	        	// Inserting row in users table
    	        	ContentValues initialValues = new ContentValues();
    				initialValues.put("loginname", uname);
    				initialValues.put("password", pass);
    	            long i = dbAdapter.addUser(initialValues);
    	            if(i == -1)
    				{
    					Toast.makeText(this, "problem in adding users: " +i,Toast.LENGTH_LONG).show();
    				}
            	}
            	sc.downloadToxicSubstance(mContext, dbAdapter);
            	sc.downloadDescriptionTopic(mContext, dbAdapter);
            	sc.downloadVisitor(mContext, dbAdapter);
                // Launch main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
            	hideDialog();
            	// Error in login. Get the error message
                Toast.makeText(this, "username or password Incorrect1", Toast.LENGTH_LONG).show();
            }
    	}else{
    		int check = dbAdapter.checkMember(uname, pass);
        	if(check > 0){
        		session.setLogin(true);
        		// Launch main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
            	hideDialog();
            	// Error in login. Get the error message
                Toast.makeText(this, "username or password Incorrect2", Toast.LENGTH_LONG).show();
            }
    	}
        
    }
 
    private void showDialog() {
        if (!processDialog.isShowing())
            processDialog.show();
    }
 
    private void hideDialog() {
        if (processDialog.isShowing())
            processDialog.dismiss();
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	
}
