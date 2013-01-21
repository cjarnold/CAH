package arnold.cja.cah;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import arnold.cja.cah.R;

/**
 * Represents a single card (black and white cards both use this
 * same class.  The type is signified by the CardType enum).
 */
public class Card implements java.io.Serializable {

   private static final long serialVersionUID = 1;
   private static final String TAG = "Card";
   public  static final String UNDERSCORES = "___";
   public  static final String UNDERSCORE_COLOR = "#00FFFF";

   // Black cards have fill in the blank underlines in them.  These are 
   // rendered by underlining a series of non breaking spaces in html
   public static final String  spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
         "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
         "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
         "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
         "&nbsp;&nbsp;&nbsp;";

   public enum CardType {BLACK, WHITE};

   private String   mText;
   private String   mPhraseToDefine;
   private CardType mCardType;
   private int      mRequiredWhiteCardCount;

   public Card(CardType cardType, JSONObject cardAsJSON) {
      try {
         initializeCard(cardType, cardAsJSON);
      } catch(Exception e){
         e.printStackTrace();
         Log.i(TAG, "Couldn't construct card");
      }
   }

   /**
    * Initializes a Card from JSON
    *
    * Here is the longer description.
    *
    * @param  cardType   whether this is a white or black card
    * @param  cardAsJSON json representation of the card
    * @return No return
    */
   public void initializeCard(CardType cardType, JSONObject cardAsJSON) throws JSONException {
      String rawText = cardAsJSON.getString("text");

      mCardType = cardType;

      if (cardType == CardType.BLACK) {
         mText = rawText;
         int numUnderscores = Util.countOccurrences(mText, UNDERSCORES);
         mRequiredWhiteCardCount = (numUnderscores == 0 ? 1 : numUnderscores);
      }
      else {
         // Get rid of periods in white cards
         mText = rawText.replaceAll("\\.", "");
         mRequiredWhiteCardCount = 0;
      }

      // Brackets around a phrase mean it is the phrase to use
      // when the user wants to define the card.  Alternately, specify
      // definitionText as another field in the json.
      mPhraseToDefine = cardAsJSON.optString("definitionText");

      if (mPhraseToDefine.length() == 0) {
         Pattern pattern = Pattern.compile("\\[(.+)\\]");
         Matcher matcher = pattern.matcher(mText);
         while (matcher.find()) {
            mPhraseToDefine = matcher.group(1);
         }
      }

      mText = mText.replaceAll("\\[", "");
      mText = mText.replaceAll("\\]", "");
   }

   @Override
   public String toString() {
      return mText;
   }

   public String getTextToDefine() { 
      if (mPhraseToDefine.length() == 0) {
         // remove ____ and extra spaces
         String returnText = mText.replaceAll(UNDERSCORES, "");
         return returnText;
      }
      else {
         return mPhraseToDefine;
      }
   }
   public int getRequiredWhiteCardCount() {
      return mRequiredWhiteCardCount;
   }

   public Intent getDefinitionIntent(int provider) {
      String url = "";

      switch(provider) {
      case R.id.define_google:
         url = "http://www.google.com/search?q=%s";
         break;
      case R.id.define_ud:
         url = "http://m.urbandictionary.com/#define?term=%s";
         break;
      default:
         url = "";
         break;
      }

      String requestURL = String.format(url, Uri.encode(this.getTextToDefine()));
      Log.i(TAG, "The URL is [" + requestURL + "]");
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestURL));
      return browserIntent;

   }


   public CharSequence getStyledStatement() {

      if (this.mCardType == CardType.WHITE) {
         return this.mText;
      }
      else {
         String text = new String(mText);
         text = text.replaceAll(Card.UNDERSCORES, "<u><font color='" + UNDERSCORE_COLOR + "'>" + Card.spaces + "</font></u>");			
         return Html.fromHtml(text);
      }

   }

   // In an attempt to speed up serialization, I tried using Externalizable
   // and implementing readExternal and writeExternal for each object. 
   // Testing showed no improvement whatsoever over default serialization!?
   // Keeping this here for now in case I come back to it.
   public Card() { }
   public void readExternal(ObjectInput in) throws IOException,
   ClassNotFoundException {
      mRequiredWhiteCardCount = in.readInt();
      mCardType = (CardType)in.readObject();
      mPhraseToDefine = in.readUTF();
      mText = in.readUTF();
   }

   public void writeExternal(ObjectOutput out) throws IOException {

      out.writeInt(mRequiredWhiteCardCount);
      out.writeObject(mCardType);
      out.writeUTF(mPhraseToDefine);
      out.writeUTF(mText);
   }

   public String getText() {
      return mText;
   }

   public void setText(String text) {
      mText = text;
   }

   public CardType getCardType() {
      return mCardType;
   }

   public void setCardType(CardType cardType) {
      this.mCardType = cardType;
   }
}
