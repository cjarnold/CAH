package arnold.cja.cah;


import java.util.ArrayList;
import org.acra.ACRA;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import arnold.cja.cah.R;
import arnold.cja.cah.Card.CardType;
import arnold.cja.cah.Util.StyleType;

/**
 * The root activity and the one that shows the main game menu.
 * Has the ability to start a new game, manage players, view card sets,
 * and change the game options.
 * A static reference to the GameManager (gm) is stored here for all other
 * activities to access.
 */
public class LaunchActivity extends ListActivity {
	
	private static final String TAG = "LaunchActivity";
	private static final String SELECT_WHITE         = "Start next round";
	private static final String VIEW_BLACK_CARD_SETS = "Browse Black Card Sets";
	private static final String VIEW_WHITE_CARD_SETS = "Browse White Card Sets";
	private static final String PLAYERS              = "Manage Players";

	public  static final String CARD_TYPE = "CARD_TYPE";
	
	private static final int DIALOG_NEED_FEWER_PLAYERS = 1;
	private static final int DIALOG_NEED_MORE_PLAYERS  = 2;
	private static final int DIALOG_PASS_TO_CARD_CZAR  = 3;
	private static final int DIALOG_NEW_GAME           = 4;
	private static final int DIALOG_EXIT               = 5;
	
	private static final String B_PASS_TO_CARD_CZAR = "PASS_TO_CARD_CZAR";
	
	private static enum RequestCodes { 
		SELECT_NEXT_PLAYER,
		SELECT_ROUND_WINNER
	};
	
	private ArrayList<String>    mMainMenu;
	private ArrayAdapter<String> mAdapter;
	
	public static GameManager gm; 
	public static int nonFinalInt = 5;
	public static final int finalInt = 6;
	public static final String myString = "Hello World";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		Util.constructGameManagerIfNecessary(this);
		
		mMainMenu = new ArrayList<String>();
		mMainMenu.add(SELECT_WHITE);
		mMainMenu.add(PLAYERS);
		mMainMenu.add(VIEW_BLACK_CARD_SETS); 
		mMainMenu.add(VIEW_WHITE_CARD_SETS);

		mAdapter = new ArrayAdapter<String>(this, R.layout.main_menu_item, mMainMenu);

		setRound();


		setListAdapter(mAdapter);
		setContentView(R.layout.main);
		
		Util.assertGameState(this, "LaunchActivity::onCreate");
	}
	
	private void setRound() {
		mMainMenu.set(0, (gm.hasRoundStarted() ? "Continue" : "Start") + " Round " + Integer.toString(gm.getRoundNumber()));
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);

		if (position == 0) {
			handleSelectWhite();
		}
		else if (item == VIEW_BLACK_CARD_SETS) {
			handleViewCardSets(CardType.BLACK);
		}
		else if (item == VIEW_WHITE_CARD_SETS) {
			handleViewCardSets(CardType.WHITE);
		}
		else if (item == PLAYERS) {
			Intent intent = new Intent(this, ManagePlayersActivity.class);
			Util.startActivity(this, intent);
		}
		else {
			Log.e(TAG, "Unknown menu type: " + item);
		}
	}
	
	private void handleViewCardSets(CardType cardType) {
		Intent intent = new Intent(this, ViewAllCardSetsActivity.class);
		intent.putExtra(CARD_TYPE, cardType.toString());
		Util.startActivity(this, intent);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch(id) {
			case DIALOG_PASS_TO_CARD_CZAR:
				AlertDialog ad = (AlertDialog)dialog;
				ad.setMessage(args.getCharSequence(B_PASS_TO_CARD_CZAR));
				break;
		}
		
		super.onPrepareDialog(id, dialog, args);
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text;
		
		switch(id) {
		case DIALOG_NEED_FEWER_PLAYERS:

			text = "There are not enough white cards to deal to all players!  " + 
					"Remove at least one player and try again.";

			builder.setMessage(text)
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

				}
			});

			return builder.create();
		case DIALOG_NEED_MORE_PLAYERS:
			text = "You need at least 2 human players to play!";
			
			builder.setMessage(text)
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			           }
			       });
			       
			return builder.create();
		case DIALOG_PASS_TO_CARD_CZAR:
			builder.setMessage(args.getCharSequence(B_PASS_TO_CARD_CZAR))
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	  
		        	   startSelectRoundWinner();
		           }
		       });
		       
			return builder.create();
		case DIALOG_NEW_GAME:
			builder.setMessage("Are you sure you want to start a new game?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					gm.resetGame();
					setRound();
					Util.toast(LaunchActivity.this, "Game reset");
				}
			})
			.setNegativeButton("No", null);
			return builder.create();
		case DIALOG_EXIT:
              builder.setMessage("Are you sure you want to exit?")
              .setCancelable(false)
              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
               	   // TODO: game is saved twice here??
               	   //Util.asyncSaveState(LaunchActivity.this);
               	   LaunchActivity.this.finish();
                  }
              })
              .setNegativeButton("No", null);
              return builder.create();
		default:
			return null;
		}
	}

	private void handleSelectWhite() {
		// make sure all players can get enough cards.  If not, don't let them start the round
		for (Player p : gm.getPlayers()) {
			if (!gm.dealEnough(p)) {
				showDialog(DIALOG_NEED_FEWER_PLAYERS, null);
				return;
			}
		}
		
		if (LaunchActivity.gm.getHumanPlayerCount() < 2) {
			showDialog(DIALOG_NEED_MORE_PLAYERS, null);
			return;
		}
		else if (gm.allPlayersSubmitted()) {
			alertCardCzar();
		}
		else {
			gm.setRoundStarted();
			Intent intent = new Intent(this, SelectNextPlayerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

			Util.startActivityForResult(this, intent, RequestCodes.SELECT_NEXT_PLAYER.ordinal());
			
		}
	}

	private void alertCardCzar() {
		Log.i(TAG, "Executing alertCardCzar(): " + LaunchActivity.gm.getCardCzar().getName());
		
		CharSequence styledName = Util.setSpanBetweenTokens("##" + LaunchActivity.gm.getCardCzar().getName() + "##", "##", StyleType.FOREGROUND_MAGENTA);
		CharSequence text = TextUtils.concat("Pass the phone to Card Czar ", styledName);
		Bundle bundle = new Bundle();
		bundle.putCharSequence(B_PASS_TO_CARD_CZAR, text);
		showDialog(DIALOG_PASS_TO_CARD_CZAR, bundle);
	
	}
	
	@Override
	public void overridePendingTransition (int enterAnim, int exitAnim) { }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == RequestCodes.SELECT_NEXT_PLAYER.ordinal()) {
			// back from selecting white cards
			// put up an alert box telling people to pass the phone to the CardCzar
			// and then start the handleRoundWinner activity
			if (resultCode == 0) {
				// not finished
				setRound();
			}
			else {
				alertCardCzar();	
			}
		}
		else if (requestCode == RequestCodes.SELECT_ROUND_WINNER.ordinal()) {
			setRound();
		}
	}
	
	private void startSelectRoundWinner() {
		Intent intent = new Intent(this, SelectRoundWinnerActivity.class);
		Util.startActivityForResult(this, intent, RequestCodes.SELECT_ROUND_WINNER.ordinal());
		
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "LaunchActivity::onStart");
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "LaunchActivity::onResume2");
        Util.assertGameState(this, "LaunchActivity::onResume2");
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "LaunchActivity::onPause");
   
        Util.saveStateIfLeavingApp(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "LaunchActivity::onStop");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "LaunchActivity::onDestroy");
        // The activity is about to be destroyed.
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	super.onOptionsItemSelected(item);

    	switch (item.getItemId()){
    	
    	case R.id.reportBuggyGameState:
    		Util.toast(this, "Making an error report...");
    		ACRA.getErrorReporter().handleException(null);
    		break;
    	case R.id.settings:
    		Intent intent = new Intent(this, PrefsActivity.class);
    		Util.startActivity(this, intent);
    		break;
    	case R.id.newGame:
    		showDialog(DIALOG_NEW_GAME, null);
    		break;
    		
    	}
    	return true;
    }
    
    @Override
    public void onBackPressed() {
    	showDialog(DIALOG_EXIT, null);
    }
}

