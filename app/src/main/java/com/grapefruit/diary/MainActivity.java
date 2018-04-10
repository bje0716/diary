package com.grapefruit.diary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.grapefruit.diary.databinding.ActivityMainBinding;
import com.grapefruit.diary.databinding.DialogWriteProfileBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_GALLERY = 2;

    private ActivityMainBinding binding;
    private DialogWriteProfileBinding dialogBinding;
    private Realm realm;
    private RealmResults<MainItem> results;
    private PreferenceUtil util;
    private View profileDialog;
    private MainAdapter adapter;

    private long pressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        util = new PreferenceUtil(this);

        realm = Realm.getDefaultInstance();
        results = realm.where(MainItem.class).findAll();
        adapter = new MainAdapter(this, realm, results);

        PermissionListener listener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            }
        };

        TedPermission.with(this)
                .setPermissionListener(listener)
                .setDeniedMessage("권한 허용해주셔야 어플을 사용하실 수 있습니다")
                .setPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        binding.recycler.setHasFixedSize(true);
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        if (adapter.getItemCount() != 0) {
            if (binding.noItem.getVisibility() == View.VISIBLE) {
                binding.noItem.setVisibility(View.GONE);
            }
            binding.recycler.setAdapter(adapter);
        } else {
            binding.noItem.setVisibility(View.VISIBLE);
        }

        if (util.getString("img", null) != null) {
            Glide.with(this)
                    .load(util.getString("img", null))
                    .into(binding.img);
        } else {
            binding.img.setBackgroundResource(R.drawable.ic_person);
        }

        if ((util.getString("name", null) != null) &&
                (util.getString("summary", null) != null)) {
            binding.name.setText(util.getString("name", null));
            binding.summary.setText(util.getString("summary", null));
        }

        binding.img.setOnClickListener(v -> new MaterialDialog.Builder(getWindow().getContext())
                .title("프로필 사진 선택")
                .items("카메라", "갤러리", "기본 이미지", "사진 크게 보기")
                .itemsCallback((dialog, itemView, position, text) -> {
                    switch (position) {
                        case 0:
                            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TYPE_CAMERA);
                            break;
                        case 1:
                            startActivityForResult(new Intent(Intent.ACTION_PICK).setType(MediaStore.Images.Media.CONTENT_TYPE), TYPE_GALLERY);
                            break;
                        case 2:
                            util.putString("img", null);
                            Glide.with(getWindow().getContext())
                                    .load(R.drawable.ic_person)
                                    .into(binding.img);
                            break;
                        case 3:
                            startActivity(new Intent(getWindow().getContext(), ProfileImageActivity.class));
                            break;
                    }
                }).show());

        profileDialog = LayoutInflater.from(this).inflate(R.layout.dialog_write_profile, null);
        dialogBinding = DataBindingUtil.bind(profileDialog);

        if (util.getString("name", null) != null &&
                util.getString("summary", null) != null) {
            dialogBinding.name.setText(util.getString("name", null));
            dialogBinding.summary.setText(util.getString("summary", null));
            Selection.setSelection(dialogBinding.name.getText(), dialogBinding.name.length());
            Selection.setSelection(dialogBinding.summary.getText(), dialogBinding.summary.length());
        }

        binding.profile.setOnClickListener(v -> new MaterialDialog.Builder(getWindow().getContext())
                .title("프로필 정보 편집")
                .cancelable(false)
                .customView(profileDialog, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    String name = dialogBinding.name.getText().toString().trim();
                    String summary = dialogBinding.summary.getText().toString().trim();
                    if (!VerifyUtil.verifyStrings(name, summary)) {
                        Toast.makeText(MainActivity.this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        util.putString("name", name);
                        util.putString("summary", summary);
                        adapter.notifyDataSetChanged();
                        binding.name.setText(name);
                        binding.summary.setText(summary);
                    }
                })
                .onNegative((dialog, which) -> dialog.dismiss()).show());

        binding.bottom.setOnClickListener(v -> {
            new MaterialDialog.Builder(getWindow().getContext())
                    .title("배경 사진 설정")
                    .items("기본", "카메라", "갤러리", "사진 크게 보기")
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                            switch (position) {
                                case 0:
                                    util.putString("background", null);
                                    binding.bottom.setBackgroundResource(R.color.colorPrimary);
                                    break;
                            }
                        }
                    }).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_write:
                startActivityForResult(new Intent(this, WriteActivity.class), 3);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TYPE_CAMERA:
                    Log.d("log", data.getDataString());
                    util.putString("img", data.getDataString());
                    binding.img.setImageURI(data.getData());
                    break;
                case TYPE_GALLERY:
                    Log.d("log", data.getDataString());
                    util.putString("img", data.getDataString());
                    binding.img.setImageURI(data.getData());
                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 3:
                    adapter.notifyDataSetChanged();
                    if (adapter.getItemCount() != 0) {
                        if (binding.noItem.getVisibility() == View.VISIBLE) {
                            binding.noItem.setVisibility(View.GONE);
                        }
                        binding.recycler.setAdapter(adapter);
                    } else {
                        binding.noItem.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }

    @Override
    public void onBackPressed() {
        if (pressedTime == 0) {
            Toast.makeText(this, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);
            if (seconds > 2000) {
                Toast.makeText(this, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
                pressedTime = 0;
            } else {
                finish();
            }
        }
    }
}
