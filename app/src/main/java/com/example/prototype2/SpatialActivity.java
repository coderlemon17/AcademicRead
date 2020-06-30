package com.example.prototype2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class SpatialActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private FileLoader fileLoader;
    private TextToSpeech textToSpeech;
    private TextView textView;
    private TextView statusTextView;
    private SeekBar seekBar;

    private boolean seekBarOnHold = false;



    //for finger position record
    private float pos_x = -1;
    private float last_pos_x = -1;
    private float pos_y = -1;
    private float last_pos_y = -1;

    //for rotation
    private long start_time;
    private long double_click_time = 500;
    private long delta = 800;
    private float range;
    private float x0;
    private float y0;


    // widget width and height
    int width = -1;
    int height = -1;

    // status
    // 0 initial
    // 1 tmpt
    // 2 move
    // 3 long press
    // 4 book mark
    int status = 0;

    // message
    Handler handler;
    private final int UPDATE_STATUS = 1;
    private final int UPDATE_SEEKBAR = 2;

    // book mark index
    private int book_mark_index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spatial);

        //text2speech
        initTextToSpeech();

        //widget
        getHeightandWidth();
        initRotation();

        //text view
        textView = (TextView) findViewById(R.id.mtextView);
//        statusTextView = (TextView) findViewById(R.id.statusTextView);

        //file loader
        fileLoader = new FileLoader();

        textView.setText(fileLoader.get_text());

    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Toast.makeText(SpatialActivity.this, actionToString(ev.getAction()), Toast.LENGTH_SHORT).show();
//        return super.dispatchTouchEvent(ev);
//    }

    public void reset() {
        start_time = -1;
        pos_x = -1;
        pos_y = -1;
        last_pos_x = -1;
        last_pos_y = -1;
        book_mark_index = 0;
    }


    // Given an action int, returns a string description
    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            pos_x = event.getX();
            pos_y = event.getY();
            start_time = System.currentTimeMillis();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            long end_time = System.currentTimeMillis();
            float x = event.getX(),
                    y = event.getY();
            Log.e("time", String.valueOf(end_time-start_time));
            if (end_time - start_time < 250 && Math.abs(pos_y - y) < height/12) {
                String s = fileLoader.get_text();
                read(s);
                return true;
            }


            if (Math.abs(pos_y - y) > height/12) {
                if (y > pos_y) {
                    fileLoader.line_switch(1);
                } else {
                    fileLoader.line_switch(-1);
                }
                String s = fileLoader.get_text();
                read(s);
                textView.setText(s);
            }
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    //    @Override
//    public boolean onTouchEvent(MotionEvent event) {
////        Toast.makeText(this, "IN", Toast.LENGTH_SHORT);
////        Log.e("E", "In: "+String.valueOf(event.getPointerCount()));
////        Log.e("S", String.valueOf(status));
////        Log.e("A", actionToString(event.getActionMasked()));
//
//        switch (status) {
//            case 0:
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                    long cur_time = System.currentTimeMillis();
//                    if (cur_time - start_time < double_click_time) {
//                        textToSpeech.stop();
//                    } else {
//                        status = 1;
//                        start_time = cur_time;
//                        x0 = event.getX();
//                        y0 = event.getY();
//                    }
//                } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
//                    status = 2;
//                }
//                break;
//            case 1:
//                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
//                    if (Math.abs(event.getY() - y0) > range) {
//                        //bookmark
//                        if (event.getY() > y0) {
//                            String res = fileLoader.add_or_delete_book_marks();
//                            read(res, 1.5f);
//                            status = 0;
//                            start_time = -1;
//                        } else {
//                            read("进入书签模式", 1.5f);
//                            start_time = -1;
//                            book_mark_index = 0;
//                            status = 4;
//                        }
//                    } else if (Math.abs(event.getX() - x0) > range/2) {
//                        long cur_time = System.currentTimeMillis();
//                        if (cur_time - start_time > delta) {
//                            textToSpeech.stop();
//                            status = 3;
//                        } else {
//                            status = 2;
//                        }
//                    }
//                } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
//                    status = 2;
//                } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
//                    status = 0;
//                    String s = fileLoader.get_text();
//                    fileLoader.setPos_char(0);
//                    textView.setText(s);
//                    read(s);
//                    last_pos_x = -1;
//                    last_pos_y = -1;
//                }
//                break;
//            case 2:
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_MOVE:
//                        pos_x = event.getX();
//                        pos_y = event.getY();
//                        if (event.getPointerCount() == 2) {
//                            // dual finger
//                            if (last_pos_x == -1 && last_pos_y == -1) {
//                                last_pos_x = pos_x;
//                                last_pos_y = pos_y;
//                                break;
//                            }
//                            float delta_x =  pos_x - last_pos_x;
//                            float delta_y = pos_y - last_pos_y;
//                            int tmpt = 0;
//                            if (Math.abs(delta_x) > Math.abs(delta_y)) {
//                                int cur_len = fileLoader.get_text().length();
//                                tmpt = (int) (cur_len*delta_x/width);
//                            } else {
//                                int cur_len = fileLoader.get_cur_paragraph_len();
////                                tmpt = (int) (cur_len*delta_y/width);
//                                if (Math.abs(delta_y) > width / 4) {
//                                    tmpt = delta_y > 0 ? 1 : -1;
//                                }
//                            }
////                            Toast.makeText(this, "Tmpt delta: " + String.valueOf(tmpt) +" C " + String.valueOf(event.getPointerCount()), Toast.LENGTH_SHORT).show();
//                            // TODO: continuous switch
//                        }
//                        break;
//                    case MotionEvent.ACTION_POINTER_UP:
//                        pos_x = event.getX(0);
//                        pos_y = event.getY(0);
//                        if (last_pos_x == -1 && last_pos_y == -1) {
//                            break;
//                        }
//                        float delta_x =  pos_x - last_pos_x;
//                        float delta_y = pos_y - last_pos_y;
//                        int tmpt = 0;
//                        if (Math.abs(delta_x) > Math.abs(delta_y)) {
//                            int cur_len = fileLoader.get_text().length();
//                            fileLoader.inner_line_char_switch((int) (2*cur_len*delta_x/width));
//                            tmpt = (int) (cur_len*delta_x/width);
////                        Log.e("Err", "Char");
//                        } else {
//                            int cur_len = fileLoader.get_cur_paragraph_len();
//                            if (Math.abs(delta_y) > width / 4) {
//                                tmpt = delta_y > 0 ? 1 : -1;
//                                fileLoader.line_switch(tmpt);
//                            }
//                        }
//                        String subs = fileLoader.get_cursor_text();
//                        Toast.makeText(this, "Final delta: " + String.valueOf(tmpt), Toast.LENGTH_SHORT).show();
//                        textView.setText(subs);
//                        read(subs);
//                        last_pos_x = -1;
//                        last_pos_y = -1;
//                        pos_x = -1;
//                        pos_y = -1;
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        status = 0;
//                        last_pos_x = -1;
//                        last_pos_y = -1;
//                        pos_x = -1;
//                        pos_y = -1;
//                        break;
//                    default:
//                        break;
//                    }
//                break;
//            case 3:
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_MOVE:
//                        pos_x = event.getX();
//                        pos_y = event.getY();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
//                            String s = fileLoader.get_cursor_text();
//                            read(s);
//                        }
//                        status = 0;
//                        pos_x = -1;
//                        pos_y = -1;
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 4:
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                    if (event.getPointerCount() == 1) {
//                        // single finger
//                        pos_x = event.getX();
//                        pos_y = event.getY();
//                        start_time = System.currentTimeMillis();
////                        Log.e("X,Y", "" + pos_x + " " + pos_y);
//                        x0 = pos_x;
//                        y0 = pos_y;
//                    } else {
//                        x0 = -1;
//                        y0 = -1;
//                    }
//                } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
//                    pos_x = event.getX();
//                    pos_y = event.getY();
//                    Vector<String> m = fileLoader.get_book_marks();
////                    Log.e("M", "" + m.size());
////                    Log.e("X,Y", "" + pos_x + " " + pos_y + " " + event.getPointerCount() + " " + range);
//
//                    if (event.getPointerCount() == 1) {
//                       // single finger
//                       if (x0 == -1 || y0 == -1) {
//                           break;
//                       }
//                       float delta_x =  pos_x - x0;
//                       float delta_y = pos_y - y0;
//
//                       if (Math.abs(delta_y) < range && Math.abs(delta_x) < range) {
//                           // click
//                           if (start_time != -1) {
//                               Vector<String> m1 = fileLoader.get_book_marks();
//                               status = 0;
//                               start_time = -1;
//                               String res = fileLoader.jump_book_marks(m1.elementAt(book_mark_index));
//                               String line = fileLoader.get_cursor_text();
//                               textView.setText(line);
//                               read(res, 1.5f);
//                               break;
//                           }
//                       }
//
//                       if (delta_y > range) {
//                           read("退出书签模式", 1.5f);
//                           status = 0;
//                           pos_x = -1;
//                           pos_y = -1;
//                           last_pos_x = -1;
//                           last_pos_y = -1;
//                           break;
//                       }
//
//                       if (m.size() == 0) {
//                           read("书签为空,已退出书签模式", 1.5f);
//                           status = 0;
//                           pos_x = -1;
//                           pos_y = -1;
//                           last_pos_x = -1;
//                           last_pos_y = -1;
//                           break;
//                       } else {
//                           String res = fileLoader.get_book_marks_info(book_mark_index);
//                           read(res, 1.5f);
//                       }
//
//                       if (Math.abs(delta_y) > range) {
//                           start_time = -1;
//                       } else if ( Math.abs(delta_x) > range) {
//                           start_time = -1;
//                           if (delta_x > 0) {
//                               if (book_mark_index == 0) {
//                                   read("已到第一条书签", 1.5f);
//                               } else {
//                                   book_mark_index -= 1;
//                                   String res = fileLoader.get_book_marks_info(book_mark_index);
//                                   read(res, 1.5f);
//                               }
//                           } else {
//                               if (book_mark_index == m.size() - 1) {
//                                   read("已到最后一条书签", 1.5f);
//                               } else {
//                                   book_mark_index += 1;
//                                   String res = fileLoader.get_book_marks_info(book_mark_index);
//                                   read(res, 1.5f);
//                               }
//                           }
//                       }
//                    }
//
//                    last_pos_x = -1;
//                    last_pos_y = -1;
//                }
//                break;
//            default:
//                status = 0;
//                break;
//        }
//        return true;
//    }

    private void getHeightandWidth() {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        height = point.x;
        width = point.y;
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
        // 音调，值越大越偏女声
        textToSpeech.setPitch(1.0f);
        // 设置语速
        textToSpeech.setSpeechRate(1.0f);
    }

    public void initRotation() {
        range = width / 8;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, String.format("TTS fail to work due to %d", result), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void read(String s, float r) {
        s = s.trim();
        if (textToSpeech != null) {
                textToSpeech.setSpeechRate(r);
            textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void read(String s) {
        s = s.trim();
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void append_read(String s, float r) {
        s = s.trim();
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(r);
            textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
        }
    }
    private void append_read(String s) {
        s = s.trim();
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (textToSpeech != null) {
//            // interrupt TTS
//            textToSpeech.stop();
//            // release resource
//            textToSpeech.shutdown();
//        }
//    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }
}


