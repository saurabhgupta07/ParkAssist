
//READ ME

ParkAssist
- developed by Saurabh Gupta and Sharmishtha Swaroop
Use - This application shows the user the open parking lots and garages available across the university and 
      city area.
User - Anyone who wants to see the nearest parking location for thier vehchiles.

//------Basic softwares need to run this app-------//
a) Android Studio Beta(0.8.6) 
----minSDKVersion    11 
----targetSDKVersion 21
b) Android Phone  

//...APIs USED...///

We have used the free API provided by StreetLine Parker Availability API to get the parking locations as well as other data.
We have also used the Google Places API to locate car repair shops.

//-----How to run the app-----------------------------------------------//

The basic aim of our app is to provide users with available parking locations and garages around the city area

We have demonstrated the following functionalities:
1) User will can see the parking locations around its current location and directions to go to that location.
Step 1   User starts the application on his device and is shown the home screen.
Step 2   After waiting for 4-5 seconds users will see the parking locations available around a radius of 1 mile. 
Step 3   On clicking on the location the name of parking location is shown to the user
Step 4   User clicks on the information window of the parking location marker and he is directed to a another activity showcasing the information -
	 Address, Parking Rate and TotalSpaceAvailable.
Step 5   User can click of direction button provided at the top right of the screen which shows him the navigation from his current location to the parking
	 location choosen by the user.

2) User will be given an option to mark his parked location.
Step 1   Users can go to this functionality by clicking the second tab button provided at the bottom.
Step 2   A user can mark his location by clicking the "Park Me" option.This location will be saved even if the user closes his app.
Step 3   Once user clicks the "Park Me" option the button will change to "Done Parking" indicating the user has parked his car at a location which will be shown 
	 using a map
Step 4   If user needs to change his parking loation he can click on "Done Parking".He will be directed to Home screen which again shows the parking location near 
	 his current location

3) Walking directions 
Step 1 Once user parks his car , he might go to another location. In case he needs to find way to reach his parked location he can click on the third tab button
	at the bottom.
Step 2 He will get walking directions to the parked location.
Step 3 If user click on the the Walking Directions button before parking the car, he is shown message to park his car first.

4) User can see the repair shops around the current location
Step 1 Click on the 4th button
Step 2 It will open an activity displaying the map with repair shops locations around his current location
Step 3 User will click on any location and he will receive directions to reach to that location.

// .....Concepts Used...//
1) We have used the concept of SQLLITE for storing user's current parked location and retrieving it when required by the user.
2) Used Google Places API and Streetline Availability API to get data for repair shops and parking spots respectively

         
