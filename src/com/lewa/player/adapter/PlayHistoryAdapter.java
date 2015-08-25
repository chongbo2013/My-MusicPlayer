package com.lewa.player.adapter;

import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.volley.toolbox.ImageLoader;
import com.lewa.Lewa;
import com.lewa.kit.MyVolley;
import com.lewa.player.R;
import com.lewa.player.model.SongCollection;
import com.lewa.util.StringUtils;
import com.lewa.view.MaskImage.MaskImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayHistoryAdapter extends PagerAdapter {

    private static final String TAG = PlayHistoryAdapter.class.getName();

    private List<SongCollection> mPlayHistories = null;
    private Map<Integer, View> mViews = new HashMap<Integer, View>();
    private ViewType mViewType;

    private OnClickListener mClickListener;
    private int mCurrentPage = 0;

    public void setCurrentPage(int mCurrentPage) {
        this.mCurrentPage = mCurrentPage;
    }


    public int getCurrentPage() {
        return mCurrentPage;
    }

    public PlayHistoryAdapter(ViewType viewType, OnClickListener clickListener) {
        this.mViewType = viewType;
        this.mClickListener = clickListener;
    }

    @Override
    public int getCount() {
        if (mPlayHistories == null) return 0;

        return mPlayHistories.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews.get(position));//删除页卡
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {  //这个方法用来实例化页卡
        View view = mViews.get(position);

        Log.i(TAG, "instantiate item: " + position);

        if (view == null) {
            view = Lewa.inflater().inflate(R.layout.item_playlist_cover, null);

            //TODO: replace image here
            container.addView(view, 0);//添加页卡
            mViews.put(position, view);
        } else {
            container.addView(view, 0);//添加页卡
        }

        view.setTag(position);

        MaskImageView iv = (MaskImageView) view.findViewById(R.id.iv);
        ImageButton playBtn = (ImageButton) view.findViewById(R.id.bt_playlist_play);
        if (position == mCurrentPage) {
            iv.setLayoutSize(Lewa.resources().getDimensionPixelSize(R.dimen.playlist_cover_large_size));
            view.findViewById(R.id.iv_highlight).setVisibility(View.VISIBLE);
            view.findViewById(R.id.llo2).setVisibility(View.VISIBLE);

            if (Lewa.getPlayStatus().isPlaying()) {
                playBtn.setImageResource(R.drawable.se_btn_pause);
                view.setTag(R.id.tag_type, true);
            } else {
                playBtn.setImageResource(R.drawable.se_btn_play);
                view.setTag(R.id.tag_type, false);
            }
        } else {
            iv.setLayoutSize(Lewa.resources().getDimensionPixelSize(R.dimen.playlist_cover_small_size));
            iv.setMaskColor(Lewa.resources().getColor(R.color.a40_black));
            view.setTag(R.id.tag_type, false);
        }
        SongCollection playHistory = mPlayHistories.get(position);
        SongCollection playingSongCollection = Lewa.getPlayingCollection();

        if (playHistory != null) {
            String coverUrl = playHistory.getCoverUrl();
            Log.i(TAG, "Play history:" + playHistory.getName() + ", " + playHistory.getCoverUrl());
            if (!StringUtils.isBlank(coverUrl)) {
                if (!coverUrl.startsWith("http")) {
                    Bitmap bitmap = Lewa.getLocalImage(coverUrl);
                    if (bitmap != null) {
                        iv.setImageBitmap(bitmap);
                    }
                } else {
                    MyVolley.getImage(coverUrl, ImageLoader.getImageListener(iv, R.drawable.cover, R.drawable.cover));
                }
            }

//        View playBtn = view.findViewById(R.id.bt_playlist_play);
            view.setTag(R.id.tag_entity, playHistory);
            view.setOnClickListener(mClickListener);
        }

        return view;
    }

    public View getView(int position) {
        return mViews.get(position);
    }

    public void setData(List<SongCollection> playHistories) {
        this.mPlayHistories = playHistories;
        notifyDataSetChanged();
    }

    public enum ViewType {
        PORTRAIT, LANDSCAPE
    }
}
