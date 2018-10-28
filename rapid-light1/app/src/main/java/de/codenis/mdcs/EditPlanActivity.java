package de.codenis.mdcs;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class EditPlanActivity extends Activity implements View.OnDragListener,View.OnLongClickListener{
	Bitmap originalBitmap, image, updatedimage, printImage;
	ImageView iv_ttx;
	EditText et_sample;
	DBAdapter dbAdapter;
	private Context mContext;
	long id, projectId, planId=0, positionId=0;
	List<PositionModel> planPositionsList;
    String planUrl;
	private static final String BUTTON_VIEW_TAG = "DRAG BUTTON";

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
		setContentView(R.layout.activity_edit_plan);
		mContext = this;
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();

		iv_ttx = (ImageView) findViewById(R.id.iv_ttx);
		et_sample = (EditText) findViewById(R.id.et_txt);

		planId = getIntent().getExtras().getLong("planId");
		projectId = getIntent().getExtras().getLong("projectId");
		planUrl = getIntent().getExtras().getString("planUrl");
		planPositionsList = dbAdapter.getPlanPositions(projectId, planId);
		Toast.makeText(this, planId+ "projectID in EDIT "+projectId ,Toast.LENGTH_LONG).show();

		int totalPositions = dbAdapter.getTotalPositions(projectId);
		totalPositions = totalPositions +1;
		if(totalPositions>0){
			et_sample.setText(""+totalPositions);
		}

		// to get screen width and hight
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		// dimentions x,y of device to create a scaled bitmap having similar
		// dimentions to screen size
		int height1 = displaymetrics.heightPixels;
		int width1 = displaymetrics.widthPixels;
		// paint object to define paint properties
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLUE);
		paint.setTextSize(25);
		// loading bitmap from drawable

		if(planUrl != null){
			originalBitmap = BitmapFactory.decodeFile(planUrl);
			if(originalBitmap != null){
				float bHeight = originalBitmap.getHeight();
				float bWidth = originalBitmap.getWidth();

				scaleFactorx = bHeight/height1;
				scaleFactory = bWidth/width1;
				Toast.makeText(this, scaleFactory+ " Edit plan "+scaleFactorx ,Toast.LENGTH_LONG).show();
				// scaling of bitmap
				//originalBitmap = Bitmap.createScaledBitmap(originalBitmap, width1, height1, false);
				// creating anoter copy of bitmap to be used for editing
				updatedimage = originalBitmap.copy(Bitmap.Config.ARGB_4444, true);
				iv_ttx.setImageBitmap(updatedimage);

				//to save plan with positions
				printImage = updatedimage.copy(Bitmap.Config.RGB_565, true);

				if(planPositionsList != null){
					for (int i = 0; i < planPositionsList.size(); i++) {
						PositionModel p = planPositionsList.get(i);
						//updateImage(p.position_xo, p.position_yo+50, p.position_number);
						Button myButton = new Button(this);
						myButton.setText(p.position_number);
						//Toast.makeText(this, planPositionsList.size()+ " Edit plan "+p.position_number ,Toast.LENGTH_LONG).show();
						RelativeLayout layout = (RelativeLayout) findViewById(R.id.linearLayout2);
						int buttonWidth, buttonHeight=60;
						if(p.position_number.length() <5)
						{
							buttonWidth = p.position_number.length()* 35;
						}else if(p.position_number.length() >=5 && p.position_number.length() <=10){
							buttonWidth = p.position_number.length()* 20;
						}else{
							if(p.position_number.length() >20){
								buttonHeight = 100;
								buttonWidth = p.position_number.length()* 10;
							}else{
								buttonWidth = p.position_number.length()* 15;
							}

						}
						RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
						pa.setMargins((int)p.position_xo, (int)(p.position_yo*1.02), 0, 0);
						Button buttonView = new Button(this);
						buttonView.setText(p.position_number);
						buttonView.setTag(p.id);
						buttonView.setOnClickListener(mThisButtonListener);
						buttonView.setBackgroundColor(Color.TRANSPARENT);
						buttonView.setTextColor(Color.BLUE);
						ShapeDrawable shapedrawable = new ShapeDrawable();
						shapedrawable.setShape(new RectShape());
						shapedrawable.getPaint().setColor(Color.RED);
						shapedrawable.getPaint().setStrokeWidth(10f);
						shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
						buttonView.setBackground(shapedrawable);
						layout.addView(buttonView, pa);

						//dragAndDrop
						buttonView.setOnLongClickListener(this);
						findViewById(R.id.linearLayout2).setOnDragListener(this);
					}
				}

				image = updatedimage.copy(Bitmap.Config.ARGB_4444, true);

				Button btn_save_img = (Button) findViewById(R.id.btn_save_image);
				Button btn_clr_all = (Button) findViewById(R.id.btn_clr_all);
				btn_clr_all.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// loading original bitmap again (undoing all editing)
						image = originalBitmap.copy(Bitmap.Config.RGB_565, true);
						iv_ttx.setImageBitmap(image);
					}
				});

				btn_save_img.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						// funtion save image is called with bitmap image as parameter
						savePlanPositon();

					}
				});

				iv_ttx.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) {
						// TODO Auto-generated method stub
						String user_text = et_sample.getText().toString();

						// gettin x,y cordinates on screen touch

						switch (arg1.getAction() & MotionEvent.ACTION_MASK) {
							case MotionEvent.ACTION_DOWN:

								if (mode == 0) {
									scr_x0 = arg1.getRawX();
									scr_y0 = arg1.getRawY();
									scr_x0 = scr_x0 - arg0.getLeft();
									scr_y0 = scr_y0 - arg0.getTop();
									tch = 3;
									createImage(scr_x0, scr_y0, scr_x1, scr_y1, user_text);
								}

								else if (mode == 1 || mode == 2) {
									if (tch == 1) {
										scr_x0 = arg1.getRawX();
										scr_y0 = arg1.getRawY();
										scr_x0 = scr_x0 - arg0.getLeft();
										scr_y0 = scr_y0 - arg0.getTop();
										tch = 2;
										return true;
									} else if (tch == 2) {
										scr_x1 = arg1.getRawX();
										scr_y1 = arg1.getRawY();
										scr_x1 = scr_x1 - arg0.getLeft();
										scr_y1 = scr_y1 - arg0.getTop();
										tch = 3;
										createImage(scr_x0, scr_y0, scr_x1, scr_y1,
												user_text);
									}
									// funtion called to perform drawing
								}

						}
						return true;
					}
				});

			}
		}
	}

	private OnClickListener mThisButtonListener = new OnClickListener() {
		public void onClick(View v) {
			positionId = Integer.parseInt(v.getTag().toString().trim());
			Intent intent = new Intent(EditPlanActivity.this, AddPositionActivity.class);
			intent.putExtra("projectId", projectId);
			intent.putExtra("positionId", positionId);
			intent.putExtra("planId", planId);
			intent.putExtra("planUrl", planUrl);
			startActivityForResult(intent,0);
		}
	};

	void saveImage(){
		planPositionsList = dbAdapter.getPlanPositions(projectId, planId);
		if(planPositionsList != null){
			for (int i = 0; i < planPositionsList.size(); i++) {
				PositionModel p = planPositionsList.get(i);
				printImage = updateImage(p.position_xo, p.position_yo+50, p.position_number);
			}
		}

		String RootDir = planUrl.replace(".jpg", "upload.jpg");
		RootDir = RootDir.replace(".jpeg", "upload.jpeg");
		RootDir = RootDir.replace(".png", "upload.png");
		File file = new File(RootDir);
		Toast.makeText(this, planUrl+" y "+RootDir,Toast.LENGTH_LONG).show();
		/*myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);*/
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);

			printImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void savePlanPositon() {
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
				ContentValues planInitialValues = new ContentValues();
				planInitialValues.put("status", "aktualisiert");
				//planId = dbAdapter.updatePlan(planInitialValues, planId);
				Long i = dbAdapter.updatePlan(planInitialValues, planId);
				if(i != -1){
					Intent intent = new Intent(this, AddPositionActivity.class);
					intent.putExtra("projectId", projectId);
					intent.putExtra("positionId", positionId);
					intent.putExtra("planId", planId);
					intent.putExtra("planUrl", planUrl);
					startActivityForResult(intent,0);
					Toast.makeText(this, "position saved successfully"+positionId,Toast.LENGTH_LONG).show();
					saveImage();
				}else{
					Toast.makeText(this, "Some problem in saving Plan Positons",Toast.LENGTH_LONG).show();
				}
			}else{
				Toast.makeText(this, "Some problem in saving Positions",Toast.LENGTH_LONG).show();
			}


		//Toast.makeText(EditPlanActivity.this, scr_x0+"Image saved to 'txt_imgs' folder"+scr_y0,Toast.LENGTH_LONG).show();

		//Toast.makeText(EditPlanActivity.this, scr_x0+" Image "+scr_y0,Toast.LENGTH_LONG).show();
	}

	public Bitmap updateImage(float scr_x0, float scr_y0, String user_text) {

		// canvas object with bitmap image as constructor
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleFactorx, scaleFactory, 0, 0);
		Canvas canvas = new Canvas(printImage);
		canvas.setMatrix(scaleMatrix);

		int Width, Height=60;
		if(user_text.length() <5)
		{
			Width = user_text.length()* 35;
		}else if(user_text.length() >=5 && user_text.length() <=10){
			Width = user_text.length()* 20;
		}else{
			/*if(user_text.length() >20){
				Height = 100;
				Width = user_text.length()* 10;
			}else{*/
				Width = user_text.length()* 15;
			/*}*/

		}

		paint.setColor(Color.RED);
		paint.setStrokeWidth(8f);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect((float) ((scr_x0 * 1.15)+20), scr_y0-150, (float) ((scr_x0 * 1.15)+20+Width),scr_y0-150+Height, paint);

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLUE);
		//paint.setTextSize(25);
		canvas.drawText("" + user_text, (float) ((scr_x0 * 1.15)+45), scr_y0-110, paint);

		//iv_ttx.setImageBitmap(printImage);
		tch = 1;
		return printImage;

		// canvas object with bitmap image as constructor
/*
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
*/
	}

	public Bitmap createImage(float scr_x0, float scr_y0, float scr_x1,float scr_y1, String user_text) {
		image = updatedimage.copy(Bitmap.Config.RGB_565, true);
		iv_ttx.setImageBitmap(image);
		// canvas object with bitmap image as constructor
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleFactorx, scaleFactory, 0, 0);
		Canvas canvas = new Canvas(image);
		canvas.setMatrix(scaleMatrix);
		//int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// viewTop=viewTop+et_sample.getHeight();
		// removing title bar hight
		// scr_y0 = scr_y0 + viewTop;
		// scr_y1 = scr_y1 + viewTop;
		// fuction to draw text on image. you can try more drawing funtions like
		// oval,point,rect,etc...

		paint.setColor(Color.RED);
		paint.setStrokeWidth(10f);
		paint.setStyle(Paint.Style.STROKE);
		if (mode == 0) {
			int radius = 80;
			radius = (user_text.length()*6) + 20;

			canvas.drawCircle(scr_x0+radius+30, scr_y0-120, radius, paint);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.BLUE);
			paint.setTextSize(25);
			canvas.drawText("" + user_text, scr_x0+50, scr_y0-120, paint);
		} else if (mode == 1) {
			canvas.drawLine(scr_x0, scr_y0, scr_x1, scr_y1, paint);
		} else if (mode == 2) {
			canvas.drawRect(scr_x0, scr_y0, scr_x1, scr_y1, paint);
		}

		iv_ttx.setImageBitmap(image);
		tch = 1;
		return image;
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
	public boolean onDrag(View view, DragEvent event) {
		int action = event.getAction();
		// Handles each of the expected events
		switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				// Determines if this View can accept the dragged data
				if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
					return true;
				}
				return false;

			case DragEvent.ACTION_DRAG_ENTERED:
				if(view != null){
					//view.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.DARKEN);
					view.invalidate();
				}
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				//view.getBackground().clearColorFilter();
				view.invalidate();

				return true;
			case DragEvent.ACTION_DROP:
				/*ClipData.Item item = event.getClipData().getItemAt(0);
				String dragData = item.getText().toString();
				Toast.makeText(this, "Dragged data is " + dragData, Toast.LENGTH_SHORT).show();
				Toast.makeText(this, "Loction"+event.getX()+" and "+event.getY(), Toast.LENGTH_SHORT).show();
				//view.getBackground().clearColorFilter();
				//view.invalidate();
				View v = (View) event.getLocalState();
				ViewGroup owner = (ViewGroup) v.getParent();
				owner.removeView(v);//remove the dragged view
				RelativeLayout container = (RelativeLayout) view;//caste the view into LinearLayout as our drag acceptable layout is LinearLayout
				container.addView(v);//Add the dragged view
				v.setVisibility(View.VISIBLE);//finally set Visibility to VISIBLE
				*/
				ClipData.Item item = event.getClipData().getItemAt(0);
				Long currentPositionId = Long.parseLong(item.getText().toString());
				//Toast.makeText(this, "Dragged data is " + currentPositionId, Toast.LENGTH_SHORT).show();

				float X = event.getX();
				float Y = event.getY();

				View v = (View) event.getLocalState();
				v.setX(X-(v.getWidth()/2));
				v.setY(Y-(v.getHeight()/2));
				v.setVisibility(View.VISIBLE);

				this.updatePosition(X-(v.getWidth()/2),Y-(v.getWidth()/2),currentPositionId);
				// Returns true. DragEvent.getResult() will return true.
				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				// Turns off any color tinting
				//view.getBackground().clearColorFilter();

				// Invalidates the view to force a redraw
				view.invalidate();

				// Does a getResult(), and displays what happened.
				if (event.getResult()){
					//Toast.makeText(this, "The drop was handled.", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_SHORT).show();


				// returns true; the value is ignored.
				return true;

			// An unknown action type was received.
			default:
				Log.d("DragDrop Example", "Unknown action type received by OnDragListener.");
				break;
		}
		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		ClipData.Item item = new ClipData.Item( view.getTag()+"");
		String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

		ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);

		// Instantiates the drag shadow builder.
		View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

		// Starts the drag
		view.startDragAndDrop(data//data to be dragged
				, shadowBuilder //drag shadow
				, view//local data about the drag and drop operation
				, 0//no needed flags
		);
		view.setVisibility(View.INVISIBLE);
		return true;
	}

	public void updatePosition(float X, float Y, long currentPositionId){

		if(currentPositionId > 0){
			ContentValues initialValues = new ContentValues();
			initialValues.put("project_id", projectId);
			initialValues.put("plan_id", planId);
			initialValues.put("position_xo", X);
			initialValues.put("position_yo", Y);
			initialValues.put("status", "aktualisiert");

			currentPositionId = dbAdapter.updatePosition(initialValues, currentPositionId);

			if(currentPositionId != -1)
			{
				//Toast.makeText(this, "position saved successfully"+currentPositionId,Toast.LENGTH_LONG).show();

			}else{
				Toast.makeText(this, "Some problem in saving",Toast.LENGTH_LONG).show();
			}
		}
	}
}
