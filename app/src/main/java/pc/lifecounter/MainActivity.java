package pc.lifecounter;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final int REPEAT = 500; // # of ms between held button triggers

    private int startLife = 20;
    private List<Timer> timers = new ArrayList<>();
    private boolean p1CommanderActive = false;
    private boolean p2CommanderActive = false;

    // Player 1 views
    private Button p1Plus;
    private Button p1Minus;
    private TextView p1Total;
    private LifeRing p1Ring;
    private TextView p1Commander;

    // Player 2 views
    private Button p2Plus;
    private Button p2Minus;
    private TextView p2Total;
    private LifeRing p2Ring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Invert player 2
        findViewById(R.id.player2).setRotation(180);

        // Set references to each player's views
        p1Plus = (Button) findViewById(R.id.p1Plus);
        p1Minus = (Button) findViewById(R.id.p1Minus);
        p1Total =  (TextView) findViewById(R.id.player1Total);
        p1Ring = (LifeRing) findViewById(R.id.player1Ring);
        p1Commander = (TextView) findViewById(R.id.player1Commander);

        p2Plus = (Button) findViewById(R.id.p2Plus);
        p2Minus = (Button) findViewById(R.id.p2Minus);
        p2Total =  (TextView) findViewById(R.id.player2Total);
        p2Ring = (LifeRing) findViewById(R.id.player2Ring);

        // Set onTouch listeners for each +/- button
        initButtonListeners();

        p1Commander.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(p1CommanderActive) {
                        p1CommanderActive = false;
                    } else {
                        p1CommanderActive = true;
                    }

                    return true;
                }
                return false;
            }
        });
    }

    // Returns TextView's integer value
    private int getTotal(TextView tv) {
        return Integer.parseInt(tv.getText().toString());
    }

    // Sets TextView to display given int
    private void setTotal(TextView tv, int newTotal) {
        tv.setText("" + newTotal);
    }

    // Sets life values to start life, animates rings and sets
    // their start life as well (i.e what # == full circle)
    private void reset(int start) {
        setTotal(p1Total, start);
        setTotal(p2Total, start);
        p1Ring.setStart(start);
        p2Ring.setStart(start);
    }

    // Sets onTouch listeners for each +/- button
    private void initButtonListeners() {
        setButtonListener(p1Minus, p1Total, p1Ring, -1);
        setButtonListener(p1Plus, p1Total, p1Ring, 1);
        setButtonListener(p2Minus, p2Total, p2Ring, -1);
        setButtonListener(p2Plus, p2Total, p2Ring, 1);
    }

    // Destroys onTouch listeners (timers can cause leak)
    private void destroyListeners() {
        for (Timer t : timers) {
            t.cancel();
            t.purge();
        }
        timers.clear();
        p1Minus.setOnTouchListener(null);
        p2Minus.setOnTouchListener(null);
        p1Plus.setOnTouchListener(null);
        p2Plus.setOnTouchListener(null);
    }


    // Given a Button, TextView, LifeRing, and button type (+/-),
    // sets an onTouch listener that updates life total and
    // animates ring appropriately on button touch / hold
    private void setButtonListener(Button b, TextView tv, LifeRing ring, int type) {
        final TextView textView = tv;
        final LifeRing lifeRing = ring;
        final int buttonType = type;
        final Timer t = new Timer();
        timers.add(t);

        b.setOnTouchListener(new View.OnTouchListener() {
            boolean buttonHeld = false;
            long initTouch = 0; // Determines whether touch was a tap or hold
            long touchTime = 0; // Time at which repeat was last registered
            int total = 0;

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
                                if(buttonHeld && touchTime < System.currentTimeMillis() - REPEAT) {
                                    if (!)
                                    total = getTotal(textView);
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
                                    touchTime += REPEAT;
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
                    if (System.currentTimeMillis() - touchTime < REPEAT &&
                            System.currentTimeMillis() - initTouch < REPEAT) {
                        total = getTotal(textView);
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
        int id = item.getItemId();

        if (id == R.id.action_reset) {
            reset(startLife);
        }
        else if (id == R.id.multiplayer) {
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

    @Override
    public void onStop() {
        destroyListeners();
        super.onStop();

    }

    @Override
    public void onResume() {
        initButtonListeners();
        super.onResume();
    }
}
