package com.virid.ViridDirectory;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EmployeeDetails extends ListActivity {

	private static final int MENU_NEW_CONTACT = 0;
	private static final int MENU_ATTACH_CONTACT = 1;
	private static final int MENU_EDIT_CONTACT = 2;
	private static final int MENU_REMOVE_CONTACT = 3;
	private static final int MENU_HELP = 4;
	
	protected TextView employeeNameText;
	protected TextView titleText;
	protected TextView deptText;
	protected List<EmployeeAction> actions;
	protected EmployeeActionAdapter adapter;
	protected int employeeId;
	protected int managerId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.employee_details);

		employeeId = getIntent().getIntExtra("EMPLOYEE_ID", 0);
		SQLiteDatabase db = (new DatabaseHelper(this)).getWritableDatabase();
		Cursor cursor = db
				.rawQuery(
						"SELECT \"(\" || emp.department || \")\" as department, emp.isManager, emp.aim, emp.msn, emp._id, emp.firstName, emp.lastName, emp.title, emp.officePhone, emp.officePhoneExt, emp.cellPhone, emp.email, emp.managerId, mgr.firstName managerFirstName, mgr.lastName managerLastName FROM viridEmployee emp LEFT OUTER JOIN viridEmployee mgr ON emp.managerId = mgr._id WHERE emp._id = ?",
						new String[] { "" + employeeId });

		if (cursor.getCount() == 1) {
			cursor.moveToFirst();

			employeeNameText = (TextView) findViewById(R.id.employeeName);
			employeeNameText.setText(cursor.getString(cursor
					.getColumnIndex("firstName"))
					+ " "
					+ cursor.getString(cursor.getColumnIndex("lastName")));

			String mgrText = "";
			String isManager = cursor.getString(cursor
					.getColumnIndex("isManager"));
			if (isManager == "1") {
				mgrText = " - [manager]" + isManager;			
			}
			
			titleText = (TextView) findViewById(R.id.title);
			titleText.setText(cursor.getString(cursor.getColumnIndex("title")));

			deptText = (TextView) findViewById(R.id.department);
			deptText.setText(cursor.getString(cursor.getColumnIndex("department")) + mgrText);

			actions = new ArrayList<EmployeeAction>();

			// OFFICE PHONE NUMBER
			String officePhone = cursor.getString(cursor
					.getColumnIndex("officePhone"));
			if (officePhone != null) {
				actions.add(new EmployeeAction("Call office", officePhone,
						EmployeeAction.ACTION_CALL));
			}

			// OFFICE PHONE NUMBER EXT
			String officePhoneExt = cursor.getString(cursor
					.getColumnIndex("officePhoneExt"));
			if (officePhoneExt != null && officePhoneExt.length() > 0) {
				actions.add(new EmployeeAction("Office Phone Extention",
						officePhoneExt, EmployeeAction.ACTION_NULL));
			}

			// CELL PHONE NUMBER
			String cellPhone = cursor.getString(cursor
					.getColumnIndex("cellPhone"));
			if (cellPhone != null && cellPhone.length() > 1) {
				actions.add(new EmployeeAction("Call mobile", cellPhone,
						EmployeeAction.ACTION_CALL));
				actions.add(new EmployeeAction("SMS", cellPhone,
						EmployeeAction.ACTION_SMS));
			}

			// EMAIL
			String email = cursor.getString(cursor.getColumnIndex("email"));
			if (email != null && email.length() > 2) {
				actions.add(new EmployeeAction("Email", email,
						EmployeeAction.ACTION_EMAIL));
			}

			// AOL
			String aim = cursor.getString(cursor.getColumnIndex("aim"));
			if (aim != null && aim.length() > 2) {
				actions.add(new EmployeeAction("AIM Handle", aim,
						EmployeeAction.ACTION_NULL));
			}

			// MSN
			String msn = cursor.getString(cursor.getColumnIndex("msn"));
			if (msn != null && msn.length() > 2) {
				actions.add(new EmployeeAction("msn", email,
						EmployeeAction.ACTION_EMAIL));
			}

			managerId = cursor.getInt(cursor.getColumnIndex("managerId"));
			if (managerId > 0) {
				actions.add(new EmployeeAction("View manager", cursor
						.getString(cursor.getColumnIndex("managerFirstName"))
						+ " "
						+ cursor.getString(cursor
								.getColumnIndex("managerLastName")),
						EmployeeAction.ACTION_VIEW));
			}

			cursor = db.rawQuery(
					"SELECT count(*) FROM viridEmployee WHERE managerId = ?",
					new String[] { "" + employeeId });
			cursor.moveToFirst();
			int count = cursor.getInt(0);
			if (count > 0) {
				actions.add(new EmployeeAction("View direct reports", "("
						+ count + ")", EmployeeAction.ACTION_REPORTS));
			}

			adapter = new EmployeeActionAdapter();
			setListAdapter(adapter);
		}

	}

	public void onListItemClick(ListView parent, View view, int position,
			long id) {

		EmployeeAction action = actions.get(position);

		Intent intent;
		switch (action.getType()) {

		case EmployeeAction.ACTION_CALL:
			Uri callUri = Uri.parse("tel:" + action.getData());
			intent = new Intent(Intent.ACTION_CALL, callUri);
			startActivity(intent);
			break;

		case EmployeeAction.ACTION_EMAIL:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL,
					new String[] { action.getData() });
			startActivity(intent);
			break;

		case EmployeeAction.ACTION_SMS:
			Uri smsUri = Uri.parse("sms:" + action.getData());
			intent = new Intent(Intent.ACTION_VIEW, smsUri);
			startActivity(intent);
			break;

		case EmployeeAction.ACTION_REPORTS:
			intent = new Intent(this, DirectReports.class);
			intent.putExtra("EMPLOYEE_ID", employeeId);
			startActivity(intent);
			break;

		case EmployeeAction.ACTION_VIEW:
			intent = new Intent(this, EmployeeDetails.class);
			intent.putExtra("EMPLOYEE_ID", managerId);
			startActivity(intent);
			break;
		}
	}

	class EmployeeActionAdapter extends ArrayAdapter<EmployeeAction> {

		EmployeeActionAdapter() {
			super(EmployeeDetails.this, R.layout.action_list_item, actions);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EmployeeAction action = actions.get(position);
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.action_list_item, parent,
					false);
			TextView label = (TextView) view.findViewById(R.id.label);
			label.setText(action.getLabel());
			TextView data = (TextView) view.findViewById(R.id.data);
			data.setText(action.getData());
			return view;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NEW_CONTACT, 0, "New Contact").setIcon(R.drawable.add);
		menu.add(0, MENU_ATTACH_CONTACT, 1, "Attach Contact").setIcon(R.drawable.add);
		menu.add(0, MENU_EDIT_CONTACT, 2, "Edit Contact").setIcon(R.drawable.edit);
		menu.add(0, MENU_REMOVE_CONTACT, 3, "Remove Contact").setIcon(R.drawable.delete);
		menu.add(0, MENU_HELP, 4, "Help").setIcon(R.drawable.help);
	    return true;
	}
	
}