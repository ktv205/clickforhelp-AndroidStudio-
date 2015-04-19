package com.example.clickforhelp.controllers.ui;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.models.RequestParams;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends ActionBarActivity implements ServerResponse {
	// private static final String TAG = FeedbackActivity.class.getSimpleName();
	private static final int RESULT_OK = 1;
	private static final int FEEDBABK_EMPTY = 2;
	private String mFeedback;
	private Context mContext;
	private static final String FEEDBACK = "feedback";
	private static final String MESSAGE = "please wait while we send your feedback";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		setContentView(R.layout.activity_feedback);
		Button button = (Button) findViewById(R.id.feedback_button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = getFields();
				// String message;
				if (id == FEEDBABK_EMPTY) {
					Toast.makeText(FeedbackActivity.this,
							"feedback cannot be empty", Toast.LENGTH_SHORT)
							.show();
				} else {
					String[] paths = { FEEDBACK,
							CommonFunctions.getEmail(mContext), mFeedback };
					RequestParams params = CommonFunctions.setParams(paths,mContext);
					new CommonResultAsyncTask(mContext, MESSAGE, 0)
							.execute(params);
				}
			}
		});
	}

	public int getFields() {
		EditText feedbackEdittext = (EditText) findViewById(R.id.feedback_edittext);
		mFeedback = feedbackEdittext.getText().toString();
		if (mFeedback.isEmpty()) {
			return RESULT_OK;
		} else {
			return FEEDBABK_EMPTY;
		}

	}

	@Override
	public void IntegerResponse(int response, int flag) {
		Toast.makeText(this, "Thank you for your feedback", Toast.LENGTH_SHORT).show();

	}

}
