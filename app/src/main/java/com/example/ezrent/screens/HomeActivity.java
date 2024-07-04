package com.example.ezrent.screens;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.ezrent.R;
import com.example.ezrent.fragment.AccountFragment;
import com.example.ezrent.fragment.ChatFragment;
import com.example.ezrent.fragment.AdminHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Map<Integer, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Initialize fragment map
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.menu_home, new AdminHomeFragment());
        fragmentMap.put(R.id.menu_chat, new ChatFragment());
        fragmentMap.put(R.id.menu_account, new AccountFragment());

        // Load the default fragment
        loadFragment(fragmentMap.get(R.id.menu_home));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = fragmentMap.get(item.getItemId());
        return loadFragment(fragment);
    }

    boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
