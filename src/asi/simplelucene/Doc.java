
package asi.simplelucene;

import java.util.HashMap;

public class Doc extends HashMap<String, String> {
	
	private static final long serialVersionUID = 1938492l;
	private Float score = 0f;
	
	public Float getScore() {
		return score;
	}
	
	public void setScore(Float score) {
		this.score = score;
	}

}
