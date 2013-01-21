package arnold.cja.cah;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

/**
 * A class to extend Application so that ACRA (crash reporter) can be
 * initialized on startup
 */
@ReportsCrashes(formKey="dDI2X2dwVDY2dHA4UlVtTXVvQ3dRcFE6MQ",
mode = ReportingInteractionMode.TOAST,
forceCloseDialogAfterToast = false, // optional, default false
resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
   @Override
   public void onCreate() {
      super.onCreate();

      // The following line triggers the initialization of ACRA
      ACRA.init(this);
   }
}
