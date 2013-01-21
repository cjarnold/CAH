package arnold.cja.cah;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import android.text.Html;

/**
 * A container to hold a single black card and 0 or more
 * white cards that are used to fill in the blanks of the black card.
 * Each turn, all Players must submit a Combo and the Card Czar selects
 * the best Combo as the round winner.
 */
public class Combo implements java.io.Serializable {
	
	private static final long serialVersionUID = 1;
	
	private Card    mBlackCard;
	private CardSet mWhiteCards;
	private Player  mPlayer;
	
	public Combo(Card blackCard, Player player) {
		mBlackCard = blackCard;
		mWhiteCards = new CardSet();
		mPlayer = player;
	}
	
	// When serializing to file, a copy of the game state is made and
	// given to a background thread.  Certain objects need a deep copy while
	// some (CardSet) only require a shallow copy. 
	public Combo(Combo other, Player player) {	
		// TODO: is this ok since we are referencing a reference to the old black card
		mBlackCard = other.mBlackCard;
		mWhiteCards = new CardSet(other.mWhiteCards);
		mPlayer = player;
	}
	
	public boolean empty() {
		return mWhiteCards.empty();
	}
	
	public Card getBlack() {
		return mBlackCard;
		
	}
	public Card popWhite() {
		return mWhiteCards.pop();
	}
	
	public boolean isComplete() {
		return (mWhiteCards.size() >= mBlackCard.getRequiredWhiteCardCount());
	}
	
	public void addWhiteCard(Card whiteCard) {
		mWhiteCards.add(whiteCard);
	}

	public Player getPlayer() { return mPlayer; }
		
	public CharSequence getStyledStatement() {
	
		String text = new String(mBlackCard.getText());
		
		for(Card c : mWhiteCards) {
			text = text.replaceFirst(Card.UNDERSCORES, "<font color='" + Card.UNDERSCORE_COLOR + "'>" + c.getText() + "</font>");
		}
		
		text = text.replaceAll(Card.UNDERSCORES, "<u><font color='" + Card.UNDERSCORE_COLOR + "'>" + Card.spaces + "</font></u>");
		return Html.fromHtml(text);
	}
	
	// Don't count the black card because it is in multiple combos
	public int countCards() {
		return mWhiteCards.size();
	}

	// In an attempt to speed up serialization, I tried using Externalizable
	// and implementing readExternal and writeExternal for each object. 
	// Testing showed no improvement whatsoever over default serialization!?
	// Keeping this here for now in case I come back to it.
	public Combo() { }
	public void readExternal(ObjectInput input) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		mBlackCard = (Card)input.readObject();
		mWhiteCards = (CardSet)input.readObject();
		mPlayer = (Player)input.readObject();
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		
		output.writeObject(mBlackCard);
		output.writeObject(mWhiteCards);
		output.writeObject(mPlayer);
	}
	
	
}
