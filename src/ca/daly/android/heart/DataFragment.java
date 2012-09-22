package ca.daly.android.heart;

import com.actionbarsherlock.app.SherlockFragment;
import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class DataFragment extends SherlockFragment 
                          implements DatabaseHelper.RecordListener,
			             Heart.idChangeListener {

  private Long id = 0L;  // current record's id (0 = not set)
  public Calendar date_time = Calendar.getInstance();
  public Integer systolic;
  public Integer diastolic;
  public Integer rate;
  public String notes;
  public Boolean location;
  public Boolean side;

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
			   Bundle savedInstanceState) {
    initialize();
    if (savedInstanceState != null) {
      // restore saved state (data & screen)
      id = savedInstanceState.getLong(DatabaseHelper.ID);
    }

    return (null);   // this is fragment is for data only -- no UI
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);

    Log.d("debug","DataFragment: onSaveInstanceState");
    state.putLong(DatabaseHelper.ID,id);
  }

  @Override
  public void onStop() {
    doSave();
    super.onStop();
  }

  public void setRecord(ContentValues new_rec) {
    id = new_rec.getAsLong(DatabaseHelper.ID);
    Log.d("debug","DataFragment: setRecord: id =" + id);
    updateData(new_rec);
    updateViewer();
  }

  private void updateData(ContentValues new_data) {
    date_time.setTimeInMillis(new_data.getAsLong(DatabaseHelper.DATE));
    systolic = new_data.getAsInteger(DatabaseHelper.SYSTOLIC);
    diastolic = new_data.getAsInteger(DatabaseHelper.DIASTOLIC);
    rate = new_data.getAsInteger(DatabaseHelper.RATE);
    notes = new_data.getAsString(DatabaseHelper.NOTES);
    location = new_data.getAsBoolean(DatabaseHelper.LOCATION);
    side = new_data.getAsBoolean(DatabaseHelper.SIDE);
  }


  public void setId(Long id) {
    this.id = id;
  }

  public void newID(Long id) {
    DatabaseHelper.getInstance(getActivity()).getRecordAsync(id, this);
  }

  public void doSave() {
    ContentValues rec = new ContentValues();
    rec.put(DatabaseHelper.DATE,date_time.getTimeInMillis());
    rec.put(DatabaseHelper.SYSTOLIC,systolic);
    rec.put(DatabaseHelper.DIASTOLIC,diastolic);
    rec.put(DatabaseHelper.RATE,rate);
    rec.put(DatabaseHelper.NOTES,notes);
    rec.put(DatabaseHelper.LOCATION,location);
    rec.put(DatabaseHelper.SIDE,side);

    doSave(rec,true);
  }

  public void doSave(ContentValues rec) {
    doSave(rec,true);
  }

  public void doSave(ContentValues rec,boolean idNotify) {
    if (isDirty(rec)) {
      rec.put(DatabaseHelper.ID,id);
      DatabaseHelper.getInstance(getActivity()).saveRecordAsync((idNotify ? this : null), rec);
      Toast.makeText(getActivity().getApplicationContext(), "Saved Entry", Toast.LENGTH_LONG).show();
      updateData(rec);
                         //TODO -- change above UI text to resource
    }
    if (! idNotify) {
      // don't want notification so must be finished with this record
      initialize();
      updateViewer();
    }
  }

  public void doDelete() {
    if (id != 0) {
      DatabaseHelper.getInstance(getActivity()).deleteRecordAsync(id);
    }
    initialize();
    updateViewer();
  }

  private boolean isDirty(ContentValues rec) {
    boolean dirty;

    Log.d("debug","DataFragment: isDirty curr data: date_time:" + date_time.getTimeInMillis()
                                        + " systolic:" + systolic
					+ " diastolic: " + diastolic
					+ " rate: " + rate
					+ " notes: " + notes
					+ " location: " + location
					+ " side: " + side);
    Log.d("debug","DataFragment: isDirty data to save: date_time:" + rec.getAsLong(DatabaseHelper.DATE)
                                        + " systolic:" + rec.getAsInteger(DatabaseHelper.SYSTOLIC)
					+ " diastolic: " + rec.getAsInteger(DatabaseHelper.DIASTOLIC)
					+ " rate: " + rec.getAsInteger(DatabaseHelper.RATE)
					+ " notes: " + rec.getAsString(DatabaseHelper.NOTES)
					+ " location: " + rec.getAsBoolean(DatabaseHelper.LOCATION)
					+ " side: " + rec.getAsBoolean(DatabaseHelper.SIDE));
    dirty = (date_time.getTimeInMillis() != rec.getAsLong(DatabaseHelper.DATE)
             || !systolic.equals(rec.getAsInteger(DatabaseHelper.SYSTOLIC))
	     || !diastolic.equals(rec.getAsInteger(DatabaseHelper.DIASTOLIC))
	     || !rate.equals(rec.getAsInteger(DatabaseHelper.RATE))
	     || !notes.equals(rec.getAsString(DatabaseHelper.NOTES))
	     || !location.equals(rec.getAsBoolean(DatabaseHelper.LOCATION))
	     || !side.equals(rec.getAsBoolean(DatabaseHelper.SIDE)));
    return dirty;
  }

  private void initialize() {
    id = 0L;
    date_time.setTime(new Date());  // today now
    systolic = 121;
    diastolic = 81;
    rate = 71;
    notes = "";
    location = true;
    side = true;
  }

  private void updateViewer() {
    ((Heart)getActivity()).myViewer.updateView();
  }
}
