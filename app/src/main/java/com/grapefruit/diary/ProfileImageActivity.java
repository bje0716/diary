package com.grapefruit.diary;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.grapefruit.diary.databinding.ActivityProfileImageBinding;

public class ProfileImageActivity extends AppCompatActivity {

    private ActivityProfileImageBinding binding;
    private PreferenceUtil util;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_image);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        util = new PreferenceUtil(this);

        if (util.getString("img", null) != null) {
            Glide.with(this)
                    .load(util.getString("img", null))
                    .into(binding.img);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
