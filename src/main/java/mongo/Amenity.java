package mongo;

import java.util.Arrays;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class Amenity {

	private String amenityType;
	private String name;

	//private String jobTitle;

	private double distance;

	//private String[] skills;

	private String rating;

	private Object longitude;

	private Object latitude;

	public Amenity() {
		// TODO Auto-generated constructor stub
	}

	public Amenity(BasicDBObject obj) {
		BasicDBObject result = (BasicDBObject) obj;
		this.distance = result.getDouble("dis");
		BasicDBObject amenityObj = (BasicDBObject) result.get("obj");		
		this.amenityType = ((BasicDBObject) amenityObj.get("tags")).getString("amenity");
		this.name = ((BasicDBObject) amenityObj.get("tags")).getString("name");
		BasicDBList locationList = (BasicDBList)amenityObj.get("loc");
		this.longitude = locationList.get(0);
		this.latitude = locationList.get(1);
		this.rating = amenityObj.getString("rating");
		
//		this.jobTitle = jobObj.getString("jobTitle");
//		
//		
//		
//		this.formattedAddress = jobObj.getString("formattedAddress");
//		this.skills = ((BasicDBList) jobObj.get("skills")).toArray(new String[0]);
	}

	public String getAmenityType() {
		return amenityType;
	}

	public void setAmenityType(String amenityType) {
		this.amenityType = amenityType;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
		this.name = name;
}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

//	public String[] getSkills() {
//		return skills;
//	}
//
//	public void setSkills(String[] skills) {
//		this.skills = skills;
//	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
	public void setLongitude(Object longitude) {
		this.longitude = longitude;
	}
	
	public Object getLongitude() {
		return longitude;
	}
	
	public void setLatitude(Object latitude) {
		this.latitude = latitude;
	}
	public Object getLatitude() {
		return latitude;
	}

	@Override //Any reason why our toString doesn't include lat and long?
	public String toString() {
		return "Amenity [name=" + name  + ", amenityType=" + amenityType + ", distance=" + distance  + ", rating="
				+ rating + ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}
	
	
	
	
}