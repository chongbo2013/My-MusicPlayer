/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.view.lyric;

import com.lewa.player.MusicUtils;
import com.lewa.player.helper.EncodingDetect;
import com.lewa.util.LewaUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lyric implements Serializable {

	private String HOME = "/sdcard/UmilePlayer/music/";
	private static Logger log = Logger.getLogger(Lyric.class.getName());
	private int width;
	private int height;
	private long time;
	private long tempTime;
	public List<Sentence> list = new ArrayList<Sentence>();
	private boolean isMoving;
	private int currentIndex;
	private boolean initDone;
	private transient PlayListItem info;
	private transient File file;
	private boolean enabled = true;
	private long during = Integer.MAX_VALUE;
	private int offset;
	private long mTotalTime;
	
	private static final Pattern pattern = Pattern
			.compile("(?<=\\[).*?(?=\\])");
	private static final String TAG = "Lyric";
    private static EncodingDetect encodingDetect;

	
	public Lyric(final PlayListItem info) {
		this.offset = info.getOffset();
		this.info = info;
		this.file = info.getLyricFile();

		if (file != null && file.exists()) {

			init(file);
			initDone = true;
			return;
		} else {
			
			new Thread() {

				public void run() {
					doInit(info);
					initDone = true;
				}
			}.start();
		}
	}

	
	public Lyric(File file, PlayListItem info, long totalTime) {
		this.offset = info.getOffset();
		this.file = file;
		this.info = info;
		this.mTotalTime = totalTime;
		init(file);
		initDone = true;
	}


	public Lyric(String lyric, PlayListItem info) {
		this.offset = info.getOffset();
		this.info = info;
		this.init(lyric);
		initDone = true;
	}

	private void doInit(PlayListItem info) {
		init(info);

		Sentence temp = null;
		
		if (list.size() == 1) {
			temp = list.remove(0);
			String lyric = "";
			if (lyric != null) {
				init(lyric);
				// saveLyric(lyric, info);
			} else {

			}
			list.add(temp);
		}
	}

	private void saveLyric(String lyric, PlayListItem info) {
		try {
			
			String name = info.getFormattedName() + ".lrc";
			File dir = new File(HOME, "Lyrics" + File.separator);
			// File dir = Config.getConfig().getSaveLyricDir();
			dir.mkdirs();
			file = new File(dir, name);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "GBK"));
			bw.write(lyric);
			bw.close();
			info.setLyricFile(file);

		} catch (Exception exe) {

		}
	}


	public void setEnabled(boolean b) {
		this.enabled = b;
	}


	public File getLyricFile() {
		return file;
	}

	
	public void adjustTime(int time) {
	
		if (list.size() == 1) {
			return;
		}
		offset += time;
		info.setOffset(offset);
	}


	private File getMathedLyricFile(File dir, PlayListItem info) {
		File matched = null;
		File[] fs = dir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".lrc");
			}
		});
		for (File f : fs) {
			
			if (matchAll(info, f) || matchSongName(info, f)) {
				matched = f;
				break;
			}
		}
		return matched;
	}

	
	private void init(PlayListItem info) {
		File matched = null;
		
		File dir = new File(HOME, "Lyrics" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
			matched = getMathedLyricFile(dir, info);
		}

		if (matched != null && matched.exists()) {
			info.setLyricFile(matched);
			file = matched;
			init(matched);
		} else {
			init("");
		}
	}


	private void init(File file) {
		BufferedReader br = null;
		try {
            InputStream ios = new FileInputStream(file);
            byte[] b = new byte[3];
            ios.read(b);
            ios.close();
            if(encodingDetect==null)
                encodingDetect = new EncodingDetect();
            String coding=encodingDetect.getJavaEncode(file);
            if(coding.equalsIgnoreCase("UTF-8")){
                br = new BufferedReader(new UnicodeReader(new FileInputStream(file), Charset.defaultCharset().name()));
                }else{
                    br = new BufferedReader(new InputStreamReader(
                          new FileInputStream(file), "GBK"));
                }
			StringBuilder sb = new StringBuilder();
			String temp = null;
			while ((temp = br.readLine()) != null) {
				sb.append(temp).append("\n");
			}
			init(sb.toString());
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);

		} finally {
			try {
				br.close();
			} catch (Exception ex) {
				Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
	}


	private boolean matchAll(PlayListItem info, File file) {
		String name = info.getFormattedName();
		String fn = file.getName()
				.substring(0, file.getName().lastIndexOf(""));
		if (name.equals(fn)) {
			return true;
		} else {
			return false;
		}
	}


	private boolean matchSongName(PlayListItem info, File file) {
		String name = info.getFormattedName();
		String rn = file.getName()
				.substring(0, file.getName().lastIndexOf(""));
		if (name.equalsIgnoreCase(rn) || info.getTitle().equalsIgnoreCase(rn)) {
			return true;
		} else {
			return false;
		}
	}

	private void init(String content) {
		
		if (content == null || content.trim().equals("")) {
			list.add(new Sentence(info.getFormattedName(), Integer.MIN_VALUE,
					Integer.MAX_VALUE));
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new StringReader(content));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				parseLine(temp.trim());
			}
			br.close();
			Collections.sort(list, new Comparator<Sentence>() {

				public int compare(Sentence o1, Sentence o2) {
					return (int) (o1.getFromTime() - o2.getFromTime());
				}
			});
			if (list.size() == 0) {
				list.add(new Sentence(info.getFormattedName(), 0,
						Integer.MAX_VALUE));
				return;
			} 
			/*
			 * fix bug 44567,lyric always show repeat title
			 */
			//			else {
			//				Sentence first = list.get(0);
			//				list.add(
			//						0,
			//						new Sentence(info.getFormattedName(), 0, first
			//								.getFromTime()));
			//			}

			int size = list.size();
			for (int i = 0; i < size; i++) {
				Sentence next = null;
				if (i + 1 < size) {
					next = list.get(i + 1);
				}
				Sentence now = list.get(i);
				if (next != null) {
					now.setToTime(next.getFromTime() - 1);
				}
			}
			if (list.size() == 1) {
				list.get(0).setToTime(Integer.MAX_VALUE);
			} else {
				Sentence last = list.get(list.size() - 1);
				last.setToTime(mTotalTime);
			}
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private int parseOffset(String str) {
		String[] ss = str.split("\\:");
		if (ss.length == 2) {
			if (ss[0].equalsIgnoreCase("offset")) {
				int os = Integer.parseInt(ss[1]);
				return os;
			} else {
				return Integer.MAX_VALUE;
			}
		} else {
			return Integer.MAX_VALUE;
		}
	}

	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		Matcher matcher = pattern.matcher(line);
		List<String> temp = new ArrayList<String>();
		int lastIndex = -1;
		int lastLength = -1;
		while (matcher.find()) {
			String s = matcher.group();
			int index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				String content = line.substring(lastIndex + lastLength + 2,
						index);
				for (String str : temp) {
					long t = parseTime(str);
					if (t != -1) {
					    MusicUtils.isLricFile=true;
						list.add(new Sentence(content, t));
					}
				}
				temp.clear();
			}
			temp.add(s);
			lastIndex = index;
			lastLength = s.length();
		}
		if (temp.isEmpty()) {
			return;
		}
		try {
			int length = lastLength + 2 + lastIndex;
			String content = line.substring(length > line.length() ? line
					.length() : length);
			if (content.equals("") && offset == 0) {
				for (String s : temp) {
					int of = parseOffset(s);
					if (of != Integer.MAX_VALUE) {
						offset = of;
						info.setOffset(offset);
						break;
					}
				}
				return;
			}
			for (String s : temp) {
				long t = parseTime(s);
				if (t != -1) {
					list.add(new Sentence(content, t));
					MusicUtils.isLricFile=true;
				}
			}
		} catch (Exception exe) {
		}
	}

	private long parseTime(String time) {
		String[] ss = time.split("\\:|\\.");
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {
			try {
				if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
					offset = Integer.parseInt(ss[1]);
					info.setOffset(offset);
					return -1;
				}
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				if (min < 0 || sec < 0 || sec >= 60) {
					throw new RuntimeException("���ֲ��Ϸ�!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L);
				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else if (ss.length == 3) {
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				int mm = Integer.parseInt(ss[2]);
				if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
					throw new RuntimeException("���ֲ��Ϸ�!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L + mm *
				// 10);
				return (min * 60 + sec) * 1000L + mm * 10;
			} catch (Exception exe) {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setTime(long time) {
		if (!isMoving) {
			tempTime = this.time = time + offset;
		}
	}

	public boolean isInitDone() {
		return initDone;
	}

	int getNowSentenceIndex(long t) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isInTime(t)) {
				return i;
			}
		}
		return -1;
	}

	public boolean canMove() {
		return list.size() > 1 && enabled;
	}

	public long getTime() {
		return tempTime;
	}

	private void checkTempTime() {
		if (tempTime < 0) {
			tempTime = 0;
		} else if (tempTime > during) {
			tempTime = during;
		}
	}
	public void startMove() {
		isMoving = true;
	}

	public void stopMove() {
		isMoving = false;
	}
	
	public void release(){
	    if(encodingDetect!=null){
    	    encodingDetect.release();
    	    encodingDetect=null;
	    }
	}
}
