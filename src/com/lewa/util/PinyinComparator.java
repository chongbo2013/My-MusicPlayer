package com.lewa.util;

import com.lewa.player.model.Artist;
import com.lewa.player.model.BaseModel;

import java.util.Comparator;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<BaseModel> {

	public int compare(BaseModel o1, BaseModel o2) {
		if (o1.getInitial().equals("@")
				|| o2.getInitial().equals("#")) {
			return -1;
		} else if (o1.getInitial().equals("#")
				|| o2.getInitial().equals("@")) {
			return 1;
		} else {
			return o1.getInitial().compareTo(o2.getInitial());
		}
	}

}
