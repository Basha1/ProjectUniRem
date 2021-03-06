package com.unirem.unirem;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;


public class AddEventActivity extends AppCompatActivity  {


    private Button btnCreate, btnCancel;
    private Button btnDate, btnTime;
    private EditText etDate_Time_Picker;
    private EditText etEventTitle, etEventLocation, etEventDetails;
    private ImageButton ibEvent;
    private Uri ibEventUri = null;

    private int day, month, year, hour, minute;
    DateFormat formatDateTime = DateFormat.getDateTimeInstance();
    Calendar dateTime = Calendar.getInstance();


    private TextView tvDropdown;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;
    private ProgressDialog mProgress;
    private static final int Dialog_ID = 1;

    private FirebaseAuth firebaseAuth;
    private StorageReference storage;
    private DatabaseReference mDatabase;

    private static final int Gallery_Request = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mProgress = new ProgressDialog(this);
        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Events");

        tvDropdown = (TextView) findViewById(R.id.tvDropdown);
        ibEvent = (ImageButton) findViewById(R.id.ibEvent);
        etEventTitle = (EditText) findViewById(R.id.etEventTitle);
        etEventDetails = (EditText) findViewById(R.id.etEventDetails);
        etEventLocation = (EditText) findViewById(R.id.etEventLocation);
        etDate_Time_Picker = (EditText) findViewById(R.id.etDate_Time_Picker);


        //spinner that will hold the privacy type
        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.Privacy_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        btnCreate = (Button) findViewById(R.id.btnCreate);

        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnDate = (Button) findViewById(R.id.btnDate);

        btnTime = (Button) findViewById(R.id.btnTime);


        ibEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Request);

            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddEventActivity.this, Nci_events_PageActivity.class));
            }
        });


        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDate();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTime();
            }


        });

    }





    private void updateDate() {

        new DatePickerDialog(this, d, dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateTime() {
        new TimePickerDialog(this, t, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();

    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateTime.set(Calendar.YEAR, year);
            dateTime.set(Calendar.MONTH, monthOfYear);
            dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateTextLabel();
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateTime.set(Calendar.MINUTE, minute);
            updateTextLabel();
        }
    };

    private void updateTextLabel() {
        etDate_Time_Picker.setText(formatDateTime.format(dateTime.getTime()));
    }


    private void startPosting() {

        mProgress.setMessage("Posting to Event... ");
        mProgress.show();
        final String etEventTitle_val = etEventTitle.getText().toString().trim();
        final String etEventLocation_val =etEventLocation.getText().toString().trim();
        final String etEventDetails_val =etEventDetails.getText().toString().trim();
        final String etDate_time_Picker_val = etDate_Time_Picker.getText().toString().trim();
        final String  spinner_val = spinner.getSelectedItem().toString();

        if (!TextUtils.isEmpty(etEventTitle_val) && !TextUtils.isEmpty(etEventLocation_val) && !TextUtils.isEmpty(etEventDetails_val)
                && ibEventUri !=null){
            Uri file = Uri.fromFile(new File("path/to/images/event_images.jpg"));
            StorageReference Event_images = storage.child("images/event_images.jpg");

            Event_images.putFile(ibEventUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            DatabaseReference newPost = mDatabase.push();
                            newPost.child("Event_Title").setValue(etEventTitle_val);
                            newPost.child("Event_Details").setValue(etEventDetails_val);
                            newPost.child("Event_Location").setValue(etEventLocation_val);
                            newPost.child("Event_Date _and _Time").setValue(etDate_time_Picker_val);
                            newPost.child("Privacy type").setValue(spinner_val);
                            newPost.child("images").setValue(ibEventUri);

                         mProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"Posted event to NCI page",Toast.LENGTH_LONG).show();

                            startActivity(new Intent(AddEventActivity.this,Nci_events_PageActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });


        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_Request && resultCode==RESULT_OK){

            ibEventUri = data.getData();
            ibEvent.setImageURI(ibEventUri);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.action_home){

            startActivity(new Intent(AddEventActivity.this,MainActivity.class));

        }

        if (item.getItemId()==R.id.action_logout){
            firebaseAuth.signOut();
            //closing activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


}
