package com.lewa.view.lyric;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.lewa.Lewa;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.R;

import java.util.List;
/**
 * Created by wuzixiu on 4/7/14.
 */
public class LyricAdapter extends BaseAdapter {
    private static final String TAG = LyricAdapter.class.getName();
    private List<Sentence> sentences;
    private int selection = -1;
    private Context mContext = null;
    
    public LyricAdapter() {
    	
    }
    
    public LyricAdapter(Context mContext) {
    	this.mContext = mContext;
    }

    public void setLyric(Lyric lyric) {
        if (lyric != null) {
            sentences = lyric.list;
        }

        selection = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sentences == null ? 0 : sentences.size();
    }

    @Override
    public Object getItem(int position) {
        return sentences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Lewa.inflater().inflate(R.layout.item_lyric, null);
            viewHolder = new ViewHolder(convertView);
            viewHolder.sentenceBtn.setOnClickListener(mClickListener);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Sentence sentence = sentences.get(position);

        viewHolder.sentenceTv.setText(sentence.getContent());
        viewHolder.sentenceBtn.setText(sentence.getContent());

//        if (position == selection) {
//            viewHolder.sentenceTv.setTextColor(Lewa.resources().getColor(R.color.blue_text));
//        } else {
//            viewHolder.sentenceTv.setTextColor(Lewa.resources().getColor(R.color.se_lyric));
//        }
        if (position == selection) {
            viewHolder.sentenceBtn.setVisibility(View.GONE);
            viewHolder.sentenceTv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sentenceBtn.setVisibility(View.VISIBLE);
            viewHolder.sentenceTv.setVisibility(View.GONE);
        }

        convertView.setTag(R.id.tag_entity, sentence);
        viewHolder.sentenceBtn.setTag(sentence);
        convertView.setTag(viewHolder);

        return convertView;
    }

    public void setTime(long time) {
        if (sentences == null || sentences.size() == 0) {
            return;
        }

        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            if (sentence.isInTime(time)) {
                if (i != selection) {
                    selection = i;
                    notifyDataSetChanged();
                }

                return;
            }
        }
    }

    static class ViewHolder {
        Button sentenceTv;
        Button sentenceBtn;

        public ViewHolder(View view) {
            sentenceTv = (Button) view.findViewById(R.id.bt_focused_lyric_sentence);
            sentenceBtn = (Button) view.findViewById(R.id.bt_lyric_sentence);
        }
    }

    public int getSelection() {
        return selection;
    }

    public void release() {
        sentences = null;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Sentence sentence = (Sentence) v.getTag();
            
            if (sentence != null) {
                Lewa.playerServiceConnector().seek(sentence.getFromTime());
            }
            //for lockscreen start
            Intent i = new Intent("com.lewa.player.refreshprogress");
            i.putExtra("duration", Lewa.playerServiceConnector().duration());
    		i.putExtra("position", sentence.getFromTime());
    		i.putExtra("time_stamp", System.currentTimeMillis());
    		i.putExtra(MediaPlaybackService.EXTRA_IS_PLAYING, Lewa.playerServiceConnector().isPlaying());
    		mContext.sendBroadcast(i);
            //for lockscreen end
            Log.i(TAG, "Lyric clicked.");
        }
    };
}
