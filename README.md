# FlynnMobilePracticalTest
PhotoViewer Mobile Application

Album Activity Launches:
this app instantly logs in as user 1 and grabs those albums. 
Displays them on a gridview, using the first image as their "cover image", if an album has no images it will use the empty.png resource as a cover image.
Albums information is retrieved through http://jsonplaceholder.typicode.com/users/1/albums
User can create a new empty album by pressing the '+' Button and filling out the title in the DialogFragment.
This will POST the album to http://jsonplaceholder.typicode.com/albums but since the server is fake, it will not be retrieved on last application boot.
I designed this PhotoViewer to be a CloudPhotoViewer not storing any data on the phone's sdcard. A LocalStorage backup system would be an ideal added feature.

If a User selects an Album, The PhotoActivity Launches.
This Activity's layout has a ViewFlipper, and another version of the floating '+' button. The ViewFlipper will animate with a Flick to the left or right to scroll the Album. 
Photo data is retrieved with http://jsonplaceholder.typicode.com/photos?albumId=id. Pressing the '+' will bring up a Dialog to enter title and url. 
In an actual application one would allow for upload, and share intents, then either send the image data to the server or upload to a cloud service and get short url. 
I skip these complications with having just a field for url. Any image added will be added to the end of the album queue. If it was an empty album, the image should show up. 
Image Title and Album Title are presented on screen. Image is added to server with POST using http://jsonplaceholder.typicode.com/photos  
If you exit PhotoActivity and go back into that album, all added albums will be gone, as it will get from server each time it enters album (fake server).
Once again this would be overcome through sdcard backup. All Activities and Fragments flow and resume properly. Potential for many moreFeatures!