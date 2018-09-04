package com.example.shivamgupta.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminActivity extends AppCompatActivity {

    private EditText mQuery;
    private TextView Name,Phone,Father,Dob,A1,A2,Lat,Lon;
    private ImageView mImageView;
    private Button mSubmit,mPDF;
    private FirebaseFirestore db;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDRef = database.getReference();
    HashMap<String, Object> student = new HashMap<>();
    private ProgressDialog mProgress;
    private StorageReference mRef;
    private Image myImg;
    final long ONE_MEGABYTE = 1024*1024;
    private boolean DOCUMENT_OK = false, HAVE_IMG = false;
    private String email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        UI();
        db = FirebaseFirestore.getInstance();
        mProgress = new ProgressDialog(AdminActivity.this);
        mRef = FirebaseStorage.getInstance().getReference();


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invalid()){
                    Toast.makeText(AdminActivity.this,"Invalid ", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgress.setMessage("Retrieving Document");
                    mProgress.show();
                    email = mQuery.getText().toString().trim();
                    mDRef = mDRef.child("Students").child(email);
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            if(dataSnapshot.getValue() == null){
                                Toast.makeText(AdminActivity.this,"Document doesnot exist", Toast.LENGTH_SHORT).show();
                                DOCUMENT_OK = false;
                                mProgress.dismiss();
                            }else {
                                student = (HashMap<String, Object>) dataSnapshot.getValue();
                                mProgress.dismiss();
                                DOCUMENT_OK = true;
                                displayData();
                            }
                            // ...
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            Toast.makeText(AdminActivity.this,"Server Failure", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                            // ...
                        }
                    };


                    mRef = mRef.child("Photos").child(email);
                    mRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , stream);
                            try {
                                myImg = Image.getInstance(stream.toByteArray());
                                myImg.setAlignment(Image.LEFT);
                                HAVE_IMG =  true;
                            }catch(Exception e){
                                Toast.makeText(AdminActivity.this,"Null Image", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    Glide.with(AdminActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(mRef)
                            .into(mImageView);
                    mDRef.addListenerForSingleValueEvent(postListener);

                }
            }
        });

        mPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DOCUMENT_OK && HAVE_IMG){

                    if(generatePDF(email,myImg,student)) {
                        Toast.makeText(AdminActivity.this,"PDF created", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(AdminActivity.this,"PDF creation failed", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(AdminActivity.this,"Invalid Document !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean generatePDF(String fname, Image myImg, Map<String, Object> student) {
        try {
            String fpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/vindroid";
            File dir = new File(fpath);
            if(!dir.exists())
                dir.mkdirs();

            File file = new File(dir,fname+".pdf");
            if (!file.exists()) {
                file.createNewFile();
            }
            Toast.makeText(AdminActivity.this,"File created ", Toast.LENGTH_SHORT).show();
            Font bfBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, new BaseColor(0, 0, 0));
            Font bf12 = new Font(Font.FontFamily.TIMES_ROMAN, 12);


            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(file.getAbsoluteFile()));
            document.open();
            addmetadata(document);

            document.add(new Paragraph("Name : " + student.get("Name")));
            document.add(new Paragraph("USN : " + student.get("USN")));
            document.add(new Paragraph("Date of Birth : "+ student.get("DOB")));
            document.add(new Paragraph(" Semester : + "+student.get("Semester")));
            document.add(new Paragraph("Fathers Name : "+ student.get("Father's Name")));
            document.add(new Paragraph("Father's Phone : "+ student.get("Father's Phone")));
            List list = new List();
            list.setListSymbol(new Chunk("Address : "));
            list.add((String)student.get("Address Line1")+student.get("Address Line2"));
            document.add(list);
            document.add(Chunk.NEWLINE);
            //TODO Add other Details
            myImg.scaleToFit(640,480);
            myImg.setAbsolutePosition(0,0);
            document.add(myImg);

            document.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private void addmetadata(Document document) {
        document.addTitle(student.get("USN")+"pdf");
        document.addSubject("Registration details");
        document.addAuthor("Students");
        document.addCreator("Students");
    }

    private void displayData() {
        Name.setText(student.get("Name").toString());
        Phone.setText(student.get("Phone").toString());
        Father.setText(student.get("Father's Name").toString());
        Dob.setText(student.get("DOB").toString());
        A1.setText(student.get("Address Line 1").toString());
        A2.setText(student.get("Address Line 2").toString());
        Lat.setText(student.get("Latitude").toString());
        Lon.setText(student.get("Longitude").toString());
        //TODO Add Other details
        //Create Admin Layout
    }

    private boolean invalid() {
        return mQuery.getText().toString().trim().isEmpty();
    }

    private void UI() {
        mQuery = (EditText) findViewById(R.id.etQuery);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mSubmit = (Button) findViewById(R.id.btnQuery);
        mPDF = (Button) findViewById(R.id.btPDF);
        Name = (TextView) findViewById(R.id.tvName);
        Phone = (TextView) findViewById(R.id.tvPhone);
        Father = (TextView) findViewById(R.id.tvFather);
        Dob = (TextView) findViewById(R.id.tvDob);
        A1 = (TextView) findViewById(R.id.tvA1);
        A2 = (TextView) findViewById(R.id.tvA2);
        Lat = (TextView) findViewById(R.id.tvLatitude);
        Lon = (TextView) findViewById(R.id.tvLongitude);
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
        finish();
        startActivity(new Intent(AdminActivity.this, MainActivity.class));
    }
}

