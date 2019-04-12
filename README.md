 <p align="center">
  <img width="80%" src ="/cleanrecycler.png" />
</p>

**CleanRecycler is a library written completely in Kotlin and built on top of KPresenterAdapter (https://github.com/vicpinm/KPresenterAdapter). While the main purpose of KPresenterAdapter is to provide a framework to organize your adapter-related code following the MPV pattern, this library aims to reduce the boilerplate code related with data loading from diferent datasources, data pagination and placeholders management.**

## Setup library
**1 - Add the CleanRecyclerView widget to your xml**
```xml
 <com.vicpin.cleanrecycler.view.CleanRecyclerView
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
| app:errorToast     | errorToast(String)      |  Text to show in a toast when there is an error trying to load more data (when data collection is paginated) |
| app:itemsPerPage     | setItemsPerPage(Int)      |  When data is paginated, size of each page (optional)
| app:refreshEnabled     | setRefreshEnabled(Boolean)      |  Enable or disable pull to refresh
| app:showHeaderIfEmptyList     | setShowHeaderIfEmptyList(Boolean)      |  Show or hide list header when there is no data to show. Default false (header is not shown when there is no data to load)
| app:dividerDrawable     | setDividerDrawable(Int)      |  Drawable resource to be used as row divider when using LinearLayoutManager
| app:cellMargin      | setCellMargin(Int)     | Set a margin around your cells |


**3 - Provide the datasources you want to retreive your data from**

You can use two datasource types:
- Cached datasource (data storaged in device, no cloud request is needed)
- Cloud datasource (data storaged in a remote server)

You can implement any or both of them. 
```kotlin
//When cloud data is not paginated
cleanRecycler.load(adapter = presenterAdapter, cloud = DataService::class, cache = DataCache::class)
//When cloud data is paginated
cleanRecycler.loadPaged(adapter = presenterAdapter, cloud = PagedDataService::class, cache = DataCache::class)
```

Notice that you have to pass an instance of your adapter usign KPresenterAdapter library (https://github.com/vicpinm/KPresenterAdapter)

## How it works
First, CleanRecyclerView loads cached data, if any. Then, it tries to update local data from your cloud datasource. When your cloud datasource retreives the data successfully, the library stores the updated data in your local storage, if a cached datasource is provided. Next, it updates the view with the new data. If an error is thrown while fetching data from your server, an error placeholder is shown to the user if there is no local data to be shown. If view already contains data fetched from your local storage, a toast is used to warn your user about the error (if a string resource is setted to ```errorToast``` attribute). If local storage does not contain any data and your cloud datasource returns no data either, an empty placeholder will be shown to the user. 

This workflow is explained in detail in the following diagram:

 <p align="center">
  <img src ="/diagram.png" />
</p>

## DataSources
CleanRecycler library uses **rxjava2** to listen to changes in your database and perform api requests. You are not bound to any database library or network client, only implement the methods required by the datasource interface you inherit from. DataSources are parameterized with the type of entity model that datasource is responsible for. You should create separate datasources for each entity you want to manage. 

This library is prepared to work with two datasource types: cache datasource and cloud datasource. You can also use only one of them if you don't need both. 

Depending on your requieremens, there are different varieties of cache/cloud datasources:

### Cloud datasources types
 * ```CloudDataSource<Model>```: Simplest cloud datasource. You only have to implement one method: ```fun getData(): Single<List<Model>>```. It returns a single object with a list of objects retreived from your server. 
 
  * ```CloudPagedDataSource<Model>```: Paginated cloud datasource. The method you have to implement is the same than before, but it receives a 'page' param, which indicates the current page you have to request to your server: ```fun getData(page: Int): Single<List<Model>>```. Every time user reaches the bottom of the list, this method will be invoked with an incremented value of the 'page' parameter. 
  
### Cache datasources types
* ```CacheDataSource<Model>```: With cache datasources, two methods are required:
````kotlin
fun getData(): Flowable<List<Model>>

fun saveData(clearOldData: Boolean, data: List<Model>)
````

When you need to observe changes in your database and update the view consequently, you should use this datasource. As can be seen, 'getData' method returns a Flowable with the result of the query you perform to your local storage. Most of the ORM libraries are able to return this type of data for listening to database changes. 

On the other hand, 'saveData' method is invoked when a new set of data is retreived from your cloud. The first parameter, clearOldData, is set to true when the previous local data need to be cleaned. This is true always when you use ```CloudDataSource```. With ```CloudPagedDataSource```, it will be only true when first data page is requested. Remaining pages will be  appended to your existing data, without removing old local data. 

## Data mapping between layers
In a clean arquitecture based app, is common to separate your database entities and your view entities, mapping the first to the second in an intermediate class. 

CleanRecyclerView class is parameterized with two generic types: 
```kotlin
class CleanRecyclerView<ViewEntity : Any, DataEntity : Any>
```

As you can see, first generic type corresponds to your view's entity type, and the second one corresponds to your database's entity type. You can set a mapper class between your two entities in your 'load' or 'loadPaged' method: 

```kotlin
val cleanRecycler = viewlist as CleanRecyclerView<ViewItem, DatabaseItem>
cleanRecycler.load(adapter = presenterAdapter, cloud = DataService::class, cache = DataCache::class, mapper: Mapper:class)
```

Your mapper class will look like:

```kotlin
class Mapper: EntityMapper<ViewItem, DatabaseItem> {
    override fun transform(dataEntity: DatabaseItem): ViewItem {
    	val viewItem = ViewItem()
	...
        return viewItem
    }
}
```

If you work only with one entity type, you can use SimpleCleanRecyclerView instead:
```kotlin
val cleanRecycler = list as SimpleCleanRecyclerView<Item>
cleanRecycler.load(adapter = presenterAdapter, cloud = DataService::class, cache = DataCache::class)
```

## Annotation Processor
This library includes a small annotation processor which helps you write even less code. One problem I have identified with generics in view classes is that you can't set your generic type in your xml, so you have to do a casting in your activity like:

```kotlin
val cleanRecycler = viewlist as CleanRecyclerView<ViewItem, DatabaseItem>
cleanRecycler.load(adapter = presenterAdapter, cloud = DataService::class, cache = DataCache::class, mapper: Mapper:class)
```
The annotation processor included is really simple and it can help you reduce the code above. It works with two annotations: @DataSource and @Mapper. In the previous example, we have two datasources: DataService and DataCache. Also, we use a mapper class to transform DatabaseItem model to ViewItem model. If we annotate both datasources classes with @DataSource and our mapper class with @Mapper, and press build project, the annotation processor will generate for you a custom view called ```DatabaseItemCleanRecyclerView```, which automatically uses with your datasources and your mapper class, so you only have to specify your adapter class. 

In your xml file, instead of declaring a view of type CleanRecyclerView, you can reference to your brand new view automatically generated for you:

```xml
<com.vicpin.cleanrecycler.DatabaseItemCleanRecyclerView
	android:id="@+id/cleanRecycler"
	...			
```

And in your activity class, you won't need to perform any casting, you only have to set your adapter to your view:

```kotlin
cleanRecycler.load(adapter = presenterAdapter)
```

Thas is all! You can see a real example of annotation processor usage in the sample project. 

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
    implementation 'com.github.vicpinm:cleanrecyclerview:5.1.2'
	//Optional annotation proccesor
	kapt 'com.github.vicpinm:crv-processor:5.0.1'
}
  ```
 
  
  <p align="center">
  <img src ="/diagram.png" />
</p>

  
