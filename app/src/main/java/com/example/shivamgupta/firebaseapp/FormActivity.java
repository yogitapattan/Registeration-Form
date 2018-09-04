package com.example.shivamgupta.firebaseapp;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class FormActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView Name,USN,DOB,FatherName,FatherPhone,FatherEmail,GuardianName,GuardianEmail,GuardianPhone;
    private RadioGroup rGroup;
    private RadioButton checkedRadioButton;
    private Spinner Semester,BloodGroup;
    private static String selectedSem;
    private String selectedBG,error_validate;
    private Button Submit;
    Bundle bd;
    // DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        UI();

      /*  AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Toast.makeText(FormActivity.this,"Connected to DB",Toast.LENGTH_SHORT).show();
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();


*/
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedRadioButton =(RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if(isChecked){
                    Toast.makeText(FormActivity.this,"SEX : " + checkedRadioButton.getText(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        ArrayAdapter<CharSequence> SemAdapter = ArrayAdapter.createFromResource(this,
                R.array.semester_array, android.R.layout.simple_spinner_item);
        SemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Semester.setAdapter(SemAdapter);

        Semester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSem = (String) parent.getItemAtPosition(position);
                Toast.makeText(FormActivity.this,"Semester :"+selectedSem,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(FormActivity.this,"Select Semester",Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<CharSequence> BGAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_group_array, android.R.layout.simple_spinner_item);
        BGAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BloodGroup.setAdapter(BGAdapter);

        BloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBG = (String) parent.getItemAtPosition(position);
                Toast.makeText(FormActivity.this,"BloodGroup :"+selectedBG,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(FormActivity.this,"Select Blood Group",Toast.LENGTH_SHORT).show();
            }
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if(validate()) {
                 bd.putString("Name", Name.getText().toString());
                 bd.putString("USN", USN.getText().toString());
                 bd.putString("DOB", DOB.getText().toString());
                 bd.putString("Semester", selectedSem);
                 bd.putString("Blood Group", selectedBG);
                 bd.putString("Father", FatherName.getText().toString());
                 bd.putString("Father's Phone", FatherPhone.getText().toString());
                 bd.putString("Father's Email", FatherEmail.getText().toString());
                 bd.putString("Guardian", GuardianName.getText().toString());
                 bd.putString("Guardian's Phone", GuardianPhone.getText().toString());
                 bd.putString("Guardian's Email", GuardianEmail.getText().toString());
                 Intent it = new Intent(FormActivity.this, FormActivity2.class);
                 it.putExtras(bd);
                 startActivity(it);
             }else {
                 Toast.makeText(FormActivity.this,error_validate,Toast.LENGTH_SHORT).show();
             }

            }
        });
    }

    private boolean validate() {
        //Validate USN using regular expressions
        //Semester and Blood Selected or not
        //Email validation using regex
        //Valid Phone numbers, 10 digits
        //set String error_validate Accordingly
        //TODO
        return true;
    }

    private void UI(){
        Name = (TextView)findViewById(R.id.etName);
        USN = (TextView) findViewById(R.id.etUSN);
        FatherPhone = (TextView) findViewById(R.id.etPhoneFather);
        FatherEmail = (TextView) findViewById(R.id.etEmailFather);
        GuardianName = (TextView) findViewById(R.id.etNameGuardian);
        GuardianPhone = (TextView) findViewById(R.id.etPhoneGuardian);
        GuardianEmail = (TextView) findViewById(R.id.etEmailGuardian);
        FatherName = (TextView)findViewById(R.id.etNameFather);
        DOB = (TextView)findViewById(R.id.etDOB);
        rGroup = (RadioGroup) findViewById(R.id.radioGroup);
        Semester = (Spinner) findViewById(R.id.semester);
        BloodGroup = (Spinner) findViewById(R.id.bloodg);
        checkedRadioButton = (RadioButton) rGroup.findViewById(rGroup.getCheckedRadioButtonId());
        Submit = (Button)findViewById(R.id.btnSubmit);
        bd = new Bundle();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    public static String getSelectedSem(){
        return selectedSem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.logoutMenu:
                Logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(FormActivity.this, MainActivity.class));
    }
}

