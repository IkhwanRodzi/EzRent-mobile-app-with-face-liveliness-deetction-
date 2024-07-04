package com.example.ezrent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ezrent.R;
import com.example.ezrent.model.RentalHouse;
import java.util.List;

public class RentalHouseAdapter extends RecyclerView.Adapter<RentalHouseAdapter.RentalHouseViewHolder> {

    private List<RentalHouse> rentalHouseList;
    private Context context;
    private OnRentalHouseClickListener listener;

    public interface OnRentalHouseClickListener {
        void onRentalHouseClick(RentalHouse rentalHouse);
    }

    public RentalHouseAdapter(Context context, List<RentalHouse> rentalHouseList, OnRentalHouseClickListener listener) {
        this.context = context;
        this.rentalHouseList = rentalHouseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RentalHouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rental_house, parent, false);
        return new RentalHouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalHouseViewHolder holder, int position) {
        RentalHouse rentalHouse = rentalHouseList.get(position);
        holder.houseName.setText(rentalHouse.getName());
        holder.houseAddress.setText(rentalHouse.getAddress());
        holder.housePrice.setText(rentalHouse.getPrice());

        if (rentalHouse.getImageUrl() != null && !rentalHouse.getImageUrl().isEmpty()) {
            Glide.with(context).load(rentalHouse.getImageUrl()).into(holder.houseImageView);
        } else {
            holder.houseImageView.setImageResource(R.drawable.ic_house_placeholder); // Placeholder if no image
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRentalHouseClick(rentalHouse);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rentalHouseList.size();
    }

    public static class RentalHouseViewHolder extends RecyclerView.ViewHolder {
        public TextView houseName;
        public TextView houseAddress;
        public TextView housePrice;
        public ImageView houseImageView;

        public RentalHouseViewHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.house_name);
            houseAddress = itemView.findViewById(R.id.house_address);
            housePrice = itemView.findViewById(R.id.house_price);
            houseImageView = itemView.findViewById(R.id.house_image);
        }
    }
}
