package de.codenis.mdcs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddPlanActivity extends Activity {

    DBAdapter dbAdapter;
    EditText number, plan_name;
    TextView plan_url;
    ImageView pdf_view;
    Button delete;
    private Context mContext;
    long id, projectId, planId=0;
    int count = 0;
    String planPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_plan);
        Log.d("my", "addPlan");
        mContext = this;
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        number 	                = (EditText) findViewById(R.id.number);
        plan_name 	            = (EditText) findViewById(R.id.plan_name);
        pdf_view                = (ImageView)findViewById(R.id.plan_view);
        plan_url 				= (TextView) findViewById(R.id.plan_url);
        delete = (Button)findViewById(R.id.delete);
        
        getActionBar().setTitle("neue Plan erstellen");

        projectId = getIntent().getExtras().getLong("projectId");
        planId = getIntent().getExtras().getLong("planId");

        if(planId > 0){

            getActionBar().setTitle("Plan bearbeiten");

            PlanModel PlanDetails = dbAdapter.getPlan(planId);
            number.setText(PlanDetails.number);
            plan_name.setText(PlanDetails.plan_name);
            if(PlanDetails.plan_url != null){
                plan_url.setText(PlanDetails.plan_url);
                pdf_view.setVisibility(View.VISIBLE);
            }

            delete.setVisibility(View.VISIBLE);

        }else{
            int totalPlans = dbAdapter.getTotalPlans(projectId);
            totalPlans = totalPlans +1;
            if(totalPlans>0){
                number.setText(""+totalPlans);
            }
        }


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //YOUR CODE
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //YOUR CODE
            }

            @Override
            public void afterTextChanged(Editable s) {
                findViewById(R.id.save).setBackgroundColor(Color.BLUE);
            }
        };
        number.addTextChangedListener(watcher);
        plan_name.addTextChangedListener(watcher);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_project, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);

       if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        }
    }

    public void selectPlan(View v) {
        findViewById(R.id.save).setBackgroundColor(Color.BLUE);
        int result_code = 2;
        if(v.getId() == R.id.upload_plan)
            result_code = 2;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), result_code);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode >= 2 ) {

            Uri selectedImageUri = data.getData();
            if(selectedImageUri!=null){
                planPath = PathUtils.getPath(getApplicationContext(), selectedImageUri);
                plan_url.setText(planPath);
                pdf_view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void openPdfClick(View v) throws Exception {
        Toast.makeText(this, "openPdfClick not found" ,Toast.LENGTH_LONG).show();
        String pdfPath="";
        pdfPath = plan_url.getText().toString();
        openPdf(pdfPath);
    }

    public void openPdf(String plan_path) throws ActivityNotFoundException, Exception {

        if(plan_path != null){
            File file = new File(plan_path);
            if (file.exists()) {
                Uri filepath = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(filepath, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(Intent.createChooser(intent, "Your title"));
                } catch (Exception e) {
                    Toast.makeText(this, "File Not opening"+e,Toast.LENGTH_LONG).show();
                    Log.e("error", "" + e);
                }
            } else {
                Toast.makeText(this, "File not found" ,Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onSave(View v)
    {
        ProjectModel pm = dbAdapter.getProject(projectId);
        if(pm.status.equalsIgnoreCase("uploaded") || pm.status.equalsIgnoreCase("downloaded")){
            ContentValues initialValues = new ContentValues();
            initialValues.put("status", "aktualisiert");
            long i = dbAdapter.update(initialValues, projectId);
            if(i == -1)
            {
                Toast.makeText(this, "Problem in updating Project status",Toast.LENGTH_LONG).show();
            }
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put("project_id", projectId);
        initialValues.put("number", number.getText().toString());
        initialValues.put("plan_name", plan_name.getText().toString());

        if(plan_url!=null){
            initialValues.put("plan_url", plan_url.getText().toString());
        }

        if(planId > 0){
            initialValues.put("status", "aktualisiert");
            long i = dbAdapter.updatePlan(initialValues, planId);
            if(i != -1)
            {
                Toast.makeText(this, "plan updated: "+i,Toast.LENGTH_LONG).show();

            }else{
                Toast.makeText(this, "Some problem in updating",Toast.LENGTH_LONG).show();
            }
        }else{
            initialValues.put("status", "erstellt");
            long i = dbAdapter.savePlan(initialValues);
            if(i != -1)
            {
                Intent addProject = new Intent(this, AddProjectActivity.class);
                addProject.putExtra("id", projectId);
                startActivityForResult(addProject,0);
                Toast.makeText(this, "plan saved successfully",Toast.LENGTH_LONG).show();

            }else{
                Toast.makeText(this, "Some problem in saving",Toast.LENGTH_LONG).show();
            }
        }
        findViewById(R.id.save).setBackgroundColor(getResources().getColor(R.color.gulabi));
    }

    public void onBack(View v)
    {
        Button saveButton = (Button) findViewById(R.id.save);
        ColorDrawable buttonColor = (ColorDrawable) saveButton.getBackground();
        int colorId = buttonColor.getColor();
        int savedCode = getResources().getColor(R.color.gulabi);

        if(colorId != savedCode){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Bestätigen");
            builder.setMessage("Hast du die Änderungen gespeichert?");

            builder.setPositiveButton("Ja", new OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    Intent addProject = new Intent(AddPlanActivity.this, AddProjectActivity.class);
                    addProject.putExtra("id", projectId);
                    startActivityForResult(addProject,0);
                }
            });

            builder.setNegativeButton("Nein", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }else{
            Intent addProject = new Intent(AddPlanActivity.this, AddProjectActivity.class);
            addProject.putExtra("id", projectId);
            startActivityForResult(addProject,0);
        }
    }


    public void onDeletePlan(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("bitte bestätigen")
        .setTitle("Plan löschen?")
        .setCancelable(true)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Ok", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //deletePlanFiles(planId);
                Toast.makeText(mContext, planId+ "Plan gelöscht",Toast.LENGTH_LONG).show();
                int i = dbAdapter.deletePlan(planId);

                if(i != -1)
                {
                    Toast.makeText(mContext, i+ "Plan gelöscht",Toast.LENGTH_LONG).show();
                    Intent addProject = new Intent(mContext, AddProjectActivity.class);
                    addProject.putExtra("id", projectId);
                    startActivityForResult(addProject,0);
                }
            }
        }).show();
    }

    public void deletePlanFiles(long planId){
        PlanModel Plan = dbAdapter.getPlan(planId);
        deletePlansImage(Plan.project_id, Plan.server_plan_id);
    }
    private void deletePlansImage(long project_id, String plan_id){

        if(project_id>0 && !plan_id.equalsIgnoreCase("")){
            deleteFiles(project_id+"_"+plan_id+"_plan.pdf",Config.imageDir);
        }
    }

    private void deleteFiles(String name, String dirName){
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

}