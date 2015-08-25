package com.lewa.player.fragment;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.Lewa;
import com.lewa.player.R;
import com.lewa.player.adapter.PlaylistSongAdapter;
import com.lewa.player.db.DBService;
import com.lewa.player.listener.EditPlaylistListener;
import com.lewa.player.listener.PlayStatusBackgroundListener;
import com.lewa.player.model.Playlist;
import com.lewa.player.model.Song;
import com.lewa.view.ClearEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.widget.CheckBox;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.Editable;
    
/**
 * Created by wuzixiu on 12/13/13.
 */
public class EditPlaylistFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "EditPlaylistFragment"; //.class.getName();
    public static final String ARG_PLAYLIST_ID = "playlistId";
    
    public final String SHARED_PREFERENCE_NAME = "play_list_size";
    public final String KEY_CREATE_NEW_PLAYLIST_INDEX = "key_create_new_playlist_index";

    public String saveReName = null;

    Playlist mPlaylist;

    ClearEditText mNameEt;
    private View mRemoveView;
    ListView mSongLv;
    TextView mTitleTv;
    ImageView mAddCoverBtn;
    View mBackBtn;
    View mAddSongBtn;
    View mDoneBtn;
    ImageView mCoverIv;
    private Toast mToast;
    private boolean isCreatePlaylist = false;
    private PlaylistSongAdapter mSongAdapter;
    private EditPlaylistListener mEditPlaylistListener;

    private final int IMAGE_CAPTURE_REQUEST_CODE = 1;
    private static final int PHOTO_PICKED_CROP_WITH_DATA = 3;
    private static final int PHOTO_PICKED_WITH_DATA = 4;
    private Bitmap mPhoto;
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private String mPlaylistImageDir = Environment.getExternalStorageDirectory() + "/LEWA/music/avatar/";
    private String mPlaylistTmpFile = "temp.jpg";
    private File mCurrentPhotoFile;
    private static int ICON_SIZE = 140;
	
	int create_new_playlist_index = 0;

    public EditPlaylistFragment() {
    }

    public static EditPlaylistFragment newInstance(Long playlistId) {
        EditPlaylistFragment editPlaylistFragment = new EditPlaylistFragment();
        Bundle args = new Bundle();

        if (playlistId != null) {
            args.putLong(ARG_PLAYLIST_ID, playlistId);
        }

        editPlaylistFragment.setArguments(args);

        return editPlaylistFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().invalidateOptionsMenu();
        View rootView = inflater.inflate(R.layout.fragment_edit_playlist, container, false);
        mNameEt = (ClearEditText) rootView.findViewById(R.id.et_name);
        mRemoveView = (View) rootView.findViewById(R.id.bt_remove_song);
        mTitleTv = (TextView) rootView.findViewById(R.id.tv_title);
        mSongLv = (ListView) rootView.findViewById(R.id.lv_song);
        mAddCoverBtn = (ImageView) rootView.findViewById(R.id.bt_add_cover);
        mCoverIv = (ImageView) rootView.findViewById(R.id.iv_cover);

        mNameEt.addTextChangedListener(textWatcher);
        mBackBtn = rootView.findViewById(R.id.bt_back);
        mBackBtn.setOnClickListener(this);
        mAddCoverBtn.setOnClickListener(this);
        mAddSongBtn = rootView.findViewById(R.id.bt_add_song);
        mAddSongBtn.setOnClickListener(this);
        mDoneBtn = rootView.findViewById(R.id.bt_done);
        mDoneBtn.setOnClickListener(this);
        mRemoveView.setOnClickListener(this);

        mSongAdapter = new PlaylistSongAdapter(this, mEditPlaylistListener);
        mSongLv.setAdapter(mSongAdapter);

        mSongLv.setOnItemClickListener(this);
        if (getArguments().getLong(ARG_PLAYLIST_ID) == 0) {
            mTitleTv.setText(getResources().getString(R.string.create_playlist));
            isCreatePlaylist = true;
        } else {
            mTitleTv.setText(getResources().getString(R.string.edit_playlist));
            isCreatePlaylist = false;
        }

        create_new_playlist_index = this.getActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, 
                                    Context.MODE_PRIVATE).getInt(KEY_CREATE_NEW_PLAYLIST_INDEX, 0);
        String content = mNameEt.getText().toString() + create_new_playlist_index;
        mNameEt.setText(content);

        mPlayStatusListener = new PlayStatusBackgroundListener(OnlinePlaylistFragment.class.getName(), mCoverIv);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mEditPlaylistListener = (EditPlaylistListener) activity;
        } catch (ClassCastException cce) {
            Log.e(TAG, "Activity should implement EditPlaylistListener.");
        }
    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.FLAG_PERFORM_NO_CLOSE, R.id.album_appwidget, 1, "删除")
                .setIcon(R.drawable.actionbar_icon_confirm)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
*/
    @Override
    public void onResume() {
        super.onResume();
        Lewa.getAndBlurCurrentCoverUrl(mPlayStatusListener);
        if(mEditPlaylistListener.getSelectedSong().size() > 0) {
            mRemoveView.setEnabled(true);
        } else {
            mRemoveView.setEnabled(false);
        }
        try {
            mPlaylist = DBService.findPlaylist(getArguments().getLong(ARG_PLAYLIST_ID));
            if (mPlaylist != null) {
                if (mPlaylist.getCoverUrl() != null) {
                    File avatarFile = new File(mPlaylist.getCoverUrl());
                    if (avatarFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(mPlaylist.getCoverUrl());
                        mAddCoverBtn.setImageBitmap(bitmap);
                    }
                }
                if (mPlaylist.getName() != null) {
                    mNameEt.setText(mPlaylist.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(null == saveReName) {
            saveReName = mNameEt.getText().toString();
        }

        if(!saveReName.equals(mNameEt.getText().toString())) {
            mNameEt.setText(saveReName);
        }
        
        mSongAdapter.setData(mEditPlaylistListener.getPickedSongs());

        refreshRemoveButton();
    }

    public void onPause() {
        super.onPause();
        saveReName = mNameEt.getText().toString();
    }

    public void refreshRemoveButton() {
        if(mEditPlaylistListener.hasRemoves()) {
            mRemoveView.setEnabled(true);
        } else {
            mRemoveView.setEnabled(false);
        }
    }

    public void refreshList() {
        mSongAdapter.setData(mEditPlaylistListener.getPickedSongs());
        mSongAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Song song = (Song) mSongAdapter.getItem(position);
        CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_select); 
        if(null != mCheckBox) {
            mCheckBox.toggle();
            Song song = (Song) view.getTag(R.id.tag_entity);
            mEditPlaylistListener.remove(song);
            if(mEditPlaylistListener.getSelectedSong().size() > 0) {
                    mRemoveView.setEnabled(true);
            } else {
                mRemoveView.setEnabled(false);
            }
        }
    }

    //pr939694 add by wjhu begin
    //to check if there exist a same name
    private boolean nameHasExisted(String name) {
    	List<Playlist> nameLists = null;
    	try {
    		nameLists = DBService.findPlaylistsForUserDefined();
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	for (int i = 0; i < nameLists.size(); i++) {
    		if (nameLists.get(i).getName().equals(name))
    			return true;
    	}
    	return false;
    }
    
    //to show toast
	private void showToast(String text) {
		if (mToast == null) {
			mToast = Toast.makeText(this.getActivity(), text,
					Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
		}
		mToast.show();
	}
	//pr939694 add by wjhu end
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                mEditPlaylistListener.cancelEdit();
                break;
            case R.id.bt_add_song:
                hideInputMethod();
                mEditPlaylistListener.showPickSongFragment();
                break;
            case R.id.bt_done:
                String name = mNameEt.getText().toString();
                String originName = null;
                if (mPlaylist != null) {
                	originName = mPlaylist.getName();
                }
                if (isCreatePlaylist) {
                	if (nameHasExisted(name)) {
                		//pr939694 modify by wjhu
                    	showToast(this.getActivity()
                				.getResources().getString(R.string.text_name_exist));
                    	break;
                	} 
				} else {
					if (originName != null && name.equals(originName)) {
					} else if (nameHasExisted(name)) {
						showToast(this.getActivity()
                				.getResources().getString(R.string.text_name_exist));
                    	break;
					}
				}
                if(name == null || name.trim().length() == 0) {
                    name = getResources().getString(R.string.default_playlist_name_text);
                    name = name + create_new_playlist_index;
                    this.getActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).
                                    edit().putInt(KEY_CREATE_NEW_PLAYLIST_INDEX, ++create_new_playlist_index).commit();
                }
                if(mPlaylist == null || mPlaylist.getName()  == null) {
                    this.getActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).
                                    edit().putInt(KEY_CREATE_NEW_PLAYLIST_INDEX, ++create_new_playlist_index).commit();
                }
                                    
                mEditPlaylistListener.doEdit(name);
                String successToast = null;
                if(mEditPlaylistListener.getNewPickedNum() > 0) {
                    successToast = getResources().getString(R.string.edit_playlist_success_toast_text);
                    successToast = String.format(successToast, mEditPlaylistListener.getNewPickedNum(), name);
                    Toast.makeText(getActivity(), successToast, Toast.LENGTH_SHORT).show();
                } else if(mEditPlaylistListener.getRemovedNum() > 0) {
                    successToast = getResources().getString(R.string.remove_playlistsong_success_toast_text);
                    successToast = String.format(successToast, mEditPlaylistListener.getRemovedNum(), name);
                    Toast.makeText(getActivity(), successToast, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_add_cover:
                saveReName = mNameEt.getText().toString();
                if (mPlaylist!= null && mPlaylist.getCoverUrl() != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.title_cover_dialog)
                            .setItems(R.array.select_photo_judge, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int position) {
                                    if (position == 0) {
                                        openSelectPhotoDialog();
                                    } else {
                                        File avatarFile = new File(mPlaylist.getCoverUrl());
                                        if (avatarFile.exists()) {
                                            avatarFile.delete();
                                        }
                                        mPlaylist.setCoverUrl(null);
                                        DBService.removeCoverForPlaylist(mPlaylist);
                                        mAddCoverBtn.setImageResource(R.drawable.addcover);
                                    }
                                }
                            }).show();
                } else {
                    openSelectPhotoDialog();
                }
                break;
            case R.id.cb_select:
                Song song = (Song) v.getTag(R.id.tag_entity);
                mEditPlaylistListener.remove(song);
                if(mEditPlaylistListener.getSelectedSong().size() > 0) {
                    mRemoveView.setEnabled(true);
                } else {
                    mRemoveView.setEnabled(false);
                }
                break;
            case R.id.bt_remove_song:
                mEditPlaylistListener.doRemove();
                mSongAdapter.setData(mEditPlaylistListener.getPickedSongs());
                break;
        }
    }

    public void openSelectPhotoDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_cover_dialog)
                .setItems(R.array.select_photo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int position) {
                        if (position == 0) {
                            String state = Environment.getExternalStorageState();
                            if (state.equals(Environment.MEDIA_MOUNTED)) {
                                PHOTO_DIR.mkdirs();
                                mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
                                final Intent intent = getTakePickIntent(mCurrentPhotoFile);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
                            } else {
                                Toast.makeText(getActivity(),
                                        R.string.common_msg_nosdcard, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            openAlbumIntent.addCategory(Intent.CATEGORY_OPENABLE);
                            openAlbumIntent.setType("image/*");
							startActivityForResult(openAlbumIntent, PHOTO_PICKED_WITH_DATA);
                        }
                    }
                }).show();
    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    protected void doCropPhoto(File f) {
        try {

            // Add the image to the media store
            MediaScannerConnection.scanFile(
                    getActivity(),
                    new String[]{f.getAbsolutePath()},
                    new String[]{null},
                    null);

            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(Uri.fromFile(f));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, PHOTO_PICKED_CROP_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ICON_SIZE);
        intent.putExtra("outputY", ICON_SIZE);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        switch (requestCode) {
            case IMAGE_CAPTURE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    doCropPhoto(mCurrentPhotoFile);
                    break;
                }
                break;
            case PHOTO_PICKED_WITH_DATA:
                if (data != null && data.getData() != null){
                    doCropStickerPhoto(data.getData(),false);
                }
                break;
            /*case SELECT_PHOTO_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if(data == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    if (uri != null) {
                        bitmap = BitmapFactory.decodeFile(uri.getPath());
                        mPhoto = bitmap;
                    }
                    if (bitmap == null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            bitmap = (Bitmap) bundle.get("data");
                            mPhoto = bitmap;
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.common_msg_get_photo_failure),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    mAddCoverBtn.setImageBitmap(mPhoto);
                    break;
                }
                break;*/
            case PHOTO_PICKED_CROP_WITH_DATA:{
                if (data != null) {
                    mPhoto = data.getParcelableExtra("data");
                    bitmap = mPhoto;
                    mAddCoverBtn.setImageBitmap(mPhoto);
                } else {
                    // The contact that requested the photo is no longer present.
                    // TODO: Show error message
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.common_msg_get_photo_failure),
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        if (bitmap != null) {
            savePhotoToSDCard(mPlaylistImageDir, mPlaylistTmpFile, bitmap);
            if(mPlaylist != null) {
                File tmpAvatarFile = new File(mPlaylistImageDir + mPlaylistTmpFile);
                String uuid = UUID.randomUUID().toString();
                String absolutePath = null;
                if(tmpAvatarFile.exists()) {
                    File avatarFile = new File(mPlaylistImageDir + uuid + ".jpg");
                    tmpAvatarFile.renameTo(avatarFile);
                    absolutePath = mPlaylistImageDir + uuid + ".jpg";
                }
                mPlaylist.setCoverUrl(absolutePath);
                try {
                    DBService.updatePlaylistWithoutSongs(mPlaylist);
                } catch (SQLException e) {
                }
            }

        }
    }

    private void doCropStickerPhoto(Uri mPhotoUri, boolean addInMediaStore) {
        try {
            if(addInMediaStore) {
                File photoFile = new File(mPhotoUri.toString());
                // Add the image to the media store
                MediaScannerConnection.scanFile(
                        getActivity(),
                        new String[] { photoFile.getAbsolutePath() },
                        new String[] { null },
                        null);
            }
            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(mPhotoUri);
//           mStatus = Status.SUB_ACTIVITY;
            startActivityForResult(intent, PHOTO_PICKED_CROP_WITH_DATA);
        } catch (Exception e) {
        }
    }

    public static void savePhotoToSDCard(String path, String photoName, Bitmap photoBitmap) {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File photoFile = new File(path, photoName); //在指定路径下创建文件
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                            fileOutputStream)) {
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null)
                        fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameEt.getWindowToken(), 0);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        private final int MAX_TEXT_SIZE = 20;
        private Toast mToast = null;
        public void afterTextChanged(Editable s) {

            if(s.toString().length() > MAX_TEXT_SIZE) {
                //Toast.makeText(EditPlaylistFragment.this.getActivity(), R.string.limit_hint_text, Toast.LENGTH_LONG).show();      
                showToast();
                s.delete(mNameEt.getSelectionEnd() - 1, mNameEt.getSelectionEnd());
            }
        }

        public void showToast() {
            if(null == mToast) {
                mToast = Toast.makeText(EditPlaylistFragment.this.getActivity(), R.string.limit_hint_text, Toast.LENGTH_SHORT);
            }
            mToast.show();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

            
        }

    };

}