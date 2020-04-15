package com.alading.dream.ui.home;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alading.dream.ApiResponse;
import com.alading.dream.ApiService;
import com.alading.dream.JsonCallback;
import com.alading.dream.model.Comment;
import com.alading.dream.model.Feed;
import com.alading.dream.model.User;
import com.alading.dream.ui.login.UserManager;
import com.alading.dream.ui.share.ShareDialog;
import com.alading.libcommon.global.AppGlobals;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class InteractionPresenter {

    private static final String URL_TOGGLE_FEED_LIK = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_SHARE = "/ugc/increaseShareCount";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> login = UserManager.get().login(AppGlobals.getApplication());
            login.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedLikeInternal(feed);
                    }
                    login.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedLikeInternal(feed);
    }

    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIK)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        super.onSuccess(response);
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse response) {
                        super.onError(response);
                    }

                    @Override
                    public void onCacheSuccess(ApiResponse response) {
                        super.onCacheSuccess(response);
                    }
                });
    }

    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> login = UserManager.get().login(AppGlobals.getApplication());
            login.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedDissInternal(feed);
                    }
                    login.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedDissInternal(feed);
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        super.onSuccess(response);
                        if (response.body != null) {
                            boolean hasDiss = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasdiss(hasDiss);
                        }
                    }
                });
    }

    public static void openShare(LifecycleOwner context, Feed feed) {
        String url = "http://h5.aliyun.ppjoke.com/item/%s?timestamp=%s&user_id=%s";
        String format = String.format(url, feed.itemId, new Date().getTime(), UserManager.get().getUserId());

        ShareDialog shareDialog = new ShareDialog(((Context) context));
        shareDialog.setShareContent(format);
        shareDialog.setShareItemClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiService.get(URL_SHARE).addParam("itemId", feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                super.onSuccess(response);
                                if (response.body != null) {
                                    int count = response.body.getIntValue("count");
                                    feed.getUgc().setShareCount(count);
                                }
                            }
                        });
            }
        });
        shareDialog.show();
    }


    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            liveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    liveData.removeObserver(this);
                    if (user != null) {
                        toggleCommentLikeInternal(comment);
                    }
                }
            });
            return;
        }
        toggleCommentLikeInternal(comment);
    }

    private static void toggleCommentLikeInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKE).addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        super.onSuccess(response);

                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }
                });
    }
}
