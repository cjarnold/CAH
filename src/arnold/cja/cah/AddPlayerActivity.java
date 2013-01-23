package arnold.cja.cah;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import arnold.cja.cah.R;

/**
 * This activity is started as a dialog where the user can enter
 * a player name and select whether or not they want that player to
 * be controlled by AI 
 */
public class AddPlayerActivity extends Activity {

   private static final String TAG               = "AddPlayerActivity";
   public  static final String EXTRA_IS_AI       = "IsAI";
   public  static final String EXTRA_PLAYER_NAME = "PlayerName";

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (!Util.constructGameManagerIfNecessary(this)) { return; }

      this.setContentView(R.layout.add_player);

      LayoutParams params = getWindow().getAttributes();
      params.width = LayoutParams.FILL_PARENT;
      getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
      
      Util.assertGameState(this, "AddPlayerActivity::onCreate");
   }

   public void OKAddPlayer(View v) {
      EditText edit   = (EditText)findViewById(R.id.playername);
      String playerName = edit.getText().toString();
      if (playerName.length() > 0) {
         Intent intent=new Intent();
         CheckBox checkBox = (CheckBox) findViewById(R.id.chk_is_ai);

         intent.putExtra(EXTRA_IS_AI,       checkBox.isChecked());
         intent.putExtra(EXTRA_PLAYER_NAME, playerName);

         setResult(RESULT_OK, intent);
         LaunchActivity.gm.setLeavingActivity();
         finish();
      }
   }

   public void CancelAddPlayer(View v) {
      setResult(RESULT_CANCELED);
      LaunchActivity.gm.setLeavingActivity();
      finish();
   }

   @Override
   protected void onPause() {
      super.onPause();
      Log.i(TAG, "AddPlayerActivity::onPause");
      Util.saveStateIfLeavingApp(this);
   }
   
   @Override
   protected void onResume() {
      super.onResume();
      Util.assertGameState(this, "AddPlayerActivity::onResume");
   }

   @Override
   public void onBackPressed() {
      LaunchActivity.gm.setLeavingActivity();
      super.onBackPressed();
   }
}
