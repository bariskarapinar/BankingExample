package com.myapp.bankingexample.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new EmvTlvFragment();
            case 1:
                return new EmvApduFragment();
            case 2:
                return new Iso8583Fragment();
            case 3:
                return new PaymentSimulatorFragment();
            case 4:
                return new KnowledgeBaseFragment();
            default:
                return new EmvTlvFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
