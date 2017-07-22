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


// write method commanderActive that returns a boolean, based on drawable attr
// rewrite runnables as custom timer tasks?

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
    private Button p1Commander;

    // Player 2 views
    private Button p2Plus;
    private Button p2Minus;
    private TextView p2Total;
    private LifeRing p2Ring;

    private long lastInteraction = 0;

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
        p1Commander = (Button) findViewById(R.id.player1Commander);

        p2Plus = (Button) findViewById(R.id.p2Plus);
        p2Minus = (Button) findViewById(R.id.p2Minus);
        p2Total =  (TextView) findViewById(R.id.player2Total);
        p2Ring = (LifeRing) findViewById(R.id.player2Ring);

        // Set onTouch listeners for each +/- button
        initButtonListeners();

        // keep track of last touch to both buttons AND commander button
        // define as global var to start, then move to setListeners func

        p1Total.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //System.out.println(buttonID);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastInteraction = System.currentTimeMillis();
                    p1CommanderActive = true;
                    p1Commander.setBackgroundResource(R.drawable.toggle_on);

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

        setTotal(p1Commander, 0);

        p1Ring.setStart(start);
        p2Ring.setStart(start);
    }

    // Sets onTouch listeners for each +/- button
    private void initButtonListeners() {
        setButtonListener(p1Minus, p1Total, p1Ring, -1, p1Commander);
        setButtonListener(p1Plus, p1Total, p1Ring, 1, p1Commander);
        setButtonListener(p2Minus, p2Total, p2Ring, -1, p1Commander);
        setButtonListener(p2Plus, p2Total, p2Ring, 1, p1Commander);
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
    private void setButtonListener(Button b, TextView tv, LifeRing ring, int type, final Button comm) {
        final TextView textView = tv;
        final LifeRing lifeRing = ring;
        final int buttonType = type;
        final Timer t = new Timer();
        final Button butt = b;
        timers.add(t);

        b.setOnTouchListener(new View.OnTouchListener() {
            boolean buttonHeld = false;
            long initTouch = 0; // Determines whether touch was a tap or hold
            long touchTime = 0; // Time at which repeat was last registered
            int total = 0;
            int commander = 0;
            String buttonID = (getResources().getResourceName(butt.getId())).substring(18,20);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //System.out.println(buttonID);
                initTouch = System.currentTimeMillis();
                t.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lifeRing.invalidate();
                                if(buttonHeld && touchTime < System.currentTimeMillis() - REPEAT) {
                                    total = getTotal(textView);
                                    commander = getTotal(comm);
                                    // write this logic into separate function?
                                    if (buttonType <= 0) {
                                        total -= 5;
                                        if (buttonID.equals("p1") && p1CommanderActive ||
                                                buttonID.equals("p2") && p2CommanderActive) {
                                            commander -=5;
                                        }
                                    } else {
                                        total += 5;
                                        if (buttonID.equals("p1") && p1CommanderActive ||
                                                buttonID.equals("p2") && p2CommanderActive) {
                                            commander +=5;
                                        }
                                    }
                                    if (total < 0) {
                                        total = 0;
                                    }
                                    if (commander < 0) {
                                        commander = 0;
                                    } else if (commander > 21) {
                                        commander = 21;
                                    }
                                    setTotal(textView, total);
                                    setTotal(comm, commander);
                                    lifeRing.setLife(total);
                                    touchTime += REPEAT;

                                    lastInteraction = System.currentTimeMillis();
                                }
                                // can be separate func

                                if (lastInteraction + 3000 < System.currentTimeMillis()) {
                                    p1CommanderActive = false;
                                    p1Commander.setBackgroundResource(R.drawable.toggle_off);
                                    if (p1Ring.getCommander()) {
                                        p1Ring.setCommander(false);
                                    }

                                } else if(lastInteraction + 2000 < System.currentTimeMillis()) {
                                    p1Commander.setBackgroundResource(R.drawable.toggle_half);
                                    p1Ring.setOuterColor(Color.parseColor("#7fffffff"));
                                } else {
                                    //System.out.println("LAST INNER FACTION: " + lastInteraction);
                                    if (!p1Ring.getCommander()) {
                                        p1Ring.setCommander(true);
                                    }
                                    p1Commander.setBackgroundResource(R.drawable.toggle_on);
                                    p1Ring.setOuterColor(Color.parseColor("#fffffff"));
                                    //p1Ring.setCommander(true);
                                }
                            }
                        });
                    }
                }, 0, 100);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonHeld = true;
                    touchTime = System.currentTimeMillis();
                    lastInteraction = System.currentTimeMillis();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastInteraction = System.currentTimeMillis();
                    buttonHeld = false;
                    if (System.currentTimeMillis() - touchTime < REPEAT &&
                            System.currentTimeMillis() - initTouch < REPEAT) {
                        total = getTotal(textView);
                        commander = getTotal(comm);
                        if (buttonType <= 0) {
                            total --;
                            if (buttonID.equals("p1") && p1CommanderActive ||
                                    buttonID.equals("p2") && p2CommanderActive && commander > 0) {
                                commander --;
                            }
                        } else {
                            total ++;
                            if (buttonID.equals("p1") && p1CommanderActive ||
                                    buttonID.equals("p2") && p2CommanderActive && commander < 21) {
                                commander ++;
                            }
                        }
                        if (total < 0) {
                            total = 0;
                        }
                        if (commander < 0) {
                            commander = 0;
                        } else if (commander > 21) {
                            commander = 21;
                        }
                        setTotal(textView, total);
                        setTotal(comm, commander);

                        lifeRing.setLife(total);
                    }
                    t.purge();
                    System.out.println("THIS (task?) = " + this.toString());
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
