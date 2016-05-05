package thesis.hoa.myo;

import android.util.Log;

/**
 * Created by Hoa on 5/3/2016.
 */
public class GestureDetectManager {
    private String TAG = "HOA: GestureDetectManager";
    private static GestureDetectManager ourInstance = null;

    public static GestureDetectManager getInstance() {
        if(ourInstance==null)
            ourInstance = new GestureDetectManager();
        return ourInstance;
    }

    private static Object lock = new Object();

    public enum State{
        Recording, Detecting, Done, None
    }

    private StringBuffer gesture = null;

    public State getState() {
        synchronized (lock) {
            return state;
        }
    }

    public synchronized void setState(State state) {
        Log.d("State", state.toString());
        synchronized (lock) {
            this.state = state;

        }
        notify();
    }

    private State state;

    private GestureDetectManager() {
        state = State.None;
    }

    public void record(String value){
//        Log.d(TAG, "record");
        if(gesture == null) {
            gesture = new StringBuffer("s1,s2,s3,s4,s5,s6,s7,s8\n");
        }
        gesture.append(value);
    }

    public String sendData(){
        String result = gesture.toString();
        gesture = null;
        return result;
    }

    public void event(String event){
        if(getState()==State.Recording){
//            Log.d("Gesture Detect", "Manager Recording");
            record(event);
        }
    }
}
