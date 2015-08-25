package com.lewa.player.service;

import com.lewa.player.model.Song;

import java.util.HashMap;

/**
 * TURTLE PLAYER
 * <p/>
 * Licensed under MIT & GPL
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * More Information @ www.turtle-player.co.uk
 *
 * @author Simon Honegger (Hoene84)
 */

public abstract class ObservableService implements ServiceAccess
{
	//---------------------------------- Observable

	HashMap<String, PlayerObserver> observers = new HashMap<String, PlayerObserver>();

	protected void notifyTrackChanged(Song song, int lengthInMillis){
		for(PlayerObserver observer : observers.values()){
			observer.trackChanged(song, lengthInMillis);
		}
	}

	protected void notifyStarted(){
		for(PlayerObserver observer : observers.values()){
			observer.started();
		}
	}

	protected void notifyStopped(){
		for(PlayerObserver observer : observers.values()){
			observer.stopped();
		}
	}

	public void addObserver(PlayerObserver observer)
	{
		observers.put(observer.getId(), observer);
	}

	public void removeObserver(Observer observer)
	{
		observers.remove(observer);
	}

	public interface PlayerObserver extends Observer
	{
		void trackChanged(Song song, int lengthInMillis);
		void started();
		void stopped();
	}
}
