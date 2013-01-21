package arnold.cja.cah;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import arnold.cja.cah.R;

/**
 * Change game settings
 * Currently the only available setting is how many white cards
 * are dealt to each Player (5, 10, 15, or 20)
 */
public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "PrefsActivity";
	public static final String PREF_CARD_COUNT = "cardCount";
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Util.constructGameManagerIfNecessary(this)) { return; }
        
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.prefs);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.compareTo(PREF_CARD_COUNT) == 0) {        	
        	String cardsDealt = sharedPreferences.getString(PREF_CARD_COUNT, "10");
        	LaunchActivity.gm.setCardsInHand(Integer.parseInt(cardsDealt));        	
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "PrefsActivity::onPause");
        Util.saveStateIfLeavingApp(this);
    }
    
    @Override
    public void onBackPressed() {
		LaunchActivity.gm.setLeavingActivity();
		super.onBackPressed();
	}
    
}

