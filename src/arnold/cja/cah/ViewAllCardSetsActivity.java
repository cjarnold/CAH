package arnold.cja.cah;

import java.util.ArrayList;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import arnold.cja.cah.R;
import arnold.cja.cah.Card.CardType;

/**
 * View a list of available card sets for either black or white cards.
 * Some card set examples are: base, first expansion, second expansion, and holiday pack
 */
public class ViewAllCardSetsActivity extends ListActivity {

   private static final String TAG = "ViewAllCardSetsActivity";
   public static final String CARD_SET_INDEX = "CARD_SET_INDEX";

   private ArrayList<CardSet> mCardSetsArray;
   private String             mCardTypeAsString;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (!Util.constructGameManagerIfNecessary(this)) { return; }

      Intent intent = getIntent();

      mCardTypeAsString = intent.getStringExtra(LaunchActivity.CARD_TYPE);
      mCardSetsArray = LaunchActivity.gm.getDeck().getCardSets(CardType.valueOf(mCardTypeAsString));

      ArrayAdapter<CardSet> adapter = new ArrayAdapter<CardSet>(this, R.layout.cardset_item, mCardSetsArray);
      setListAdapter(adapter);
   }

   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {	
      Intent intent = new Intent(this, ViewCardSetActivity.class);
      intent.putExtra(LaunchActivity.CARD_TYPE, mCardTypeAsString);
      intent.putExtra(ViewAllCardSetsActivity.CARD_SET_INDEX, position);
      Util.startActivity(this, intent);
   }

   @Override
   protected void onPause() {
      super.onPause();
      Log.i(TAG, "ViewAllCardSetsActivity::onPause");
      Util.saveStateIfLeavingApp(this);
   }

   @Override
   public void onBackPressed() {
      LaunchActivity.gm.setLeavingActivity();
      super.onBackPressed();
   }

}

