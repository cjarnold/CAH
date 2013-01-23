package arnold.cja.cah;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import arnold.cja.cah.R;

/**
 * Each Player in the array is displayed in a TextView which shows
 * the name of the player, whether or not they have submitted their combo, 
 * their score, and whether they are the current leader (star next to name)
 * This is used by SelectNextPlayerActivity
 */
public class PlayerSelectArrayAdapter extends ArrayAdapter<Player> {

   private ArrayList<Player> mListItems;
   private Context           mContext; 

   public PlayerSelectArrayAdapter(Context context, ArrayList<Player> listItems) {
      super(context, R.layout.player_entry, listItems);
      mListItems = listItems;
      mContext = context;
   }

   @Override
   public void notifyDataSetChanged() {	
      Collections.sort(mListItems, new Comparator<Player>() {
         public int compare(Player one, Player other) {
            if (!one.hasPickedWhite() && other.hasPickedWhite()) {
               return -1;
            }
            else if (one.hasPickedWhite() && !other.hasPickedWhite()) {
               return 1;
            }
            else {
               return one.getName().compareTo(other.getName());
            }
         }
      }); 

      super.notifyDataSetChanged();
   }

   @Override  
   public View getView(int position, View view, ViewGroup viewGroup)
   {
      LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
      view = inflater.inflate(R.layout.player_entry, viewGroup, false);

      TextView t1 = (TextView)view.findViewById(R.id.playerEntryName);
      TextView t2 = (TextView)view.findViewById(R.id.playerEntryScore);
      CheckBox cb = (CheckBox)view.findViewById(R.id.playerEntryCheckBox);

      Player p = mListItems.get(position);

      boolean checked = (p.isAI() || LaunchActivity.gm.getCardCzar() == p || p.hasPickedWhite());
      cb.setChecked(checked);

      int points = p.getPointTotal();

      if (p.isLeader()) {
         t1.setText(Util.getInlineImageText(this.getContext(), p.getName() + " :*"));
      }
      else {
         t1.setText(p.getName());
      }

      if (!p.hasPickedWhite()) {
         t1.setTextColor(mContext.getResources().getColor(R.color.Cyan));
      }
      else {
         view.setBackgroundColor(mContext.getResources().getColor(R.color.DimGray));
      }

      t2.setText(points + (points == 1 ? " point" : " points"));

      return view;
   }


}
