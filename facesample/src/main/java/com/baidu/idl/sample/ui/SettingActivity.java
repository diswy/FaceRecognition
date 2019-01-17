package com.baidu.idl.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.idl.sample.R;

/**
 * Created by litonghui on 2018/11/27.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private RecyclerView mRecyclerView;
    private SetAdapter mSetAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        initView();
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.setting);

        mRecyclerView = findViewById(R.id.recyclerview);
        mSetAdapter = new SetAdapter(mContext);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mSetAdapter);
    }

    @Override
    public void onClick(View v) {

    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public TextView textView;

        public SetViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = itemView.findViewById(R.id.title);
        }
    }

    public class SetAdapter extends RecyclerView.Adapter<SetViewHolder> implements View.OnClickListener {

        private Context context;

        public SetAdapter(Context context) {
            this.context = context;
        }

        private int[] txtItem = {
                // R.string.setting_detect_threshold,
                R.string.setting_live_threshold,
                R.string.setting_feature_threshold,
                R.string.setting_live_status,
                // R.string.setting_track_angle,
                R.string.setting_camera_preview,
                R.string.license_activate,
        };

        @Override
        public void onClick(View v) {
            int id = (int) v.getTag();
            Intent intent = null;
            if (id == 0) {
                intent = new Intent(context, SettingLiveActivity.class);
            } else if (id == 1) {
                intent = new Intent(context, SettingFeatureActivity.class);
            } else if (id == 2) {
                intent = new Intent(context, SettingCameraActivity.class);
            } else if (id == 3) {
                intent = new Intent(context, SettingCameraPreviewAngleActivity.class);
            } else if (id == 4) {
                intent = new Intent(context, LicenseActivity.class);
            }
//            else if (id == 4) {
//                intent = new Intent(context, SettingFaceTrackAngleActivity.class);
//            }
//            if (id == 0) {
//                intent = new Intent(context, SettingDetectActivity.class);
//            }
            if (intent != null) {
                startActivity(intent);
            }
        }

        @Override
        public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_setting, parent, false);
            SetViewHolder viewHolder = new SetViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SetViewHolder holder, int position) {
            holder.itemView.setTag(position);
            holder.textView.setText(txtItem[position]);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return txtItem.length;
        }

    }
}
