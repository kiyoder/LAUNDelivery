package com.example.laundelivery;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LaundryForm extends AppCompatActivity {

    private LinearLayout llInventoryList;
    private EditText txtDatePickup, txtAddress, txtLandmark, etAddItem;
    private Button btnGoBack2, btnSubmit, btnAdd;
    private RadioGroup radioService, radioDetergent, radioTimePickUp;
    private Switch switchFabricSoftener;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_form);

        initializeFirebase();
        initializeUIComponents();
        setupEventListeners();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void initializeUIComponents() {
        llInventoryList = findViewById(R.id.llInventoryList);
        etAddItem = findViewById(R.id.etAddItem);
        btnAdd = findViewById(R.id.btnAdd);
        txtDatePickup = findViewById(R.id.txtDatePickup);
        txtAddress = findViewById(R.id.txtAddress);
        txtLandmark = findViewById(R.id.txtLandmark);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoBack2 = findViewById(R.id.btnGoBack2);
        radioService = findViewById(R.id.radioService);
        radioDetergent = findViewById(R.id.radioDetergent);
        radioTimePickUp = findViewById(R.id.radioTimePickUp);
        switchFabricSoftener = findViewById(R.id.switchFabricSoftener);
    }

    private void setupEventListeners() {
        btnAdd.setOnClickListener(v -> addItemToList());

        txtDatePickup.setOnClickListener(v -> showDatePickerDialog());

        btnSubmit.setOnClickListener(v -> submitLaundryForm());

        btnGoBack2.setOnClickListener(v -> navigateToMainActivity());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                LaundryForm.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1;
                    txtDatePickup.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void submitLaundryForm() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String datePickup = txtDatePickup.getText().toString();
            String address = txtAddress.getText().toString();
            String landmark = txtLandmark.getText().toString();
            String service = ((RadioButton) findViewById(radioService.getCheckedRadioButtonId())).getText().toString();
            String detergent = ((RadioButton) findViewById(radioDetergent.getCheckedRadioButtonId())).getText().toString();
            String timePickUp = ((RadioButton) findViewById(radioTimePickUp.getCheckedRadioButtonId())).getText().toString();
            boolean fabricSoftener = switchFabricSoftener.isChecked();

            Map<String, Object> laundryData = new HashMap<>();
            laundryData.put("datePickup", datePickup);
            laundryData.put("address", address);
            laundryData.put("landmark", landmark);
            laundryData.put("service", service);
            laundryData.put("detergent", detergent);
            laundryData.put("timePickUp", timePickUp);
            laundryData.put("fabricSoftener", fabricSoftener);

            db.collection("users").document(uid).collection("laundry_forms")
                    .add(laundryData)
                    .addOnSuccessListener(aVoid -> {
                        // Clear the form
                        txtDatePickup.setText("");
                        txtAddress.setText("");
                        txtLandmark.setText("");
                        radioService.clearCheck();
                        radioDetergent.clearCheck();
                        radioTimePickUp.clearCheck();
                        switchFabricSoftener.setChecked(false);

                        // Display success message
                        Toast.makeText(LaundryForm.this, "Form Submitted", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), LaundryList.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(LaundryForm.this, "Submission Failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addItemToList() {
        String itemName = etAddItem.getText().toString().trim();
        if (!itemName.isEmpty() && currentUser != null) {
            String uid = currentUser.getUid();

            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.inventory_insert_layout, llInventoryList, false);

            TextView textView = itemLayout.findViewById(R.id.textView2);
            textView.setText(itemName);

            Button btnDecrease = itemLayout.findViewById(R.id.btnDecrease);
            Button btnIncrease = itemLayout.findViewById(R.id.btnRemove);
            EditText editTextQuantity = itemLayout.findViewById(R.id.textViewItemName);

            btnDecrease.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                if (currentQuantity > 0) {
                    int newQuantity = currentQuantity - 1;
                    editTextQuantity.setText(String.valueOf(newQuantity));
                    updateItemQuantityInFirestore(uid, itemName, newQuantity, itemLayout);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                int newQuantity = currentQuantity + 1;
                editTextQuantity.setText(String.valueOf(newQuantity));
                updateItemQuantityInFirestore(uid, itemName, newQuantity, itemLayout);
            });

            llInventoryList.addView(itemLayout);

            db.collection("users").document(uid).collection("inventory").document(itemName)
                    .set(new Item(uid, itemName, 1));

            etAddItem.setText("");
        }
    }

    private void updateItemQuantityInFirestore(String uid, String itemName, int newQuantity, LinearLayout itemLayout) {
        if (newQuantity > 0) {
            db.collection("users").document(uid).collection("inventory").document(itemName)
                    .update("quantity", newQuantity);
        } else {
            db.collection("users").document(uid).collection("inventory").document(itemName)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the item layout from the list
                        llInventoryList.removeView(itemLayout);
                    });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static class Item {
        private String userId;
        private String name;
        private int quantity;

        public Item(String userId, String name, int quantity) {
            this.userId = userId;
            this.name = name;
            this.quantity = quantity;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
