package arnold.cja.cah;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.res.AssetManager;
import android.util.Log;
import arnold.cja.cah.Card.CardType;

/**
 * The Deck class loads Cards into CardSets by loading
 * all json files in the assets/cards/black and assets/cards/white directories.
 * Deck is responsible for maintaining the draw and discard piles as
 * well as holding a reference to the current black card
 */
public class Deck implements java.io.Serializable {

   private static final long serialVersionUID = 2;
   private static final String TAG = "Deck";

   private ArrayList<CardSet> mWhiteCardSets;
   private ArrayList<CardSet> mBlackCardSets;

   private CardSet mBlackDrawPile;
   private CardSet mWhiteDrawPile;
   private CardSet mWhiteDiscard;
   private CardSet mBlackDiscard;
   private Card    mCurrentBlack;
   private int     mBlackCardCount;
   private int     mWhiteCardCount;

   public Deck() {
      mWhiteCardSets = new ArrayList<CardSet>();
      mBlackCardSets = new ArrayList<CardSet>();

      mBlackDrawPile = new CardSet();
      mWhiteDrawPile = new CardSet();
      mWhiteDiscard = new CardSet();
      mBlackDiscard = new CardSet();

      mBlackCardCount = 0;
      mWhiteCardCount = 0;
   }

   // When serializing to file, a copy of the game state is made and
   // given to a background thread.  Certain objects need a deep copy while
   // some (CardSet) only require a shallow copy. 
   public Deck(Deck other) {
      mWhiteCardSets = other.mWhiteCardSets;
      mBlackCardSets = other.mBlackCardSets;
      mBlackDrawPile = new CardSet(other.mBlackDrawPile);
      mWhiteDrawPile = new CardSet(other.mWhiteDrawPile);
      mWhiteDiscard = new CardSet(other.mWhiteDiscard);
      mBlackDiscard = new CardSet(other.mBlackDiscard);
      mCurrentBlack = other.mCurrentBlack;
      mBlackCardCount = other.mBlackCardCount;
      mWhiteCardCount = other.mWhiteCardCount;
   }

   public boolean loadFile(AssetManager assetManager, String filename, CardType cardType) {
      BufferedReader br = null;
      try {
         br = new BufferedReader(new InputStreamReader(assetManager.open(filename)));
      } catch (IOException e) {
         Log.i(TAG, "Couldn't construct BufferedReader with filename [" + filename + "]");
         e.printStackTrace();
      }

      String line;
      CardSet cardSet = new CardSet();

      StringBuilder sb = new StringBuilder();

      try {
         while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      } 

      try{
         JSONObject jObject = new JSONObject(sb.toString());

         cardSet.setName(jObject.getString("cardSetName"));
         JSONArray jArray = jObject.getJSONArray("cards");
         for (int i=0; i < jArray.length(); ++i) {
            JSONObject cardAsJSON = jArray.getJSONObject(i);
            Card c = new Card(cardType, cardAsJSON);
            cardSet.add(c);
            if (cardType == CardType.BLACK) {
               ++mBlackCardCount;
            }
            else {
               ++mWhiteCardCount;
            }
         }
      }
      catch(Exception e) {
         e.printStackTrace();
         Log.i(TAG, "Error loading json");
         return false;
      }

      cardSet.sort();

      if (cardType == CardType.BLACK) {
         mBlackCardSets.add(cardSet);
      }
      else {
         mWhiteCardSets.add(cardSet);
      }

      Log.i(TAG, "Finished loading file " + filename);

      return true;
   }

   public void reset(CardSet live, CardSet discard) {
      while(!discard.empty()) {
         live.add(discard.pop());
      }

      live.shuffle();
   }

   public void reset() {
      reset(mWhiteDrawPile, mWhiteDiscard);
      reset(mBlackDrawPile, mBlackDiscard);
      setNextBlack();
   }

   public Card nextWhiteCard() {
      if (mWhiteDrawPile.empty()) {
         reset(mWhiteDrawPile, mWhiteDiscard);
      }

      return mWhiteDrawPile.pop();
   }

   private Card nextBlackCard() {
      if (mBlackDrawPile.empty()) {
         reset(mBlackDrawPile, mBlackDiscard);
      }

      return mBlackDrawPile.pop();
   }

   public void discard(Card card) {
      if (card.getCardType() == CardType.BLACK) {
         mBlackDiscard.add(card);
      }
      else {
         mWhiteDiscard.add(card);
      }

   }

   public void setNextBlack() {
      if (mCurrentBlack != null) {
         discard(mCurrentBlack);
      }
      mCurrentBlack = nextBlackCard();
   }

   public void setup() {
      mWhiteDrawPile = new CardSet();

      for (CardSet cs : mWhiteCardSets) {
         for (Card c : cs) {
            mWhiteDrawPile.add(c);
         }
      }

      mBlackDrawPile = new CardSet();

      for (CardSet cs : mBlackCardSets) {
         for (Card c : cs) {
            mBlackDrawPile.add(c);
         }
      }

      mWhiteDrawPile.shuffle();
      mBlackDrawPile.shuffle();

      setNextBlack();
   }

   public Card getCurrentBlack() {
      return mCurrentBlack;
   }

   public int whiteCardsAvailable() { 
      return mWhiteDrawPile.size() + mWhiteDiscard.size();
   }

   public ArrayList<CardSet> getCardSets(CardType cardType) {
      if (cardType == CardType.BLACK) {
         return mBlackCardSets;
      }
      else {
         return mWhiteCardSets;
      }
   }

   public int getCardCount() {
      return mBlackCardCount + mWhiteCardCount;
   }

   public int countCards() {
      return mBlackDrawPile.size() + mWhiteDrawPile.size() + mBlackDiscard.size() + mWhiteDiscard.size() + (mCurrentBlack == null ? 0 : 1);
   }

   // In an attempt to speed up serialization, I tried using Externalizable
   // and implementing readExternal and writeExternal for each object. 
   // Testing showed no improvement whatsoever over default serialization!?
   // Keeping this here for now in case I come back to it.
   public void readExternal(ObjectInput input) throws IOException,
   ClassNotFoundException {
      int size = input.readInt();
      for(int i = 0; i < size; ++i) {
         mWhiteCardSets.add((CardSet)input.readObject());
      }

      size = input.readInt();
      for(int i = 0; i < size; ++i) {
         mBlackCardSets.add((CardSet)input.readObject());
      }

      mBlackDrawPile = (CardSet)input.readObject();
      mWhiteDrawPile = (CardSet)input.readObject();
      mWhiteDiscard = (CardSet)input.readObject();
      mBlackDiscard = (CardSet)input.readObject();
      mCurrentBlack = (Card)input.readObject(); 

   }

   public void writeExternal(ObjectOutput output) throws IOException {

      int size = mWhiteCardSets.size();
      output.writeInt(size);
      Log.i(TAG, "Writing Deck");
      for(CardSet cs : mWhiteCardSets) {
         output.writeObject(cs);
      }

      size = mBlackCardSets.size();
      output.writeInt(size);
      for(CardSet cs : mBlackCardSets) {
         output.writeObject(cs);
      }

      output.writeObject(mBlackDrawPile);
      output.writeObject(mWhiteDrawPile);
      output.writeObject(mWhiteDiscard);
      output.writeObject(mBlackDiscard);
      output.writeObject(mCurrentBlack);
   }
}
