package thesis.hoa.myo;

import java.util.HashMap;

/**
 * Created by Hoa on 5/5/2016.
 */
public class GestureInfo {
    public static final int NUMBER_1 = R.drawable.number_1;
    public static final int NUMBER_2 = R.drawable.number_2;
    public static final int NUMBER_3 = R.drawable.number_3;
    public static final int NUMBER_4 = R.drawable.number_4;
    public static final int NUMBER_5 = R.drawable.number_5;
    public static final int TAPPING_1 = R.drawable.tap_1_index_finger;
    public static final int TAPPING_2 = R.drawable.tap_2_middle_finger;
    public static final int TAPPING_3 = R.drawable.tap_3_ring_finger;
    public static final int TAPPING_4 = R.drawable.tap_4_little_finger;
    public static final int WRIST = R.drawable.wrist;
    public static final int INIT_PICTURE = R.drawable.init_picture;

    public static final HashMap<Integer, GestureInfo> gestureName = new HashMap<Integer, GestureInfo>();
    static{
        gestureName.put(1, new GestureInfo(NUMBER_1, "Number 1"));
        gestureName.put(2, new GestureInfo(NUMBER_2, "Number 2"));
        gestureName.put(3, new GestureInfo(NUMBER_3, "Number 1"));
        gestureName.put(4, new GestureInfo(NUMBER_4, "Number 1"));
        gestureName.put(5, new GestureInfo(NUMBER_5, "Number 5"));
        gestureName.put(6, new GestureInfo(TAPPING_1, "Tapping index finger"));
        gestureName.put(7, new GestureInfo(TAPPING_2, "Tapping middle finger"));
        gestureName.put(8, new GestureInfo(TAPPING_3, "Tapping ring finger"));
        gestureName.put(9, new GestureInfo(TAPPING_4, "Tapping little finger"));
        gestureName.put(15, new GestureInfo(WRIST, "Whirling wrist"));
    };

    public static boolean checkGesture(int code){
        return gestureName.containsKey(code);
    }

    public int pic;
    public String name;
    public GestureInfo(int pic, String name){
        this.pic = pic;
        this.name = name;
    }
}
