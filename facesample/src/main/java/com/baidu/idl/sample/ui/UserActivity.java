package com.baidu.idl.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.listener.OnItemClickListener;
import com.baidu.idl.sample.manager.UserInfoManager;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.view.CircleImageView;
import com.baidu.idl.sample.view.ProgressDialog;
import com.bumptech.glide.Glide;

import java.util.List;


/**
 * 用户管理界面
 * Created by litonghui on 2018/11/18.
 */

public class UserActivity extends BaseActivity implements View.OnClickListener, OnItemClickListener {
    private static final String TAG = UserActivity.class.getSimpleName();

    private Context mContext;

    private RecyclerView mRecyclerView;
    private FaceAdapter mFaceAdapter;
    private ProgressDialog mProgressDialog;

    private TextView mImportTv;
    private TextView mDeleteTv;
    private TextView mDBImportTv;
    private TextView mAllCheckTv;
    private TextView mNoDataTv;
    private EditText mEditContent;
    private ImageView mImageSearch;
    private RelativeLayout mRelativeDialog;
    private RelativeLayout mRelativeDelete;
    private TextView mTextYes;
    private TextView mTextNo;
    private Button mBtnDelete;
    private Button mBtnCancel;

    private List<Feature> mListFeatureInfo;
    private UserInfoManager.UserInfoListener mUserInfoListener;
    // 判断是否处在批量删除的编辑状态
    private boolean mIsBatchDelete;
    private boolean mIsAllSelected;
    private int mSelectCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_mg);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateDeleteUI(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListFeatureInfo != null) {
            mListFeatureInfo = null;
        }

        if (mContext != null) {
            mContext = null;
        }

        if (mFaceAdapter != null) {
            mFaceAdapter = null;
        }

        if (mUserInfoListener != null) {
            mUserInfoListener = null;
        }
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.face_manager);

        mRecyclerView = findViewById(R.id.recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(layoutManager);
        mFaceAdapter = new FaceAdapter();
        mRecyclerView.setAdapter(mFaceAdapter);

        mImportTv = findViewById(R.id.batch_import_btn);
        mDeleteTv = findViewById(R.id.batch_delete_btn);
        mDBImportTv = findViewById(R.id.db_import_btn);
        mAllCheckTv = findViewById(R.id.check_all_btn);
        mNoDataTv = findViewById(R.id.text_no_data);
        mEditContent = findViewById(R.id.layout_search).findViewById(R.id.title);
        mImageSearch = findViewById(R.id.image_search);
        mRelativeDialog = findViewById(R.id.relative_delete_bg);
        mRelativeDelete = findViewById(R.id.relative_delete);
        mTextYes = findViewById(R.id.text_yes);
        mTextNo = findViewById(R.id.text_no);
        mBtnDelete = findViewById(R.id.button_delete);
        mBtnCancel = findViewById(R.id.button_cancel);

        mImportTv.setOnClickListener(this);
        mDeleteTv.setOnClickListener(this);
        mAllCheckTv.setOnClickListener(this);
        mImageSearch.setOnClickListener(this);
        mFaceAdapter.setItemClickListener(this);
        mDBImportTv.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    private void initData() {
        // View动态适配
        DensityUtil.getAdaptationHeight(35, 400,
                mBtnDelete, mContext);
        DensityUtil.getAdaptationHeight(35, 400,
                mBtnCancel, mContext);
        mUserInfoListener = new UserListener();
        // 初始化数据库
        DBManager.getInstance().init(getApplicationContext());
        // 读取数据库信息
        UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
    }

    /**
     * 更新删除UI
     * @param delete
     */
    private void updateDeleteUI(boolean delete) {
        // 如果打开批量删除功能
        if (delete) {
            // 头像右上角显示多选框
            mFaceAdapter.setShowBox(true);
            mFaceAdapter.notifyDataSetChanged();
            // “批量导入”按钮置灰
            mImportTv.setTextColor(getResources().getColor(R.color.textGrey));
            // 显示全选按钮
            mAllCheckTv.setVisibility(View.VISIBLE);
            // 显示删除布局
            mRelativeDelete.setVisibility(View.VISIBLE);
            mIsBatchDelete = true;
        } else { // 如果关闭批量删除功能
            // 头像右上角隐藏多选框
            mFaceAdapter.setShowBox(false);
            mFaceAdapter.notifyDataSetChanged();
            // “批量导入”按钮置蓝
            mImportTv.setTextColor(getResources().getColor(R.color.textBlue));
            // 隐藏多选按钮
            mAllCheckTv.setVisibility(View.GONE);
            // 隐藏删除布局
            mRelativeDelete.setVisibility(View.GONE);
            // 关闭是否删除对话框
            if (mRelativeDialog.getVisibility() == View.VISIBLE) {
                mRelativeDialog.setVisibility(View.GONE);
            }
            mIsBatchDelete = false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 批量导入按钮
        if (id == R.id.batch_import_btn) {
            if (!mIsBatchDelete) {
                // 复制assets下文件到sdcard目录
                if (!GlobalSet.getIsImportSample()) {
                    FileUtils.copyAssetsFiles2SDCard(this, "ImportSrc",
                            Environment.getExternalStorageDirectory().getPath());
                    GlobalSet.setIsImportSample(true);
                }
                // 进入批量导入页面
                Intent intent = new Intent(this, BatchImportActivity.class);
                startActivityForResult(intent, GlobalSet.IMPORT_REQUEST_CODE);
            }
        }

        // 导入/导出数据库按钮
        if (v == mDBImportTv) {
            Intent intent = new Intent(this, DBImportActivity.class);
            startActivityForResult(intent, GlobalSet.DB_REQUEST_CODE);
        }

        // 搜索按钮
        if (v == mImageSearch) {
            String searchContent = mEditContent.getText().toString().trim();
            // 根据关键字查找人脸库
            UserInfoManager.getInstance().getFeatureInfo(searchContent, mUserInfoListener);
        }

        // “批量删除”按钮
        if (v == mDeleteTv) {
            // 如果当前不是删除状态
            if (!mIsBatchDelete) {
                // 更新打开删除功能UI
                updateDeleteUI(true);
            } else {
                // 更新关闭删除功能UI
                updateDeleteUI(false);
            }
        }

        // 点击“删除”按钮
        if (v == mBtnDelete) {
            if (mSelectCount != 0) {
                // 如果选择要删除的个数不为0，则弹出确认删除框
                mRelativeDelete.setVisibility(View.GONE);
                mRelativeDialog.setVisibility(View.VISIBLE);
                mTextYes.setOnClickListener(this);
                mTextNo.setOnClickListener(this);
            } else {
                updateDeleteUI(false);
            }
        }

        // 取消按钮
        if (v == mBtnCancel) {
            updateDeleteUI(false);
        }

        // 全选按钮
        if (v == mAllCheckTv) {
            if (mIsBatchDelete) {
                if (!mIsAllSelected) { // 全选，所有多选框全部选中
                    for (int i = 0; i < mListFeatureInfo.size(); i++) {
                        mListFeatureInfo.get(i).setChecked(true);
                        mSelectCount = mListFeatureInfo.size();
                        mIsAllSelected = true;
                    }
                } else {  // 取消全选，所有多选框全部取消选中
                    for (int i = 0; i < mListFeatureInfo.size(); i++) {
                        mListFeatureInfo.get(i).setChecked(false);
                        mSelectCount = 0;
                        mIsAllSelected = false;
                    }
                }
                mFaceAdapter.notifyDataSetChanged();
            }
        }

        // 删除对话框“是”按钮
        if (v == mTextYes) {
            // 显示进度对话框
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            // 从数据库中删除
            UserInfoManager.getInstance().batchRemoveFeatureInfo(mListFeatureInfo, mUserInfoListener,
                    mSelectCount);
        }

        // 删除对话框“否”按钮
        if (v == mTextNo) {
            updateDeleteUI(false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        // 如果处在批量删除的编辑状态
        if (mIsBatchDelete) {
            // 如果当前要删除的信息未选中，则选中
            if (!mListFeatureInfo.get(position).isChecked()) {
                mListFeatureInfo.get(position).setChecked(true);
                mSelectCount++;
            } else { // 如果当前要删除的信息选中，则未选中
                mListFeatureInfo.get(position).setChecked(false);
                mSelectCount--;
            }
            mFaceAdapter.notifyDataSetChanged();
        } else { // 如果处在正常显示状态，则进入图片详情页面
            Intent intent = new Intent(mContext, ImageDetailActivity.class);
            intent.putExtra("pic_name", mListFeatureInfo.get(position).getImageName());
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 如果从批量导入页面返回，则刷新数据库
        if (requestCode == GlobalSet.IMPORT_REQUEST_CODE && resultCode == GlobalSet.IMPORT_RESULT_CODE) {
            // 读取数据库信息
            UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
        }
        // 如果从导入/导出数据库页面返回，则刷新数据库
        if (requestCode == GlobalSet.DB_REQUEST_CODE && resultCode == GlobalSet.DB_RESULT_CODE) {
            // 读取数据库信息
            UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
        }
    }

    // 用于返回读取数据库的结果
    private class UserListener extends UserInfoManager.UserInfoListener {

        // 人脸库信息查找成功
        @Override
        public void featureQuerySuccess(final List<Feature> listFeatureInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listFeatureInfo == null || listFeatureInfo.size() == 0) {
                        mNoDataTv.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    } else {
                        mListFeatureInfo = listFeatureInfo;
                        mNoDataTv.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mFaceAdapter.setListData(listFeatureInfo);

                    if (mIsBatchDelete) {
                        // 更新删除UI
                        updateDeleteUI(false);
                        ToastUtils.toast(mContext, "删除成功");
                    } else {
                        mFaceAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        // 人脸库信息查找失败
        @Override
        public void featureQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }

        // 显示删除进度条
        @Override
        public void showDeleteProgressDialog(final float progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null && mContext != null) {
                        mProgressDialog.setProgress(progress);
                    }
                }
            });
        }

        // 删除成功
        @Override
        public void deleteSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    // 读取数据库信息
                    UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
                }
            });

        }
    }

    private static class FaceViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView textView;
        private CircleImageView imageView;
        private CheckBox checkView;

        private FaceViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = itemView.findViewById(R.id.txt);
            imageView = itemView.findViewById(R.id.image);
            checkView = itemView.findViewById(R.id.check_btn);
        }
    }

    public class FaceAdapter extends RecyclerView.Adapter<FaceViewHolder> implements
            View.OnClickListener {
        private List<Feature> mList;
        private boolean mShowBox;
        private OnItemClickListener mItemClickListener;

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        private void setListData(List<Feature> listData) {
            mList = listData;
        }

        private void setShowBox(boolean showBox) {
            mShowBox = showBox;
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public FaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item, parent, false);
            FaceViewHolder viewHolder = new FaceViewHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FaceViewHolder holder, int position) {
            holder.itemView.setTag(position);
            // 是否显示多选按钮
            if (mShowBox) {
                holder.checkView.setVisibility(View.VISIBLE);
                if (mList.get(position).isChecked()) {
                    holder.checkView.setChecked(true);
                } else {
                    holder.checkView.setChecked(false);
                }
            } else {
                holder.checkView.setVisibility(View.GONE);
            }

            // 获取本地SD卡图片路径
            String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                    + "/" + mList.get(position).getCropImageName();
            Glide.with(mContext).load(imgPath).into(holder.imageView);
            holder.textView.setText(mList.get(position).getUserName());
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, (Integer) v.getTag());
            }
        }
    }
}
