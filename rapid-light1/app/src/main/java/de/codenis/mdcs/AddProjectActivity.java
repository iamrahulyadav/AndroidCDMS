package de.codenis.mdcs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddProjectActivity extends Activity {
	    
		DBAdapter dbAdapter;
		EditText station_name, station_number, address, object, auftraggeber,namePlan1,namePlan2,namePlan3,namePlan4,namePlan5;
		Spinner visitor;
		ArrayAdapter<String> spinnerArrayAdapter;
		ImageView imageview1;
		Button delete, addPosition, addPlan, visit_date;
		TextView photo_path;
		String planPath;
		long projectId=0;
		byte[] imageInByte;
		long id,positionId,planId;
		String selectedImagePath;
		private Context mContext;
		private DatePicker datePicker;
		private Calendar calendar;
		private int year, month, day;
		private PositionListAdapter positionAdapter;
		private PlanListAdapter planAdapter;
		ListView positionListView, planListView;
		List<PositionModel> projectPositionsList;
		List<PlanModel> projectPlansList;
		ArrayList<String> member;
		private Uri fileUri;
		String picturePath;
		Uri selectedImage;
		Bitmap photo;
		String ba1;
		
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.add_project);
	        Log.d("my", "in ADDProject");
	        mContext = this;
	        dbAdapter = new DBAdapter(this);
			dbAdapter.open();
	        station_name 			=(EditText) findViewById(R.id.station_name);
	        station_number 			=(EditText) findViewById(R.id.station_number);
			object 		            =(EditText) findViewById(R.id.object);
	        address 				=(EditText) findViewById(R.id.address);
			auftraggeber            =(EditText) findViewById(R.id.auftraggeber);
	        visit_date 				=(Button) findViewById(R.id.visit_date);
	        visitor					= (Spinner) findViewById(R.id.visitor);
			imageview1				= (ImageView) findViewById(R.id.imageView1);

	        delete = (Button)findViewById(R.id.delete);
	        addPosition = (Button)findViewById(R.id.add_position);
			addPlan = (Button)findViewById(R.id.add_plan);
	        calendar = Calendar.getInstance();
	        year = calendar.get(Calendar.YEAR);
	        month = calendar.get(Calendar.MONTH);
	        day = calendar.get(Calendar.DAY_OF_MONTH);
	        showDate(year, month+1, day);


	        member = dbAdapter.get("member", "team");
	        if(member != null){
	        	ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, member);
	        	visitor.setAdapter(adapter1);
	        }
	        getActionBar().setTitle("neues Projekt erstellen");
	        projectId = getIntent().getExtras().getLong("id");
	        if(projectId > 0){
	        	getActionBar().setTitle("Projekt bearbeiten");
	        	ProjectModel ProjectDetails = dbAdapter.getProject(projectId);
	        	station_name.setText(ProjectDetails.name_station);
				station_number.setText(ProjectDetails.number_station);
				address.setText(ProjectDetails.address);
				object.setText(ProjectDetails.object);
				auftraggeber.setText(ProjectDetails.auftraggeber);
				visit_date.setText(ProjectDetails.date_visit);
				//Toast.makeText(getApplicationContext(), ProjectDetails.name_evaluator+"resize fail", Toast.LENGTH_SHORT).show();
				int colorIndex = member.indexOf(ProjectDetails.name_evaluator);
				visitor.setSelection(colorIndex);
				
				if(ProjectDetails.image != null ){
					Bitmap b1 = BitmapFactory.decodeFile(ProjectDetails.image);
					imageview1.setImageBitmap(b1);
				}

				delete.setVisibility(View.VISIBLE);
				addPosition.setVisibility(View.VISIBLE);
				addPlan.setVisibility(View.VISIBLE);
				projectPositionsList = dbAdapter.getPositions(projectId);
				positionAdapter  = new PositionListAdapter(this, projectPositionsList);
				positionListView = (ListView)findViewById(R.id.position_list);
				positionListView.setAdapter(positionAdapter);
				Boolean resize = setListViewHeightBasedOnItems(positionListView);
				if(resize){
					//Toast.makeText(getApplicationContext(), "resized", Toast.LENGTH_SHORT).show();
				}else{
					//Toast.makeText(getApplicationContext(), "resize fail", Toast.LENGTH_SHORT).show();
				}


				projectPlansList = dbAdapter.getPlans(projectId);
				planAdapter  = new PlanListAdapter(this, projectPlansList);
				planListView = (ListView)findViewById(R.id.plan_list);
				planListView.setAdapter(planAdapter);
				Boolean resize1 = setListViewHeightBasedOnItems(planListView);
	        }
	    	
	    	/*visitor.setOnItemSelectedListener(new OnItemSelectedListener()
	        {
	            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
	            {    	
	            	String selectedItem = parent.getItemAtPosition(position).toString();
	            }

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					Toast.makeText(getBaseContext(),"Select Time", Toast.LENGTH_SHORT).show();
				}        
			});*/


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
			station_name.addTextChangedListener(watcher);
			station_number.addTextChangedListener(watcher);
			address.addTextChangedListener(watcher);
			object.addTextChangedListener(watcher);
			auftraggeber.addTextChangedListener(watcher);

			View.OnTouchListener spinnerOnTouch = new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					findViewById(R.id.save).setBackgroundColor(Color.BLUE);
					return false;
				}
			};

			visitor.setOnTouchListener(spinnerOnTouch);
	    }

	    	 @SuppressWarnings("deprecation")
	    	   public void setDate(View view) {
				  findViewById(R.id.save).setBackgroundColor(Color.BLUE);
	    	      showDialog(999);
	    	      //Toast.makeText(getApplicationContext(), "ca", Toast.LENGTH_SHORT).show();
	    	   }

	    	   @Override
	    	   protected Dialog onCreateDialog(int id) {
	    	      if (id == 999) {
	    	         return new DatePickerDialog(this, myDateListener, year, month, day);
	    	      }
	    	      return null;
	    	   }

	    	   private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
	    	      @Override
	    	      public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
	    	         showDate(arg1, arg2+1, arg3);
	    	      }
	    	   };

	    	   private void showDate(int year, int month, int day) {
	    		   String day1   = "" + day;
	    		   String month1 = "" + month;
	    		   String year1  = "" + year;
	    		   if(day<10){
	    			   day1 = "0"+day;
	    		   }
	    		   if(month<10){
	    			   month1 = "0"+month;
	    		   }
	    	      visit_date.setText(new StringBuilder().append(day1).append(".").append(month1).append(".").append(year1));
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
	        /*if (id == R.id.action_save_car) {
	        	//this.onSave();
	            return true;
	        }*/
	        return super.onOptionsItemSelected(item);
	    }

	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	            super.onConfigurationChanged(newConfig); 

	       if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	           // Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	        }
	    }
	    
	    public static boolean setListViewHeightBasedOnItems(ListView listView) {

	        ListAdapter listAdapter = listView.getAdapter();
	        if (listAdapter != null) {

	            int numberOfItems = listAdapter.getCount();

	            // Get total height of all items.
	            int totalItemsHeight = 0;
	            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
	                View item = listAdapter.getView(itemPos, null, listView);
	                item.measure(0, 0);
	                totalItemsHeight += item.getMeasuredHeight();
	            }

	            // Get total height of all item dividers.
	            int totalDividersHeight = listView.getDividerHeight() * 
	                    (numberOfItems - 1);

	            // Set list height.
	            ViewGroup.LayoutParams params = listView.getLayoutParams();
	            params.height = totalItemsHeight + totalDividersHeight;
	            listView.setLayoutParams(params);
	            listView.requestLayout();

	            return true;

	        } else {
	            return false;
	        }

	    }

	    
	    public void selectImage(View v) {
			findViewById(R.id.save).setBackgroundColor(Color.BLUE);
	    	Log.d("my", "select Image");
	    	final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
	    	AlertDialog.Builder builder = new AlertDialog.Builder(AddProjectActivity.this);
	    	builder.setTitle("Add Photo!");
	    	builder.setItems(items, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int item) {
		    		if (items[item].equals("Take Photo")) {
		    				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    	            fileUri = getOutputMediaFileUri(1);
		    	            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		    	            startActivityForResult(intent, 0);
		    		} else if (items[item].equals("Choose from Library")) {
		    				Intent intent = new Intent(
		    						Intent.ACTION_PICK,
		    						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		    				intent.setType("image/*");
		    				startActivityForResult(Intent.createChooser(intent, "Select File"),1);
		    		} else if (items[item].equals("Cancel")) {
		    			dialog.dismiss();
		    		}
		    	}
		    });
	    	builder.show();
	    }



	    public void selectPlan(View v) {
			findViewById(R.id.save).setBackgroundColor(Color.BLUE);
	    		int result_code = 2;
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
		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {

				selectedImagePath = fileUri.getPath();
				Bitmap thumbnail = getBitmap(selectedImagePath);
				imageview1.setImageBitmap(thumbnail);

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
				File destination = getOutputMediaFile(1,"RP_object");
				selectedImagePath = destination.getAbsolutePath();

				//selectedImagePath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+System.currentTimeMillis() + ".jpg";
				//File destination = new File(selectedImagePath);
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


	    			/*Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
	    			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    			thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
	    			File destination = new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera/",
	    					System.currentTimeMillis() + ".jpg");

	    			selectedImagePath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+System.currentTimeMillis() + ".jpg";
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
	    			}*/

			} else if (requestCode == 1) {
				Uri selectedImageUri = data.getData();
				String[] projection = { MediaColumns.DATA };
				CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,null);
				Cursor cursor =cursorLoader.loadInBackground();
				int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				cursor.moveToFirst();
				selectedImagePath = cursor.getString(column_index);
				Bitmap bm;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(selectedImagePath, options);

				final int REQUIRED_SIZE = 200;
				int scale = 1;
				while (options.outWidth / scale / 2 >= REQUIRED_SIZE
						&& options.outHeight / scale / 2 >= REQUIRED_SIZE)
					scale *= 2;
				options.inSampleSize = scale;
				options.inJustDecodeBounds = false;
				bm = BitmapFactory.decodeFile(selectedImagePath, options);
				Toast.makeText(getApplicationContext(),"photo  = "+ selectedImagePath, Toast.LENGTH_LONG).show();
				imageview1.setImageBitmap(bm);
				//photo_path.setText(selectedImagePath);

	    				/*ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		    			bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
		    			imageInByte = bytes.toByteArray();*/
			}
		}
	}
	    
	    public Bitmap getBitmap(String path){
	    	final BitmapFactory.Options options = new BitmapFactory.Options();
	    	options.inSampleSize = 4;
	    	Bitmap myBitmap = BitmapFactory.decodeFile(path,options);
        	 try {
                 ExifInterface exif = new ExifInterface(path);
                 int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                 Log.d("EXIF", "Exif: " + orientation);
                 Matrix matrix = new Matrix();
                 if (orientation == 6) {
                     matrix.postRotate(90);
                 }
                 else if (orientation == 3) {
                     matrix.postRotate(180);
                 }
                 else if (orientation == 8) {
                     matrix.postRotate(270);
                 }
                 myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
             }
             catch (Exception e) {

             }
	    	return myBitmap;
	    }


	    public void openPdf(PlanModel planModel) throws ActivityNotFoundException, Exception {
			String plan_path = planModel.plan_url;
			Long planId = planModel.id;

	    	if(plan_path != null){
	    	File file = new File(plan_path);
				//Toast.makeText(getApplicationContext(),"planPath  = "+ plan_path, Toast.LENGTH_LONG).show();
            if (file.exists()) {
                Uri filepath = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(filepath, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    //startActivity(intent);
					startActivity(Intent.createChooser(intent, "Your title"));
                } catch (Exception e) {
                	Toast.makeText(this, "File Not opening"+e,Toast.LENGTH_LONG).show();
                    Log.e("error", "" + e);
                }
            } else {
            	Toast.makeText(this, "File not found" ,Toast.LENGTH_LONG).show();
            }
	    	}

	    	if(planId>0)
	  			updatePlanStatus(planId);
	    }

	    public void updatePlanStatus(Long planId){
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
			initialValues.put("status", "aktualisiert");
			if(planId > 0) {
				long i = dbAdapter.updatePlan(initialValues, planId);
				if (i != -1) {
					//Toast.makeText(this, "plan updated: " + i, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "Some problem in updating", Toast.LENGTH_LONG).show();
				}
			}
		}

	    public void onSave(View v)
		{
	        ContentValues initialValues = new ContentValues();
			initialValues.put("name_station", station_name.getText().toString());
			initialValues.put("number_station", station_number.getText().toString());
			initialValues.put("address", address.getText().toString());
			initialValues.put("object", object.getText().toString());
			initialValues.put("auftraggeber", auftraggeber.getText().toString());
			initialValues.put("date_visit", visit_date.getText().toString());
			initialValues.put("name_evaluator", visitor.getSelectedItem().toString());
			initialValues.put("date_creation", visit_date.getText().toString());

			if(selectedImagePath != null){
				initialValues.put("image", selectedImagePath);
			}

			if(projectId > 0){
				initialValues.put("status", "aktualisiert");
				long i = dbAdapter.update(initialValues, projectId);
				if(i != -1)
				{
					Toast.makeText(this, "Project updated " +i,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(this, "Some problem in Updating Project",Toast.LENGTH_LONG).show();
				}
			}else{
				initialValues.put("status", "erstellt");
				long i = dbAdapter.save(initialValues);
				if(i != -1)
				{
					Intent homepage = new Intent(AddProjectActivity.this, MainActivity.class);
					this.startActivity(homepage);
	        		Toast.makeText(this, "project saved "+i,Toast.LENGTH_LONG).show();
	        		//ProjectModel p = dbAdapter.getProject(i);
					
					/*if (p.image!=null) {
						Bitmap b1=BitmapFactory.decodeByteArray(p.image, 0, p.image.length);
						imageview2.setImageBitmap(b1);
						Toast.makeText(this, "not null"+p.status,Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(this, "null",Toast.LENGTH_LONG).show();
					}*/
	        		
				}else{
					Toast.makeText(this, "Some problem in saving",Toast.LENGTH_LONG).show();
				}
			}
			findViewById(R.id.save).setBackgroundColor(getResources().getColor(R.color.gulabi));
		}

		public boolean haveUnsavedChange(){
			Button saveButton = (Button) findViewById(R.id.save);
			ColorDrawable buttonColor = (ColorDrawable) saveButton.getBackground();
			int colorId = buttonColor.getColor();
			int savedCode = getResources().getColor(R.color.gulabi);
			if(colorId != savedCode){
				return true;
			}else{
				return false;
			}
		}

	    public void onBack(View v)
		{
			if(haveUnsavedChange()){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Bestätigen");
				builder.setMessage("Hast du die Änderungen gespeichert?");

				builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog

						dialog.dismiss();
						Intent homepage = new Intent(AddProjectActivity.this, MainActivity.class);
						startActivity(homepage);
					}
				});

				builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// Do nothing
						dialog.dismiss();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

			}else{
				Intent homepage = new Intent(AddProjectActivity.this, MainActivity.class);
				startActivity(homepage);
			}
		}
	    
	    public void onNewPosition(View v)
		{
			if(haveUnsavedChange()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Bestätigen");
				builder.setMessage("Hast du die Änderungen gespeichert?");

				builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog

						dialog.dismiss();
						Intent addPosition = new Intent(AddProjectActivity.this, AddPositionActivity.class);
						addPosition.putExtra("projectId", projectId);
						startActivityForResult(addPosition, 0);

					}
				});

				builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// Do nothing
						dialog.dismiss();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}else{
				Intent addPosition = new Intent(AddProjectActivity.this, AddPositionActivity.class);
				addPosition.putExtra("projectId", projectId);
				startActivityForResult(addPosition, 0);
			}
    	}


		public void onNewPlan(View v)
		{
			if(haveUnsavedChange()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Bestätigen");
				builder.setMessage("Hast du die Änderungen gespeichert?");

				builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog

						dialog.dismiss();
						Intent addPosition = new Intent(AddProjectActivity.this, AddPlanActivity.class);
						addPosition.putExtra("projectId", projectId);
						startActivityForResult(addPosition, 0);

					}
				});

				builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// Do nothing
						dialog.dismiss();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}else{
				Intent addPosition = new Intent(AddProjectActivity.this, AddPlanActivity.class);
				addPosition.putExtra("projectId", projectId);
				startActivityForResult(addPosition, 0);
			}
		}

	    public void onDeleteProject(View v)
		{
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			ProjectModel project = dbAdapter.getProject(projectId);
			builder.setMessage("Wollen Sie das Projekt ("+project.number_station+", "+project.object+", "+project.address+") mit allen Fotos und Plänen wirklich vom Tablet löschen? ?")
			.setTitle("bitte bestätigen")
			.setCancelable(true)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteProjectFiles(projectId);
					int i = dbAdapter.deleteProject(projectId);
					if(i != -1)
					{
						Toast.makeText(mContext, i+" Project Deleted Successfully",Toast.LENGTH_LONG).show();
						Intent homepage = new Intent(mContext, MainActivity.class);
			            mContext.startActivity(homepage);
					}
				}
			}).show();
		}    

		public void deleteProjectFiles(long projectId){
			ProjectModel ProjectDetails = dbAdapter.getProject(projectId);
			//deleteProjectsPdf(ProjectDetails);
			deleteProjectsImage(ProjectDetails);

			if(projectPlansList != null){
				for(int i=0;i<projectPlansList.size();i++) {
					PlanModel plans = projectPlansList.get(i);
					if(plans.number != null)
					deleteProjectsPdf(ProjectDetails, plans.number);
				}
			}

			if(projectPositionsList != null){
				for(int i=0;i<projectPositionsList.size();i++) {
					PositionModel positions = projectPositionsList.get(i);
					deletePositionsImage(positions);
				}
			}
		}

	private void deletePositionsImage(PositionModel position){

		if(position.server_position_id !=null){
			if(position.project_id>0 && !position.server_position_id.equalsIgnoreCase("")){
				deleteFiles(position.project_id+"_"+position.server_position_id+"_position1.jpg",Config.imageDir);
				deleteFiles(position.project_id+"_"+position.server_position_id+"_position2.jpg",Config.imageDir);
			}
		}

		if(position.photo1 != null) {
			deleteFiles(getImageName(position.photo1),Config.rpPosition1);
		}
		if(position.photo2 != null) {
			deleteFiles(getImageName(position.photo2),Config.rpPosition2);
		}
	}

	public String getImageName(String imgPath){
		String result = imgPath.substring(imgPath.lastIndexOf("/") + 1);
		return result;
	}

	public void deleteProjectsImage(ProjectModel ProjectDetails){
		if(ProjectDetails.server_project_id!=null){
			if(!ProjectDetails.server_project_id.equalsIgnoreCase("")){
				deleteFiles(ProjectDetails.server_project_id+"_Project.jpg",Config.imageDir);
			}
		}

		if(ProjectDetails.image != null){
			deleteFiles(getImageName(ProjectDetails.image),Config.rpObject);
		}
	}

	public void deleteProjectsPdf(ProjectModel Project, String number){

		if(Project.id > 0){
			deleteFiles(Project.id+"_"+"plan"+number+".pdf",Config.pdfDir);
		}
	}

	private void deleteFiles(String name, String dirName){
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

	    public void startPositionDetailActivity(long id) {

			positionId = id;
			if(haveUnsavedChange()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Bestätigen");
				builder.setMessage("Hast du die Änderungen gespeichert?");

				builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog

						dialog.dismiss();
						Intent intent = new Intent(AddProjectActivity.this, AddPositionActivity.class);
						intent.putExtra("projectId", projectId);
						intent.putExtra("positionId", positionId);
						startActivityForResult(intent,0);

					}
				});

				builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
						dialog.dismiss();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}else{
				Intent intent = new Intent(AddProjectActivity.this, AddPositionActivity.class);
				intent.putExtra("projectId", projectId);
				intent.putExtra("positionId", positionId);
				startActivityForResult(intent,0);
			}
		}

	public void startPlanDetailActivity(long id) {

		planId = id;
		if(haveUnsavedChange()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("Bestätigen");
			builder.setMessage("Hast du die Änderungen gespeichert?");

			builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog

					dialog.dismiss();
					Intent intent = new Intent(AddProjectActivity.this, AddPlanActivity.class);
					intent.putExtra("projectId", projectId);
					intent.putExtra("planId", planId);
					startActivityForResult(intent,0);

				}
			});

			builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
					dialog.dismiss();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}else{
			Intent intent = new Intent(AddProjectActivity.this, AddPlanActivity.class);
			intent.putExtra("projectId", projectId);
			intent.putExtra("planId", planId);
			startActivityForResult(intent,0);
		}
	}


	/**
	     * Here we store the file url as it will be null after returning from camera
	     * app
	     */
	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	  
	        // save file url in bundle as it will be null on screen orientation
	        // changes
	        outState.putParcelable("file_uri", fileUri);
	    }
	  
	    @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	        super.onRestoreInstanceState(savedInstanceState);
	  
	        // get the file url
	        fileUri = savedInstanceState.getParcelable("file_uri");
	    }
	    
	    /**
	     * Creating file uri to store image/video
	     */
	    public Uri getOutputMediaFileUri(int type) {
	        return Uri.fromFile(getOutputMediaFile(type,"mdcsPhoto"));
	    }
	  
	    /**
	     * returning image / video
	     */
	    private static File getOutputMediaFile(int type, String myFolder) {
	  
	        // External sdcard location
	        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),myFolder);
	  
	        // Create the storage directory if it does not exist
	        if (!mediaStorageDir.exists()) {
	            if (!mediaStorageDir.mkdirs()) {
	                Log.d("my", "Oops! Failed create directory");
	                return null;
	            }
	        }
	  
	        // Create a media file name
	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
	        File mediaFile;
	        if (type == 1) {
	            mediaFile = new File(mediaStorageDir.getPath() + File.separator+ "IMG_" + timeStamp + ".jpg");
	        } else {
	            return null;
	        }
	        return mediaFile;
	    }
	}