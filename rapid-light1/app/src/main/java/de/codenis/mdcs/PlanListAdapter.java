package de.codenis.mdcs;

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

import java.util.List;

public class PlanListAdapter extends BaseAdapter{

	private Context mContext;
	private List<PlanModel> mPlans;
	public PlanListAdapter(Context context, List<PlanModel> Plans) {
		mContext = context;
		mPlans = Plans;
	}
	
	public void setPlans(List<PlanModel> Plans) {
		mPlans = Plans;
	}
	
	@Override
	public int getCount() {
		if (mPlans != null) {
			return mPlans.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mPlans != null) {
			return mPlans.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mPlans != null) {
			return mPlans.get(position).id;
		}
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.plan_list_item, parent, false);
			FrameLayout layout = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.plan_list_item, parent, false);
			layout.setLongClickable(true);
		}
		
		PlanModel model = (PlanModel) getItem(position);
		TextView Plan_number = (TextView) view.findViewById(R.id.number);
		Plan_number.setText(model.number);
				
		TextView plan_name = (TextView) view.findViewById(R.id.plan_name);
		plan_name.setText(model.plan_name);

		if(model.plan_url != null){

			TextView plan_url = (TextView) view.findViewById(R.id.plan_url);
			plan_url.setText(model.plan_url);
			Button plan_view = (Button) view.findViewById(R.id.plan_view);
			plan_view.setVisibility(View.VISIBLE);
			plan_view.setTag(model);
			plan_view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg) {
					try {
						((AddProjectActivity) mContext).openPdf((PlanModel) arg.getTag());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		view.setTag(Long.valueOf(model.id));

		Button edit_plan = (Button) view.findViewById(R.id.edit_plan);
		edit_plan.setTag(Long.valueOf(model.id));
		edit_plan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg) {
				((AddProjectActivity) mContext).startPlanDetailActivity(((Long) arg.getTag()).longValue());
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
				//((MainActivity) mContext).deletePlan(((Long) view.getTag()).longValue());
				return true;
			}
		});
		
		
		return view;
	}
}
