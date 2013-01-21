package arnold.cja.cah;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import android.util.Log;

/**
 * Represents one player (either human or AI)
 * Players store a CardSet which represents their hand of white cards
 * They also store a Combo which is updated as they select cards for
 * submission to the Card Czar.
 */
public class Player implements java.io.Serializable {

   private static final long serialVersionUID = 4;
   private static final String TAG = "Player";

   private CardSet mWhiteHand;
   private String  mName;
   private Combo   mComboInProgress;
   private int     mPointTotal;
   private boolean mHasPickedWhite;
   private boolean mIsAI;
   private boolean mIsLeader;
   private String  mLastWinText;

   public Player(String name) {
      mName = name;
      mPointTotal = 0;
      mHasPickedWhite = false;
      mIsLeader = false;
      mIsAI = false;
      Log.i(TAG, "Constructing player " + name );
      mWhiteHand = new CardSet();
      mComboInProgress = new Combo(LaunchActivity.gm.getDeck().getCurrentBlack(), this);
      mLastWinText = "No Wins Yet!";
   }

   // When serializing to file, a copy of the game state is made and
   // given to a background thread.  Certain objects need a deep copy while
   // some (CardSet) only require a shallow copy. 
   public Player(Player other) {
      mWhiteHand = new CardSet(other.mWhiteHand);
      mName = other.mName;
      mPointTotal = other.mPointTotal;
      mComboInProgress = new Combo(other.mComboInProgress, this);
      mHasPickedWhite = other.mHasPickedWhite;
      mIsAI = other.mIsAI;
      mIsLeader = other.mIsLeader;
      mLastWinText = other.mLastWinText;
   }

   public void setLeader(boolean leader) { mIsLeader = leader; }
   public boolean isLeader() { return mIsLeader; }

   public void reset() {
      while(!mWhiteHand.empty()) {
         LaunchActivity.gm.getDeck().discard(mWhiteHand.pop());
      }

      mPointTotal = 0;

      mHasPickedWhite = false;

      while(!mComboInProgress.empty()) {
         LaunchActivity.gm.getDeck().discard(mComboInProgress.popWhite());
      }
   }

   public String getLastWinText() {
      return mLastWinText;
   }

   public boolean isCardCzar() { 
      return this == LaunchActivity.gm.getCardCzar();
   }

   public void give(Card whiteCard) {
      mWhiteHand.add(whiteCard);
   }

   public void addWin(Combo combo) {
      mLastWinText = combo.getStyledStatement().toString();
      ++mPointTotal;
      LaunchActivity.gm.updateLeaders();
   }

   public Combo getComboInProgress() { 
      return mComboInProgress; 
   }

   public void resetCombo() { 
      if (mComboInProgress != null) {
         while(!mComboInProgress.empty()) {
            LaunchActivity.gm.getDeck().discard(mComboInProgress.popWhite());
         }
      }

      Card c = LaunchActivity.gm.getDeck().getCurrentBlack();
      mComboInProgress = new Combo(c, this);
   }

   public void satisfyComboAI() {		
      mWhiteHand.shuffle();

      while(!mComboInProgress.isComplete()) {
         Card c = mWhiteHand.pop();
         mComboInProgress.addWhiteCard(c);
      }

      mHasPickedWhite = true;
   }

   @Override
   public String toString() {
      String result = getName();

      if (LaunchActivity.gm.getCardCzar() == this) {
         result += " <font color='#FF00FF'>[Card Czar]</font>";
      }

      result += " <font color='#FFD700'>[" + Integer.toString(this.mPointTotal) + (this.mPointTotal == 1 ? " point" : " points") + "]</font>";
      return result;
   }


   public int getPointTotal() { return mPointTotal; }
   public String getName() {
      if (mIsAI) {
         return "AI: " + mName;
      }
      else {
         return mName;
      }
   }

   // In an attempt to speed up serialization, I tried using Externalizable
   // and implementing readExternal and writeExternal for each object. 
   // Testing showed no improvement whatsoever over default serialization!?
   // Keeping this here for now in case I come back to it.
   public Player() { }
   public void readExternal(ObjectInput input) throws IOException,
   ClassNotFoundException {

      mWhiteHand = (CardSet)input.readObject();
      mName = input.readUTF();
      mPointTotal = input.readInt();
      mComboInProgress = (Combo)input.readObject();
      mHasPickedWhite = input.readBoolean();
      mIsAI = input.readBoolean();
      mIsLeader = input.readBoolean();
      mLastWinText = input.readUTF();
   }

   public void writeExternal(ObjectOutput output) throws IOException {
      output.writeObject(mWhiteHand);
      output.writeUTF(mName);
      output.writeInt(mPointTotal);
      output.writeObject(mComboInProgress);
      output.writeBoolean(mHasPickedWhite);
      output.writeBoolean(mIsAI);
      output.writeBoolean(mIsLeader);
      output.writeUTF(mLastWinText);
   }

   public boolean hasPickedWhite() {
      return mHasPickedWhite;
   }

   public void setHasPickedWhite(boolean mHasPickedWhite) {
      this.mHasPickedWhite = mHasPickedWhite;
   }

   public boolean isAI() {
      return mIsAI;
   }

   public void setIsAI(boolean mIsAI) {
      this.mIsAI = mIsAI;
   }

   public CardSet getWhiteHand() {
      return mWhiteHand;
   }

   public int countCards() {
      return mWhiteHand.size() + mComboInProgress.countCards();
   }

}
