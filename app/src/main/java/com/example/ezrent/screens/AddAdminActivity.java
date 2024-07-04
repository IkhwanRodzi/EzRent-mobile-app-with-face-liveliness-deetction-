package com.example.ezrent.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezrent.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class AddAdminActivity extends AppCompatActivity {

    private EditText adminEmailEditText;
    private Button addAdminButton, deleteAdminButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        adminEmailEditText = findViewById(R.id.adminEmailEditText);
        addAdminButton = findViewById(R.id.addAdminButton);
        deleteAdminButton = findViewById(R.id.deleteAdminButton);
        db = FirebaseFirestore.getInstance();

        addAdminButton.setOnClickListener(v -> {
            String email = adminEmailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(AddAdminActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String userId = document.getId();
                            Log.d("AddAdmin", "User found: " + userId);

                            db.collection("users").document(userId)
                                    .update("role", "admin")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddAdminActivity.this, "Admin added successfully", Toast.LENGTH_SHORT).show();
                                        adminEmailEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AddAdminActivity.this, "Error adding admin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Log.d("AddAdmin", "User not found with email: " + email);
                            Toast.makeText(AddAdminActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        deleteAdminButton.setOnClickListener(v -> {
            String email = adminEmailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(AddAdminActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String userId = document.getId();
                            Log.d("DeleteAdmin", "User found: " + userId);

                            db.collection("users").document(userId)
                                    .update("role", "tenant")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddAdminActivity.this, "Admin role removed, user is now a tenant", Toast.LENGTH_SHORT).show();
                                        adminEmailEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AddAdminActivity.this, "Error removing admin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Log.d("DeleteAdmin", "User not found with email: " + email);
                            Toast.makeText(AddAdminActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
