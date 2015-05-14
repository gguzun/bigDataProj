package rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bson.types.BasicBSONList;

import mongo.Amenity;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;

@Path("/amenities")
public class JobsRestService {

	@Inject
	private DB db;

	@GET
	@Path("/{tags}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Amenity> allJobsNearToLocationWithSkill(
			@PathParam("tags") String tags,
			@QueryParam("rating") double rating,
			@QueryParam("topk") int topk,
			@QueryParam("longitude") double longitude,
			@QueryParam("latitude") double latitude) {
		
		String[] tagsArr = tags.split(",");
		BasicDBObject cmd = new BasicDBObject();
		cmd.put("geoNear", "amenities");
		double lnglat[] = { longitude, latitude };
	//	double rating = 4.0;
		cmd.put("near", lnglat);
		cmd.put("maxDistance", 1000);
		cmd.put("num", topk);
		BasicDBObject tagsQuery = new BasicDBObject();
		///tagsQuery.put("tags",
		//		new BasicDBObject("amenity", Arrays.asList(tagsArr)));
		
		
		int i = 0;
		BasicDBObject[] tagsArray = new BasicDBObject[tagsArr.length];
				
		while( i < tagsArr.length )
		{
			tagsArray[i] = new BasicDBObject();
			tagsArray[i].put("tags.amenity", tagsArr[i]);
			i++;
		}
		
		tagsQuery.put("$or", tagsArray);
		
		BasicDBObject ratingQuery = new BasicDBObject();
		ratingQuery.put("$gte", rating);
		tagsQuery.append("rating", ratingQuery);

		//BasicDBObject contains = new BasicDBObject();
		//contains.put("contains", tagsQuery);
		cmd.put("query", tagsQuery);
		cmd.put("distanceMultiplier", 111);
		//cmd.put("query", tagsQuery);
		//cmd.put("distanceMultiplier", 111);

		System.out.println("Query -> " + cmd.toString());
		CommandResult commandResult = db.command(cmd);

		BasicDBList results = (BasicDBList)commandResult.get("results");
		List<Amenity> amenities = new ArrayList<Amenity>();
		for (Object obj : results) {
			Amenity amenity = new Amenity((BasicDBObject)obj);
			amenities.add(amenity);
		}
		System.out.println("Result : " + amenities);

		return amenities;
	}
	@GET
	@Path("/nightPlan")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Amenity> nightPlanner(
			@QueryParam("skills1") String skills1,
			@QueryParam("skills2") String skills2,
			@QueryParam("rideDistance") double rideDistance,
			@QueryParam("walkDistance") double walkDistance,
			@QueryParam("rating") double rating,
			@QueryParam("longitude") double longitude,
			@QueryParam("latitude") double latitude) {

		System.out.println("\nExecuting night planner logic following path\n");
		
		String tags=skills1;
		String[] tagsArr = tags.split(",");
		BasicDBObject cmd = new BasicDBObject();
		cmd.put("geoNear", "amenities");
		double lnglat[] = { longitude, latitude };
	//	double rating = 4.0;
		cmd.put("near", lnglat);
		cmd.put("maxDistance", rideDistance);
		cmd.put("num", 10);
		BasicDBObject tagsQuery = new BasicDBObject();
		///tagsQuery.put("tags",
		//		new BasicDBObject("amenity", Arrays.asList(tagsArr)));
		int i = 0;
		BasicDBObject[] tagsArray = new BasicDBObject[tagsArr.length];
				
		while( i < tagsArr.length )
		{
			tagsArray[i] = new BasicDBObject();
			tagsArray[i].put("tags.amenity", tagsArr[i]);
			i++;
		}
		
		tagsQuery.put("$or", tagsArray);
		
		BasicDBObject ratingQuery = new BasicDBObject();
		ratingQuery.put("$gte", rating);
		tagsQuery.append("rating", ratingQuery);

		//BasicDBObject contains = new BasicDBObject();
		//contains.put("contains", tagsQuery);
		cmd.put("query", tagsQuery);

		cmd.put("distanceMultiplier", 111);
		
//		cmd.put("distanceMultiplier", 111);
		//cmd.put("query", tagsQuery);
		//cmd.put("distanceMultiplier", 111);

		System.out.println("Query -> " + cmd.toString());
		
		CommandResult commandResult = db.command(cmd);

		BasicDBList results = (BasicDBList)commandResult.get("results");
		List<Amenity> amenities = new ArrayList<Amenity>();
		List<Amenity> completeAmenities = new ArrayList<Amenity>();
		
		
		for (Object obj : results) {
			Amenity amenity = new Amenity((BasicDBObject)obj);
			amenities.add(amenity);
			completeAmenities.add(amenity);
		}
		
		System.out.println("Size of result is: "+amenities.size()+"\nResult : " + amenities);

		//Find matching amenities from the first amenities
		
		String secondAmenities=skills2;
		String[] secondAmenitiesArr = secondAmenities.split(",");
		
		double amenityLongitude;
		double amenityLatitude;
		
		for(Amenity amenity:amenities){
			
			amenityLongitude = (Double) amenity.getLongitude();
			amenityLatitude = (Double) amenity.getLatitude();
			
			System.out.println("-- Running query for second amenity with longitude "+amenityLongitude+" and latitude "+amenityLatitude);
			
			BasicDBObject secondCmd = new BasicDBObject();
			secondCmd.put("geoNear", "amenities");
			double amenitylnglat[] = {amenityLongitude,amenityLatitude};
		//	double rating = 4.0;
			secondCmd.put("near", amenitylnglat);
			secondCmd.put("maxDistance", walkDistance);
			secondCmd.put("num", 10);
			BasicDBObject secondQuery = new BasicDBObject();
			///tagsQuery.put("tags",
			//		new BasicDBObject("amenity", Arrays.asList(tagsArr)));
			int innerI = 0;
			BasicDBObject[] secondAmenitiesArray = new BasicDBObject[secondAmenitiesArr.length];
					
			while( innerI < secondAmenitiesArr.length )
			{
				secondAmenitiesArray[innerI] = new BasicDBObject();
				secondAmenitiesArray[innerI].put("tags.amenity", secondAmenitiesArr[innerI]);
				innerI++;
			}
			
			secondQuery.put("$or", secondAmenitiesArray);
			ratingQuery.put("$gte", rating); //use same rating as before
			secondQuery.append("rating", ratingQuery);
	
			//BasicDBObject contains = new BasicDBObject();
			//contains.put("contains", tagsQuery);
			secondCmd.put("query", secondQuery);
	//		cmd.put("distanceMultiplier", 111);
			//cmd.put("query", tagsQuery);
			//cmd.put("distanceMultiplier", 111);

			secondCmd.put("distanceMultiplier", 111);
			
			System.out.println("Second Query -> " + secondCmd.toString());
			
			commandResult = db.command(secondCmd);
	
			BasicDBList secondResults = (BasicDBList)commandResult.get("results");
			
			for (Object obj : secondResults) {
				Amenity secondAmenity = new Amenity((BasicDBObject)obj);
				completeAmenities.add(secondAmenity);
			}

		}
		
		return completeAmenities;
	}
}