package com.baidu.idl.sample.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;

/**
 * Created by litonghui on 2018/11/27.
 */

public class SettingCameraActivity extends BaseActivity implements View.OnClickListener {

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
        mLableTxt.setText(R.string.setting_live_status);

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

        public CheckBox checkBox;
        public TextView titleView;
        public TextView messageView;
        public Spinner spinner;

        public SetViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title);
            messageView = itemView.findViewById(R.id.message);
            checkBox = itemView.findViewById(R.id.check_btn);
            spinner = itemView.findViewById(R.id.spinner);
        }
    }

    public class SetAdapter extends RecyclerView.Adapter<SetViewHolder> implements View.OnClickListener {

        private Context context;

        public SetAdapter(Context context) {
            this.context = context;
        }

        private int[] txtItem = {
                R.string.setting_live_title_no,
                R.string.setting_live_title_rgb,
                R.string.setting_live_title_bino,
                R.string.setting_live_title_sl,
        };

        private int[] msgItem = {
                R.string.setting_live_no,
                R.string.setting_live_rgb,
                R.string.setting_live_bino,
                R.string.setting_live_sl,
        };

        @Override
        public void onClick(View v) {
            int id = v.getId();
        }

        @Override
        public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_setting_live, parent, false);
            SetViewHolder viewHolder = new SetViewHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SetViewHolder holder, final int position) {
            holder.titleView.setText(txtItem[position]);
            holder.messageView.setText(msgItem[position]);
            holder.checkBox.setChecked(GlobalSet.getLiveStatusValue().ordinal() == position ? true : false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        GlobalSet.setLiveStatusValue(GlobalSet.LIVE_STATUS.values()[position]);
                        notifyDataSetChanged();
                    }
                }
            });
            holder.spinner.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
            holder.spinner.setSelection(GlobalSet.getStructuredLightValue().ordinal());
            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    GlobalSet.setStructuredLightValue(GlobalSet.STRUCTURED_LIGHT.values()[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return txtItem.length;
        }

    }
}