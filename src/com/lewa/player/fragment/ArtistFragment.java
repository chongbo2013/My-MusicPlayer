package com.lewa.player.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lewa.player.R;
import com.lewa.player.adapter.LibraryArtistAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.enums.StarCatalog;
import com.lewa.player.helper.ViewHelper;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.model.Artist;
import com.lewa.util.CharacterParser;
import com.lewa.util.PinyinComparator;
import com.lewa.view.MaskEndListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wuzixiu on 11/27/13.
 */
public class ArtistFragment extends BaseFragment implements View.OnClickListener {
    private final static String TAG = ArtistFragment.class.getName();

    private ImageButton mBackBtn;
    private MaskEndListView mArtistLv;
    private TextView mTitle;
    private LibraryArtistAdapter mAdapter;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<Artist> artistList;

    private LibraryListener mLibraryListener;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private StarCatalog mCatalog;

    public ArtistFragment(StarCatalog catalog) {
        this.mCatalog = catalog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_allstar_artist, container, false);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View rootView) {
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mArtistLv = (MaskEndListView) rootView.findViewById(R.id.lv_artist);
        mTitle = (TextView) rootView.findViewById(R.id.tv_title);
        ViewHelper.addTranspantFooter(mArtistLv);
        mAdapter = new LibraryArtistAdapter(this);
        mArtistLv.setAdapter(mAdapter);
        mBackBtn.setOnClickListener(this);
        mArtistLv.setOnTrackListener(mLibraryListener);
        mTitle.setText(mCatalog.value());

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        mArtistLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Artist artist = artistList.get(position);
                mLibraryListener.showAlbumFragment(artist.getId(), artist.getName());
            }
        });
//
//        artistList = filledData(getResources().getStringArray(R.array.artist_list));
//
//        // 根据a-z进行排序源数据
//        Collections.sort(artistList, pinyinComparator);
//        adapter.setData(artistList);
//        mArtistLv.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            artistList = DBService.getInstance().findArtists(null);
            // 根据a-z进行排序源数据
            Collections.sort(artistList, pinyinComparator);

            mAdapter.setData(artistList);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to query artist info: \n" + e.getMessage());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mLibraryListener = (LibraryListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement LibraryListener.");
        }
    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<Artist> filledData(String[] date) {
        List<Artist> mSortList = new ArrayList<Artist>();

        for (int i = 0; i < date.length; i++) {
            Artist artist = new Artist();
            artist.setId((long) i);
            artist.setName(date[i]);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                artist.setInitial(sortString.toUpperCase());
            } else {
                artist.setInitial("#");
            }

            mSortList.add(artist);
        }
        return mSortList;

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<Artist> filterDateList = new ArrayList<Artist>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = artistList;
        } else {
            filterDateList.clear();
            for (Artist sortModel : artistList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        mAdapter.setData(filterDateList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                mLibraryListener.hideArtistFragment();
                break;
            case R.id.bt_play:
                //TODO: pass artist name here.
                mLibraryListener.showAlbumFragment(1l, "");
                break;
        }
    }
}
