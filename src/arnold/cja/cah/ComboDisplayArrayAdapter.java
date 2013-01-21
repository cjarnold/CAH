package arnold.cja.cah;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import arnold.cja.cah.R;

/**
 * Each Combo in the array is displayed in a TextView which contains
 * the Combo's styled statement
 */
public class ComboDisplayArrayAdapter extends ArrayAdapter<Combo> {

   private ArrayList<Combo> mListItems;

   public ComboDisplayArrayAdapter(Context context, ArrayList<Combo> listItems) {
      super(context, R.layout.select_winner_item, listItems);
      mListItems = listItems;	
   }

   @Override  
   public View getView(int position, View view, ViewGroup viewGroup) {	
      View v = super.getView(position, view, viewGroup);

      TextView tv = (TextView)v;

      Combo combo = mListItems.get(position);
      tv.setText(combo.getStyledStatement(), BufferType.SPANNABLE);

      return v;
   }

}


