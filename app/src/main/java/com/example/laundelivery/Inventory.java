package com.example.laundelivery;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Inventory extends AppCompatActivity {

    private LinearLayout llInventoryList;
    private EditText etAddItem;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        llInventoryList = findViewById(R.id.llInventoryList);
        etAddItem = findViewById(R.id.etAddItem);
        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> addItemToList());
    }

    private void addItemToList() {
        String itemName = etAddItem.getText().toString().trim();
        if (!itemName.isEmpty()) {
            // Inflate the new layout
            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.inventory_item_layout, llInventoryList, false);

            // Set the item name
            TextView textView = itemLayout.findViewById(R.id.textView2);
            textView.setText(itemName);

            // Set up the buttons
            Button btnDecrease = itemLayout.findViewById(R.id.btnDecrease);
            Button btnIncrease = itemLayout.findViewById(R.id.btnIncrease);
            EditText editTextQuantity = itemLayout.findViewById(R.id.editTextText2);

            btnDecrease.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                if (currentQuantity > 0) {
                    editTextQuantity.setText(String.valueOf(currentQuantity - 1));
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                editTextQuantity.setText(String.valueOf(currentQuantity + 1));
            });

            // Add the new layout to the inventory list
            llInventoryList.addView(itemLayout);

            // Clear the EditText
            etAddItem.setText("");
        }
    }
}
