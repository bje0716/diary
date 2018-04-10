package com.grapefruit.diary;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.grapefruit.diary.databinding.ActivityWriteBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class WriteActivity extends AppCompatActivity {

    private ActivityWriteBinding binding;
    private PreferenceUtil util;
    private Realm realm;
    private RealmResults<MainItem> results;
    private URL url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_write);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(Util.setBackArrowColor(this));
        util = new PreferenceUtil(this);

        realm = Realm.getDefaultInstance();
        results = realm.where(MainItem.class).findAll();

        if (util.getString("img", null) != null &&
                (util.getString("name", null ) != null)) {
            Glide.with(this)
                    .load(util.getString("img", null))
                    .into(binding.img);
            binding.name.setText(util.getString("name", null));
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_person_purple)
                    .into(binding.img);
            binding.name.setText("이름");
        }
        binding.date.setText(getDate());

        binding.content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.toolbar.setTitle(binding.content.getText().length() + "자");
            }
        });

        binding.content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        try {
                            url = new URL(binding.content.getText().toString().trim());
                        } catch (MalformedURLException e) {
                            return false;
                        }

                        binding.link.setVisibility(View.VISIBLE);
                        if (Util.isNetwork(getWindow().getContext())) {
                            new LinkParser().execute(url.toString());
                        } else {
                            Toast.makeText(WriteActivity.this, "네트워크를 확인해주세요", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }

                return false;
            }
        });

        binding.linkClose.setOnClickListener(v -> {
            binding.link.setVisibility(View.GONE);
        });
    }

    private class LinkParser extends AsyncTask<String, String, Element> {

        @Override
        protected Element doInBackground(String... strings) {
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                Elements elements = doc.select("meta[property^=og:]");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Element e : elements) {
                            switch (e.attr("property")) {
                                case "og:title":
                                    if (e.attr("content") != null ||
                                            !e.attr("content").equals("")) {
                                        binding.linkTitle.setText(e.attr("content"));
                                    } else {
                                        binding.linkTitle.setText("");
                                    }
                                    break;
                                case "og:description":
                                    if (e.attr("content") != null ||
                                            !e.attr("content").equals("")) {
                                        binding.linkSummary.setText(e.attr("content"));
                                    } else {
                                        binding.linkSummary.setText("");
                                    }
                                    break;
                                case "og:image":
                                    if (e.attr("content") != null ||
                                            !e.attr("content").equals("")) {
                                        binding.linkImg.setVisibility(View.GONE);
                                    }
                                    binding.linkImg.setVisibility(View.VISIBLE);
                                    Glide.with(getWindow().getContext())
                                            .load(e.attr("content"))
                                            .into(binding.linkImg);
                                    break;
                                case "og:url":
                                    binding.linkUrl.setText(e.attr("content"));
                                    break;
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(WriteActivity.this, "링크를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Element e) {
            super.onPostExecute(e);
            binding.progress.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.link.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
        }
    }

    private String getDate() {
        return new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREA).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_upload:
                if (!VerifyUtil.verifyString(binding.content.getText().toString())) {
                    Toast.makeText(this, "글 작성을 해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            MainItem asdf = realm.createObject(MainItem.class);
                            asdf.setDate(System.currentTimeMillis());
                            asdf.setContent(binding.content.getText().toString());
                            Toast.makeText(WriteActivity.this, "글 등록되었습니다", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_CANCELED, new Intent());
                            finish();
                        }
                    });
                }
                break;
        }
        return true;
    }
}
