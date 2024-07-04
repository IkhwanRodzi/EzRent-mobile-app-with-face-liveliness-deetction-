package com.example.ezrent.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezrent.R;
import com.example.ezrent.screens.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    private TextView emailTextView, roleTextView;
    private EditText updateEmailEditText, updatePasswordEditText;
    private Button updateEmailButton, updatePasswordButton, viewAccountDetailsButton, deleteAccountButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        emailTextView = view.findViewById(R.id.emailTextView);
        roleTextView = view.findViewById(R.id.roleTextView);
        updateEmailEditText = view.findViewById(R.id.updateEmailEditText);
        updatePasswordEditText = view.findViewById(R.id.updatePasswordEditText);
        updateEmailButton = view.findViewById(R.id.updateEmailButton);
        updatePasswordButton = view.findViewById(R.id.updatePasswordButton);
        viewAccountDetailsButton = view.findViewById(R.id.viewAccountDetailsButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
            fetchAccountDetails(currentUser.getUid());
        }

        updateEmailButton.setOnClickListener(v -> {
            String newEmail = updateEmailEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(newEmail)) {
                updateEmail(newEmail);
            } else {
                Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = updatePasswordEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(newPassword)) {
                updatePassword(newPassword);
            } else {
                Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAccountButton.setOnClickListener(v -> {
            deleteAccount();
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void fetchAccountDetails(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                roleTextView.setText(role);
            }
        });
    }

    private void updateEmail(String newEmail) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "userCurrentPassword");
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updateEmail(newEmail).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update email", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "userCurrentPassword");
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAccount() {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "userCurrentPassword");
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
