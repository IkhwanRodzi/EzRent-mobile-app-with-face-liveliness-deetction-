package com.example.ezrent.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezrent.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddAdminFragment extends Fragment {

    private EditText emailEditText;
    private Button addButton, deleteButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_admin, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        addButton = view.findViewById(R.id.addButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        addButton.setOnClickListener(v -> addAdmin());
        deleteButton.setOnClickListener(v -> deleteAdmin());

        return view;
    }

    private void addAdmin() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                String userId = task.getResult().getDocuments().get(0).getId();
                db.collection("users").document(userId)
                        .update("role", "admin")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "User is now an admin", Toast.LENGTH_SHORT).show();
                            emailEditText.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update role", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAdmin() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                String userId = task.getResult().getDocuments().get(0).getId();
                db.collection("users").document(userId)
                        .update("role", "tenant")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Admin role removed, user is now a tenant", Toast.LENGTH_SHORT).show();
                            emailEditText.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update role", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
