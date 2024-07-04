package com.example.ezrent.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ezrent.R;

public class ChatFragment extends Fragment {

    private Button openWhatsAppButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        openWhatsAppButton = view.findViewById(R.id.openWhatsAppButton);

        openWhatsAppButton.setOnClickListener(v -> openWhatsAppChat());

        return view;
    }

    private void openWhatsAppChat() {
        String phoneNumber = "+601137131267"; // Replace with your phone number
        String message = "Hi, I have an enquiry for you."; // Pre-filled message

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp not installed on your device.", Toast.LENGTH_SHORT).show();
        }
    }
}
