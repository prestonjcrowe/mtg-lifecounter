package pc.lifecounter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;

import static pc.lifecounter.R.id.player1Total;
import static pc.lifecounter.R.id.player2Total;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button p1Plus = (Button) findViewById(R.id.p1Plus);
        Button p1Minus = (Button) findViewById(R.id.p1Minus);
        findViewById(R.id.player2).setRotation(180);
        Button p2Plus = (Button) findViewById(R.id.p2Plus);
        Button p2Minus = (Button) findViewById(R.id.p2Minus);

        setButtonListener(p1Minus, (TextView) findViewById(player1Total),
                (LifeRing) findViewById(R.id.player1Ring), -1);
        setButtonListener(p1Plus,  (TextView) findViewById(player1Total),
                (LifeRing) findViewById(R.id.player1Ring), 1);
        setButtonListener(p2Minus, (TextView) findViewById(player2Total),
                (LifeRing) findViewById(R.id.player2Ring), -1);
        setButtonListener(p2Plus,  (TextView) findViewById(player2Total),
                (LifeRing) findViewById(R.id.player2Ring), 1);
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

    public void setButtonListener(Button b, TextView tv, LifeRing ring, int type) {
        final TextView textView = tv;
        final LifeRing lifeRing = ring;
        final int buttonType = type;

        b.setOnTouchListener(new View.OnTouchListener() {
            boolean buttonHeld = false;
            long touchTime = 0;
            Timer t = new Timer();


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                t.purge();
                t.scheduleAtFixedRate(new ButtonTask(textView) {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lifeRing.invalidate();
                                if(buttonHeld && touchTime < System.currentTimeMillis() - 750) {
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
                                    touchTime += 750;
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
                    if (System.currentTimeMillis() - touchTime < 750) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a boner activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_reset) {

        } else if (id == R.id.single_player) {

        } else if (id == R.id.multiplayer) {

        } else if (id == R.id.edh) {

        }

        return super.onOptionsItemSelected(item);
    }
}
