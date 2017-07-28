package pc.lifecounter;

/**
 * Created by prestoncrowe on 7/28/17.
 */

public class PlayerState {
    private long lastTouched = 0;
    private boolean commanderMode = false;

    PlayerState() {
        lastTouched = 0;
        commanderMode = false;
    }

    public long getLastTouch() {
        return lastTouched;
    }

    public boolean getMode() {
        return commanderMode;
    }

    public void setLastTouched(long time) {
        lastTouched = time;
    }

    public void setCommanderMode(boolean mode) {
        commanderMode = mode;
    }

    public void resetState() {
        lastTouched = 0;
        commanderMode = false;
    }
}
