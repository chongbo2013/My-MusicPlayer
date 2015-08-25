package com.lewa.player.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.baidu.music.model.Music;
import com.lewa.Lewa;
import com.lewa.il.MusicInterfaceLayer;
import com.lewa.il.MusicInterfaceLayer.OnGetSuggestionListener;
import com.lewa.il.MusicInterfaceLayer.OnSearchMusicListener;
import com.lewa.player.R;
import com.lewa.player.activity.LibraryActivity;
import com.lewa.player.adapter.HistroyAdapter;
import com.lewa.player.adapter.HistroyAdapter.TYPE;
import com.lewa.player.model.HistoryBean;
import com.lewa.player.model.HistoryBean.INFO_TYPE;
import com.lewa.player.adapter.SearchAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.helper.PlaylistHelper;
import com.lewa.player.listener.LibraryListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Song;
import com.lewa.player.model.SongCollection;
import com.lewa.util.LewaUtils;
import com.lewa.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.lewa.player.online.OnlineLoader;
import java.lang.reflect.Field;

/**
 * Created by wuzixiu on 1/11/14.
 */
public class SearchFragment extends BaseFragment implements View.OnClickListener, 
                                    SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, 
                                    AdapterView.OnItemLongClickListener,    OnSearchMusicListener,
                                    OnGetSuggestionListener,    OnTouchListener {
    private static final String TAG = "SearchFragment";
    private ImageButton mBackBtn;
    private SearchView mSearchEt;
    private ListView mSearchLv;
    ImageView mCoverIv;
    private SearchAdapter mSearchAdapter;
    List<Song> mSongs = null;
    List<Song> mAllSongs = null;
    private LibraryListener mLibraryListener;
    private SearchType type;
    private int PAGENO=1;
    private int PAGESIZE=25;
    private boolean isKeyEmpty;
    private ListView mHistroyLv;
    private HistroyAdapter mHistroyAdapter;
    private List<HistoryBean> histroies;
    private int MAX_HISTROY_SIZE=10;
    private boolean isItemClickSearch;
    private LinearLayout histroy_ll;
    private Button bt_clear;
    private TextView search_result_hint;
    private String filter;
    private Handler handler = new Handler();
    
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MusicInterfaceLayer.getInstance().getSuggestionAsync(Lewa.context(), filter, SearchFragment.this);
        }
    };
  
    public enum SearchType{ //it`s not use!
        ONLINE,
        LOCAL
    }
    
    public static SearchFragment getInstance(SearchType type){
        SearchFragment fragment=new SearchFragment();
        Bundle b =new Bundle();
        b.putString("type", type.toString());
        fragment.setArguments(b);
        return fragment;
    }

    public SearchFragment() {
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        hasBackground = true;
        Bundle b=getArguments();
        type=SearchType.valueOf(b.getString("type"));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(false);
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mBackBtn = (ImageButton) rootView.findViewById(R.id.bt_back);
        mSearchEt = (SearchView) rootView.findViewById(R.id.et_search);
        resetSerachViewStyle();
        mSearchLv = (ListView) rootView.findViewById(R.id.lv_search);
        mHistroyLv = (ListView) rootView.findViewById(R.id.lv_histroy);
        histroy_ll =(LinearLayout) rootView.findViewById(R.id.histroy_ll);
        bt_clear =(Button) rootView.findViewById(R.id.bt_clear);
        
        search_result_hint = (TextView)rootView.findViewById(R.id.search_result_hint);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);
        
        mSearchAdapter = new SearchAdapter();
        mHistroyAdapter=new HistroyAdapter();
        histroies = new ArrayList<HistoryBean>();
        List<String> histroiesList = DBService.getSearchHistroy();
        for(String history : histroiesList) {
            histroies.add(new HistoryBean(history, HistoryBean.INFO_TYPE.HISTORY));
        }
        mHistroyAdapter.setDatas(histroies);
        setAdapterType(TYPE.HISTORY);
        
        mHistroyLv.setAdapter(mHistroyAdapter);
        mHistroyLv.setOnItemClickListener(this);
        mHistroyLv.setOnTouchListener(this);
        mSearchLv.setAdapter(mSearchAdapter);

        bt_clear.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);        
        mSearchLv.setOnItemClickListener(this);
        mSearchLv.setOnItemLongClickListener(this);
        mSearchLv.setOnTouchListener(this);
        mSearchEt.setIconified(false);  
        mSearchEt.onActionViewExpanded();
        mSearchEt.setOnQueryTextListener(this);
        mSearchEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        rootView.setOnClickListener(this);
        return rootView;
    }

    public void resetSerachViewStyle( ) {
        try {
            Field ownField = mSearchEt.getClass().getDeclaredField("mSearchPlate");                 
            ownField.setAccessible(true);  
            View mView = (View) ownField.get(mSearchEt);  
            mView.setBackground(this.getActivity().getResources().getDrawable(R.drawable.se_et_search));  

            ownField = mSearchEt.getClass().getDeclaredField("mQueryTextView");                 
            ownField.setAccessible(true);
            //pr955808 add by wjhu begin
            //to make the cursor more clear
			Class<?> mTextViewClass = ownField.get(mSearchEt).getClass()
					.getSuperclass().getSuperclass().getSuperclass();
			Field mCursorDrawableRes = mTextViewClass
					.getDeclaredField("mCursorDrawableRes");
			mCursorDrawableRes.setAccessible(true);
			mCursorDrawableRes.set(ownField.get(mSearchEt),
					R.drawable.sv_cursor);
			//pr955808 add by wjhu end
            TextView tView = (TextView) ownField.get(mSearchEt);  
            tView.setTextColor(0xffffffff);  //white color
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);
        super.onResume();
        mSearchEt.requestFocus();
        if (mSearchEt.hasFocus() && mLibraryListener.getKeyboardStatus()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(!imm.isActive()) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } else {
            hideKeyboard();
        }
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!isHidden()) {
            if (mSearchEt.hasFocus()) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mLibraryListener = (LibraryListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement LibraryListener.");
            cce.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        hideKeyboard();
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
        hideKeyboard();
        Song song = null;
        switch (adapterView.getId()) {
            case R.id.lv_search:
                song = (Song) view.getTag(R.id.tag_entity);
                List<Song> songs = new ArrayList<Song>(1);
                songs.add(song);
                SongCollection songCollection = new SongCollection(SongCollection.Type.SINGLE, song);
                songCollection.setSongs(songs);
                Lewa.playerServiceConnector().playSongCollection(getActivity(), songCollection, -1);
                break;
            case R.id.lv_histroy:
                HistoryBean bean = (HistoryBean) mHistroyAdapter.getItem(position);
                if(bean == null) {
                break;
                }
                String text =(String)bean.getInfo();

                if( !TextUtils.isEmpty(text)){
                    if(bean.getType() == HistoryBean.INFO_TYPE.ONLINE || bean.getType() == HistoryBean.INFO_TYPE.HISTORY) {
                        histroy_ll.setVisibility(View.GONE);
                        isItemClickSearch=true;
                        mSearchEt.setQuery(text, false);
                        updateHistroies(text, bean.getType());
                        searchLocalMusic(text);
                        MusicInterfaceLayer.getInstance().searchMusicAsync(Lewa.context(), text, PAGENO, PAGESIZE, this);
                    } else if(bean.getType() == HistoryBean.INFO_TYPE.LOCAL){
                        song = (Song)bean.getOwner();
                        if(null == song) {
                            return;
                        }
                        mSearchEt.setQuery(song.getName(), false);
                        updateHistroies(song.getName(), HistoryBean.INFO_TYPE.HISTORY);
                        Lewa.playerServiceConnector().playSongCollection(getActivity(), new SongCollection(SongCollection.Type.SINGLE, song), -1);
                    }
                }
                break;
            default:
                break;
        }
      
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        hideKeyboard();
        Song song = (Song) view.getTag(R.id.tag_entity);
        SongCollection songCollection = new SongCollection(SongCollection.Type.SINGLE, null);
        songCollection.setSongs(mSearchAdapter.getData());
        ((LibraryActivity)getActivity()).setLongClickSong(song);
        mLibraryListener.setSongCollection(songCollection);
        mLibraryListener.showBatchCheckSongFragment();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_back:
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchEt.getWindowToken(), 0);
                mLibraryListener.hideSearchFragment();
                break;
            case R.id.bt_clear:
                histroies.clear();
                setAdapterType(TYPE.HISTORY);
                mHistroyAdapter.notifyDataSetChanged();
                DBService.clearSearchHistroy();
                break;
        }

    }

    public void hideKeyboard() {
        if(mSearchEt!=null&&mSearchEt.getWindowToken()!=null){
	        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(mSearchEt.getWindowToken(), 0);
	        mSearchEt.clearFocus();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
    	if(!TextUtils.isEmpty(query)){
	    	updateHistroies(query, HistoryBean.INFO_TYPE.ONLINE);
	    	MusicInterfaceLayer.getInstance().searchMusicAsync(Lewa.context(), query, PAGENO, PAGESIZE, this);
    	}
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        try {
            mSearchAdapter.setFilter(newText);
            filter = newText;
            if(isItemClickSearch){
                isKeyEmpty=StringUtils.isBlank(newText);
                return true;
            }
            if (StringUtils.isBlank(newText)) {
                mSearchAdapter.setData(new ArrayList<Song>());
                hideSearchResultHint();
                if(type == SearchType.ONLINE){
                    setAdapterType(TYPE.HISTORY);
                    histroy_ll.setVisibility(View.VISIBLE);
                    mHistroyAdapter.setDatas(histroies);
                    //mHistroyAdapter.notifyDataSetChanged();
                }
                isKeyEmpty=true;
            } else {
                isKeyEmpty=false;
                mSongs = DBService.findSongsByName(newText);
                List<HistoryBean> dataList = new ArrayList<HistoryBean>();
                for(Song song : mSongs) {
                    dataList.add(new HistoryBean(song, HistoryBean.INFO_TYPE.LOCAL));
                }
                mHistroyAdapter.setDatas(dataList);
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 200);
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

       public void searchLocalMusic(String songName) {
            try {
                mSongs = DBService.findSongsByName(songName);                
                mSearchAdapter.setData(mSongs);
            }catch (SQLException e) {
                e.printStackTrace();
            }
       }
	@Override
	public void onSearchMusic(List<Music> lists) {
		// TODO Auto-generated method stub
		if(lists!=null&&lists.size()>0&&!isKeyEmpty){

                    List<Song> tmpSongList = new ArrayList<Song>();
                    tmpSongList.addAll(mSearchAdapter.getData());
                    
			if(mSongs==null){
				mSongs=new ArrayList<Song>();
			}else{
				mSongs.clear();
			}

                    
			for(int i = 0;i<lists.size();i++){
				Music music=lists.get(i);
				Song song=PlaylistHelper.songFromMusic(null, music);
                          
				if(null != song && null != song.getArtist() && null == song.getArtist().getId()) {
				    song.getArtist().setId(new Long(-1));	//this for bug 61982
				}
				mSongs.add(song);
			}
			if(histroy_ll.getVisibility()==View.VISIBLE)
				histroy_ll.setVisibility(View.GONE);
                    
                    if(0 < tmpSongList.size()) {
                        mSongs.addAll(0, tmpSongList);
                    }

                    if(null == mSongs || mSongs.size() <= 0) {
                        showSearchResultHint(R.string.no_result);
                    }
			mSearchAdapter.setData(mSongs);
		}
		if(isItemClickSearch)
			isItemClickSearch=false;
	}
	
	public void onSongsDownloaded(){
		if(mSongs!=null){
			DBService.matchSongs(mSongs);
			mSearchAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onGetSuggestion(List<String> datas) {
		// TODO Auto-generated method stub
		if(datas!=null&&!isKeyEmpty){
			if(histroy_ll.getVisibility()==View.GONE) {
			    histroy_ll.setVisibility(View.VISIBLE);
			}
			setAdapterType(TYPE.SUGGESTION);
                    List<HistoryBean> dataList = mHistroyAdapter.getDatas();
                    for(String data : datas) {
                        dataList.add(new HistoryBean(data,HistoryBean.INFO_TYPE.ONLINE));
                    }
                    
			mHistroyAdapter.setDatas(dataList);

                    if(dataList.size()<=0){
                        
			    showSearchResultHint(R.string.no_result);
        		}else{
        			hideSearchResultHint();
        		}  
		}
		
	}
	
	public void updateSearchHistroy(){
		if(histroies!=null&&type==SearchType.ONLINE) {
                    List<String> historyList = new ArrayList<String>();
                    for(HistoryBean bean : histroies) {
                        
                        historyList.add(bean.getInfo());
                        
                    }
			DBService.updateSearchHistroy(historyList);

		}
	}
	
	private void updateHistroies(String text, HistoryBean.INFO_TYPE type){
		if(histroies!=null){
                    HistoryBean tmpBean = null;
                    for(HistoryBean bean : histroies) {
                        if(bean.getInfo().equals(text)) {
                            tmpBean = bean;
                        }
                    }
                    histroies.remove(tmpBean);
                     HistoryBean bean = new HistoryBean(text, type); 
			if(histroies.size()<MAX_HISTROY_SIZE){
				histroies.add(0, bean);
			}else{
				histroies.remove(histroies.size()-1);
				histroies.add(0, bean);
			}
		}
	}
	
	private void setAdapterType(TYPE type){
		if(type==TYPE.HISTORY){
			if(histroies.size()>0){
				bt_clear.setVisibility(View.VISIBLE);
			}else{
				bt_clear.setVisibility(View.GONE);
			}
		}else{
			bt_clear.setVisibility(View.GONE);
		}
		mHistroyAdapter.setType(type);
	}
	
    private void showSearchResultHint(int strId){
        if(!OnlineLoader.isWiFiActive(this.getActivity()) && !OnlineLoader.isNetworkAvailable()) {
            search_result_hint.setText(Lewa.string(R.string.no_network));
        } else {
            search_result_hint.setText(Lewa.string(strId));
        }
        search_result_hint.setVisibility(View.VISIBLE);
    }
    
    private void hideSearchResultHint(){
        search_result_hint.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        // TODO Auto-generated method stub
        hideKeyboard();
        return false;
    }
}
