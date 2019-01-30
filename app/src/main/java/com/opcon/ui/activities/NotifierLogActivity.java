package com.opcon.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.components.NotifierLog;
import com.opcon.database.NotifierLogBase;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.NotifierLogEventDispatcher;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.adapters.NotifierLogAdapter;
import com.opcon.ui.views.HelperHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotifierLogActivity extends AppCompatActivity {

		public static final String NOTIFIER_ID = "notifierId";
		public static final String SHOW_ALL = "sa";

		private RecyclerView mRecyclerView;
		private NotifierLogAdapter mAdapter;
		private int mNotifierId;
		private LinearLayoutManager mLayoutManager;
		private List<Component> mComponents;

		private RelativeLayout mHelperRoot;
		private HelperHolder mHelperHolder;

		private boolean mShowAll;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_notifier_log);

				mShowAll = getIntent().getBooleanExtra(SHOW_ALL, false);

				if (getSupportActionBar() != null) {
						getSupportActionBar().setDisplayHomeAsUpEnabled(true);
						getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
						getSupportActionBar().setElevation(5);
						getSupportActionBar().setTitle(R.string.title_of_notififer_log_activity);
				}

				mNotifierId = getIntent().getExtras().getInt(NOTIFIER_ID);
				mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
				mHelperRoot = (RelativeLayout) findViewById(R.id.rootOfHelper);
				mHelperHolder = new HelperHolder(mHelperRoot);
				initHelper();

				mLayoutManager = new LinearLayoutManager(this);
				mRecyclerView.setLayoutManager(mLayoutManager);
				mAdapter = new NotifierLogAdapter(getLogs());
				mRecyclerView.setAdapter(mAdapter);



		}

		void initHelper() {

			if (HelperHolder.isGotIt(this, R.string.what_is_notification_of_notifier)) {
				mHelperHolder.gone();
				mHelperRoot.setVisibility(View.GONE);
			} else {
				mHelperRoot.setVisibility(View.VISIBLE);
				mHelperHolder.newBuilder().setTitle(R.string.what_is_notification_of_notifier)
						.setMessage(R.string.what_is_notification_of_notifier_answer)
						.setNegativeButton(R.string.understand, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								HelperHolder.gotIt(getApplicationContext(), R.string.what_is_notification_of_notifier);
								mHelperHolder.gone();
                mHelperRoot.setVisibility(View.GONE);
							}
						});
			}

		}


		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
				getMenuInflater().inflate(R.menu.only_delete, menu);
				return super.onCreateOptionsMenu(menu);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
				if (item.getItemId() == R.id.delete) {

						final DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
										if (which == DialogInterface.BUTTON_POSITIVE) {

											ProgressDialog show = ProgressDialog.show(NotifierLogActivity.this, getString(R.string.please_wait), getString(R.string.deleting), true, false);

											if (mShowAll) {
												for (Component mComponent : mComponents) {
													if (mComponent instanceof Notifier) {
														deleteLogsFor(mComponent.getId());
													}
												}
											} else {
												deleteLogsFor(mNotifierId);
											}

											if (mComponents != null) {
													mComponents.clear();
													if (mAdapter !=null) {
															mAdapter.notifyDataSetChanged();
													}
											}

											show.dismiss();
										}
								}
						};

						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.are_you_sure);
						builder.setMessage(R.string.are_you_sure_for_delete_all_logs_for_notifier);
						builder.setPositiveButton(R.string.delete, onClick);
						builder.setNegativeButton(R.string.no, onClick);
						builder.show();
				} else if (android.R.id.home == item.getItemId()) {
						super.onBackPressed();
				}
				return super.onOptionsItemSelected(item);
		}

		public void deleteLogsFor(int mNotifierId) {
			NotifierLogBase.Utils.delete(getApplicationContext(), mNotifierId);
			NotifierLogEventDispatcher.getInstance().dispatchDeletedAll(mNotifierId);
		}

		public List<Component> getLogs() {
				if (mShowAll) {
						mComponents = new ArrayList<>();
						List<Notifier> notifiers = NotifierLogBase.Utils.getDistinctNotifiers(getApplicationContext());
						if (!notifiers.isEmpty()) {
							for (Notifier notifier : notifiers) {
								List<NotifierLog> nonSeenLogs = NotifierLogBase.Utils.getNonSeenLogs(getApplicationContext(), notifier.getId());
								if (nonSeenLogs != null && !nonSeenLogs.isEmpty()) {
									Collections.reverse(nonSeenLogs);
									mComponents.add(notifier);
									mComponents.addAll(nonSeenLogs);
								}
							}
						}
				} else {
						List<NotifierLog> nonSeenLogs = NotifierLogBase.Utils.getNonSeenLogs(getApplicationContext(), mNotifierId);
						List<NotifierLog> seenLogs = NotifierLogBase.Utils.getSeenLogs(getApplicationContext(), mNotifierId);
						NotifierLogBase.Utils.setSeen(getApplicationContext(), nonSeenLogs);
						Collections.reverse(nonSeenLogs);
						Collections.reverse(seenLogs);
						mComponents = new ArrayList<>();
						mComponents.add(NotifierProvider.Utils.get(getApplicationContext(), mNotifierId));
						mComponents.addAll(nonSeenLogs);
						mComponents.addAll(seenLogs);
						NotifierEventDispatcher.getInstance().dispatchUpdate(mNotifierId);
				}
				return mComponents;
		}

		@Override
		protected void onDestroy() {
				super.onDestroy();
				mRecyclerView.setAdapter(null);
				mRecyclerView = null;
				mComponents.clear();
				mComponents = null;
				mLayoutManager = null;
		}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	public static void showLogs(Context context, int lid) {
				Intent intent = new Intent(context, NotifierLogActivity.class);
				intent.putExtra(NOTIFIER_ID, lid);

				context.startActivity(intent);
	}

	public static void showLogs(Context context, int lid, String destination) {
				Intent intent = new Intent(context, NotifierLogActivity.class);
				intent.putExtra(NOTIFIER_ID, lid);
				intent.putExtra("destination", destination);

				context.startActivity(intent);
	}

}
