package com.baidu.idl.sample.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.listener.OnExplainClickListener;
import com.baidu.idl.sample.view.MessageDialog;

/**
 * Created by litonghui on 2018/11/27.
 */

public class SettingFeatureActivity extends BaseActivity implements OnExplainClickListener {

    private Context mContext;

    private RecyclerView mRecyclerView;
    private MessageDialog mMessageDialog;
    private SetAdapter mSetAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageDialog.isShowing()) {
            mMessageDialog.dismiss();
        }
        mMessageDialog = null;
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.setting_feature_threshold);

        mRecyclerView = findViewById(R.id.recyclerview);
        mMessageDialog = new MessageDialog(mContext);
        mMessageDialog.setCancelable(true);
        mMessageDialog.setCanceledOnTouchOutside(true);
        mSetAdapter = new SetAdapter(mContext);
        mSetAdapter.setExplainClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mSetAdapter);
    }


    @Override
    public void onExplainClick(int tag) {
        if (tag == 0) {
            showDialogMessage(R.string.vis_feature, R.string.vis_feature_message);
        }

        if (tag == 1) {
            showDialogMessage(R.string.nir_feature, R.string.nir_feature_message);
        }
    }

    private void showDialogMessage(int title, int message) {
        if (mMessageDialog != null) {
            if (mMessageDialog.isShowing()) {
                mMessageDialog.dismiss();
            }
            mMessageDialog.show();
            mMessageDialog.setData(title, message);
        }
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public TextView defaultValue;
        public TextView explain;
        public TextView explainDetails;
        public SeekBar seekBar;

        public SetViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            defaultValue = itemView.findViewById(R.id.default_value);
            explain = itemView.findViewById(R.id.explain);
            explainDetails = itemView.findViewById(R.id.explain_details);
            seekBar = itemView.findViewById(R.id.seekBar);
        }
    }

    public class SetAdapter extends RecyclerView.Adapter<SetViewHolder> implements View.OnClickListener {

        private Context context;
        private OnExplainClickListener mExplainClickListener;

        public SetAdapter(Context context) {
            this.context = context;
        }

        public void setExplainClickListener(OnExplainClickListener explainClickListener) {
            mExplainClickListener = explainClickListener;
        }

        private int[] txtItem = {
                R.string.defalut_argb_feature,
                R.string.defalut_phone_feature,
                R.string.default_conf,
        };

        @Override
        public void onClick(View v) {
            int id = (int) v.getTag();
            if (mExplainClickListener != null) {
                mExplainClickListener.onExplainClick(id);
            }
        }

        @Override
        public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_setting_quality, parent, false);
            SetViewHolder viewHolder = new SetViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final SetViewHolder holder, final int position) {
            holder.explain.setTag(position);
            holder.explain.setOnClickListener(this);
            holder.defaultValue.setText(txtItem[position]);
            holder.explainDetails.setText(String.valueOf(getValue(position)));
            if (position == 2) {
                holder.seekBar.setProgress((int) (getValue(position) * 20));
            } else {
                holder.seekBar.setProgress((int) getValue(position));
            }
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    switch (position) {
                        case 0:
                            holder.explainDetails.setText(progress + " ");
                            GlobalSet.setFeatureRgbValue((float) progress);
                            break;
                        case 1:
                            holder.explainDetails.setText(progress + " ");
                            GlobalSet.setFeaturePhoneValue((float) progress);
                            break;
                        case 2:
                            holder.explainDetails.setText((float) progress / 20 + " ");
                            GlobalSet.setDetectConfValue((float) progress / 20);
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return txtItem.length;
        }

        public float getValue(int position) {
            return position == 0 ? GlobalSet.getFeatureRgbValue() : position == 1
                    ? GlobalSet.getFeaturePhoneValue() : GlobalSet.getDetectConfValue();
        }
    }
}
