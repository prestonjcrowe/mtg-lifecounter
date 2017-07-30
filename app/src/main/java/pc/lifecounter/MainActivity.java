package pc.lifecounter;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
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

    private int startLife = 20;
    private List<Timer> timers = new ArrayList<>(); // For memory management

    private final int REPEAT = 500; // # of ms between held button triggers

    // Player 1 views
    private Button p1Plus;
    private Button p1Minus;
    private TextView p1Total;
    private TextView p1Commander;
    private LifeRing p1Ring;
    private PlayerState p1State;

    // Player 2 views
    private Button p2Plus;
    private Button p2Minus;
    private TextView p2Total;
    private TextView p2Commander;
    private LifeRing p2Ring;
    private PlayerState p2State;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Invert player 2 view
        findViewById(R.id.player2).setRotation(180);

        // Set references to each player's views
        p1Plus = (Button) findViewById(R.id.p1Plus);
        p1Minus = (Button) findViewById(R.id.p1Minus);
        p1Total =  (TextView) findViewById(R.id.player1Total);
        p1Ring = (LifeRing) findViewById(R.id.player1Ring);
        p1Commander = (TextView) findViewById(R.id.player1Commander);
        p1State = new PlayerState();

        p2Plus = (Button) findViewById(R.id.p2Plus);
        p2Minus = (Button) findViewById(R.id.p2Minus);
        p2Total =  (TextView) findViewById(R.id.player2Total);
        p2Ring = (LifeRing) findViewById(R.id.player2Ring);
        p2Commander = (TextView) findViewById(R.id.player2Commander);
        p2State = new PlayerState();

        p1Commander.setVisibility(View.INVISIBLE);
        p2Commander.setVisibility(View.INVISIBLE);

        // Set onTouch listeners for each +/- button
        initButtonListeners();
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
        setTotal(p1Commander, 0);
        setTotal(p2Commander, 0);
        p1Ring.setStart(start);
        p2Ring.setStart(start);
    }

    // Sets onTouch listeners for each +/- button and commander textview
    private void initButtonListeners() {
        setButtonListener(p1Minus, p1Total, p1Commander, p1Ring, p1State, -1);
        setButtonListener(p1Plus, p1Total, p1Commander, p1Ring, p1State, 1);
        setButtonListener(p2Minus, p2Total, p2Commander, p2Ring, p2State, -1);
        setButtonListener(p2Plus, p2Total, p2Commander, p2Ring, p2State, 1);
        setCommanderListener(p1State, p1Total, p1Commander);
        setCommanderListener(p2State, p2Total, p2Commander);
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
        p1Commander.setOnTouchListener(null);
        p2Commander.setOnTouchListener(null);
    }

    private void setCommanderListener(PlayerState pState, TextView total, TextView com) {
        final PlayerState state = pState;
        final TextView commanderView = com;
        final Timer t = new Timer();
        timers.add(t);
        total.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                state.setCommanderMode(true);
                state.setLastTouched(System.currentTimeMillis());
                commanderView.setTextColor(Color.RED);
                t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (state.getLastTouch() + 2000 < System.currentTimeMillis()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    commanderView.setTextColor(Color.WHITE);
                                    state.setCommanderMode(false);
                                }
                            });

                        }
                    }
                }, 0, 250);

                return false;
            }
        });
    }

    // Given a Button, TextView [total], TextView [commander],
    // LifeRing, and button type (+/-) sets an onTouch listener
    // that updates totals and animates ring appropriately on
    // button touch / hold
    private void setButtonListener(Button b, TextView tv, TextView com, LifeRing ring,
                                   PlayerState pState, int type) {
        final TextView textView = tv;
        final LifeRing lifeRing = ring;
        final int buttonType = type;
        final TextView commanderView = com;
        final PlayerState state = pState;
        final Timer t = new Timer();
        timers.add(t);

        b.setOnTouchListener(new View.OnTouchListener() {
            boolean buttonHeld = false;
            long initTouch = 0; // Determines whether touch was a tap or hold
            long touchTime = 0; // Time at which repeat was last registered
            int total = 0;
            int commander = 0;
            boolean heldTriggered = false;

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
                                    heldTriggered  = true;
                                    total = getTotal(textView);
                                    commander = getTotal(commanderView);

                                    if (buttonType <= 0) {
                                        total -= 5;
                                        if (state.getMode()) {
                                            commander += 5;
                                            if (commander > 21) {
                                                commander = 21;
                                            }
                                            setTotal(commanderView, commander);
                                        }
                                    } else {
                                        total += 5;
                                    }
                                    if (total < 0) {
                                        total = 0;
                                    }
                                    setTotal(textView, total);
                                    lifeRing.setLife(total);
                                    touchTime += REPEAT;
                                    state.setLastTouched(System.currentTimeMillis());
                                }
                            }
                        });
                    }
                }, 0, 100);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonHeld = true;
                    heldTriggered  = false;
                    touchTime = System.currentTimeMillis();
                    //
                    state.setLastTouched(System.currentTimeMillis());
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonHeld = false;
                    state.setLastTouched(System.currentTimeMillis());
                    if (!heldTriggered) {
                        total = getTotal(textView);
                        commander = getTotal(commanderView);
                        if (buttonType <= 0) {
                            total --;
                            if (state.getMode()) {
                                commander++;
                                if (commander > 21) {
                                    commander = 21;
                                }
                                setTotal(commanderView, commander);
                            }
                        } else {
                            total ++;
                        }
                        if (total < 0) {
                            total = 0;
                        }
                        if (commander > 21) {
                            commander = 21;
                        }
                        setTotal(textView, total);
                        lifeRing.setLife(total);
                    }
                    heldTriggered = false;
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
                p1Commander.setVisibility(View.VISIBLE);
                p2Commander.setVisibility(View.VISIBLE);
            } else {
                startLife = 20;
                reset(startLife);
                p1Commander.setVisibility(View.INVISIBLE);
                p2Commander.setVisibility(View.INVISIBLE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        destroyListeners();
        p1State.resetState();
        p2State.resetState();
        p1Commander.setTextColor(Color.WHITE);
        p1Commander.setTextColor(Color.WHITE);
        super.onStop();
    }

    @Override
    public void onResume() {
        initButtonListeners();
        super.onResume();
    }
}
