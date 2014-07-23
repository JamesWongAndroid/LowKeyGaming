package rss.reader.lowkeygaming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

public class LowKeySplash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashlayout);
		
		CountDownTimer countDownSplash = new CountDownTimer(1000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinish() {
				Intent mainIntent = new Intent(getApplicationContext(), BlogPostListScreen.class);
				startActivity(mainIntent);
				finish();
			}
		};
		
		countDownSplash.start();
	}

}
