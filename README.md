# Snob

<img height="150" src="https://user-images.githubusercontent.com/117776720/230206162-4794a5ca-e085-4f6f-beb6-334847078880.png" />

Snob is an application to showcase usage of [Google Maps and Places API](https://developers.google.com/maps).

As the name implies, it shows only high quality Places on the map. Those Places can be Restaurants or Cafes so you can wine & dine like a true snob!
To be shown on the map, Place needs to have rating of 4.7 or higher and minimum of 100 ratings.

This app uses:
- Google Maps and Places API
- MVVM architecture
- LiveData
- Location permissions

<br/><br/>

If you want to try the app out, please add your API key to the ```local.properties``` file in the root directory of the project in the following format:
```
API_KEY=YOUR_API_KEY
```

<br/><br/>

User can select which location permission level to give (or not to give any, in which case app defaults location to center of Zagreb) and app handles all cases.

| Location Permission FINE  | Location Permission COARSE | Location Permission denied |
| ------------- | ------------- | ------------- |
| <video src="https://user-images.githubusercontent.com/117776720/230211959-abdeb972-ffd4-4dbb-b298-6b67e240bf30.mp4">  | <video src="https://user-images.githubusercontent.com/117776720/230211881-aea99d64-7637-4c74-bd44-18c98527ebe5.mp4">  |  <video src="https://user-images.githubusercontent.com/117776720/230211810-3a007de0-6699-458a-aaa9-ff57e4097473.mp4">

<br/><br/>
  
User can choose between Restaurants and Cafes. App loads Places in 3 km radius of a given location and user can load new Places by tapping on "Search this area" button.
Also, every Place can be tapped on to show more details about that Place.
  
| General usage |
| ------------- |
| <video src="https://user-images.githubusercontent.com/117776720/230212074-9339573b-dd7f-4366-b568-b8b3c82ffe9c.mp4"> | 

<br/><br/>

If there are many Places on the map, they get clustered to provide better and clearer experience.

| Clustering |
| ------------- |
| <video src="https://user-images.githubusercontent.com/117776720/230212181-c6ce5f34-4dd3-4c35-9907-ef23eb536a35.mp4"> |

<br/><br/>

When Place details are opened, user can tap on "TAKE ME THERE" button to use default navigation app to navigate to the desired Place.

| Tap on "TAKE ME THERE" |
| ------------- |
| <video src="https://user-images.githubusercontent.com/117776720/230212258-8bde1495-f647-4bd5-8bd1-78949db26e4f.mp4"> |
