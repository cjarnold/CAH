package arnold.cja.cah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import arnold.cja.cah.R;

/**
 * View, add, and remove Players.
 */
public class ManagePlayersActivity extends ListActivity{

   private static final String TAG = "ManagePlayersActivity";
   private static final int DIALOG_PLAYER             = 1;
   private static final int DIALOG_REMOVE_ALL_PLAYERS = 2;
   private static final String B_PLAYER_TEXT = "PLAYER_TEXT";

   private ArrayAdapter<Player> mAdapter;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Log.i(TAG, "ManagePlayersActivity::onCreate()");

      if (!Util.constructGameManagerIfNecessary(this)) { return; }

      ArrayList<Player> players = LaunchActivity.gm.getPlayers();

      Collections.sort(players, new Comparator<Player>() {
         public int compare(Player one, Player other) {
            if (one.getPointTotal() == other.getPointTotal()) {
               return other.getName().compareTo(one.getName());
            } 
            else {
               return Integer.valueOf(other.getPointTotal()).compareTo(Integer.valueOf(one.getPointTotal()));
            }
         }
      }); 


      mAdapter = new PlayerStatArrayAdapter(this, players);
      setListAdapter(mAdapter);
      this.registerForContextMenu(this.getListView());

      Log.i(TAG, "ManagePlayersActivity: finished onCreate");
   }

   @Override
   protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {	
      switch(id) {
      case DIALOG_PLAYER:
         AlertDialog ad = (AlertDialog)dialog;
         ad.setMessage(args.getString(B_PLAYER_TEXT));
         break;
      }

      super.onPrepareDialog(id, dialog, args);
   }

   @Override
   protected Dialog onCreateDialog(int id, Bundle args) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      switch(id) {
      case DIALOG_PLAYER:
         builder.setMessage(args.getString(B_PLAYER_TEXT))
         .setCancelable(false)
         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
         });

         return builder.create();
      case DIALOG_REMOVE_ALL_PLAYERS:
         builder.setMessage("Are you sure you want to remove all players?")
         .setCancelable(false)
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               LaunchActivity.gm.removeAllPlayers();
               mAdapter.notifyDataSetChanged();
            }
         })
         .setNegativeButton("No", null);
         return builder.create();
      default:
         return null;
      }
   }

   public void showStats(Player p) {
      String playerDialogText = p.getName() + " (" + p.getPointTotal() + 
            " point" + (p.getPointTotal() == 1 ? "" : "s") + ")\n\nLast Winning Combo:\n\n" + p.getLastWinText();
      Bundle bundle = new Bundle();
      bundle.putString(B_PLAYER_TEXT, playerDialogText);
      showDialog(DIALOG_PLAYER, bundle);
   }

   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      Player player = (Player) getListAdapter().getItem(position);
      showStats(player);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.manage_players_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected (MenuItem item) {
      super.onOptionsItemSelected(item);

      switch (item.getItemId()){
      case R.id.add_player:    		
         Intent intent = new Intent(this, AddPlayerActivity.class);
         Util.startActivityForResult(this, intent, 0);    		
         break;
      default:
         break;
      }
      return true;
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode != 0) {
         String playerName = data.getStringExtra(AddPlayerActivity.EXTRA_PLAYER_NAME);
         Player p = new Player(playerName);
         p.setIsAI(data.getBooleanExtra(AddPlayerActivity.EXTRA_IS_AI, false));
         LaunchActivity.gm.dealEnough(p);
         LaunchActivity.gm.getPlayers().add(p);
         LaunchActivity.gm.fixCardCzar();

         mAdapter.notifyDataSetChanged();
         Util.toast(this, "Player " + playerName + " was added");
      }

   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v,
         ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.remove_player, menu);
   }


   @Override
   public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

      switch(item.getItemId()) {
      case R.id.remove_player:
         Player player = (Player) getListAdapter().getItem((int) info.id);
         Util.toast(this, player.getName() + " has been removed");
         LaunchActivity.gm.removePlayer(player);
         mAdapter.notifyDataSetChanged();
         return true;
      case R.id.remove_all_players:
         showDialog(this.DIALOG_REMOVE_ALL_PLAYERS, null);
         return true;
      default:
         return false;
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      Log.i(TAG, "ManagePlayersActivity::onPause");
      Util.saveStateIfLeavingApp(this);
   }

   @Override
   protected void onStart() {
      super.onStart();
      Log.i(TAG, "ManagePlayersActivity::onStart");
      // The activity is about to become visible.
   }
   @Override
   protected void onResume() {
      super.onResume();
      Log.i(TAG, "ManagePlayersActivity::onResume");
      // The activity has become visible (it is now "resumed").
   }

   @Override
   protected void onStop() {
      super.onStop();
      Log.i(TAG, "ManagePlayersActivity::onStop");
      // The activity is no longer visible (it is now "stopped")
   }
   @Override
   protected void onDestroy() {
      super.onDestroy();
      Log.i(TAG, "ManagePlayersActivity::onDestroy");
      // The activity is about to be destroyed.
   }

   @Override
   public void onBackPressed() {
      LaunchActivity.gm.setLeavingActivity();
      super.onBackPressed();
   }

}
