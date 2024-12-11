package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.Adaptors.FragmentsAdaptor;
import com.example.whatsapp.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();


        FragmentsAdaptor adaptor = new FragmentsAdaptor(getSupportFragmentManager(), getLifecycle());
        binding.viewPager.setAdapter(adaptor);

        binding.viewPager.setCurrentItem(0, false);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    // Inflate custom tab layout
                    View customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);

                    // Set the custom icon and text
                    ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                    TextView tabTitle = customView.findViewById(R.id.tab_title);

                    switch (position) {
                        case 0:
                            tabIcon.setImageResource(R.drawable.chatss);  // Set your chat icon here
                            tabTitle.setText("Chat");
                            break;
                        case 1:
                            tabIcon.setImageResource(R.drawable.status);  // Set your status icon here
                            tabTitle.setText("Status");
                            break;
                        case 2:
                            tabIcon.setImageResource(R.drawable.call);  // Set your call icon here
                            tabTitle.setText("Call");
                            break;
                    }

                    // Set the custom view to the tab
                    tab.setCustomView(customView);
                }
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        } else if (itemId == R.id.logout) {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, SignIn.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (itemId == R.id.groupChat) {
            Intent intent = new Intent(MainActivity.this, GroupChat.class);
            startActivity(intent);
        }
        return true;
    }
}