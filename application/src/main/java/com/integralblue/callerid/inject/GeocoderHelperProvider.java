package com.integralblue.callerid.inject;

import java.lang.reflect.Method;

import roboguice.util.Ln;
import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.integralblue.callerid.geocoder.AndroidGeocoder;
import com.integralblue.callerid.geocoder.Geocoder;
import com.integralblue.callerid.geocoder.NominatimGeocoder;

public class GeocoderHelperProvider implements Provider<Geocoder> {
	@Inject
	Application application;

	@Inject
	Injector injector;
	
	public Geocoder get() {
		//Only use the built in (aka Android) geocoder if it is present
		//Otherwise, use the Nominatim geocoder (from OpenStreetMaps)
		
		//the GeoCoder.isPresent() method exists only starting with API 9,
		//so use reflection to check it
		Class<android.location.Geocoder> geocoderClass = android.location.Geocoder.class;
		try {
			Method method = geocoderClass.getMethod("isPresent");
			Boolean isPresent = (Boolean) method.invoke(null, (Object[])null);
			if(isPresent){
				return new AndroidGeocoder(application);
			}
		} catch (Exception e) {
			Ln.e(e);
			//ignore the exception - we'll just fall back to our geocoder
		}

		Geocoder ret = new NominatimGeocoder();
		injector.injectMembers(ret);
		return ret;
	}

}
