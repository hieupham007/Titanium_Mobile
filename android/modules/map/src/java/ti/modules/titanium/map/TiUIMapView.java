package ti.modules.titanium.map;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class TiUIMapView extends TiUIFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener{

	private GoogleMap map;
	protected boolean animate = false;
	protected boolean preLayout = true;
	protected ArrayList<TiMarker> timarkers;
	protected AnnotationProxy selectedAnnotation;
	
	private static final String TRAFFIC = "traffic";

	public static final int MSG_VIEW_CREATED = 600;
	public TiUIMapView(final TiViewProxy proxy, Activity activity) {
		super(proxy, activity);
		timarkers = new ArrayList<TiMarker>();
	}

	@Override
	protected Fragment createFragment() {
		return SupportMapFragment.newInstance();
	}

	
	protected void processPreloadAnnotations() {
		ArrayList<AnnotationProxy> annotations = ((ViewProxy)proxy).getPreloadAnnotations();
		for (int i = 0; i < annotations.size(); i++) {
			addAnnotation(annotations.get(i));
		}
	}
	
	protected void processPreloadRoutes() {
		ArrayList<RouteProxy> routes = ((ViewProxy)proxy).getPreloadRoutes();
		for (int i = 0; i < routes.size(); i++) {
			addRoute(routes.get(i));
		}
	}
	
	protected void onViewCreated() {
		processMapProperties(proxy.getProperties());
		processPreloadAnnotations();
		processPreloadRoutes();
		getMap().setOnMarkerClickListener(this);
		getMap().setOnMapClickListener(this);
	}

	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);

		if (getMap() == null) {
			return;
		}
		processMapProperties(d);
	}
	
	public void processMapProperties(KrollDict d) {

		if (d.containsKey(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocation(d.getBoolean(TiC.PROPERTY_USER_LOCATION));
		}
		if (d.containsKey(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(d.getInt(TiC.PROPERTY_MAP_TYPE));
		}
		if (d.containsKey(TRAFFIC)) {
			setTrafficEnabled(d.getBoolean(TRAFFIC));
		}
		if (d.containsKey(TiC.PROPERTY_ANIMATE)) {
			animate = d.getBoolean(TiC.PROPERTY_ANIMATE);
		}
		if (d.containsKey(TiC.PROPERTY_REGION)) {
			updateCamera(d.getKrollDict(TiC.PROPERTY_REGION));
		}
		if (d.containsKey(TiC.PROPERTY_ANNOTATIONS)) {
			Object[] annotations = (Object[]) d.get(TiC.PROPERTY_ANNOTATIONS);
			addAnnotations(annotations);
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
		if (key.equals(TRAFFIC)) {
			setTrafficEnabled(TiConvert.toBoolean(newValue));
		}
		if (key.equals(TiC.PROPERTY_ANIMATE)) {
			animate = TiConvert.toBoolean(newValue);
		}
		if (key.equals(TiC.PROPERTY_ANNOTATIONS)) {
			updateAnnotations((Object[]) newValue);
		}
		super.propertyChanged(key, oldValue, newValue, proxy);

	}

	public GoogleMap getMap() {
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
	
	protected void addAnnotation(AnnotationProxy annotation) {
		//if annotation already on map, remove it first then re-add it
		TiMarker tiMarker = annotation.getTiMarker();
		if (tiMarker != null) {
			removeAnnotation(tiMarker);
		}
		annotation.processOptions();
		//add annotation to map view
		Marker marker = map.addMarker(annotation.getMarkerOptions());
		tiMarker = new TiMarker(marker, annotation);
		annotation.setTiMarker(tiMarker);
		timarkers.add(tiMarker);
	}

	protected void addAnnotations(Object[] annotations) 
	{
		for (int i = 0; i < annotations.length; i++) {
			Object obj = annotations[i];
			if (obj instanceof AnnotationProxy) {
				AnnotationProxy annotation = (AnnotationProxy) obj;
				addAnnotation(annotation);
			}
		}
	}
	
	protected void updateAnnotations(Object[] annotations)
	{
		//First, remove old annotations from map
		removeAllAnnotations();
		//Then we add new annotations to the map
		addAnnotations(annotations);
	}
	
	protected void removeAllAnnotations() {
		for (int i = 0; i < timarkers.size(); i++) {
			TiMarker timarker = timarkers.get(i);
			timarker.getMarker().remove();
			timarker.getProxy().setTiMarker(null);
		}
		timarkers.clear();
	}
	
	private TiMarker findMarkerByTitle(String title) {
		for (int i = 0; i < timarkers.size(); i++) {
			TiMarker timarker = timarkers.get(i);
			if (timarker.getMarker().getTitle().equals(title)) {
				return timarker;
			}
		}
		
		return null;
	}
	protected void removeAnnotation(Object annotation) {
		
		if (annotation instanceof TiMarker) {
			if (timarkers.contains(annotation)) {
				TiMarker timarker = (TiMarker)annotation;
				timarker.getMarker().remove();
				timarker.getProxy().setTiMarker(null);
				timarkers.remove(annotation);
			}
		}

		if (annotation instanceof AnnotationProxy) {
			TiMarker timarker = ((AnnotationProxy)annotation).getTiMarker();
			for (int i = 0; i < timarkers.size(); i++) {
				TiMarker temp = timarkers.get(i);
				if (timarker.equals(temp)) {
					temp.getMarker().remove();
					temp.getProxy().setTiMarker(null);
					timarkers.remove(i);
					return;
				}
			}
		}

		if (annotation instanceof String) {
			String title = (String)annotation;
			TiMarker timarker = findMarkerByTitle(title);
			if (timarker != null) {
				timarker.getMarker().remove();
				timarker.getProxy().setTiMarker(null);
				timarkers.remove(timarker);
			}
		}
	}
	
	protected void selectAnnotation(Object annotation) {
		if (annotation instanceof AnnotationProxy) {
			AnnotationProxy proxy = (AnnotationProxy)annotation;
			if (proxy.getTiMarker() != null) {
				proxy.showInfo();
				selectedAnnotation = proxy;
			}
		}
		
		if (annotation instanceof String) {
			String title = (String) annotation;
			TiMarker marker = findMarkerByTitle(title);
			if (marker != null) {
				marker.getMarker().showInfoWindow();
				selectedAnnotation = marker.getProxy();
			}

		}
	}
	
	protected void deselectAnnotation(Object annotation) {
		if (annotation instanceof AnnotationProxy) {
			AnnotationProxy proxy = (AnnotationProxy)annotation;
			if (proxy.getTiMarker() != null) {
				((AnnotationProxy)annotation).hideInfo();
			}
		}
		
		if (annotation instanceof String) {
			String title = (String) annotation;
			TiMarker marker = findMarkerByTitle(title);
			if (marker != null) {
				marker.getMarker().hideInfoWindow();
			}
		}
		selectedAnnotation = null;
	}

	private AnnotationProxy getProxyByMarker(Marker m) {
		for (int i = 0; i < timarkers.size(); i++) {
			TiMarker timarker = timarkers.get(i);
			if (m.equals(timarker.getMarker())){
				return timarker.getProxy();
			}
		}
		return null;
	}
	
	public void addRoute(RouteProxy r) {
		//check if route already added.
		if (r.getRoute() != null) {
			return;
		}

		r.processOptions();
		r.setRoute(map.addPolyline(r.getOptions()));
	}
	
	public void removeRoute (RouteProxy r) {
		if (r.getRoute() == null) {
			return;
		}
		
		r.getRoute().remove();
		r.setRoute(null);
	}

	public void fireClickEvent(Marker marker, AnnotationProxy annoProxy, Object clickSource) {
		KrollDict d = new KrollDict();
		d.put(TiC.PROPERTY_TITLE, marker.getTitle());
		d.put(TiC.PROPERTY_SUBTITLE, marker.getSnippet());
		d.put(TiC.PROPERTY_LATITUDE, marker.getPosition().latitude);
		d.put(TiC.PROPERTY_LONGITUDE, marker.getPosition().longitude);
		d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
		d.put("map", proxy);
		d.put(TiC.PROPERTY_TYPE, TiC.EVENT_CLICK);
		d.put(TiC.PROPERTY_SOURCE, proxy);
		d.put(TiC.EVENT_PROPERTY_CLICKSOURCE, clickSource);
		proxy.fireEvent(TiC.EVENT_CLICK, d);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (selectedAnnotation == null) {
			annoProxy.showInfo();
			selectedAnnotation = annoProxy;
		} else if (!selectedAnnotation.equals(annoProxy)) {
			selectedAnnotation.hideInfo();
			annoProxy.showInfo();
			selectedAnnotation = annoProxy;
		} else {
			selectedAnnotation.hideInfo();
			selectedAnnotation = null;
		}
		fireClickEvent(marker, annoProxy, annoProxy);
		return true;
	}

	@Override
	public void onMapClick(LatLng point) {
		if (selectedAnnotation != null) {
			fireClickEvent(selectedAnnotation.getTiMarker().getMarker(), selectedAnnotation, null);
			selectedAnnotation = null;
		}
		
	}
	
	public void release() {
		selectedAnnotation = null;
		map.clear();
		map = null;
		timarkers.clear();
	}

}