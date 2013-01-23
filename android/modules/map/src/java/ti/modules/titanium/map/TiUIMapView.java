package ti.modules.titanium.map;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class TiUIMapView extends TiUIFragment {
	
	public static class TiMapFragment extends SupportMapFragment {
		
		public static TiMapFragment newInstance() {
			return new TiMapFragment();
		}
		
		public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
			View v = super.onCreateView(arg0, arg1, arg2);
			if (mapView != null && dict != null) {
				mapView.processProperties(dict);
				dict = null;
			}
		    return v;
		}
		
		public void onCreate(Bundle b) {
			super.onCreate(b);
		}
	}

	private GoogleMap map;
	protected static TiUIMapView mapView;
	protected static KrollDict dict;
	protected boolean animate = false;
	protected boolean preLayout = true;

	public TiUIMapView(TiViewProxy proxy, Activity activity) {
		super(proxy, activity);
		mapView = this;
	}

	@Override
	protected Fragment createFragment() {
		return TiMapFragment.newInstance();
	}

	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);

		if (getMap() == null) {
			dict = d;
			return;
		}
		if (d.containsKey(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocation(d.getBoolean(TiC.PROPERTY_USER_LOCATION));
		}
		if (d.containsKey(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(d.getInt(TiC.PROPERTY_MAP_TYPE));
		}
		if (d.containsKey(TiC.PROPERTY_TRAFFIC)) {
			setTrafficEnabled(d.getBoolean(TiC.PROPERTY_TRAFFIC));
		}
		if (d.containsKey(TiC.PROPERTY_ANIMATE)) {
			animate = d.getBoolean(TiC.PROPERTY_ANIMATE);
		}
		if (d.containsKey(TiC.PROPERTY_REGION)) {
			updateCamera(d.getKrollDict(TiC.PROPERTY_REGION));
		}
		
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) {
		
		if (key.equals(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocation(TiConvert.toBoolean(newValue));
		}
		if (key.equals(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(TiConvert.toInt(newValue));
		}
		if (key.equals(TiC.PROPERTY_REGION)) {
			updateCamera((HashMap) newValue);
		}
		super.propertyChanged(key, oldValue, newValue, proxy);

	}

	private GoogleMap getMap() {
		if (map == null) {
			map = ((SupportMapFragment) getFragment()).getMap();
		}

		return map;		
	}

	protected void setUserLocation(boolean enabled) {
		getMap().setMyLocationEnabled(enabled);
	}
	
	protected void setMapType(int type) {
		getMap().setMapType(type);
	}
	
	protected void setTrafficEnabled(boolean enabled) {
		getMap().setTrafficEnabled(enabled);
	}
	
	protected void updateCamera(HashMap<String, Object> dict) {
		double longitude = 0;
		double longitudeDelta = 0;
		double latitude = 0;
		double latitudeDelta = 0;

		if (dict.containsKey(TiC.PROPERTY_LATITUDE)) {
			latitude = TiConvert.toDouble(dict, TiC.PROPERTY_LATITUDE);
		}
		if (dict.containsKey(TiC.PROPERTY_LONGITUDE)) {
			longitude = TiConvert.toDouble(dict, TiC.PROPERTY_LONGITUDE);
		}
		
		CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
		LatLng location = new LatLng(latitude, longitude);
		cameraBuilder.target(location);
		
		if (dict.containsKey(TiC.PROPERTY_LATITUDE_DELTA)) {
			latitudeDelta = TiConvert.toDouble(dict, TiC.PROPERTY_LATITUDE_DELTA);
		}

		if (dict.containsKey(TiC.PROPERTY_LONGITUDE_DELTA)) {
			longitudeDelta = TiConvert.toDouble(dict, TiC.PROPERTY_LONGITUDE_DELTA);
		}
	 
		if (latitudeDelta != 0 && longitudeDelta != 0) {
			LatLng northeast = new LatLng(latitude + (latitudeDelta / 2.0), longitude + (longitudeDelta / 2.0));
			LatLng southwest = new LatLng(latitude - (latitudeDelta / 2.0), longitude - (longitudeDelta / 2.0));

			final LatLngBounds bounds = new LatLngBounds(southwest, northeast);
			if (preLayout) {
				map.setOnCameraChangeListener(new OnCameraChangeListener() {

					@Override
					public void onCameraChange(CameraPosition arg0) {
						moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
						// Remove listener to prevent position reset on camera move.
						map.setOnCameraChangeListener(null);
						preLayout = false;
					}
				});
				return;
			} else {
				moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
				return;
			}
		}
		
		CameraPosition position = cameraBuilder.build();
		CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
		moveCamera(camUpdate);
	}
	
	protected void moveCamera(CameraUpdate camUpdate)
	{
		if (animate) {
			map.animateCamera(camUpdate);
		} else {
			map.moveCamera(camUpdate);
		}
	}
}