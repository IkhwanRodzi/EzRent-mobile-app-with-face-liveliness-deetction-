package com.example.ezrent.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.ezrent.R;
import com.example.ezrent.model.RentalHouse;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RentalHouseDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView nameTextView, addressTextView, priceTextView, descriptionTextView;
    private EditText nameEditText, addressEditText, priceEditText, descriptionEditText;
    private ImageView houseImageView;
    private Button updateButton, deleteButton, uploadImageButton, contactOwnerButton;
    private FirebaseFirestore db;
    private RentalHouse rentalHouse;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_house_detail);

        nameTextView = findViewById(R.id.nameTextView);
        addressTextView = findViewById(R.id.addressTextView);
        priceTextView = findViewById(R.id.priceTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        priceEditText = findViewById(R.id.priceEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        houseImageView = findViewById(R.id.houseImageView);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        contactOwnerButton = findViewById(R.id.contactOwnerButton);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("rental_house_images");

        rentalHouse = (RentalHouse) getIntent().getSerializableExtra("rentalHouse");
        String userRole = getIntent().getStringExtra("userRole");

        if (rentalHouse != null) {
            nameEditText.setText(rentalHouse.getName());
            addressEditText.setText(rentalHouse.getAddress());
            priceEditText.setText(rentalHouse.getPrice());
            descriptionEditText.setText(rentalHouse.getDescription());
            nameTextView.setText(rentalHouse.getName());
            addressTextView.setText(rentalHouse.getAddress());
            priceTextView.setText(rentalHouse.getPrice());
            descriptionTextView.setText(rentalHouse.getDescription());
            if (rentalHouse.getImageUrl() != null && !rentalHouse.getImageUrl().isEmpty()) {
                Glide.with(this).load(rentalHouse.getImageUrl()).into(houseImageView);
            }
        }

        if ("admin".equals(userRole)) {
            showAdminView();
        } else {
            showTenantView();
        }

        uploadImageButton.setOnClickListener(v -> openFileChooser());
        updateButton.setOnClickListener(v -> updateRentalHouse());
        deleteButton.setOnClickListener(v -> deleteRentalHouse());
        contactOwnerButton.setOnClickListener(v -> contactHouseOwner());
    }

    private void showAdminView() {
        nameEditText.setVisibility(View.VISIBLE);
        addressEditText.setVisibility(View.VISIBLE);
        priceEditText.setVisibility(View.VISIBLE);
        descriptionEditText.setVisibility(View.VISIBLE);
        nameTextView.setVisibility(View.GONE);
        addressTextView.setVisibility(View.GONE);
        priceTextView.setVisibility(View.GONE);
        descriptionTextView.setVisibility(View.GONE);
        updateButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.VISIBLE);
        contactOwnerButton.setVisibility(View.GONE);
    }

    private void showTenantView() {
        nameEditText.setVisibility(View.GONE);
        addressEditText.setVisibility(View.GONE);
        priceEditText.setVisibility(View.GONE);
        descriptionEditText.setVisibility(View.GONE);
        nameTextView.setVisibility(View.VISIBLE);
        addressTextView.setVisibility(View.VISIBLE);
        priceTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setVisibility(View.VISIBLE);
        updateButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        uploadImageButton.setVisibility(View.GONE);
        contactOwnerButton.setVisibility(View.VISIBLE);
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
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        rentalHouse.setImageUrl(downloadUrl);
                        updateRentalHouse();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(RentalHouseDetailActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRentalHouse() {
        String name = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(price) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        rentalHouse.setName(name);
        rentalHouse.setAddress(address);
        rentalHouse.setPrice(price);
        rentalHouse.setDescription(description);

        db.collection("rental_houses").document(rentalHouse.getId())
                .set(rentalHouse)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RentalHouseDetailActivity.this, "House updated successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Close the detail activity and return to the previous screen
                })
                .addOnFailureListener(e -> Toast.makeText(RentalHouseDetailActivity.this, "Error updating house", Toast.LENGTH_SHORT).show());
    }

    private void deleteRentalHouse() {
        db.collection("rental_houses").document(rentalHouse.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RentalHouseDetailActivity.this, "House deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Close the detail activity and return to the previous screen
                })
                .addOnFailureListener(e -> Toast.makeText(RentalHouseDetailActivity.this, "Error deleting house", Toast.LENGTH_SHORT).show());
    }

    private void contactHouseOwner() {
        String ownerPhoneNumber = rentalHouse.getOwnerPhoneNumber();
        if (!TextUtils.isEmpty(ownerPhoneNumber)) {
            Intent intent = new Intent(this, FacialDetectionActivity.class);
            intent.putExtra("ownerPhoneNumber", ownerPhoneNumber);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Owner contact number not available.", Toast.LENGTH_SHORT).show();
        }
    }
}
