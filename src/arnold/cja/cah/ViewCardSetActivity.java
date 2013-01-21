package arnold.cja.cah;

import java.util.ArrayList;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import arnold.cja.cah.R;
import arnold.cja.cah.Card.CardType;

/**
 * Shows all Cards in a specific card set.
 * Clicking on a card brings up a context menu that allows the
 * user to search for the card text on various search providers such
 * as google and urban dictionary
 */
public class ViewCardSetActivity extends ListActivity {
	
	private static final String TAG = "ViewCardSetActivity";
	private ArrayAdapter<Card> mAdapter;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Util.constructGameManagerIfNecessary(this)) { return; }
		
		Intent intent = getIntent();
		
		String cardTypeAsString = intent.getStringExtra(LaunchActivity.CARD_TYPE);
		int position = intent.getIntExtra(ViewAllCardSetsActivity.CARD_SET_INDEX, 0);
		
		ArrayList<CardSet> cardSets = LaunchActivity.gm.getDeck().getCardSets(CardType.valueOf(cardTypeAsString));
		CardSet cs = cardSets.get(position);
		
		mAdapter = new CardSetArrayAdapter(this, cs.getAsArrayList());
		setListAdapter(mAdapter);
		
		this.registerForContextMenu(this.getListView());
		this.setTitle("CardSet: " + cs.getName());
	}
	
	public void onListItemClick(ListView l, View v, int position, long id){
		  l.showContextMenuForChild(v);   
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.define_phrase, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    Card card = (Card) getListAdapter().getItem((int) info.id);
	    Intent defineIntent = card.getDefinitionIntent(item.getItemId());
	    startActivity(defineIntent);
	    return true;
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "ViewCardSetActivity::onPause");
        Util.saveStateIfLeavingApp(this);
    }
	
	@Override
    public void onBackPressed() {
		LaunchActivity.gm.setLeavingActivity();
		super.onBackPressed();
	}
}

