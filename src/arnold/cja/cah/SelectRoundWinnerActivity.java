package arnold.cja.cah;

import java.util.ArrayList;
import java.util.Collections;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import arnold.cja.cah.Util.StyleType;

/**
 * This activity shows a list of submitted Combos to the Card Czar.  
 * The CardCzar taps the combo that he/she thinks is most funny
 * and the Player that submitted that Combo wins the round and gets a point.
 */
public class SelectRoundWinnerActivity extends ListActivity {
	
	private static final String TAG = "SelectRoundWinnerActivity";
	private static final int ROUND_WINNER_DIALOG = 1;
	
	private ArrayAdapter<Combo> mAdapter;
	private ArrayList<Combo>    mCombos;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Util.constructGameManagerIfNecessary(this)) { return; }
	
		mCombos = LaunchActivity.gm.getCombos();
		Collections.shuffle(mCombos);
		
		mAdapter = new ComboDisplayArrayAdapter(this, mCombos);
		
		setListAdapter(mAdapter);
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
		case ROUND_WINNER_DIALOG:
			builder.setMessage(args.getCharSequence("ROUND_WINNER_TEXT"))
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent=new Intent();
					intent.putExtra("ComingFrom", "Hello");
					setResult(RESULT_OK, intent);
					LaunchActivity.gm.setLeavingActivity();
					finish();
				}
			});

			return builder.create();
		default:
			return null;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Combo combo = (Combo) getListAdapter().getItem(position);
		Player p = combo.getPlayer();
		p.addWin(combo);

		CharSequence styledName = Util.setSpanBetweenTokens("##" + p.getName() + "##", "##", StyleType.FOREGROUND_MAGENTA);

		CharSequence result = TextUtils.concat(styledName, 
				" won the round with:\n\n",
				combo.getStyledStatement());
		
		LaunchActivity.gm.endOfRound();
		mCombos.clear();
		mAdapter.notifyDataSetChanged();

		Bundle bundle = new Bundle();
		bundle.putCharSequence("ROUND_WINNER_TEXT", result);
		showDialog(ROUND_WINNER_DIALOG, bundle);
	}

	
	@Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "SelectRoundWinnerActivity::onPause");
        Util.saveStateIfLeavingApp(this);
    }

	@Override
    public void onBackPressed() {
		LaunchActivity.gm.setLeavingActivity();
		super.onBackPressed();
	}
	
}
