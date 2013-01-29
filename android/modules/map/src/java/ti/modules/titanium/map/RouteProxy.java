package ti.modules.titanium.map;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

@Kroll.proxy(creatableInModule=MapModule.class, propertyAccessors = {
	"points",
	TiC.PROPERTY_COLOR,
	TiC.PROPERTY_WIDTH,
	TiC.PROPERTY_VISIBLE
})
public class RouteProxy extends KrollProxy{
	
	private PolylineOptions options;
	private Polyline route;
	
	private static final String POINTS = "points";

	public RouteProxy() {
		super();
		options = new PolylineOptions();
	}
	
	public RouteProxy(TiContext tiContext) {
		this();
	}
	
	public void processOptions() {

		if (hasProperty(POINTS)) {
			 processPoints(getProperty(POINTS));
		}
		
		if (hasProperty(TiC.PROPERTY_WIDTH)) {
			options.width(TiConvert.toFloat(getProperty(TiC.PROPERTY_WIDTH)));
		}
		
		if (hasProperty(TiC.PROPERTY_COLOR)) {
			options.color(TiConvert.toColor(TiC.PROPERTY_COLOR));
		}
		
	}

	public void processPoints(Object points) {
		//multiple points
		if (points instanceof Object[]) {
			Object[] pointsArray = (Object[]) points;
			for (int i = 0; i < pointsArray.length; i++) {
				Object obj = pointsArray[i];
				if (obj instanceof HashMap<?, ?>) {
					HashMap<String, String> point = (HashMap<String, String>) obj;
					LatLng location = new LatLng(TiConvert.toDouble(point.get("latitude")), TiConvert.toDouble(point.get("longitude")));
					options.add(location);
				}
			}
		}
		//single point
		if (points instanceof HashMap) {
			HashMap<String, String> point = (HashMap<String, String>) points;
			LatLng location = new LatLng(TiConvert.toDouble(point.get("latitude")), TiConvert.toDouble(point.get("longitude")));
			options.add(location);
		}
	}
	
	public PolylineOptions getOptions() {
		return options;
	}
	
	public void setRoute(Polyline r) {
		route = r;
	}
	
	public Polyline getRoute() {
		return route;
	}
	
	public void onPropertyChanged(String name, Object value) {
		super.onPropertyChanged(name, value);
	}
}
