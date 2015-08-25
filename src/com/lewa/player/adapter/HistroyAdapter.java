package com.lewa.player.adapter;

import java.util.ArrayList;
import java.util.List;

import com.lewa.Lewa;
import com.lewa.player.adapter.SearchAdapter.ViewHolder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lewa.player.R;
import com.lewa.player.model.HistoryBean;
import com.lewa.player.model.HistoryBean.INFO_TYPE;

public class HistroyAdapter extends BaseAdapter {
	private List<HistoryBean> datas = new ArrayList<HistoryBean>();
	private TYPE type;
	public enum TYPE{
		HISTORY,
		SUGGESTION
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			convertView = Lewa.inflater().inflate(R.layout.histroy_item, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
             HistoryBean bean = datas.get(position);
             
		if(type==TYPE.HISTORY){
			holder.arrow_right.setVisibility(View.GONE);
			holder.history_icon.setVisibility(View.VISIBLE);
		}else{
		       if(bean.getType() == HistoryBean.INFO_TYPE.LOCAL) {
                        holder.arrow_right.setVisibility(View.GONE);
                        
		       } else {
			    holder.arrow_right.setVisibility(View.VISIBLE);
                        
		       }
			holder.history_icon.setVisibility(View.GONE);
		}
		
		holder.search_text.setText(bean.getInfo());
		convertView.setTag(holder);
		return convertView;
	}

    public void setDatas(List<HistoryBean> datas) {
        this.datas = datas;
        this.notifyDataSetChanged();
    }

        public List<HistoryBean> getDatas() {
            return datas;
        }

	public void setType(TYPE type) {
		this.type = type;
	}


	public TYPE getType() {
		return type;
	}



	static class ViewHolder {
		TextView search_text;
		ImageView arrow_right;
		ImageView history_icon;
		public ViewHolder(View view) {
			search_text = (TextView) view.findViewById(R.id.search_text);
			arrow_right = (ImageView) view.findViewById(R.id.arrow_right);
			history_icon = (ImageView) view.findViewById(R.id.history_icon);
		}
	}

}
