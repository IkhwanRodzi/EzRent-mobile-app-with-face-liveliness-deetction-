package com.example.ezrent.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezrent.R;
import com.example.ezrent.adapter.RentalHouseAdapter;
import com.example.ezrent.model.RentalHouse;
import com.example.ezrent.screens.AddRentalHouseActivity;
import com.example.ezrent.screens.RentalHouseDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment implements RentalHouseAdapter.OnRentalHouseClickListener {

    public static final int DETAIL_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private RentalHouseAdapter adapter;
    private List<RentalHouse> rentalHouseList;
    private Button addHouseButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rentalHouseList = new ArrayList<>();
        adapter = new RentalHouseAdapter(getContext(), rentalHouseList, this);
        recyclerView.setAdapter(adapter);

        addHouseButton = view.findViewById(R.id.addHouseButton);
        addHouseButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddRentalHouseActivity.class);
            startActivity(intent);
        });

        fetchRentalHouses();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRentalHouses(); // Fetch the latest data whenever the fragment is resumed
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            fetchRentalHouses();
        }
    }

    private void fetchRentalHouses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rental_houses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rentalHouseList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            RentalHouse rentalHouse = document.toObject(RentalHouse.class);
                            rentalHouse.setId(document.getId()); // Ensure the ID is set
                            rentalHouseList.add(rentalHouse);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public void onRentalHouseClick(RentalHouse rentalHouse) {
        Intent intent = new Intent(getActivity(), RentalHouseDetailActivity.class);
        intent.putExtra("rentalHouse", rentalHouse);
        intent.putExtra("userRole", "admin"); // Pass the role
        startActivityForResult(intent, DETAIL_REQUEST_CODE);
    }
}
