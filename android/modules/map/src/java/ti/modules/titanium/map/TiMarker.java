package ti.modules.titanium.map;

import com.google.android.gms.maps.model.Marker;

public class TiMarker {
	private Marker marker;
	private AnnotationProxy proxy;

	public TiMarker(Marker m, AnnotationProxy p) {
		marker = m;
		proxy = p;
	}

	public void setMarker(Marker m) {
		marker = m;
	}
	public Marker getMarker() {
		return marker;
	}

	public AnnotationProxy getProxy() {
		return proxy;
	}
}
