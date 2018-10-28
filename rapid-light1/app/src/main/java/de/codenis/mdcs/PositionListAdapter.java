package de.codenis.mdcs;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PositionListAdapter extends BaseAdapter{

	private Context mContext;
	private List<PositionModel> mPositions;
	public PositionListAdapter(Context context, List<PositionModel> Positions) {
		mContext = context;
		mPositions = Positions;
	}
	
	public void setPositions(List<PositionModel> Positions) {
		mPositions = Positions;
	}
	
	@Override
	public int getCount() {
		if (mPositions != null) {
			return mPositions.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mPositions != null) {
			return mPositions.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mPositions != null) {
			return mPositions.get(position).id;
		}
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.position_list_item, parent, false);
			FrameLayout layout = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.position_list_item, parent, false);
			layout.setLongClickable(true);
		}
		
		PositionModel model = (PositionModel) getItem(position);


		TextView Position_number = (TextView) view.findViewById(R.id.position_number1);
		Position_number.setText(model.position_number);
				
		TextView description = (TextView) view.findViewById(R.id.description);
		description.setText(model.description_topic);
		
		if(model.photo1 != null){
			//Bitmap b1 = BitmapFactory.decodeFile(model.photo1);
			Bitmap b1 = getBitmap(model.photo1);
			ImageView photo1 = (ImageView) view.findViewById(R.id.photo1);
			photo1.setImageBitmap(b1);
		}
		if(model.photo2 != null){
			//Bitmap b2 = BitmapFactory.decodeFile(model.photo2);
			Bitmap b2 = getBitmap(model.photo2);
			ImageView photo2 = (ImageView) view.findViewById(R.id.photo2);
			photo2.setImageBitmap(b2);
		}
		/*public void editPosition(View view){
		//((MainActivity) mContext).startDetailActivity(1);
		Toast.makeText(mContext,"Select Time", Toast.LENGTH_SHORT).show();
	}*/
		
		view.setTag(Long.valueOf(model.id));
		Button edit_position = (Button) view.findViewById(R.id.edit_position);	
		edit_position.setTag(Long.valueOf(model.id));
		edit_position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg) {
				((AddProjectActivity) mContext).startPositionDetailActivity(((Long) arg.getTag()).longValue());
			}
		});
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				//((MainActivity) mContext).startDetailActivity(((Long) view.getTag()).longValue());
			}
		});
		
		view.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				//((MainActivity) mContext).deletePosition(((Long) view.getTag()).longValue());
				return true;
			}
		});
		
		
		return view;
	}

	 public Bitmap getBitmap(String path){
		 final BitmapFactory.Options options = new BitmapFactory.Options();
	     options.inSampleSize = 8;
    	 Bitmap myBitmap = BitmapFactory.decodeFile(path, options);
   	/* try {
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

        }*/
    	return myBitmap;
    }
	
	

}
