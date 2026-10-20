package com.myapp.bankingexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.myapp.bankingexample.ui.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private final String[] TAB_TITLES = new String[]{
            "EMV TLV Çözücü",
            "APDU Simülatörü",
            "ISO 8583 Paketleyici",
            "Uçtan Uca Ödeme",
            "Bilgi Bankası"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position < TAB_TITLES.length) {
                tab.setText(TAB_TITLES[position]);
            }
        }).attach();
    }
}
