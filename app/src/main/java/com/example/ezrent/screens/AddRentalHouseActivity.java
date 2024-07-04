package com.example.ezrent.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ezrent.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddRentalHouseActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameEditText, addressEditText, priceEditText, ownerPhoneEditText, descriptionEditText;
    private ImageView houseImageView;
    private Button saveButton, uploadImageButton;
    private FirebaseFirestore db;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rental_house);

        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        priceEditText = findViewById(R.id.priceEditText);
        ownerPhoneEditText = findViewById(R.id.ownerPhoneEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        houseImageView = findViewById(R.id.houseImageView);
        saveButton = findViewById(R.id.saveButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("rental_house_images");

        uploadImageButton.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v -> saveRentalHouse());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(houseImageView);
        }
    }

    private void uploadImage(String houseId, Map<String, Object> houseData) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        houseData.put("imageUrl", uri.toString());
                        saveHouseData(houseId, houseData);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(AddRentalHouseActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            saveHouseData(houseId, houseData);
        }
    }

    private void saveRentalHouse() {
        String name = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String ownerPhoneNumber = ownerPhoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(price) || TextUtils.isEmpty(ownerPhoneNumber) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> houseData = new HashMap<>();
        houseData.put("name", name);
        houseData.put("address", address);
        houseData.put("price", price);
        houseData.put("ownerPhoneNumber", ownerPhoneNumber);
        houseData.put("description", description);
        houseData.put("userId", userId);

        String houseId = db.collection("rental_houses").document().getId();
        uploadImage(houseId, houseData);
    }

    private void saveHouseData(String houseId, Map<String, Object> houseData) {
        db.collection("rental_houses").document(houseId)
                .set(houseData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddRentalHouseActivity.this, "House added successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity and return to the previous screen
                })
                .addOnFailureListener(e -> Toast.makeText(AddRentalHouseActivity.this, "Error adding house", Toast.LENGTH_SHORT).show());
    }
}
