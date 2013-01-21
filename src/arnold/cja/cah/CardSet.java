package arnold.cja.cah;

import android.util.Log;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * A collection of Cards.
 * Used in many places- to hold the draw piles, the discard piles,
 * the player hands, and the collection of white cards in a Combo
 */
public class CardSet implements Iterable<Card>, java.io.Serializable {

   private static final long serialVersionUID = 1;
   private static final String TAG = "CardSet";

   private ArrayList<Card> mCards;
   private String          mName;

   // When serializing to file, a copy of the game state is made and
   // given to a background thread.  Certain objects need a deep copy while
   // some (CardSet) only require a shallow copy. 
   public CardSet(CardSet other) {
      mName = other.mName;
      mCards = new ArrayList<Card>(other.mCards);
   }

   public CardSet() {
      mName = "unnamed";
      mCards = new ArrayList<Card>();
   }

   public void add(Card card) {
      mCards.add(card);
   }

   public void remove(Card card) {
      mCards.remove(card);
   }

   public void setName(String name) {
      this.mName = name;
   }

   @Override
   public String toString() {
      return getName();
   }

   public String getName() {
      return mName;
   }

   public Card pop() {
      int lastIndex = mCards.size() - 1;
      Card c = mCards.get(lastIndex);
      mCards.remove(lastIndex);
      return c;
   }

   public void shuffle() {
      Collections.shuffle(mCards);
   }

   public void reverse() {
      Collections.reverse(mCards);
   }

   public void sort() {
      Collections.sort(mCards, new Comparator<Card>() {
         public int compare(Card one, Card other) {
            return one.getText().compareTo(other.getText());
         }
      }); 
   }

   // Unfortunately we need this so that the mCards can be given to 
   // an ArrayAdapter
   public ArrayList<Card> getAsArrayList() {
      return mCards;
   }

   public int size() { return mCards.size(); }
   public boolean empty() { return mCards.size() == 0; }

   public Iterator<Card> iterator() {
      return mCards.iterator();
   }

   // In an attempt to speed up serialization, I tried using Externalizable
   // and implementing readExternal and writeExternal for each object. 
   // Testing showed no improvement whatsoever over default serialization!?
   // Keeping this here for now in case I come back to it.
   public void readExternal(ObjectInput input) throws IOException,
   ClassNotFoundException {
      mName = input.readUTF();
      int size = input.readInt();
      for(int i = 0; i < size; ++i) {
         Card c = (Card)input.readObject();
         mCards.add(c);
      }

   }

   public void writeExternal(ObjectOutput output) throws IOException {
      Log.i(TAG, "Writing CardSet: " + mName);
      output.writeUTF(mName);
      int size = mCards.size();
      output.writeInt(size);
      for(Card c : mCards) {
         output.writeObject(c);
      }

   }	
}
