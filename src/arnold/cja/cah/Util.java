package arnold.cja.cah;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Toast;
import arnold.cja.cah.R;

/**
 * Class to hold static utility functions
 */
public class Util {

   private static final String TAG = "Util";
   private static final String GAME_MANAGER_SERIALIZED_FILE = "GameManager.ser";

   public static int countOccurrences(String haystack, char needle)
   {
      int count = 0;
      for (int i=0; i < haystack.length(); i++)
      {
         if (haystack.charAt(i) == needle)
         {
            count++;
         }
      }
      return count;
   }

   public static int countOccurrences(String str, String sub) {
      Pattern p = Pattern.compile(sub);
      Matcher m = p.matcher(str);
      int count = 0;
      while (m.find()){
         ++count;
      }
      return count;
   }

   public static void toast(Context context, String msg) {
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
   }

   /**
    * Given either a Spannable String or a regular String and a token, apply
    * the given CharacterStyle to the span between the tokens, and also
    * remove tokens.
    * <p>
    * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
    * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence
    * {@code "Hello world!"} with {@code world} in red.
    *
    * @param mText The mText, with the tokens, to adjust.
    * @param token The token string; there should be at least two instances
    *             of token in mText.
    * @param cs The style to apply to the CharSequence. WARNING: You cannot
    *            send the same two instances of this parameter, otherwise
    *            the second call will remove the original span.
    * @return A Spannable CharSequence with the new style applied.
    *
    * @see http://developer.android.com/reference/android/mText/style/CharacterStyle.html
    */
   public static CharSequence setSpanBetweenTokens(CharSequence text,
         String token, StyleType... styles)
   {
      // Start and end refer to the points where the span will apply
      int tokenLen = token.length();
      int start = text.toString().indexOf(token) + tokenLen;
      int end = text.toString().indexOf(token, start);

      // Copy the spannable string to a mutable spannable string
      SpannableStringBuilder ssb = new SpannableStringBuilder(text);

      while (start > -1 && end > -1)
      {
         for (StyleType s : styles) {
            CharacterStyle c = styleFactory(s);
            ssb.setSpan(c, start, end, 0);
         }

         // Delete the tokens before and after the span
         ssb.delete(end, end + tokenLen);
         ssb.delete(start - tokenLen, start);

         text = ssb;

         start = text.toString().indexOf(token) + tokenLen;
         end = text.toString().indexOf(token, start);
      }

      return text;
   }

   public enum StyleType {
      UNDERLINE,
      FOREGROUND_RED,
      FOREGROUND_GREEN,
      FOREGROUND_CYAN,
      FOREGROUND_MAGENTA
   }

   public static CharacterStyle styleFactory(StyleType style) {
      switch(style) {
      case UNDERLINE:
         return new UnderlineSpan();
      case FOREGROUND_RED:
         return new ForegroundColorSpan(Color.RED);
      case FOREGROUND_GREEN:
         return new ForegroundColorSpan(Color.GREEN);
      case FOREGROUND_CYAN:
         return new ForegroundColorSpan(Color.CYAN);
      case FOREGROUND_MAGENTA:
         return new ForegroundColorSpan(Color.MAGENTA);
      default:
         return new UnderlineSpan();
      }
   }

   private static final HashMap<String, Integer> images = new HashMap<String, Integer>();
   static {
      images.put(":*", R.drawable.leader);
   }

   public static Spannable getInlineImageText(Context context, String text) {

      SpannableStringBuilder builder = new SpannableStringBuilder(text);
      int index;

      for (index = 0; index < builder.length(); index++) {
         for (Entry<String, Integer> entry : images.entrySet()) {
            int length = entry.getKey().length();
            if (index + length > builder.length())
               continue;
            if (builder.subSequence(index, index + length).toString().equals(entry.getKey())) {
               builder.setSpan(new ImageSpan(context, entry.getValue()), index, index + length,
                     Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
               index += length - 1;
               break;
            }
         }
      }
      return builder;
   }

   /**
    * Constructs and initializes the GameManager object if it is not already available.
    * 
    * When loading the app, there are a few situations to handle concerning GameManager:<p>
    * <ul>
    *   <li> The app will load into the last Activity that was used, but GameManager might be null.
    *     Therefore, every Activity's onCreate needs to call this method to ensure the GameManager
    *     is constructed and loaded.</li>  
    *   <li> Even if an Activity is destroyed and recreated, GameManager can still exist (it is a static).
    *     Therefore there are some cases where we don't need to do anything here.</li>
    *   <li> If for some reason the GameManager cannot be loaded from disk, the activity stack is cleared
    *     so that we go back to LaunchActivity with a fresh GameManager.</li>
    * </ul>
    * 
    * @param activity The Activity in which onCreate is being called
    * @return         true if the GameManager is constructed ok, false if the GameManager failed to construct
    */
   public static boolean constructGameManagerIfNecessary(Activity activity) {
      if (LaunchActivity.gm == null) {
         Log.i(TAG, "gm is null in constructGameManagerIfNecessary so we need to load from file");

         LaunchActivity.gm = Util.loadState(activity);

         if (LaunchActivity.gm == null) {
            // todo: show an alert dialog telling the user that save data was lost
            // and they'll be going back to the home screen
            Log.i(TAG, "loadState didn't work so using new");
            LaunchActivity.gm = new GameManager();
            LaunchActivity.gm.setupGame(activity.getAssets());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String cardsDealt = prefs.getString(PrefsActivity.PREF_CARD_COUNT, "10");
            LaunchActivity.gm.setCardsInHand(Integer.parseInt(cardsDealt));

            // todo: test this
            if (! (activity instanceof LaunchActivity)) {
               Intent intent = new Intent(activity, LaunchActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               Log.i(TAG, "About to start LaunchActivity and clear the activity stack");
               Util.startActivity(activity, intent);
               return false;
            }
         }
         else {
            Log.i(TAG, "Loaded GameManager from disk");
         }
      }
      else {
         Log.i(TAG, "gm is already constructed!");
      }

      return true;
   }

   public static void startActivity(Activity activity, Intent intent) {
      LaunchActivity.gm.setLeavingActivity();
      activity.startActivity(intent);
   }

   public static void startActivityForResult(Activity activity, Intent intent, int requestCode) {
      LaunchActivity.gm.setLeavingActivity();
      activity.startActivityForResult(intent, requestCode);
   }

   /**
    * Save state if the user may be leaving the app.
    * The android standard is to save state in an Activity's onPause()
    * method.  Because CAH is composed of movement between many activities,
    * onPause will trigger hundreds of times per game.  Since saving state is
    * an expensive operation, we will NOT save if we know that we are just
    * moving between Activities within CAH.  On the other hand, if we cannot
    * detect that we are moving between Activities internal to CAH, then the
    * save to file will be triggered.  Unfortunately android does not provide
    * a way to detect if we are leaving an Activity to go and do something else on 
    * the device.  Therefore each CAH Activity must notify the GameManager if
    * it is about to either finish or trigger the start of a new Activity by 
    * calling {@link GameManager#setLeavingActivity()}
    * 
    * @param context A reference to the calling Activity
    * @see   Util#asyncSaveState(Context)
    * @see   GameManager#setLeavingActivity()
    */
   public static void saveStateIfLeavingApp(Context context) {
      if (!LaunchActivity.gm.getLeavingActivity()) {
         Log.i(TAG, "Application being sent to background");
         LaunchActivity.gm.resetLeavingActivity();
         asyncSaveState(context);
      }
      else {
         Log.i(TAG, "Application is still running");
         LaunchActivity.gm.resetLeavingActivity();
      }
   }

   /**
    * Use a background thread to save the game state to file.
    * Since serialization is a relatively slow procedure (upwards of 1 second),
    * the serialization needs to happen apart from the UI thread.  To prevent race
    * conditions, a copy of the GameManager is made and handed off to the AsyncTask.
    * <p>TODO: Prevent multiple SaveStateTasks from running at the same time.
    * 
    * @param context  A reference to the calling Activity
    */
   public static void asyncSaveState(Context context) {
      GameManager gmCopy = new GameManager(LaunchActivity.gm);
      GameManagerAndContext gmAndContext = new GameManagerAndContext(gmCopy, context);
      new SaveStateTask().execute(gmAndContext);
   }

   /**
    * This is run in a background thread therefore it CANNOT access UI elements
    * 
    * @param gm      The GameManager which will be serialized and saved
    * @param context A reference to the calling Activity
    * @return        How long it took to save, in milliseconds
    */
   public static long saveState(GameManager gm, Context context) {
      try {
         long startTime = System.currentTimeMillis();

         FileOutputStream fileOut = context.openFileOutput(GAME_MANAGER_SERIALIZED_FILE, Context.MODE_PRIVATE);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(gm);
         out.close();
         fileOut.close();
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;

         Log.i(TAG, "Saved (" + duration + " ms)");
         return duration;

      } catch(IOException i) {
         Log.i(TAG, "ERROR saving GameManager to disk");

         i.printStackTrace();
         return 0;
      }
   }

   public static GameManager loadState(Context context) {
      try {

         long startTime = System.currentTimeMillis();

         // TODO: If the file does not exist, then assume this is the first time
         // they have played the game.  We want to start fresh and NOT show an error msg
         FileInputStream fileIn = context.openFileInput(GAME_MANAGER_SERIALIZED_FILE);

         ObjectInputStream in = new ObjectInputStream(fileIn);
         Log.i(TAG, "About to readObject");
         GameManager result = (GameManager) in.readObject();
         Log.i(TAG, "Read object");
         in.close();
         fileIn.close();
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
         Util.toast(context, "Loaded (" + duration + " ms)");
         return result;
      }catch(IOException i) {
         Log.i(TAG, "IO error during GameManager load: First time playing?");
         return null;
      }catch(ClassNotFoundException c) {
         Log.i(TAG, "GameManager class not found during deserialization");
         Util.toast(context, "Error encounted during load");
         c.printStackTrace();
         return null;
      }
   }

   public static void assertGameState(Activity activity, String comingFrom) {
      // count all black and white cards in all locations and verify that the counts
      // are the same as when we started

      Deck d = LaunchActivity.gm.getDeck();

      int cardCount = d.countCards();
      for(Player p : LaunchActivity.gm.getPlayers()) {
         cardCount += p.countCards();
      }

      if (cardCount != d.getCardCount()) {
         Log.i(TAG, "Card State Failure: Expected: " + d.getCardCount() + ", Actual: " + cardCount + ", Note: " + comingFrom);
      }
      else {
         Log.i(TAG, "Card State OK: Expected: " + d.getCardCount() + ", Actual: " + cardCount + ", Note: " + comingFrom);
      }
   }

}


