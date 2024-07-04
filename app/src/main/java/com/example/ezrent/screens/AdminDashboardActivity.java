package com.example.ezrent.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.ezrent.R;
import com.example.ezrent.fragment.AdminHomeFragment;
import com.example.ezrent.fragment.ChatFragment;
import com.example.ezrent.fragment.AccountFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        loadFragment(new AdminHomeFragment());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.menu_home) {
            fragment = new AdminHomeFragment();
        } else if (itemId == R.id.menu_chat) {
            fragment = new ChatFragment();
        } else if (itemId == R.id.menu_account) {
            fragment = new AccountFragment();
        } else if (itemId == R.id.menu_add_admin) {
            startActivity(new Intent(this, AddAdminActivity.class));
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
