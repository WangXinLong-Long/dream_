package com.alading.dream.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.Observer;

import com.alading.dream.ApiResponse;
import com.alading.dream.ApiService;
import com.alading.dream.JsonCallback;
import com.alading.dream.R;
import com.alading.dream.databinding.LayoutCommentDialogBinding;
import com.alading.dream.model.Comment;
import com.alading.dream.ui.login.UserManager;
import com.alading.dream.ui.publish.CaptureActivity;
import com.alading.libcommon.dialog.LoadingDialog;
import com.alading.libcommon.global.AppGlobals;
import com.alading.libcommon.utils.FileUploadManager;
import com.alading.libcommon.utils.FileUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static android.content.ContentValues.TAG;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {
    LayoutCommentDialogBinding mBinding;
    private static final String KEY_ITEM_ID = "key_item_id";
    private commentAddListener mListener;
    private long itemId;
    private String filePath;
    private int width, height;
    private boolean isVideo;
    private String coverUrl;
    private String fileUrl;
    private LoadingDialog loadingDialog;

    public static CommentDialog newInstance(long itemId) {

        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(0);

        mBinding = LayoutCommentDialogBinding.inflate(inflater, ((ViewGroup) window.findViewById(android.R.id.content)), false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        this.itemId = getArguments().getLong(KEY_ITEM_ID);


        return mBinding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.comment_send) {
            publishComment();
        } else if (v.getId() == R.id.comment_video) {
            CaptureActivity.startActivityForResult(getActivity());
        } else if (v.getId() == R.id.comment_delete) {
            filePath = null;
            isVideo = false;
            width = 0;
            height = 0;
            mBinding.commentCover.setImageDrawable(null);
            mBinding.commentExtLayout.setVisibility(View.GONE);

            mBinding.commentVideo.setEnabled(true);
            mBinding.commentVideo.setAlpha(255);
        }
    }

    private void publishComment() {

        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }

        if (isVideo && !TextUtils.isEmpty(filePath)) {
            FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFile(coverPath, filePath);
                }
            });
        } else if (!TextUtils.isEmpty(filePath)) {
            uploadFile(null, filePath);
        } else {
            publish();
        }
    }

    private void publish() {
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .addParam("video_url", isVideo ? fileUrl : null)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败:" + response.msg);
                        dismissLoadingDialog();
                    }
                });
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (mListener != null) {
                mListener.onAddComment(body);
            }
            dismiss();
        });
    }

    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener) {

        mListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(filePath);
                if (isVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(80);
        }
    }

    private void uploadFile(String coverPath, String filePath) {
        //AtomicInteger, CountDownLatch, CyclicBarrier
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <= 0) {
                        if (!TextUtils.isEmpty(fileUrl)
                                && !TextUtils.isEmpty(coverUrl)
                        ) {
                            publish();
                        }else {
                            dismissLoadingDialog();
                            showToast(getString(R.string.file_upload_failed));
                        }
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int remain = count.decrementAndGet();
                fileUrl = FileUploadManager.upload(filePath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            }
        });

    }


    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.upload_text));
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            //dismissLoadingDialog  的调用可能会出现在异步线程调用
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                });
            } else if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }
}