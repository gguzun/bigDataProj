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
			@QueryParam("latitude") double latitude,
			@QueryParam("matches") int matches) {

		System.out.println("\nExecuting night planner logic following path\n");
		
		String tags=skills1;
		String[] tagsArr = tags.split(",");
		BasicDBObject cmd = new BasicDBObject();
		cmd.put("geoNear", "amenities");
		double lnglat[] = { longitude, latitude };
		cmd.put("near", lnglat);
		cmd.put("maxDistance", rideDistance/6378.1);
		cmd.put("spherical",true); //since we are specifying legacy coordinate, the distance is in radians
		BasicDBObject tagsQuery = new BasicDBObject();

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
		
		cmd.put("query", tagsQuery);

		cmd.put("distanceMultiplier", 6378.1);//formats output to be in km
		
		System.out.println("Query -> " + cmd.toString());
		
		CommandResult commandResult = db.command(cmd);

		BasicDBList results = (BasicDBList)commandResult.get("results");
		List<Amenity> amenities = new ArrayList<Amenity>();
		List<Amenity> completeAmenities = new ArrayList<Amenity>();
		
		for (Object obj : results) {
			Amenity amenity = new Amenity((BasicDBObject)obj);
			amenities.add(amenity);
		}
		
		System.out.println("Size of result is: "+amenities.size()+"\nResult : " + amenities);

		
		
		//Find matching amenities from the first amenities
		
		String secondAmenities=skills2;
		String[] secondAmenitiesArr = secondAmenities.split(",");
		
		double amenityLongitude;
		double amenityLatitude;
		int totalMatchCount=0; //count current number of matches to the total specified by users
		
		String firstAmenityType;
		int secondAmenityMatches;
		
		for(Amenity amenity:amenities){
	
			secondAmenityMatches=matches;
			
			boolean sameAmenityType=false; //need to check if same amenity is used.
			
			firstAmenityType=amenity.getAmenityType();
			
			for(int a2=0;a2<secondAmenitiesArr.length;a2++){
				if(firstAmenityType.equalsIgnoreCase(secondAmenities)){
					sameAmenityType=true;
					secondAmenityMatches++;//add +1 since will use lat/long to search
					break;
				}
			}
			System.out.println("Detected same amenity for secondAmenity: "+sameAmenityType);
			
			firstAmenityType= amenity.getAmenityType();
			amenityLongitude = (Double) amenity.getLongitude();
			amenityLatitude = (Double) amenity.getLatitude();
			
			System.out.println("-- Running query for second amenity with longitude "+amenityLongitude+" and latitude "+amenityLatitude);
			
			BasicDBObject secondCmd = new BasicDBObject();
			secondCmd.put("geoNear", "amenities");
			double amenitylnglat[] = {amenityLongitude,amenityLatitude};
			secondCmd.put("near", amenitylnglat);
			secondCmd.put("maxDistance", walkDistance/6378.1); //converts the km to radians
			secondCmd.put("spherical",true); //causes distances to be measure in radians
			secondCmd.put("num", secondAmenityMatches);
			
			BasicDBObject secondQuery = new BasicDBObject();
			
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
			secondCmd.put("query", secondQuery);
			secondCmd.put("distanceMultiplier", 6378.1);//formats distance output in km
			
			System.out.println("Second Query -> " + secondCmd.toString());

			commandResult = db.command(secondCmd);
	
			BasicDBList secondResults = (BasicDBList)commandResult.get("results");
			
			int secondResultSize=secondResults.size();
			System.out.println("Second Result Size -> "+secondResults.size());

			//when querying with the long/lat of the same amenity type then query matches should be >1(otherwise, only the long/lat location corresponding the loc's amenities we used is matched
			if((secondResultSize>1&&sameAmenityType)||(secondResultSize>0&&!sameAmenityType)){

				totalMatchCount++; //found a viable option for user
				
				//add the amenity that we have used long/lat to search with
				//first need to check if it has already been added as a children (can only happen when first and second amenities are the same)
				if(!sameAmenityType){
					amenity.setLevel(0);
					completeAmenities.add(amenity);
				}
				
				
				
				//for each of the newly found amenities
				for (Object obj : secondResults) {
					
					boolean newAmenity = true;
					
					Amenity secondAmenity = new Amenity((BasicDBObject)obj);
					
					//at least one second amenity is the name of the loc's long/lat used when first and second are the same type
					if(secondAmenity.getName().equalsIgnoreCase(amenity.getName())){//to use actual distance
						
						//check for child
						boolean isChild = false;
						for(Amenity globalAmenity:completeAmenities){
							if(globalAmenity.getName().equalsIgnoreCase(amenity.getName())){//a child (lvl 1) has already been added as a child to some other amenity(
								isChild = true;
								globalAmenity.setLevel(0);
								globalAmenity.setParent("");
								globalAmenity.setDistance(amenity.getDistance());
							}
						}
						
						if(!isChild){//just add if it hasn't previously been seen
							amenity.setLevel(0);
							completeAmenities.add(amenity);
						}
						
					}else{
						//can def be improved with some other structure. Used for prototype
						
						//check if child is a parent when of the same type
						if(sameAmenityType){
							
							//check that second amenity does not correspond to a level 0
							for(Amenity globalAmenity:completeAmenities){
								
								//means there is an entry already corresponding to some other second amenity from some other lvl 0 amenity.
								if(globalAmenity.getName().equalsIgnoreCase(secondAmenity.getName())){

									newAmenity=false;
									globalAmenity.addParentMessage(amenity.getName()+"; ");
									
									if(globalAmenity.getLevel()!=0){
										//just add  parent message (so that user knows that this is close to something (it is also a second amenity)
										//update distance and parent
										if(secondAmenity.getDistance()<globalAmenity.getDistance()){
											globalAmenity.setParent(amenity.getName());
											globalAmenity.setDistance(secondAmenity.getDistance());
										}
									}
									
									break;
								}
								
							}//end of check glboal amenity
							if(newAmenity){
								secondAmenity.setLevel(1);
								secondAmenity.setParent(amenity.getName());
								secondAmenity.addParentMessage(amenity.getName()+"; ");
								completeAmenities.add(secondAmenity);
							}
							
						}else{
							for(Amenity globalAmenity:completeAmenities){
								
								//means there is an entry already corresponding to some other second amenity from some other amenity.
								if(globalAmenity.getName().equalsIgnoreCase(secondAmenity.getName())){
									
									newAmenity=false;
									globalAmenity.addParentMessage(amenity.getName()+"; ");
									
									if(globalAmenity.getLevel()!=0){
										//just add  parent message (so that user knows that this is close to something (it is also a second amenity)
										//update distance and parent
										if(secondAmenity.getDistance()<globalAmenity.getDistance()){
											globalAmenity.setParent(amenity.getName());
											globalAmenity.setDistance(secondAmenity.getDistance());
										}
									}
									
									break;
								}
							}
							
							//add new second amenity
							if(newAmenity){
								secondAmenity.setLevel(1);
								secondAmenity.setParent(amenity.getName());
								secondAmenity.addParentMessage(amenity.getName()+"; ");
								completeAmenities.add(secondAmenity);
							}
						}
					}
				} //end of second results for a level 0 amenity
			}//block that checks if the result using the lvl 0 amenity long/lat has a viable match 
			
			if(totalMatchCount==matches){
				System.out.println("Found "+matches+" matching first destination with at least one corresponding second destination");
				break;
			}
		}
		
		return completeAmenities;
	}
}			
		