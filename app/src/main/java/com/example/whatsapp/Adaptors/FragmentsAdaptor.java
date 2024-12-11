package com.example.whatsapp.Adaptors;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.whatsapp.Fragments.Call;
import com.example.whatsapp.Fragments.Chat;
import com.example.whatsapp.Fragments.Status;
import com.example.whatsapp.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentsAdaptor extends FragmentStateAdapter {
    public FragmentsAdaptor(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    /*private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();
    private final List<Integer> fragmentIconList = new ArrayList<>();*/



    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0 :
                return new Chat();
            case 1 :
                return new Status();
            case 2 :
                return new Call();
            default :
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    /*public void addFragment(Fragment fragment, String title, int icon) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
        fragmentIconList.add(icon);
    }

    public String getTitle(int position) {
        return fragmentTitleList.get(position);
    }

    public int getIcon(int position) {
        return fragmentIconList.get(position);
    }*/

    /*// Method to get the title for each tab
    public String getTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Status";
            case 2:
                return "Calls";
            default:
                return "Chats"; // Default to Chats
        }
    }

    // Method to get the icon for each tab
    public int getIcon(int position) {
        switch (position) {
            case 0:
                return R.drawable.message; // Replace with your icon drawable for Chats
            case 1:
                return R.drawable.status; // Replace with your icon drawable for Status
            case 2:
                return R.drawable.call; // Replace with your icon drawable for Calls
            default:
                return R.drawable.message; // Default to Chats icon
        }
    }*/
}
