package de.codenis.mdcs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreen extends Activity {
 
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_splash);
 
        new Handler().postDelayed(new Runnable() {
        	
        	@Override
            public void run() {
               Intent i = new Intent(SplashScreen.this, LoginActivity.class);
               startActivity(i);
               finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
