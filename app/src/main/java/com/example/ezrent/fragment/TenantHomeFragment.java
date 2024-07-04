package com.example.ezrent.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezrent.R;
import com.example.ezrent.adapter.RentalHouseAdapter;
import com.example.ezrent.model.RentalHouse;
import com.example.ezrent.screens.RentalHouseDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TenantHomeFragment extends Fragment implements RentalHouseAdapter.OnRentalHouseClickListener {

    private RecyclerView recyclerView;
    private RentalHouseAdapter adapter;
    private List<RentalHouse> rentalHouseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tenant, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rentalHouseList = new ArrayList<>();
        adapter = new RentalHouseAdapter(getContext(), rentalHouseList, this);
        recyclerView.setAdapter(adapter);

        fetchRentalHouses();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRentalHouses(); // Fetch the latest data whenever the fragment is resumed
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
                        Log.d("TenantHomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public void onRentalHouseClick(RentalHouse rentalHouse) {
        Intent intent = new Intent(getActivity(), RentalHouseDetailActivity.class);
        intent.putExtra("rentalHouse", rentalHouse);
        intent.putExtra("userRole", "tenant"); // Pass the role
        startActivity(intent);
    }
}
