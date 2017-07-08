package pc.lifecounter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import static pc.lifecounter.R.id.player1Total;
import static pc.lifecounter.R.id.player2Total;

public class MainActivity extends AppCompatActivity {

    private int startLife = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.player2).setRotation(180);

        Button p1Plus = (Button) findViewById(R.id.p1Plus);
        Button p1Minus = (Button) findViewById(R.id.p1Minus);

        Button p2Plus = (Button) findViewById(R.id.p2Plus);
        Button p2Minus = (Button) findViewById(R.id.p2Minus);

        initButtonListeners();

//        setButtonListener(p1Minus, (TextView) findViewById(player1Total),
//                (LifeRing) findViewById(R.id.player1Ring), -1);
//        setButtonListener(p1Plus,  (TextView) findViewById(player1Total),
//                (LifeRing) findViewById(R.id.player1Ring), 1);
//        setButtonListener(p2Minus, (TextView) findViewById(player2Total),
//                (LifeRing) findViewById(R.id.player2Ring), -1);
//        setButtonListener(p2Plus,  (TextView) findViewById(player2Total),
//                (LifeRing) findViewById(R.id.player2Ring), 1);
    }

    private int getTotal(TextView tv) {
        System.out.println("" + Integer.parseInt(tv.getText().toString()));
        return Integer.parseInt(tv.getText().toString());
    }

    private void setTotal(TextView tv, int newTotal) {

        if (newTotal > 0) {
            tv.setText("" + newTotal);
        } else {
            tv.setText("" + 0);
        }
    }

    private void reset(int start) {
        setTotal((TextView) findViewById(R.id.player1Total), start);
        setTotal((TextView) findViewById(R.id.player2Total), start);
        ((LifeRing) findViewById(R.id.player1Ring)).setStart(start);
        ((LifeRing) findViewById(R.id.player2Ring)).setStart(start);
    }

    public void initButtonListeners() {
        setButtonListener((Button) findViewById(R.id.p1Minus),
                (TextView) findViewById(player1Total),
                (LifeRing) findViewById(R.id.player1Ring), -1);
        setButtonListener((Button) findViewById(R.id.p1Plus),
                (TextView) findViewById(player1Total),
                (LifeRing) findViewById(R.id.player1Ring), 1);
        setButtonListener((Button) findViewById(R.id.p2Minus),
                (TextView) findViewById(player2Total),
                (LifeRing) findViewById(R.id.player2Ring), -1);
        setButtonListener((Button) findViewById(R.id.p2Plus),
                (TextView) findViewById(player2Total),
                (LifeRing) findViewById(R.id.player2Ring), 1);
    }

    public void setButtonListener(Button b, TextView tv, LifeRing ring, int type) {
        final TextView textView = tv;
        final LifeRing lifeRing = ring;
        final int buttonType = type;

        b.setOnTouchListener(new View.OnTouchListener() {
            boolean buttonHeld = false;
            long initTouch;
            long touchTime = 0;
            Timer t = new Timer();


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                initTouch = System.currentTimeMillis();
                t.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lifeRing.invalidate();
                                if(buttonHeld && touchTime < System.currentTimeMillis() - 500) {
                                    int total = getTotal(textView);
                                    System.out.println(total);
                                    if (buttonType <= 0) {
                                        total -= 5;
                                    } else {
                                        total += 5;
                                    }
                                    if (total < 0) {
                                        total = 0;
                                    }
                                    setTotal(textView, total);
                                    lifeRing.setLife(total);
                                    touchTime += 500;
                                }
                            }
                        });
                    }
                }, 0, 100);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonHeld = true;
                    touchTime = System.currentTimeMillis();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonHeld = false;
                    if (System.currentTimeMillis() - touchTime < 500 &&
                            System.currentTimeMillis() - initTouch < 500) {
                        int total = getTotal(textView);
                        if (buttonType <= 0) {
                            total --;
                        } else {
                            total ++;
                        }
                        if (total < 0) {
                            total = 0;
                        }
                        setTotal(textView, total);
                        lifeRing.setLife(total);
                    }
                    t.purge();
                    //t.cancel();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem mode = menu.findItem(R.id.multiplayer);
        MenuItem edh = menu.findItem(R.id.edh);
        if (mode.getTitle().toString().equals("Multiplayer") &&
                findViewById(R.id.player2).getVisibility() == View.VISIBLE) {
            mode.setTitle("Single Player");
        } else if (findViewById(R.id.player2).getVisibility() == View.GONE){
            mode.setTitle("Multiplayer");
        }
        if (edh.getTitle().toString().equals("Standard")  && startLife != 40) {
            edh.setTitle("EDH");
        } else if (startLife != 20){
            edh.setTitle("Standard");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a boner activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_reset) {
            reset(startLife);
        } else if (id == R.id.multiplayer) {
            if (item.getTitle().toString().equals("Multiplayer")) {
                findViewById(R.id.player2).setVisibility(View.VISIBLE);
                reset(startLife);
            } else {
                findViewById(R.id.player2).setVisibility(View.GONE);
            }
            reset(startLife);
        }
        else if (id == R.id.edh) {
            if (item.getTitle().toString().equals("EDH")) {
                startLife = 40;
                reset(startLife);
            }
            else {
                startLife = 20;
                reset(startLife);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void destoryListeners() {
        System.out.println("destroying");
        (findViewById(R.id.p1Minus)).setOnTouchListener(null);
        (findViewById(R.id.p2Minus)).setOnTouchListener(null);
        (findViewById(R.id.p1Plus)).setOnTouchListener(null);
        (findViewById(R.id.p2Plus)).setOnTouchListener(null);
    }

    @Override
    public void onStop() {
        destoryListeners();
        super.onStop();

    }

    @Override
    public void onResume() {
        System.out.println("resuming");
        initButtonListeners();
        super.onResume();
    }
}
