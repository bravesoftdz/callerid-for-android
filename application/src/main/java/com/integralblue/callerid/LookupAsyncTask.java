package com.integralblue.callerid;

import org.osmdroid.views.MapView;

import roboguice.inject.InjectorProvider;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.view.View;
import android.widget.TextView;

import com.blundell.tut.LoaderImageView;
import com.google.inject.Inject;
import com.integralblue.callerid.CallerIDLookup;
import com.integralblue.callerid.CallerIDResult;
import com.integralblue.callerid.GeocoderAsyncTask;
import com.integralblue.callerid.R;

public class LookupAsyncTask extends RoboAsyncTask<CallerIDResult> {

	final CharSequence phoneNumber;
	
	final View layout;
	final TextView text;
	final TextView address;
	final LoaderImageView image;
	final MapView mapView;
	
	final String lookupNoResult;
	final String lookupError;
	final String lookupInProgress;
	
	protected GeocoderAsyncTask geocoderAsyncTask = null;
	
	@Inject
	CallerIDLookup callerIDLookup;

	public LookupAsyncTask(CharSequence phoneNumber, View layout) {
		((InjectorProvider)context).getInjector().injectMembers(this); //work around RoboGuice bug
		this.layout = layout;
		this.phoneNumber = phoneNumber;
		text = (TextView) layout.findViewById(R.id.text);
		address = (TextView) layout.findViewById(R.id.address);
		image = (LoaderImageView) layout.findViewById(R.id.image);
		mapView = (MapView) layout.findViewById(R.id.map_view);
		
		lookupNoResult = context.getString(R.string.lookup_no_result);
		lookupError = context.getString(R.string.lookup_error);
		lookupInProgress = context.getString(R.string.lookup_in_progress);
	}

	public CallerIDResult call() throws Exception {
		return callerIDLookup.lookup(phoneNumber);
	}

	@Override
	protected void onPreExecute() throws Exception {
		super.onPreExecute();
		address.setVisibility(View.GONE);
		//mapView.setVisibility(View.GONE);
		image.setVisibility(View.VISIBLE);
		text.setVisibility(View.VISIBLE);
		text.setText(lookupInProgress);
		image.spin();
	}

	@Override
	protected void onSuccess(CallerIDResult result)
			throws Exception {
		super.onSuccess(result);
		if(result.getAddress()==null){
			address.setVisibility(View.GONE);
		}else{
			address.setVisibility(View.VISIBLE);
			address.setText(result.getAddress());
			// since we're about to start a new lookup,
			// we want to cancel any lookups in progress
			if (geocoderAsyncTask != null)
				geocoderAsyncTask.cancel(true);
			geocoderAsyncTask = new GeocoderAsyncTask(result.getAddress(),layout);
			geocoderAsyncTask.execute();
		}
		image.setImageDrawable(null);
		text.setText(result.getName());
	}

	@Override
	protected void onException(Exception e) throws RuntimeException {
		if (e instanceof CallerIDLookup.NoResultException) {
			text.setText(lookupNoResult);
		} else {
			Ln.e(e);
			text.setText(lookupError);
		}
		address.setVisibility(View.GONE);
		mapView.setVisibility(View.GONE);
		image.setImageDrawable(null);
	}

	@Override
	protected void onInterrupted(Exception e) {
		super.onInterrupted(e);
		address.setVisibility(View.GONE);
		mapView.setVisibility(View.GONE);
		image.setVisibility(View.GONE);
		text.setVisibility(View.GONE);
	}
};