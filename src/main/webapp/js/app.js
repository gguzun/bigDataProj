// app.js
(function($){

		var LocalJobs = {};
		window.LocalJobs = LocalJobs;
		
		var template = function(name) {
			return Mustache.compile($('#'+name+'-template').html());
		};
		
		LocalJobs.HomeView = Backbone.View.extend({
			tagName : "form",
			el : $("#main"),
			
			events : {
				"submit" : "findJobs",
				'click #nightPlannerButton':"planNight"
			},
			
			render : function(){
				console.log("rendering home page..");
				$("#results").empty();
				return this;
			},
			
			findJobs : function(event){
				console.log('in findJobs()...');
				event.preventDefault();
				$("#results").empty();
				$("#jobSearchForm").mask("Finding Locations ...");
				var skills = this.$('input[name=skills]').val().split(',');

				console.log("skills : "+skills);
				
				var self = this;
				
				  var mapOptions = {
				    zoom: 3,
				    center: new google.maps.LatLng(-87.8, 41.8),
				    mapTypeControlOptions: {
				      style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
				    },
				    mapTypeId: google.maps.MapTypeId.ROADMAP,
				    zoomControlOptions: {
					  style: google.maps.ZoomControlStyle.SMALL
				  	}
				  };
				   

				  var map = new google.maps.Map(document.getElementById('map-canvas'),
				      mapOptions);
					var longitude;
			    	var latitude; 
				
				 navigator.geolocation.getCurrentPosition(function(position){
					
					var longitude_input = this.$('input[name=longitude]').val();
					var latitude_input = this.$('input[name=latitude]').val();
					var rating = this.$('select[name=rating]').val();
					var topk = this.$('input[name=topk]').val();
					
					if (longitude_input.length>0 && latitude_input.length>0){
					longitude = longitude_input;
					latitude = latitude_input;
					}else{
					longitude = position.coords.longitude;
					latitude = position.coords.latitude;
					}
					
			    	console.log('longitude .. '+longitude);
			    	console.log('latitude .. '+latitude);
					
			    	
			    	$("#jobSearchForm").unmask();
			    	self.plotUserLocation(new google.maps.LatLng(latitude, longitude),map);
			    	  
			    	$.get("api/amenities/"+skills+"/?longitude="+longitude+"&latitude="+latitude+"&rating="+rating+"&topk="+topk  , function (results){ 
		                    $("#jobSearchForm").unmask();
		                    self.renderResults(results,self,map);
		             });
					 
					  
					 
				}, function(e){
					$("#jobSearchForm").unmask();
					switch (e.code) {
						case e.PERMISSION_DENIED:
							alert('You have denied access to your position. You will ' +
									'not get the most out of the application now.'); 
							break;
						case e.POSITION_UNAVAILABLE:
							alert('There was a problem getting your position.'); 
							break;
						case e.TIMEOUT:
									alert('The application has timed out attempting to get ' +
											'your location.'); 
							break;
						default:
							alert('There was a horrible Geolocation error that has ' +
									'not been defined.');
					}
				},
					{ timeout: 45000 }
				
				);
				
			},
			
			planNight : function(event){
				console.log('in findJobs()...');
				event.preventDefault();
				$("#results").empty();
				$("#jobSearchForm").mask("Finding Locations ...");
				var skills1 = this.$('input[name=skills1]').val();
				var skills2 = this.$('input[name=skills2]').val();

				console.log("amenities1: "+skills1);
				console.log("amenities2 : "+skills2);
				
				var self = this;
				
				  var mapOptions = {
				    zoom: 3,
				    center: new google.maps.LatLng(-87.8, 41.8),
				    mapTypeControlOptions: {
				      style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
				    },
				    mapTypeId: google.maps.MapTypeId.ROADMAP,
				    zoomControlOptions: {
					  style: google.maps.ZoomControlStyle.SMALL
				  	}
				  };
				  var map = new google.maps.Map(document.getElementById('map-canvas'),
				      mapOptions);
				
				
				navigator.geolocation.getCurrentPosition(function(position){
					var longitude;
			    	var latitude;
					var longitude_input = this.$('input[name=longitude]').val();
					var latitude_input = this.$('input[name=latitude]').val();
					var rating = this.$('select[name=rating]').val();
					var rideDistance = this.$('input[name=rideDistance]').val();
					var walkDistance = this.$('input[name=walkDistance]').val();
					
					
					console.log('rideDistance .. '+rideDistance);
					console.log('walkDistance .. '+walkDistance);
					console.log('rating .. '+rating);
					
					if (longitude_input.length>0 && latitude_input.length>0){
					longitude = longitude_input;
					latitude = latitude_input;
					}else{
					longitude = position.coords.longitude;
					latitude = position.coords.latitude;
					}
					
			    	console.log('longitude .. '+longitude);
			    	console.log('latitude .. '+latitude);
					
			    	
			    	$("#jobSearchForm").unmask();
			    	self.plotUserLocation(new google.maps.LatLng(latitude, longitude),map);
			    	  
			    	$.get("api/amenities/nightPlan/?longitude="+longitude+"&latitude="+latitude+"&rating="+rating+"&skills1="+skills1+"&skills2="+skills2+"&rideDistance="+rideDistance+"&walkDistance"+walkDistance, function (results){ 
		                    $("#jobSearchForm").unmask();
		                    self.renderResults(results,self,map);
		             });
				}, function(e){
					$("#jobSearchForm").unmask();
					switch (e.code) {
						case e.PERMISSION_DENIED:
							alert('You have denied access to your position. You will ' +
									'not get the most out of the application now.'); 
							break;
						case e.POSITION_UNAVAILABLE:
							alert('There was a problem getting your position.'); 
							break;
						case e.TIMEOUT:
									alert('The application has timed out attempting to get ' +
											'your location.'); 
							break;
						default:
							alert('There was a horrible Geolocation error that has ' +
									'not been defined.');
					}
				},
					{ timeout: 45000 }
				
				);
				
			},
			plotUserLocation : function(latLng , map){
				map.setCenter(latLng); 
				var marker = new google.maps.Marker({
					position: latLng,
					draggable:true,
					icon: 'http://icons.iconarchive.com/icons/icons-land/vista-people/32/Office-Customer-Male-Light-icon.png', 
					animation: google.maps.Animation.DROP
				}); 
				marker.setMap(map);
				map.setCenter(latLng);
				map.setZoom(6);
				
				google.maps.event.addListener(marker, 'dragend', function() {					
					map.setCenter(marker.getPosition());
					document.getElementById("longitude").value = marker.getPosition().lng();	
					document.getElementById("latitude").value = marker.getPosition().lat();
				});
				
				
				
				
			},

			renderResults : function(results,self,map){
				var infoWindow = new google.maps.InfoWindow();
				_.each(results,function(result){
					self.renderJob(result,map , infoWindow);
				});
				
			},
			
			renderJob : function(result , map , infoWindow){
			
				console.log("latitude : " + result.latitude + " , longitude: " + result.longitude);
				result.marker = new google.maps.Marker({
					position: new google.maps.LatLng(result.latitude, result.longitude),
					icon: 'http://icons.iconarchive.com/icons/icons-land/vista-map-markers/32/Map-Marker-Push-Pin-1-Left-Pink-icon.png', 
					animation: google.maps.Animation.DROP,
					type: result.name,
					html: this.jobInfo(result)
				});
				
				google.maps.event.addListener(result.marker, 'click', function() {
					/*map.setZoom(16);*/
					map.setCenter(result.marker.getPosition());
					infoWindow.setContent(this.html);
					infoWindow.open(map, this); 
				});

				
				google.maps.event.addListener(infoWindow,'closeclick',function(){
					/*map.setZoom(4);*/
				});
			
				result.marker.setMap(map);
			},
			
		 jobInfo : function(job) {
			var text = '';
			text += '<div class="job_info">';
			
			text += '<h3>' + job['name'] + '</h3>';
			text += '<p>' + 'Amenity: ' + job['amenityType'] + '</p>';
			text += '<p>' + 'Rating: ' + job['rating'] + '</p>';
			//text += '<p>' + job['skills'] + '</p>';
			text += '<p>' + 'Distance: ' + job['distance'] + ' KM</p>';
			return text;
		}
			
			

		});
		
		LocalJobs.JobView = Backbone.View.extend({
				template : template("job"),
				initialize  : function(options){
					this.result = options.result;
				},
		
				render : function(){
					this.$el.html(this.template(this));
					return this;
				},
				amenityType : function(){
					return this.result['amenityType'];
				},
				address : function(){
					return this.result['rating'];
				},
				skills : function(){
					return this.result['skills'];
				},
				company : function(){
					return this.result['companyName'];
				},
				distance : function(){
					return this.result['distance'] + " KM";
				}
				
		});
		
		
		LocalJobs.Router = Backbone.Router.extend({
			el : $("#main"),
			
			routes : {
				"" : "showHomePage"
			},
			showHomePage : function(){
				console.log('in home page...');
				var homeView = new LocalJobs.HomeView();
				this.el.append(homeView.render().el);
			}
		
		});
		
		var app = new LocalJobs.Router();
		Backbone.history.start();
		
		
})(jQuery);