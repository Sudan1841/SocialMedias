package www.pickens.whatscool.socialmedias;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
    //    }


    /**********************************FIREBASE HELPER START************************/
    public class FirebaseHelper {

    DatabaseReference db;
    Boolean saved;
    ArrayList<Teacher> teachers = new ArrayList<>();
    ListView mListView;
    Context c;

    /*
    let's receive a reference to our FirebaseDatabase
    */
public FirebaseHelper(DatabaseReference db, Context context, ListView mListView) {
        this.db = db;
        this.c = context;
        this.mListView = mListView;
        this.retrieve();
        }

        /*
        let's now write how to save a single Teacher to FirebaseDatabase
        */
public Boolean save(Teacher teacher) {
        //check if they have passed us a valid teacher. If so then return false.
        if (teacher == null) {
        saved = false;
        } else {
        //otherwise try to push data to firebase database.
        try {
        //push data to FirebaseDatabase. Table or Child called Teacher will be created.
        db.child("Teacher").push().setValue(teacher);
        saved = true;

        } catch (DatabaseException e) {
        e.printStackTrace();
        saved = false;
        }
        }
        //tell them of status of save.
        return saved;
}

/*
Retrieve and Return them clean data in an arraylist so that they just bind it to ListView.
*/
        public ArrayList<Teacher> retrieve() {
            db.child("Teacher").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    teachers.clear();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //Now get Teacher Objects and populate our arraylist.
                            Teacher teacher = ds.getValue(Teacher.class);
                            teachers.add(teacher);
                        }
                        adapter = new CustomAdapter(c, teachers);
                        mListView.setAdapter(adapter);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mListView.smoothScrollToPosition(teachers.size());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("mTAG", databaseError.getMessage());
                    Toast.makeText(c, "ERROR " + databaseError.getMessage(), Toast.LENGTH_LONG).show();

                }
            });

            return teachers;
        }
    }



        /**********************************CUSTOM ADAPTER START************************/
        class CustomAdapter extends BaseAdapter {
            Context c;
            ArrayList<Teacher> teachers;

            public CustomAdapter(Context c, ArrayList<Teacher> teachers) {
                this.c = c;
                this.teachers = teachers;
            }

            @Override
            public int getCount() {
                return teachers.size();
            }

            @Override
            public Object getItem(int position) {
                return teachers.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(c).inflate(R.layout.model, parent, false);
                }

                TextView nameTextView = convertView.findViewById(R.id.nameTextView);
                TextView quoteTextView = convertView.findViewById(R.id.quoteTextView);
                TextView descriptionTextView = convertView.findViewById(R.id.descriptionTextView);

                final Teacher s = (Teacher) this.getItem(position);

                nameTextView.setText(s.getName());
                quoteTextView.setText(s.getPropellant());
                descriptionTextView.setText(s.getDescription());

                //ONITECLICK
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(c, s.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
                return convertView;
            }
        }


        /**********************************MAIN ACTIVITY CONTINUATION************************/
        //instance fields
        DatabaseReference db;
        FirebaseHelper helper;
        CustomAdapter adapter;
        ListView mListView;
        EditText nameEditTxt, quoteEditText, descriptionEditText;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mListView = (ListView) findViewById(R.id.myListView);
            //initialize firebase database
            db = FirebaseDatabase.getInstance().getReference();
            helper = new FirebaseHelper(db, this, mListView);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListView.smoothScrollToPosition(4);
                    displayInputDialog();
                }
            });
        }

        //DISPLAY INPUT DIALOG
        private void displayInputDialog() {
            //create input dialog
           Dialog d = new Dialog(this);
            d.setTitle("Save To Firebase");
            d.setContentView(R.layout.input_dailog);

            //find widgets
            nameEditTxt = d.findViewById(R.id.nameEditText);
            quoteEditText = d.findViewById(R.id.quoteEditText);
            descriptionEditText = d.findViewById(R.id.descEditText);
           Button saveBtn = d.findViewById(R.id.saveBtn);

            //save button clicked
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //get data from edittexts
                    String name = nameEditTxt.getText().toString();
                    String quote = quoteEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();

                    //set data to POJO
                    Teacher s = new Teacher();
                    s.setName(name);
                    s.setPropellant(quote);
                    s.setDescription(description);

                    //perform simple validation
                    if (name != null && name.length() > 0) {
                        //save data to firebase
                        if (helper.save(s)) {
                            //clear edittexts
                            nameEditTxt.setText("");
                            quoteEditText.setText("");
                            descriptionEditText.setText("");

                            //refresh listview
                            ArrayList<Teacher> fetchedData = helper.retrieve();
                            adapter = new CustomAdapter(MainActivity.this, fetchedData);
                            mListView.setAdapter(adapter);
                            mListView.smoothScrollToPosition(fetchedData.size());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Name Must Not Be Empty Please", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            d.show();
        }

    }


