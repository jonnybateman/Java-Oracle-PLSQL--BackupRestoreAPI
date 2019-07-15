package com.development.smartlist;

import com.journeyapps.barcodescanner.CaptureActivity;

public class ActivityScanOrientationPortrait extends CaptureActivity {

    @Override
    public void onBackPressed() {

        // Calling the global application class
        GlobalClass globalObject = (GlobalClass) getApplicationContext();

        // If we were adding items to the list via the barcode scanner then set flag to false to stop.
        if (globalObject.getBarcodeScannerFlag()) {
            globalObject.setBarcodeScannerFlag(false);
        }

        // Continue with default actions of onBackPressed() method.
        super.onBackPressed();
    }

}
