package arnold.cja.cah;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import arnold.cja.cah.R;

/**
 * Each Player in the array is displayed in a TextView which shows the
 * name of the player and their score.  This is used by the ManagePlayersActivity
 */
public class PlayerStatArrayAdapter extends ArrayAdapter<Player> {

	private ArrayList<Player> mListItems;

	public PlayerStatArrayAdapter(Context context, ArrayList<Player> listItems) {
		super(context, R.layout.manage_player_entry, listItems);
		this.mListItems = listItems;
	}

	@Override  
	public View getView(int position, View view, ViewGroup viewGroup)
	{
		LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
        view = inflater.inflate(R.layout.manage_player_entry, viewGroup, false);
		
        TextView textPlayersName  = (TextView)view.findViewById(R.id.managePlayersName);
        TextView textPlayersScore = (TextView)view.findViewById(R.id.managePlayersScore);
        
		Player p = mListItems.get(position);
	
		int points = p.getPointTotal();

		if (p.isLeader()) {
			textPlayersName.setText(Util.getInlineImageText(this.getContext(), p.getName() + " :*"));
		}
		else {
			textPlayersName.setText(p.getName());
		}
		
		textPlayersName.setTextColor(this.getContext().getResources().getColor(R.color.Cyan));
		textPlayersScore.setText(points + (points == 1 ? " point" : " points"));
		
		return view;
	}
	
	
}

