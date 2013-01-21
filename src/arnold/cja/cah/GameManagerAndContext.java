package arnold.cja.cah;

import android.content.Context;

/**
 * AsyncTask (used by SaveStateTask) needs to be passed two objects:
 * the GameManager and the Context.  Since java does not have a Pair class, 
 * we must make our own.
 */
public class GameManagerAndContext {

   public GameManager mGm;
   public Context mContext;

   public GameManagerAndContext(GameManager gm, Context context) {
      mGm = gm;
      mContext = context;
   }
}
