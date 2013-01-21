package arnold.cja.cah;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Each Card in the array is displayed in a TextView which contains
 * the Card's styled statement
 */
public class CardSetArrayAdapter extends ArrayAdapter<Card> {

   private ArrayList<Card> mListItems;

   public CardSetArrayAdapter(Context context, ArrayList<Card> listItems) {
      super(context, android.R.layout.simple_list_item_1, listItems);
      mListItems = listItems;
   }

   @Override  
   public View getView(int position, View view, ViewGroup viewGroup) {	
      View v = super.getView(position, view, viewGroup);

      TextView tv = (TextView)v;

      Card card = mListItems.get(position);
      tv.setText(card.getStyledStatement(), BufferType.SPANNABLE);

      return v;
   }

}

