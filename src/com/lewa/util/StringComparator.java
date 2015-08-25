package com.lewa.util;

import com.lewa.player.model.Artist;

import java.util.Comparator;

/**
 * 
 * @author xiaanming
 *
 */
public class StringComparator implements Comparator<String> {

	public int compare(String letter1,String letter2) {
		if (letter1.equals("@")
				|| letter2.equals("#")) {
			return -1;
		} else if (letter1.equals("#")
				|| letter2.equals("@")) {
			return 1;
		} else {
			return letter1.compareTo(letter2);
		}
	}

}
