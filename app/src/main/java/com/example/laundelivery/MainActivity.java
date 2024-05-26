package com.example.laundelivery;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    ImageButton imgbtnInventory, imgbtnLaundry, imgbtnLogout, imgbtnLocation;
    TextView textViewEmail;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        textViewEmail = findViewById(R.id.textViewEmail);
        user = mAuth.getCurrentUser();


        imgbtnInventory = findViewById(R.id.imgbtnOrders);
        imgbtnLaundry = findViewById(R.id.imgbtnLaundry);
        imgbtnLogout = findViewById(R.id.imgbtnLogout);
        imgbtnLocation = findViewById(R.id.imgbtnLocation);



        if(user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textViewEmail.setText(user.getEmail());
        }

        imgbtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        imgbtnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LaundryList.class);
                startActivity(intent);
                finish();
            }
        });

        imgbtnLaundry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LaundryForm.class);
                startActivity(intent);
                finish();
            }
        });

        imgbtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LocationFinder.class);
                startActivity(intent);
                finish();
            }
        });
    }
}