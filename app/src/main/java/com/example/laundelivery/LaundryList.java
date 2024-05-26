package com.example.laundelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LaundryList extends AppCompatActivity {

    private LinearLayout llInventoryList, FormsList;
    private Button btnGoBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        llInventoryList = findViewById(R.id.llInventoryList);
        FormsList = findViewById(R.id.FormsList);
        btnGoBack = findViewById(R.id.btnGoBack);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            loadInventoryList();
            loadLaundryList();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        }

        btnGoBack.setOnClickListener(v -> navigateToMainActivity());
    }

    private void loadInventoryList() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).collection("inventory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemName = document.getString("name");
                        long quantity = document.getLong("quantity");
                        addItemToList(itemName, (int) quantity);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Inventory", "Error loading inventory", e);
                    Toast.makeText(LaundryList.this, "Error loading inventory", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadLaundryList() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).collection("laundry_forms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String address = document.getString("address");
                        String landMark = document.getString("landmark");
                        String datePickup = document.getString("datePickup");
                        String timePickup = document.getString("timePickUp");
                        String detergent = document.getString("detergent");
                        boolean fabricSoftener = Boolean.TRUE.equals(document.getBoolean("fabricSoftener"));
                        String service = document.getString("service");
                        Double total = document.getDouble("total");

                        // Check if total is not null
                        double totalValue = total != null ? total : 0.0;

                        addFormToList(address, landMark, datePickup, timePickup, detergent, String.valueOf(fabricSoftener), service, totalValue);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LaundryList", "Error loading laundry forms", e);
                    Toast.makeText(LaundryList.this, "Error loading laundry forms", Toast.LENGTH_SHORT).show();
                });
    }

    private void addItemToList(String itemName, int itemQuantity) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.inventory_list_layout, llInventoryList, false);

        TextView textViewItemName = itemLayout.findViewById(R.id.textViewItemName);
        TextView textViewQuantity = itemLayout.findViewById(R.id.textViewQuantity);
        Button btnRemove = itemLayout.findViewById(R.id.btnRemove);

        if (textViewItemName != null && textViewQuantity != null && btnRemove != null) {
            textViewItemName.setText(itemName);
            textViewQuantity.setText(String.valueOf(itemQuantity));

            btnRemove.setOnClickListener(v -> removeItemFromInventory(itemName, itemLayout));

            llInventoryList.addView(itemLayout);
        } else {
            Log.e("Inventory", "Error finding views in inventory item layout");
        }
    }

    private void addFormToList(String address, String landMark, String datePickup, String timePickup, String detergent, String fabricSoftener, String service, double total) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout formLayout = (LinearLayout) inflater.inflate(R.layout.laundry_request_layout, FormsList, false);
        String uid = currentUser.getUid();
        TextView textViewAddress = formLayout.findViewById(R.id.textViewAddress);
        TextView textViewLandMark = formLayout.findViewById(R.id.textViewLandMark);
        TextView textViewDatePickup = formLayout.findViewById(R.id.textViewDatePickup);
        TextView textViewTimePickup = formLayout.findViewById(R.id.textViewTimePickup);
        TextView textViewDetergent = formLayout.findViewById(R.id.textViewDetergent);
        TextView textViewFabricSoftener = formLayout.findViewById(R.id.textViewFabricSoftener);
        TextView textViewService = formLayout.findViewById(R.id.textViewService);
        TextView textViewTotal = formLayout.findViewById(R.id.textViewTotal);
        Button btnCancel = formLayout.findViewById(R.id.btnCancel);

        if (textViewAddress != null && textViewLandMark != null && textViewDatePickup != null && textViewTimePickup != null
                && textViewDetergent != null && textViewFabricSoftener != null && textViewService != null && btnCancel != null) {

            textViewAddress.setText(address);
            textViewLandMark.setText(landMark);
            textViewDatePickup.setText(datePickup);
            textViewTimePickup.setText(timePickup);
            textViewDetergent.setText(detergent);
            textViewFabricSoftener.setText(fabricSoftener);
            textViewService.setText(service);
            textViewTotal.setText(String.valueOf(total));

            btnCancel.setOnClickListener(v -> cancelForm(uid, formLayout));

            FormsList.addView(formLayout);
        } else {
            Log.e("LaundryList", "Error finding views in laundry form layout");
        }
    }

    private void removeItemFromInventory(String itemName, LinearLayout itemLayout) {
        db.collection("users").document(currentUser.getUid()).collection("inventory").document(itemName)
                .delete()
                .addOnSuccessListener(aVoid -> llInventoryList.removeView(itemLayout))
                .addOnFailureListener(e -> {
                    Log.e("Inventory", "Error deleting item", e);
                    Toast.makeText(LaundryList.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelForm(String uid, LinearLayout formLayout) {
        String formId = formLayout.getTag().toString();
        db.collection("users").document(uid).collection("laundry_forms").document(formId)
                .delete()
                .addOnSuccessListener(aVoid -> FormsList.removeView(formLayout))
                .addOnFailureListener(e -> {
                    Log.e("LaundryList", "Error cancelling form", e);
                    Toast.makeText(LaundryList.this, "Error cancelling form", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
