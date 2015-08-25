package com.lewa.player;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
public class LockScreenProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        // TODO Auto-generated method stub
        String path=Environment.getExternalStorageDirectory()+"/LEWA/music/artist";
        String cleanArtistName = MusicUtils.buildArtistName(uri.getEncodedPath());
        if (cleanArtistName != null && !cleanArtistName.endsWith(".jpg")) {
            cleanArtistName += ".jpg";
        }

        File file=new File(path+cleanArtistName);
        int imode = 0;  

        if (mode.contains("w")) {  
            imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;  
        }  
        if (mode.contains("r"))  
            imode |= ParcelFileDescriptor.MODE_READ_ONLY;  
        if (mode.contains("+"))  
            imode |= ParcelFileDescriptor.MODE_APPEND;  
        return ParcelFileDescriptor.open(file, imode);
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode)
            throws FileNotFoundException {
        // TODO Auto-generated method stub
        return super.openAssetFile(uri, mode);
    }
    
    

  
    
    

}
