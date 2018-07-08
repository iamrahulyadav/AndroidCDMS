package de.codenis.mdcs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ProjectListAdapter extends BaseAdapter implements Filterable{

	private Context mContext;
	private List<ProjectModel> mProjects;
	private List<ProjectModel> sProjects;
	public ProjectListAdapter(Context context, List<ProjectModel> Projects) {
		mContext = context;
		mProjects = Projects;
		sProjects= Projects;
	}
	
	public void setProjects(List<ProjectModel> Projects) {
		mProjects = Projects;
		sProjects= Projects;
	}
	
	@Override
	public int getCount() {
		if (mProjects != null) {
			return mProjects.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mProjects != null) {
			return mProjects.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mProjects != null) {
			return mProjects.get(position).id;
		}
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.project_list_item, parent, false);
			FrameLayout layout = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.project_list_item, parent, false);
			layout.setLongClickable(true);
		}
		
		ProjectModel model = (ProjectModel) getItem(position);
		
		TextView create_date = (TextView) view.findViewById(R.id.create_date);
		create_date.setText(model.date_visit);
		
		/*TextView project_number = (TextView) view.findViewById(R.id.project_number);
		project_number.setText(" "+model.id);*/
		
		TextView station_name = (TextView) view.findViewById(R.id.station_name);
		String substr=model.name_station;
		if(substr.length() > 11){
			substr = substr.substring(0,12)+"..";
		}
		station_name.setText(substr);

		TextView station_number = (TextView) view.findViewById(R.id.station_number);
		String subnumber=model.number_station;
		if(subnumber.length() > 11){
			subnumber = subnumber.substring(0,12)+"..";
		}
		station_number.setText(subnumber);
		
		TextView address = (TextView) view.findViewById(R.id.address);
		String subaddress=model.address;
		if(subaddress.length() > 11){
			subaddress = subaddress.substring(0,12)+"..";
		}
		address.setText(subaddress);
		
		TextView status = (TextView) view.findViewById(R.id.status);
		status.setText(model.status);
		/*public void editProject(View view){
		//((MainActivity) mContext).startDetailActivity(1);
		Toast.makeText(mContext,"Select Time", Toast.LENGTH_SHORT).show();
	}*/
		
		view.setTag(Long.valueOf(model.id));
		Button edit_button = (Button) view.findViewById(R.id.edit_button);	
		edit_button.setTag(Long.valueOf(model.id));
		edit_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg) {
				((MainActivity) mContext).startDetailActivity(((Long) arg.getTag()).longValue());
			}
		});
		/*view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				((MainActivity) mContext).startDetailActivity(((Long) view.getTag()).longValue());
			}
		});
		
		view.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				//((MainActivity) mContext).deleteProject(((Long) view.getTag()).longValue());
				return true;
			}
		});*/
		
		return view;
	}

	
	
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            	mProjects = (List<ProjectModel>) results.values;
            	notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
            	
                FilterResults results = new FilterResults();
                ArrayList<ProjectModel> FilteredArrayNames = new ArrayList<ProjectModel>();
                constraint = constraint.toString().toLowerCase();
                if(constraint == null || constraint.length() == 0){
                	results.count = sProjects.size();
                	results.values = sProjects;
                }
                else{
                	
                	for (int i = 0; i < sProjects.size(); i++) {
                        String idNumber = String.valueOf(sProjects.get(i).id);
                		String station_name = sProjects.get(i).name_station;
                        String station_number = sProjects.get(i).number_station;
                        String address = sProjects.get(i).address;
                        String visit_date = sProjects.get(i).date_visit;
                        String status = sProjects.get(i).status;
                        
                        if (idNumber.toLowerCase().startsWith(constraint.toString()) ||
                        		station_name.toLowerCase().startsWith(constraint.toString()) ||
                        		station_number.toLowerCase().startsWith(constraint.toString())||
                        		address.toLowerCase().startsWith(constraint.toString())||
                        		visit_date.toLowerCase().startsWith(constraint.toString())||
                        		status.toLowerCase().startsWith(constraint.toString())){
                        	
                        	FilteredArrayNames.add(sProjects.get(i));
                        }
                    }
                	results.count = FilteredArrayNames.size();
                	results.values = FilteredArrayNames;
                }
            	return results;
            }
        };

        return filter;
	}

}
