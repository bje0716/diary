package com.grapefruit.diary;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.grapefruit.diary.databinding.ItemBinding;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context context;
    private Realm realm;
    private RealmResults<MainItem> items;
    private PreferenceUtil util;

    public MainAdapter(Context context, Realm realm, RealmResults<MainItem> data) {
        super();
        this.context = context;
        this.realm = realm;
        items = data;
        util = new PreferenceUtil(context);
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        if (items.get(position) != null) {
            holder.binding.img.setImageURI(Uri.parse(util.getString("img", null)));
            holder.binding.name.setText(util.getString("name", null));
            holder.binding.date.setText(Util.getDate(items.get(position).getDate()));
            holder.binding.content.setText(items.get(position).getContent());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        private ItemBinding binding;

        public MainViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    switch (getAdapterPosition()) {
                        default:
                            new MaterialDialog.Builder(itemView.getContext())
                                    .cancelable(false)
                                    .title("글 삭제하시겠습니까?")
                                    .content("삭제된 글은 복구 불가능합니다")
                                    .positiveText(android.R.string.ok)
                                    .negativeText(android.R.string.cancel)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    RealmResults<MainItem> delete = realm.where(MainItem.class)
                                                            .equalTo("content", binding.content.getText().toString())
                                                            .findAll();
                                                    delete.deleteAllFromRealm();
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            break;
                    }
                    return true;
                }
            });
        }
    }
}
