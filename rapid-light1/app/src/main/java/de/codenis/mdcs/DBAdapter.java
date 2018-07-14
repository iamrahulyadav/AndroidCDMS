package de.codenis.mdcs;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBAdapter 
{
	private static final String PROJECT_DATABASE_TABLE = "project";
	private static final String POSITION_DATABASE_TABLE = "position";
	private static final String PLAN_DATABASE_TABLE = "plan";
	private static final String USER_DATABASE_TABLE = "user";
	private static final String TOXIC_DATABASE_TABLE = "toxic_substance";
	private static final String DES_DATABASE_TABLE = "preset_preselect_description";
	private static final String MEMBER_DATABASE_TABLE = "team";
	public static final String KEY_ROW_ID = "_id";
	
	
	SQLiteDatabase mDb;
	Context mCtx;
	DBHelper mDbHelper;
	
	public DBAdapter(Context context)
	{
		this.mCtx = context;
	}

	public DBAdapter open() throws SQLException
	{   
		mDbHelper = new DBHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		mDbHelper.close();
	}


	//Project
	public long save(ContentValues initialValues)
	{
		return mDb.insert(PROJECT_DATABASE_TABLE, null, initialValues);
	}
	
	
	public ProjectModel getProject(long id) {
		String select = "SELECT * FROM " + PROJECT_DATABASE_TABLE + " WHERE " + KEY_ROW_ID + " = " + id;
		
		Cursor c = mDb.rawQuery(select, null);
		
		if (c.moveToNext()) {
			return populateModel(c);
		}
		
		return null;
	}
	
	public long getProjectId(int server_project_id) {
		String select = "SELECT * FROM " + PROJECT_DATABASE_TABLE + " WHERE server_project_id = '" + server_project_id +"'";
		
		Cursor c = mDb.rawQuery(select, null);
		
		if (c.moveToNext()) {
			return c.getLong(c.getColumnIndex(KEY_ROW_ID));
		}
		
		return -1;
	}
	
	
	public List<ProjectModel> getProjects() {
		String select = "SELECT * FROM " + PROJECT_DATABASE_TABLE + " ORDER BY date_creation DESC, _id DESC";//LOWER(address), date_creation DESC, _id DESC";
		
		Cursor c = mDb.rawQuery(select, null);
		
		List<ProjectModel> projectList = new ArrayList<ProjectModel>();
		
		while (c.moveToNext()) {
			projectList.add(populateModel(c));
		}
		
		if (!projectList.isEmpty()) {
			return projectList;
		}
		
		return null;
	}

	public List<ProjectModel> getLocalProjects() {
		String select = "SELECT * FROM " + PROJECT_DATABASE_TABLE +" WHERE status = 'erstellt' OR status = 'aktualisiert' LIMIT 3";
		Log.d("my", select);
		Cursor c = mDb.rawQuery(select, null);
		
		List<ProjectModel> projectList = new ArrayList<ProjectModel>();
		
		while (c.moveToNext()) {
			projectList.add(populateModel(c));
		}
		
		if (!projectList.isEmpty()) {
			return projectList;
		}
		
		return null;
	}

	
	private ProjectModel populateModel(Cursor c) {
		ProjectModel model = new ProjectModel();
		model.id = c.getLong(c.getColumnIndex(KEY_ROW_ID));
		model.name_station = c.getString(c.getColumnIndex("name_station"));
		model.number_station = c.getString(c.getColumnIndex("number_station"));
		model.address = c.getString(c.getColumnIndex("address"));
		model.object = c.getString(c.getColumnIndex("object"));
		model.auftraggeber = c.getString(c.getColumnIndex("auftraggeber"));
		model.date_creation = c.getString(c.getColumnIndex("date_creation"));
		model.date_visit = c.getString(c.getColumnIndex("date_visit"));
		model.name_evaluator = c.getString(c.getColumnIndex("name_evaluator"));
		model.photo = c.getString(c.getColumnIndex("photo"));
		//model.number_of_plan = c.getString(c.getColumnIndex("num_plan"));
		model.image = c.getString(c.getColumnIndex("image"));
		model.status = c.getString(c.getColumnIndex("status"));
		model.server_project_id = c.getString(c.getColumnIndex("server_project_id"));
		return model;
	}
	
	public int deleteServerProject(int id) {
		return mDb.delete(PROJECT_DATABASE_TABLE, "server_project_id = ?", new String[] { String.valueOf(id) });
	}
	
	public int deleteProject(long id) {
		int result = mDb.delete(PROJECT_DATABASE_TABLE, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
		if(result != -1){
			mDb.delete(PLAN_DATABASE_TABLE, "project_id = ?", new String[] { String.valueOf(id) });
			mDb.delete(POSITION_DATABASE_TABLE, "project_id = ?", new String[] { String.valueOf(id) });
		}
		return result;
	}
	
	
	public long update(ContentValues initialValues, Long id)
	{
		return mDb.update(PROJECT_DATABASE_TABLE, initialValues, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
	}
	//End Project




	//Position
	public long savePosition(ContentValues initialValues)
	{
		return mDb.insert(POSITION_DATABASE_TABLE, null, initialValues);
	}
	
	
	public long getPositonId(int server_position_id) {
		String select = "SELECT * FROM " + POSITION_DATABASE_TABLE + " WHERE server_position_id = '" + server_position_id +"'";
		
		Cursor c = mDb.rawQuery(select, null);
		
		if (c.moveToNext()) {
			return c.getLong(c.getColumnIndex(KEY_ROW_ID));
		}
		
		return -1;
	}
	
	public List<PositionModel> getPositions(long id) {
		
		String select = "SELECT * FROM " + POSITION_DATABASE_TABLE + " WHERE project_id = '" + id + "' ORDER BY cast(position_number as unsigned) ASC";
		Log.d("my", "selecting Positions "+ select);
		Cursor c = mDb.rawQuery(select, null);
		
		List<PositionModel> positionList = new ArrayList<PositionModel>();
		
		while (c.moveToNext()) {
			positionList.add(populatePositionModel(c));
		}
		
		if (!positionList.isEmpty()) {
			return positionList;
		}
		
		return null;
	}

	public int getTotalPositions(long id) {

		String select = "SELECT COUNT(*) as total FROM " + POSITION_DATABASE_TABLE + " WHERE project_id = '" + id + "'";
		Log.d("my", "selecting Positions "+ select);
		Cursor c = mDb.rawQuery(select, null);

		if (c.moveToNext()) {
			return c.getInt(c.getColumnIndex("total"));
		}else{
			return 0;
		}
	}
	
	public List<PositionModel> getLocalPositions(long id) {
		
		String select = "SELECT * FROM " + POSITION_DATABASE_TABLE + " WHERE project_id = '" + id + "' AND (status = 'erstellt' OR status = 'aktualisiert')";
		Log.d("my", "selecting Positions "+ select);
		Cursor c = mDb.rawQuery(select, null);
		
		List<PositionModel> positionList = new ArrayList<PositionModel>();
		
		while (c.moveToNext()) {
			positionList.add(populatePositionModel(c));
		}
		
		if (!positionList.isEmpty()) {
			return positionList;
		}
		
		return null;
	}
	
	public PositionModel getPosition(long id) {
		String select = "SELECT * FROM " + POSITION_DATABASE_TABLE + " WHERE " + KEY_ROW_ID + " = " + id;
		
		Cursor c = mDb.rawQuery(select, null);
		
		if (c.moveToNext()) {
			return populatePositionModel(c);
		}
		
		return null;
	}

	public int deleteServerPosition(int id) {
		return mDb.delete(POSITION_DATABASE_TABLE, "server_position_id = ?", new String[] { String.valueOf(id) });
	}
	
	public int deletePosition(long id) {
		return mDb.delete(POSITION_DATABASE_TABLE, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
	}
	
	public long updatePosition(ContentValues initialValues, Long id)
	{
		return mDb.update(POSITION_DATABASE_TABLE, initialValues, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
	}
	
	private PositionModel populatePositionModel(Cursor c) {
		
		PositionModel model = new PositionModel();
		Log.d("my", "Positions number "+ c.getString(c.getColumnIndex("position_number")));
		model.id = c.getLong(c.getColumnIndex(KEY_ROW_ID));
		model.position_number = c.getString(c.getColumnIndex("position_number"));
		model.description_topic = c.getString(c.getColumnIndex("description_topic"));
		model.description = c.getString(c.getColumnIndex("description"));
		model.degree = c.getString(c.getColumnIndex("degree"));
		model.project_id = c.getInt(c.getColumnIndex("project_id"));
		model.toxic_substance = c.getString(c.getColumnIndex("toxic_substance"));
		model.comment = c.getString(c.getColumnIndex("comment"));
		model.investigation = c.getInt(c.getColumnIndex("investigation"));
		model.priority = c.getInt(c.getColumnIndex("priority"));
		model.photo1 = c.getString(c.getColumnIndex("photo1"));
		model.photo2 = c.getString(c.getColumnIndex("photo2"));
		model.status = c.getString(c.getColumnIndex("status"));
		model.server_position_id = c.getString(c.getColumnIndex("server_position_id"));
		return model;
	}
	//end Position




	// plans
	public long savePlan(ContentValues initialValues)
	{
		return mDb.insert(PLAN_DATABASE_TABLE, null, initialValues);
	}


	public long getPlanId(int server_plan_id) {
		String select = "SELECT * FROM " + PLAN_DATABASE_TABLE + " WHERE server_plan_id = '" + server_plan_id +"'";

		Cursor c = mDb.rawQuery(select, null);

		if (c.moveToNext()) {
			return c.getLong(c.getColumnIndex(KEY_ROW_ID));
		}

		return -1;
	}

	public List<PlanModel> getPlans(long id) {

		String select = "SELECT * FROM " + PLAN_DATABASE_TABLE + " WHERE project_id = '" + id + "' ORDER BY number ASC";
		Log.d("my", "selecting Plans "+ select);
		Cursor c = mDb.rawQuery(select, null);

		List<PlanModel> planList = new ArrayList<PlanModel>();

		while (c.moveToNext()) {
			planList.add(populatePlanModel(c));
		}

		if (!planList.isEmpty()) {
			return planList;
		}

		return null;
	}

	public int getTotalPlans(long id) {

		String select = "SELECT COUNT(*) as total FROM " + PLAN_DATABASE_TABLE + " WHERE project_id = '" + id + "'";
		Log.d("my", "selecting Plan "+ select);
		Cursor c = mDb.rawQuery(select, null);

		if (c.moveToNext()) {
			return c.getInt(c.getColumnIndex("total"));
		}else{
			return 0;
		}
	}

	public List<PlanModel> getLocalPlans(long id) {

		String select = "SELECT * FROM " + PLAN_DATABASE_TABLE + " WHERE project_id = '" + id + "' AND (status = 'erstellt' OR status = 'aktualisiert')";
		Log.d("my", "selecting Plans "+ select);
		Cursor c = mDb.rawQuery(select, null);

		List<PlanModel> planList = new ArrayList<PlanModel>();

		while (c.moveToNext()) {
			planList.add(populatePlanModel(c));
		}

		if (!planList.isEmpty()) {
			return planList;
		}

		return null;
	}

	public PlanModel getPlan(long id) {
		String select = "SELECT * FROM " + PLAN_DATABASE_TABLE + " WHERE " + KEY_ROW_ID + " = " + id;

		Cursor c = mDb.rawQuery(select, null);

		if (c.moveToNext()) {
			return populatePlanModel(c);
		}

		return null;
	}

	public int deleteServerPlan(int id) {
		return mDb.delete(PLAN_DATABASE_TABLE, "server_plan_id = ?", new String[] { String.valueOf(id) });
	}

	public int deletePlan(long id) {
		return mDb.delete(PLAN_DATABASE_TABLE, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
	}

	public long updatePlan(ContentValues initialValues, Long id)
	{
		return mDb.update(PLAN_DATABASE_TABLE, initialValues, KEY_ROW_ID + " = ?", new String[] { String.valueOf(id) });
	}

	private PlanModel populatePlanModel(Cursor c) {

		PlanModel model = new PlanModel();
		model.id = c.getLong(c.getColumnIndex(KEY_ROW_ID));
		model.server_plan_id = c.getString(c.getColumnIndex("server_plan_id"));
		model.project_id = c.getInt(c.getColumnIndex("project_id"));
		model.plan_url = c.getString(c.getColumnIndex("plan_url"));
		model.plan_name = c.getString(c.getColumnIndex("plan_name"));
		model.number = c.getString(c.getColumnIndex("number"));
		model.status = c.getString(c.getColumnIndex("status"));
		return model;
	}
	//end plans




	//user
	public long addUser(ContentValues initialValues)
	{
		return mDb.insert(USER_DATABASE_TABLE, null, initialValues);
	}
	
	public int checkMember(String username, String password)
	{
		String select = "SELECT * FROM " + MEMBER_DATABASE_TABLE + " WHERE loginname ='"+ username + "'  AND password ='"+ password + "'";
		Cursor c = mDb.rawQuery(select, null);
		if (c.moveToNext()) {
			return c.getInt(c.getColumnIndex(KEY_ROW_ID));
		}else{
			return 0;
		}
	}


	//substance
	public long addSubstance(ContentValues initialValues)
	{
		return mDb.insert(TOXIC_DATABASE_TABLE, null, initialValues);
	}
	
	public ArrayList<String> get(String selectField, String Table)
	{
		String select = "SELECT * FROM " + Table+ " ORDER BY "+selectField+" COLLATE NOCASE ASC";
		Log.d("my", select);
		Cursor c = mDb.rawQuery(select, null);
		
		ArrayList<String>  substanceList = new ArrayList<String>();
		
		while (c.moveToNext()) {
			substanceList.add(c.getString(c.getColumnIndex(selectField)));
		}
		
		if (!substanceList.isEmpty()) {
			return substanceList;
		}
		
		return null;
	}

	public String getPriorityDescription(String selectField, String Table)
	{
		String select = "SELECT * FROM " + Table + " WHERE  preselect_description = '"+selectField+"'";
		Log.d("my", select);
		Cursor c = mDb.rawQuery(select, null);
		if (c.moveToNext()) {
			return c.getString(c.getColumnIndex("priority"));
		}else{
			return "0";
		}
	}
	
	public long addDescriptionTopic(ContentValues initialValues)
	{
		return mDb.insert(DES_DATABASE_TABLE, null, initialValues);
	}
	
	public long addMember(ContentValues initialValues)
	{
		return mDb.insert(MEMBER_DATABASE_TABLE, null, initialValues);
	}
	public int deleteToxic() {
		return mDb.delete(TOXIC_DATABASE_TABLE, null,  new String[] {});
	}
	public int deleteDescriptionTopic() {
		return mDb.delete(DES_DATABASE_TABLE, null,  new String[] {});
	}
	public int deleteMember() {
		return mDb.delete(MEMBER_DATABASE_TABLE,  null,  new String[] {});
	}
	
	public Boolean checkExist(String tableName, String colomnName, String colomnValue)
	{
		String select = "SELECT * FROM " + tableName + " WHERE " + colomnName + " = '"+ colomnValue + "'";
		Cursor c = mDb.rawQuery(select, null);
		if (c.moveToNext()) {
			if(c.getInt(c.getColumnIndex(KEY_ROW_ID)) > 0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
}