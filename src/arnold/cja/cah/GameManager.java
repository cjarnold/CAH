package arnold.cja.cah;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import android.content.res.AssetManager;
import android.util.Log;
import arnold.cja.cah.Card.CardType;

/**
 * GameManager is the class which contains all game state and
 * has methods to implement the rules and flow of the game.
 * When saving or loading state to/from file the entire GameManager
 * object is serialized/deserialized.
 */
public class GameManager implements java.io.Serializable {

   private static final long serialVersionUID = 10;
   private static final String TAG = "GameManager";

   // The game state is defined by the following:
   // This information is saved to file when the app looses focus
   // ===============================================================

   // A list of players participating
   // Each player holds a hand of cards and a current combo
   private ArrayList<Player> mPlayers;

   // The deck of cards
   private Deck mDeck;

   // The index of the Card Czar in the players list
   private int mCardCzarIndex;

   // Round number
   private int mRoundNumber;

   // Whether we are in the middle of the current round or not
   private boolean mRoundHasStarted;

   // The player that is currently taking his/her turn
   int mActivePlayerIndex;

   // The number of cards that each player should have at the beginning
   // of each turn
   private int mCardsInFullHand;

   // Whether we are intentionally about to leave the current activity.
   // This can be used by the onPause() methods to know whether to save
   // state or not (we save state only when we are NOT intentionally pausing)
   private boolean mLeavingActivity;

   // ===============================================================

   public GameManager() {
      mCardCzarIndex = 0;
      mRoundNumber = 1;
      mRoundHasStarted = false;
      mActivePlayerIndex = 0;
      mCardsInFullHand = 10;
      mLeavingActivity = false;
      mPlayers = new ArrayList<Player>();
      mDeck = new Deck();
   }

   // When serializing to file, a copy of the game state is made and
   // given to a background thread.  Certain objects need a deep copy while
   // some (CardSet) only require a shallow copy.  The copying is handled in
   // all the copy constructors starting here:
   public GameManager(GameManager other) {	
      mPlayers = new ArrayList<Player>();
      for(int i = 0; i < other.mPlayers.size(); ++i) {
         mPlayers.add(new Player(other.mPlayers.get(i)));
      }

      mDeck = new Deck(other.mDeck);
      mCardCzarIndex = other.mCardCzarIndex;
      mRoundNumber = other.mRoundNumber;
      mRoundHasStarted = other.mRoundHasStarted;
      mActivePlayerIndex = other.mActivePlayerIndex;
      mCardsInFullHand = other.mCardsInFullHand;
      mLeavingActivity = other.mLeavingActivity;
   }

   public int getRoundNumber() { return mRoundNumber; }
   public boolean hasRoundStarted() { return mRoundHasStarted; }
   public void setRoundStarted() { mRoundHasStarted = true; }

   public void setActivePlayer(Player p) {
      mActivePlayerIndex = mPlayers.indexOf(p);
   }

   public Player getActivePlayer() { return mPlayers.get(mActivePlayerIndex); }

   public ArrayList<Player> getPlayers() { return mPlayers; }
   public Deck getDeck() { return mDeck; }

   public void setCardsInHand(int count) {
      mCardsInFullHand = count;
      Log.i(TAG, "Cards in hand is now " + mCardsInFullHand);

      for(Player p : mPlayers) {
         this.dealEnough(p);
      }
   }

   public void discardCombos() {
      // Discard all the white cards.
      // Black card is discarded when mDeck.setNextBlack() is called
      for(Player p : mPlayers) {
         Combo c = p.getComboInProgress();
         while(!c.empty()) {
            mDeck.discard(c.popWhite());
         }
      }
   }

   public void resetCombos() {
      Log.i(TAG, "Resetting combos");

      for(Player p : mPlayers) {
         p.resetCombo();
      }
   }

   /**
    * Reset everything except for the list of players who are participating
    */
   public void resetGame() {
      for(Player p : mPlayers) {
         p.reset();
      }

      mRoundNumber = 1;
      mRoundHasStarted = false;
      this.mCardCzarIndex = 0;
      fixCardCzar();

      // Move the discarded cards back into the draw piles
      mDeck.reset();

      for(Player p : mPlayers) {
         p.resetCombo();
      }

      updateLeaders();
   }

   public void setLeavingActivity() {
      mLeavingActivity = true;
   }

   public void resetLeavingActivity() {
      mLeavingActivity = false;
   }

   public boolean getLeavingActivity() {
      return mLeavingActivity;
   }

   public void updateLeaders() {
      int leaderScore = 0;

      for(Player p : mPlayers) {
         if (p.getPointTotal() > leaderScore) {
            leaderScore = p.getPointTotal();
         }
      }

      if (leaderScore > 0) {
         for (Player p : mPlayers) {
            p.setLeader(p.getPointTotal() == leaderScore);
         }		
      }
      else {
         for (Player p : mPlayers) {
            p.setLeader(false);
         }
      }
   }

   public void endOfRound() {
      discardCombos();
      rotateCardCzar();
      mDeck.setNextBlack();
      ++mRoundNumber;
      mRoundHasStarted = false;
      clearPickedPlayerFlags();
      resetCombos();
   }

   public ArrayList<Combo> getCombos() { 
      ArrayList<Combo> result = new ArrayList<Combo>();

      for(Player p : mPlayers) {
         Combo c = p.getComboInProgress();
         if (c.isComplete()) {
            result.add(c);
         }
      }

      return result;
   }

   public void setupGame(AssetManager am) {
      String[] whiteFiles = null;
      String[] blackFiles = null;

      try {
         whiteFiles = am.list("cards/white");
         blackFiles = am.list("cards/black");
      } catch (IOException e) {
         e.printStackTrace();
      }

      for (String filename : whiteFiles) {
         filename = "cards/white/" + filename;
         Log.i(TAG, "Found White [" + filename + "]");
         mDeck.loadFile(am, filename, CardType.WHITE);
      }

      for (String filename : blackFiles) {
         Log.i(TAG, "Found Black [" + filename + "]");
         filename = "cards/black/" + filename;
         mDeck.loadFile(am, filename, CardType.BLACK);
      }

      Log.i(TAG, "Finished loading files");

      mDeck.setup();
      createPlayers();
   }

   public boolean deal(Player player, int numWhiteCards) {
      if (mDeck.whiteCardsAvailable() < numWhiteCards) {
         Log.i(TAG, "Could not deal cards to " + player.getName() + 
               " because whiteCardsAvailable is not enough");
         return false;
      }
      else {
         Log.i(TAG, "Dealing " + Integer.toString(numWhiteCards) + " to " + player.getName());
         for(int i = 0; i < numWhiteCards; ++i) {
            player.give(mDeck.nextWhiteCard());
         }
      }
      return true;
   }

   public boolean dealEnough(Player player) {
      Log.i(TAG, "I need to deal until player has " + Integer.toString(mCardsInFullHand) + " cards");

      while(player.getWhiteHand().size() > mCardsInFullHand) {
         mDeck.discard(player.getWhiteHand().pop());
      }

      return deal(player, mCardsInFullHand - player.getWhiteHand().size());
   }

   
   /**
    * Create some example players the first time the user loads the game
    * That way they can try the game out without having to go to manage players
    */
   public void createPlayers() {

      for(int i = 0; i < 5; ++i) {
         Player p;
         if (i < 3) {
            p = new Player("Player " + (i + 1));
         }
         else {
            p = new Player("Rando Cardrissian " + (i-2));
            p.setIsAI(true);
         }

         dealEnough(p);
         mPlayers.add(p);
      }
   }

   public int getHumanPlayerCount() {
      int count = 0;
      for (Player p : mPlayers) {
         if (!p.isAI()) { ++count; }
      }

      return count;
   }

   // TODO: Other activities need to react appropriately when
   // key mPlayers are removed. 
   // Examples:
   // The card czar is removed, that means the card czar is rotated
   // If we are in the middle of a turn, the new card czar must discard any 
   // combos that were already in play.
   // If the new resulting player set has all the necessary combo submissions
   // then we should go straight to the select winner activity.
   // TODO: There are bugs when removing players in the middle of an activity
   // One such bug showed a blank list of combos.

   public void removePlayer(Player playerToRemove) {
      playerToRemove.reset();
      mPlayers.remove(playerToRemove);
      fixCardCzar();

      Player p = getCardCzar();

      if (p != null) {
         Log.i(TAG, "Removed " + playerToRemove.getName() + " the new card car is " + p.getName());
      }
      else {
         Log.i(TAG, "Removed " + playerToRemove.getName() + " there is no card czar");
      }

      LaunchActivity.gm.updateLeaders();
   }

   public void removeAllPlayers() {
      ArrayList<Player> players_copy = new ArrayList<Player>(mPlayers); 

      for (Player p : players_copy) {
         removePlayer(p);
      }
   }

   public boolean allPlayersSubmitted() {
      // check if all mPlayers have submitted
      boolean all_players_submitted = true;
      for(Player p : mPlayers) {
         if (!p.isCardCzar() && !p.hasPickedWhite()) { 
            all_players_submitted = false;
            break;
         }
      }

      return all_players_submitted;
   }

   public Player getCardCzar() {
      if (mCardCzarIndex < 0 || mPlayers.size() == 0) {
         Log.i(TAG, "null card czar");
         return null;
      }
      else {
         Log.i(TAG, "card czar index is " + mCardCzarIndex);
         return mPlayers.get(mCardCzarIndex);
      }
   }

   public void fixCardCzar() {
      Log.i(TAG, "Fixing card czar");

      if (getHumanPlayerCount() == 0) {
         mCardCzarIndex = -1;
         return;
      }
      else {
         // there's at least one human.  If mCardCzarIndex is -1 then 
         // set it to 0.

         Log.i(TAG, "cci before the mod = " + mCardCzarIndex);

         if (mCardCzarIndex < 0) { mCardCzarIndex = 0; }

         mCardCzarIndex = mCardCzarIndex % mPlayers.size();
         Log.i(TAG, "cci after the mod = " + mCardCzarIndex);

         Player p = getCardCzar();

         if (p != null && p.isAI()) {
            rotateCardCzar();
         }
      }
   }

   public void rotateCardCzar() {
      if (getHumanPlayerCount() == 0) {
         mCardCzarIndex = -1;
         return;
      }
      else {
         do {
            mCardCzarIndex = (mCardCzarIndex + 1) % mPlayers.size();
         } while (getCardCzar().isAI());
      }
   }

   public void clearPickedPlayerFlags() {
      for(Player p : mPlayers) {
         p.setHasPickedWhite(false);
      }
   }

   // In an attempt to speed up serialization, I tried using Externalizable
   // and implementing readExternal and writeExternal for each object. 
   // Testing showed no improvement whatsoever over default serialization!?
   // Keeping this here for now in case I come back to it.

   public void readExternal(ObjectInput input) throws IOException,
   ClassNotFoundException {
      int size = input.readInt();

      for(int i = 0; i < size; ++i) {
         mPlayers.add((Player)input.readObject());
      }

      mDeck = (Deck)input.readObject();
      mCardCzarIndex = input.readInt();
      mRoundNumber = input.readInt();
      mRoundHasStarted = input.readBoolean();
      mActivePlayerIndex = input.readInt();
      mCardsInFullHand = input.readInt();

   }

   public void writeExternal(ObjectOutput output) throws IOException {

      int size = mPlayers.size();
      output.writeInt(size);

      for(Player p : mPlayers) {
         output.writeObject(p);
      }

      output.writeObject(mDeck);
      output.writeInt(mCardCzarIndex);
      output.writeInt(mRoundNumber);
      output.writeBoolean(mRoundHasStarted);
      output.writeInt(mActivePlayerIndex);
      output.writeInt(mCardsInFullHand);	
   }	
}
