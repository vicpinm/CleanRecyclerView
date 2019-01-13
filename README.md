 <p align="center">
  <img width="80%" src ="/cleanrecycler.png" />
</p>

**CleanRecycler is a library written completely in Kotlin and built on top of KPresenterAdapter (https://github.com/vicpinm/KPresenterAdapter). While the main purpose of KPresenterAdapter is to provide a framework to organize your adapter-related code following the MPV pattern, this library aims to reduce the boilerplate code related with data loading from diferent datasources, data pagination and placeholders management.**

## Setup library
**1 - Add the CleanRecyclerView widget to your xml**
```xml
 <com.vicpin.cleanrecyclerview.view.CleanRecyclerView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:itemsPerPage="50"
        app:refreshEnabled="true"
        app:emptyLayout="@layout/empty_layout"
        app:errorLayout="@layout/empty_layout_error"
        app:cellMargin="14dp"/>
````

**2 - Customize the widget with the following attributes**

| XML Attribute        | Method           | Description  |
| :------------- |:-------------| :----|
| app:emptyLayout      | setEmptyLayout(Int)     | Layout placeholder to show when there is no data |
| app:errorLayout     | setErrorLayout(Int)      |  Error placeholder to show when cloud datasource returns an error and there is no cached data to show |
| app:errorLoadMore     | setErrorLoadMore(String)      |  Text to show in a toast when there is an error trying to load more data (when data collection is paginated) |
| app:itemsPerPage     | setItemsPerPage(Int)      |  When data is paginated, size of each page (optional)
| app:refreshEnabled     | setRefreshEnabled(Boolean)      |  Enable or disable pull to refresh
| app:showHeaderIfEmptyList     | setShowHeaderIfEmptyList(Boolean)      |  Show or hide list header when there is no data to show. Default false (header is not shown when there is no data to load)
| app:dividerDrawable     | setDividerDrawable(Int)      |  Drawable resource to be used as row divider when using LinearLayoutManager
| app:cellMargin      | setCellMargin(Int)     | Set a margin around your cells |


**3 - Provide the datasources you want to retreive your data from**
You can use two datasource types:
- Cached datasource (data storaged in device, no cloud request is needed)
- Cloud datasource (data storaged in a remote server)

You can implement any of them or both of them. 
```kotlin
//When cloud data is not paginated
cleanRecycler.load(adapter = presenterAdapter, cloud = DataService::class, cache = DataCache::class)
//When cloud data is paginated
cleanRecycler.loadPaged(adapter = presenterAdapter, cloud = PagedDataService::class, cache = DataCache::class)
```

Notice that you have to pass an instance of your adapter usign KPresenterAdapter library (https://github.com/vicpinm/KPresenterAdapter)

## How it works

## Download 

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
	     ...
	     maven { url 'https://jitpack.io' }
    }
}
```
  And add the dependency
  ```groovy
dependencies {
	   compile 'com.github.vicpinm:CleanRecyclerView:4.0.1'
}
  ```
  
  <p align="center">
  <img src ="/diagram.png" />
</p>

  
