package de.codenis.mdcs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.codenis.mdcs.tools.EditingToolsAdapter;
import de.codenis.mdcs.tools.ToolType;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.ViewType;

public class EditPlan1Activity extends AppCompatActivity implements OnPhotoEditorListener,View.OnClickListener,EditingToolsAdapter.OnItemSelected {
	Bitmap originalBitmap, image, updatedimage;
	ImageView iv_ttx;
	EditText et_sample;
	DBAdapter dbAdapter;
	private Context mContext;
	long id, projectId, planId=0, positionId=0;
	List<PositionModel> planPositionsList;
    String planUrl;

	private PhotoEditor mPhotoEditor;
	private PhotoEditorView mPhotoEditorView;
	private TextView mTxtCurrentTool;
	private RecyclerView mRvTools, mRvFilters;
	private ConstraintLayout mRootView;
	public static final int READ_WRITE_STORAGE = 52;
	private ProgressDialog mProgressDialog;
	private EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);

	float scr_x0 = 0;
	float scr_y0 = 0;

	float scr_x1 = 0;
	float scr_y1 = 0;

	int tch = 1;
	float scaleFactorx = 1, scaleFactory = 1;

	int mode = 0;

	Paint paint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_plan1);
		mContext = this;
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();

		planId = getIntent().getExtras().getLong("planId");
		projectId = getIntent().getExtras().getLong("projectId");
		planUrl = getIntent().getExtras().getString("planUrl");

		planPositionsList = dbAdapter.getPlanPositions(projectId, planId);

		initViews();
		LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mRvTools.setLayoutManager(llmTools);
		mRvTools.setAdapter(mEditingToolsAdapter);

		mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
				.setPinchTextScalable(true) // set flag to make text scalable when pinch
				//.setDefaultTextTypeface(mTextRobotoTf)
				//.setDefaultEmojiTypeface(mEmojiTypeFace)
				.build(); // build photo editor sdk

		mPhotoEditor.setOnPhotoEditorListener(this);

		mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(planUrl)));

		OnTouchListener touch = new OnTouchListener() {


			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				View parent = (View) arg0.getParent();
				parent.performClick();
				// gettin x,y cordinates on screen touch
				scr_x0 = arg1.getRawX();
				scr_y0 = arg1.getRawY();
				//Toast.makeText(EditPlan1Activity.this, scr_x0+"kk !"+scr_y0,Toast.LENGTH_SHORT).show();
				switch (arg1.getAction() & MotionEvent.ACTION_MASK) {

					case MotionEvent.ACTION_POINTER_UP:
						scr_x0 = arg1.getRawX();
						scr_y0 = arg1.getRawY();
						break;
				}
				return true;
			}
		};
		View rootView = mPhotoEditor.mLayoutInflater.inflate(ja.burhanrashid52.photoeditor.R.layout.view_photo_editor_text, null);
		rootView.setOnTouchListener(touch);

		Toast.makeText(EditPlan1Activity.this, rootView.getLeft()+"LeftTop !"+rootView.getTop(),
				Toast.LENGTH_SHORT).show();
		mPhotoEditorView.getSource().setOnTouchListener(touch);

		if(planPositionsList != null){
			for (int i = 0; i < planPositionsList.size(); i++) {
				PositionModel p = planPositionsList.get(i);
				updateImage(p.position_xo, p.position_yo, p.position_number);
				Button myButton = new Button(this);
				myButton.setText(p.position_number);

				RelativeLayout layout = (RelativeLayout) findViewById(R.id.linearLayout2);
				RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(250, 80);
				pa.setMargins((int)p.position_xo, (int)p.position_yo, 0, 0);
				Button buttonView = new Button(this);
				buttonView.setText(p.position_number);
				buttonView.setTag(p.id);
				buttonView.setOnClickListener(mThisButtonListener);
				layout.addView(buttonView, pa);
			}
		}
	}
	private void initViews() {
		ImageView imgUndo;
		ImageView imgRedo;
		ImageView imgCamera;
		ImageView imgGallery;
		ImageView imgSave;
		ImageView imgClose;

		mPhotoEditorView = findViewById(R.id.photoEditorView);
		mTxtCurrentTool = findViewById(R.id.txtCurrentTool);
		mRvTools = findViewById(R.id.rvConstraintTools);
		mRvFilters = findViewById(R.id.rvFilterView);
		mRootView = findViewById(R.id.rootView);

		imgUndo = findViewById(R.id.imgUndo);
		imgUndo.setOnClickListener(this);

		imgRedo = findViewById(R.id.imgRedo);
		imgRedo.setOnClickListener(this);

		imgCamera = findViewById(R.id.imgCamera);
		imgCamera.setOnClickListener(this);

		imgGallery = findViewById(R.id.imgGallery);
		imgGallery.setOnClickListener(this);

		imgSave = findViewById(R.id.imgSave);
		imgSave.setOnClickListener(this);

		imgClose = findViewById(R.id.imgClose);
		imgClose.setOnClickListener(this);

	}

	private OnClickListener mThisButtonListener = new OnClickListener() {
		public void onClick(View v) {
			positionId = Integer.parseInt(v.getTag().toString().trim());
			Toast.makeText(EditPlan1Activity.this, "Hello !"+positionId,
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(EditPlan1Activity.this, AddPositionActivity.class);
			intent.putExtra("projectId", projectId);
			intent.putExtra("positionId", positionId);
			intent.putExtra("planId", planId);
			startActivityForResult(intent,0);
		}
	};

	void saveImage(Bitmap img) {
			ContentValues initialValues = new ContentValues();
			initialValues.put("project_id", projectId);
			initialValues.put("investigation", 0);
			initialValues.put("toxic_substance", "Asbest");
			initialValues.put("degree", "");
			initialValues.put("priority", "keine");
			initialValues.put("position_number", et_sample.getText().toString());
		    initialValues.put("plan_id", planId);
			initialValues.put("position_xo", scr_x0);
			initialValues.put("position_yo", scr_y0);
			initialValues.put("status", "erstellt");

			positionId = dbAdapter.savePosition(initialValues);

			if(positionId != -1)
			{
				Intent intent = new Intent(this, AddPositionActivity.class);
				intent.putExtra("projectId", projectId);
				intent.putExtra("positionId", positionId);
				intent.putExtra("planId", planId);
				startActivityForResult(intent,0);
				Toast.makeText(this, "position saved successfully"+positionId,Toast.LENGTH_LONG).show();

			}else{
				Toast.makeText(this, "Some problem in saving",Toast.LENGTH_LONG).show();
			}


		Toast.makeText(EditPlan1Activity.this, scr_x0+"Image saved to 'txt_imgs' folder"+scr_y0,
				Toast.LENGTH_LONG).show();

		Toast.makeText(EditPlan1Activity.this, scr_x0+" Image "+scr_y0,
				Toast.LENGTH_LONG).show();
	}

	public Bitmap updateImage(float scr_x0, float scr_y0, String user_text) {
		// canvas object with bitmap image as constructor
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleFactorx, scaleFactory, 0, 0);
		Canvas canvas = new Canvas(updatedimage);
		canvas.setMatrix(scaleMatrix);
		int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();
		if (mode == 0) {
			canvas.drawText("" + user_text, scr_x0, scr_y0, paint);
		}

		iv_ttx.setImageBitmap(updatedimage);
		tch = 1;
		return updatedimage;
	}

	// Initiating Menu XML file (menu.xml)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

			// Single menu item is selected do something
			// Ex: launching new activity/screen or show alert message
			mode = 0;
			return true;

	}

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		Intent addProject = new Intent(this, AddProjectActivity.class);
		addProject.putExtra("projectId", projectId);
		addProject.putExtra("planId", planId);
		startActivityForResult(addProject,0);
	}

	@Override
	public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
		TextEditorDialogFragment textEditorDialogFragment =
				TextEditorDialogFragment.show(this, text, colorCode);
		textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
			@Override
			public void onDone(String inputText, int colorCode) {
				mPhotoEditor.editText(rootView, inputText, colorCode);
				//et_sample.setText("hello String");
			}
		});
	}

	@Override
	public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
		//Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
	}

	@Override
	public void onRemoveViewListener(int numberOfAddedViews) {
		//Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [" + numberOfAddedViews + "]");
	}

	@Override
	public void onStartViewChangeListener(ViewType viewType) {
		//Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
	}

	@Override
	public void onStopViewChangeListener(ViewType viewType) {
		//Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
	}

	@Override
	public void onToolSelected(ToolType toolType) {
		switch (toolType) {

			case TEXT:
				TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
				textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
					@Override
					public void onDone(String inputText, int colorCode) {
						mPhotoEditor.addText(inputText, colorCode);
						mTxtCurrentTool.setText("hello");
					}
				});
				break;
			case ERASER:
				mPhotoEditor.brushEraser();
				mTxtCurrentTool.setText("hello");
				break;

		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

			case R.id.imgUndo:
				mPhotoEditor.undo();
				break;

			case R.id.imgRedo:
				mPhotoEditor.redo();
				break;

			case R.id.imgSave:
				saveImage();
				break;

			case R.id.imgClose:
				onBackPressed();
				break;

			case R.id.imgCamera:
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, 52);
				break;

			case R.id.imgGallery:
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 53);
				break;
		}
	}

	@SuppressLint("MissingPermission")
	private void saveImage() {
		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			showLoading("Saving...");
			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + ""
					+ System.currentTimeMillis() + ".png");
			try {
				file.createNewFile();
				mPhotoEditor.saveAsFile(file.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
					@Override
					public void onSuccess(@NonNull String imagePath) {
						hideLoading();
						showSnackbar("Image Saved Successfully");
						mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));
					}

					@Override
					public void onFailure(@NonNull Exception exception) {
						hideLoading();
						showSnackbar("Failed to save Image");
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
				hideLoading();
				showSnackbar(e.getMessage());
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 52:
					mPhotoEditor.clearAllViews();
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					mPhotoEditorView.getSource().setImageBitmap(photo);
					break;
				case 53:
					try {
						mPhotoEditor.clearAllViews();
						Uri uri = data.getData();
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
						mPhotoEditorView.getSource().setImageBitmap(bitmap);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
			}
		}
	}

	public boolean requestPermission(String permission) {
		boolean isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
		if (!isGranted) {
			ActivityCompat.requestPermissions(
					this,
					new String[]{permission},
					READ_WRITE_STORAGE);
		}
		return isGranted;
	}

	public void isPermissionGranted(boolean isGranted, String permission) {

	}

	public void makeFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case READ_WRITE_STORAGE:
				isPermissionGranted(grantResults[0] == PackageManager.PERMISSION_GRANTED, permissions[0]);
				break;
		}
	}

	protected void showLoading(@NonNull String message) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(message);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	protected void hideLoading() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	protected void showSnackbar(@NonNull String message) {
		View view = findViewById(android.R.id.content);
		if (view != null) {
			Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}
}
