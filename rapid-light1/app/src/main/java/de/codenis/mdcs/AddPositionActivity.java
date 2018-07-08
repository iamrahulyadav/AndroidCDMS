package de.codenis.mdcs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

	public class AddPositionActivity extends Activity {
	    
		DBAdapter dbAdapter;
		EditText position_number, description, degree, remark;
		Spinner toxic, description_topic, priority;
		CheckBox investigation;
		ArrayAdapter<String> spinnerArrayAdapter;
		ImageView imageview1, imageview2;
		Button delete;
		private Context mContext;
		byte[] photo1InByte,photo2InByte;
		long id, projectId, positionId=0;
		int count = 0;
		String photo1Path=null,photo2Path=null,photoPath;
		Bitmap bmGallery;
		ArrayList<String> substance, topic;
		 private Uri fileUri;
		 String picturePath;
		 Uri selectedImage;
		 Bitmap photo;
		 String ba1;
		 
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.add_position);
	        Log.d("my", "addProject");
	        mContext = this;
	        dbAdapter = new DBAdapter(this);
			dbAdapter.open();
	        position_number 	=(EditText) findViewById(R.id.position_number);
	        description_topic 	=(Spinner) findViewById(R.id.description_topic);
	        description 		=(EditText) findViewById(R.id.description);
	        degree 				=(EditText) findViewById(R.id.degree);
	        remark 				=(EditText) findViewById(R.id.remark);
	        toxic				= (Spinner) findViewById(R.id.toxic);
	        imageview1 = (ImageView)findViewById(R.id.imageView1);
	        imageview2 = (ImageView)findViewById(R.id.imageView2);
	        delete = (Button)findViewById(R.id.delete);
			priority 	=(Spinner) findViewById(R.id.priority);
			investigation = (CheckBox) findViewById(R.id.investigation);

	        substance = dbAdapter.get("toxic_substance", "toxic_substance");
	        if(substance != null){
	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, substance);
	        	toxic.setAdapter(adapter);
	        }
	        
	        topic = dbAdapter.get("preselect_description", "preset_preselect_description");
	        if(topic != null){
	        	ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, topic);
	        	description_topic.setAdapter(adapter1);
	        }

			ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.priority_list));
			priority.setAdapter(adapter2);
	        //Button selectImage = (Button) findViewById(R.id.upload_image);
	        
	        getActionBar().setTitle("neue Position erstellen");

	        projectId = getIntent().getExtras().getLong("projectId");
	        positionId = getIntent().getExtras().getLong("positionId");
	        //Toast.makeText(this,projectId+" :Intent IDs: "+positionId, Toast.LENGTH_SHORT).show();
	        if(positionId > 0){
	        	
	        	getActionBar().setTitle("Position bearbeiten");
	        	
	        	PositionModel PositionDetails = dbAdapter.getPosition(positionId);
				position_number.setText(PositionDetails.position_number);
				description.setText(PositionDetails.description);
				degree.setText(PositionDetails.degree);
				remark.setText(PositionDetails.comment);
				if(PositionDetails.investigation == 1)
					investigation.setChecked(true);
				else
					investigation.setChecked(false);

				priority.setSelection(PositionDetails.priority);
				//Toast.makeText(getBaseContext(),PositionDetails.description_topic+"Time"+PositionDetails.toxic_substance, Toast.LENGTH_SHORT).show();
				//Toast.makeText(this,PositionDetails.priority+" aa bb!!"+PositionDetails.investigation, Toast.LENGTH_LONG).show();

				int colorIndex = substance.indexOf(PositionDetails.toxic_substance);
				if(colorIndex>=0)
					toxic.setSelection(colorIndex);
				else
					toxic.setSelection(0);
				
				int desIndex = topic.indexOf(PositionDetails.description_topic);
				if(desIndex>=0)
					description_topic.setSelection(desIndex);
				else
					description_topic.setSelection(0);

				if(PositionDetails.photo1 != null){
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					Bitmap b1 = BitmapFactory.decodeFile(PositionDetails.photo1);
					imageview1.setImageBitmap(b1);
				}
				if(PositionDetails.photo2 != null){
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					Bitmap b2 = BitmapFactory.decodeFile(PositionDetails.photo2);
					imageview2.setImageBitmap(b2);
				}
				delete.setVisibility(View.VISIBLE);
			
	        }else{
				int totalPositions = dbAdapter.getTotalPositions(projectId);
				totalPositions = totalPositions +1;
				if(totalPositions>0){
					position_number.setText(""+totalPositions);
				}
			}
	    	

	    	description_topic.setOnItemSelectedListener(new OnItemSelectedListener()
	        {
				String Priority1;
				int priorityIndex;
	            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
	            {    	
	            	String selectedItem = parent.getItemAtPosition(position).toString();
	            	if(!selectedItem.equalsIgnoreCase(""))
						Priority1 = dbAdapter.getPriorityDescription(selectedItem, "preset_preselect_description");

	            	priorityIndex = Integer.parseInt(Priority1);
					priority.setSelection(priorityIndex);
	            }

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					//Toast.makeText(getBaseContext(),"Select Time", Toast.LENGTH_SHORT).show();
				}        
			});

			toxic.setOnItemSelectedListener(new OnItemSelectedListener()
			{
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					//Toast.makeText(getBaseContext(),"Select Time", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});


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
			position_number.addTextChangedListener(watcher);
			description.addTextChangedListener(watcher);
			degree.addTextChangedListener(watcher);
			remark.addTextChangedListener(watcher);

			investigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					findViewById(R.id.save).setBackgroundColor(Color.BLUE);
				}
			});

			View.OnTouchListener spinnerOnTouch = new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					findViewById(R.id.save).setBackgroundColor(Color.BLUE);
					return false;
				}
			};

			description_topic.setOnTouchListener(spinnerOnTouch);
			toxic.setOnTouchListener(spinnerOnTouch);
			priority.setOnTouchListener(spinnerOnTouch);
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
	       /* if (id == R.id.action_save_car) {
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
	            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	        }
	    }
	    
	    public void selectImage(View v) {
			findViewById(R.id.save).setBackgroundColor(Color.BLUE);
	    	Log.d("my", "select Image");
	    	final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
	    	AlertDialog.Builder builder = new AlertDialog.Builder(AddPositionActivity.this);
	    	builder.setTitle("Add Photo 1!");
	    	builder.setItems(items, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int item) {
		    		if (items[item].equals("Take Photo")) {
		    			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    	            fileUri = getOutputMediaFileUri(1);
	    	            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    	            startActivityForResult(intent, 1);
		    		} else if (items[item].equals("Choose from Library")) {
		    				Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		    				intent.setType("image/*");
		    				startActivityForResult(Intent.createChooser(intent, "Select File"),4);
		    		} else if (items[item].equals("Cancel")) {
		    			dialog.dismiss();
		    		}
		    	}
		    });
	    	builder.show();
	    }
	    
	    public void selectImage2(View v) {
			findViewById(R.id.save).setBackgroundColor(Color.BLUE);
	    	Log.d("my", "select Image");
	    	final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
	    	AlertDialog.Builder builder = new AlertDialog.Builder(AddPositionActivity.this);
	    	builder.setTitle("Add Photo 2!");
	    	builder.setItems(items, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int item) {
		    		if (items[item].equals("Take Photo")) {
		    			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    	            fileUri = getOutputMediaFileUri(1);
	    	            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    	            startActivityForResult(intent,2);
		    		} else if (items[item].equals("Choose from Library")) {
		    				Intent intent = new Intent(
		    						Intent.ACTION_PICK,
		    						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		    				intent.setType("image/*");
		    				startActivityForResult(Intent.createChooser(intent, "Select File"),3);
		    		} else if (items[item].equals("Cancel")) {
		    			dialog.dismiss();
		    		}
		    	}
		    });
	    	builder.show();
	    }
	    
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	super.onActivityResult(requestCode, resultCode, data);
	    	if (resultCode == RESULT_OK) {
	    		if (requestCode == 1 || requestCode == 2) {
	    			
	    			photoPath = fileUri.getPath();
	    			Bitmap thumbnail = getBitmap(photoPath);
	    			
	    			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    			thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

					deleteOriginalImage(photoPath);

					File destination = getOutputMediaFile(1,"RP_position"+requestCode);
	    			photoPath = destination.getAbsolutePath();
	    			//photoPath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+System.currentTimeMillis() + ".jpg";
	    			//File destination = new File(photoPath);

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
	    			if (requestCode == 1) {
		    			photo1Path = photoPath;
		    			imageview1.setImageBitmap(thumbnail);
		    			//photo1InByte = bytes.toByteArray();
	    			}else if (requestCode == 2) {
		    			photo2Path = photoPath;
		    			imageview2.setImageBitmap(thumbnail);
		    			//photo2InByte = bytes.toByteArray();
	    			}
	    			
		    	}else if (requestCode == 4 || requestCode == 3) {
	    			Uri selectedImageUri = data.getData();
	    			String[] projection = { MediaColumns.DATA };
	    			CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,
	    					null);
	    			Cursor cursor =cursorLoader.loadInBackground();
	    			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
	    			cursor.moveToFirst();
	    			photoPath = cursor.getString(column_index);
	    			//Bitmap bm;
	    			BitmapFactory.Options options = new BitmapFactory.Options();
	    			options.inJustDecodeBounds = true;
	    			BitmapFactory.decodeFile(photoPath, options);
	    			
	    			final int REQUIRED_SIZE = 200;
	    			int scale = 1;
	    			while (options.outWidth / scale / 2 >= REQUIRED_SIZE
	    					&& options.outHeight / scale / 2 >= REQUIRED_SIZE)
	    				scale *= 2;
	    				options.inSampleSize = scale;
	    				options.inJustDecodeBounds = false;
	    				bmGallery = BitmapFactory.decodeFile(photoPath, options);
	    				
	    		}
	    		if(requestCode == 4){
	    			photo1Path = photoPath;
	    			imageview1.setImageBitmap(bmGallery);
    				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    				bmGallery.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
	    			photo1InByte = bytes.toByteArray();
	    			}
    				else if (requestCode == 3){
    					photo2Path = photoPath;
    	    			imageview2.setImageBitmap(bmGallery);
        				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        				bmGallery.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
    	    			photo2InByte = bytes.toByteArray();
    				}
	    	}
	    	else{
	    		Toast.makeText(this, "Take photo Again!",Toast.LENGTH_LONG).show();
	    	}
	    }

	    public void deleteOriginalImage(String originalImagePath){
			File fdelete = new File(originalImagePath);
			if (fdelete.exists()) {
				if (fdelete.delete()) {
					System.out.println("file Deleted :" + originalImagePath);
				} else {
					System.out.println("file not Deleted :" + originalImagePath);
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

			int investigationValue = 0;
			if(investigation.isChecked())
	    		investigationValue = 1;

	        ContentValues initialValues = new ContentValues();
	        initialValues.put("project_id", projectId);
			initialValues.put("position_number", position_number.getText().toString());
			initialValues.put("toxic_substance", toxic.getSelectedItem().toString());
			initialValues.put("description_topic", description_topic.getSelectedItem().toString());
			initialValues.put("description", description.getText().toString());
			initialValues.put("degree", degree.getText().toString());
			initialValues.put("comment", remark.getText().toString());
			initialValues.put("investigation", investigationValue);
			initialValues.put("priority", priority.getSelectedItemPosition());
			if(photo1Path!=null){
				initialValues.put("photo1", photo1Path);
			}
			if(photo2Path!=null){
				initialValues.put("photo2", photo2Path);
			}
			
			
	    	
			if(positionId > 0){
				initialValues.put("status", "aktualisiert");
				long i = dbAdapter.updatePosition(initialValues, positionId);
				if(i != -1)
				{	

					Toast.makeText(this, "position updated: "+i,Toast.LENGTH_LONG).show();
				
				}else{
					Toast.makeText(this, "Some problem in updating",Toast.LENGTH_LONG).show();
				}
			}else{
				initialValues.put("status", "erstellt");
				long i = dbAdapter.savePosition(initialValues);
				if(i != -1)
				{
					Intent addProject = new Intent(this, AddProjectActivity.class);
					addProject.putExtra("id", projectId);
					startActivityForResult(addProject,0);
					Toast.makeText(this, "position saved successfully",Toast.LENGTH_LONG).show();
				
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

			//Toast.makeText(mContext, colorId+" Color "+savedCode,Toast.LENGTH_LONG).show();

			if(colorId != savedCode){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Bestätigen");
				builder.setMessage("Hast du die Änderungen gespeichert?");

				builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog

						dialog.dismiss();
						Intent addProject = new Intent(AddPositionActivity.this, AddProjectActivity.class);
						addProject.putExtra("id", projectId);
						startActivityForResult(addProject,0);
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
				Intent addProject = new Intent(AddPositionActivity.this, AddProjectActivity.class);
				addProject.putExtra("id", projectId);
				startActivityForResult(addProject,0);
			}
	    }
	    
	    
	    public void onDeletePosition(View v)
		{
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("bitte bestätigen")
			.setTitle("Position löschen?")
			.setCancelable(true)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deletePositionFiles(positionId);
					int i = dbAdapter.deletePosition(positionId);
					if(i != -1)
					{
						Toast.makeText(mContext, i+ "Position gelöscht",Toast.LENGTH_LONG).show();
						Intent addProject = new Intent(mContext, AddProjectActivity.class);
				    	addProject.putExtra("id", projectId);
						startActivityForResult(addProject,0);
					}
				}
			}).show();
		}    
	    
	    public void deletePositionFiles(long positionId){
			PositionModel Position = dbAdapter.getPosition(positionId);
			deletePositionsImage(Position);
		}
		private void deletePositionsImage(PositionModel Position){

	    	if(Position.server_position_id != null){
				if(Position.project_id>0 && !Position.server_position_id.equalsIgnoreCase("")){
					deleteFiles(Position.project_id+"_"+Position.server_position_id+"_position1.jpg",Config.imageDir);
					deleteFiles(Position.project_id+"_"+Position.server_position_id+"_position2.jpg",Config.imageDir);
				}
			}
			if(Position.photo1 != null) {
				deleteFiles(getImageName(Position.photo1),Config.rpPosition1);
	    	}
			if(Position.photo2 != null) {
				deleteFiles(getImageName(Position.photo2),Config.rpPosition2);
			}
		}

		public String getImageName(String imgPath){
			String result = imgPath.substring(imgPath.lastIndexOf("/") + 1);
			return result;
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
	        return Uri.fromFile(getOutputMediaFile(type, "mdcsPhoto"));
	    }
	  
	    /**
	     * returning image / video
	     */
	    private static File getOutputMediaFile(int type, String myFolder) {
	  
	        // External sdcard location
	        File mediaStorageDir = new File(
	                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), myFolder);
	  
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
	            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	        } else {
	            return null;
	        }
	  
	        return mediaFile;
	    }
	}