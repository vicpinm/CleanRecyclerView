package com.vicpin.cleanrecycler.domain

import com.vicpin.cleanrecycler.repository.ListRepository
import com.vicpin.cleanrecycler.repository.datasource.CRDataSource
import com.vicpin.cleanrecycler.view.interfaces.CollectionMapper
import com.vicpin.cleanrecycler.view.interfaces.EntityMapper
import com.vicpin.cleanrecycler.view.interfaces.Mapper
import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
class GetDataCase<ViewEntity, DataEntity>
constructor(
        val repository: ListRepository<DataEntity, *>,
        val mapper: Mapper<ViewEntity, DataEntity>? = null,
        val source: CRDataSource,
        val observableDbMode: Boolean) : CRUseCase<Result<ViewEntity>>() {

    var page: Int = 0
    var dataLoaded = listOf<ViewEntity>()

    fun with(page: Int) = apply { this.page = page }

    override fun buildUseCase(): Flowable<Result<ViewEntity>> {
        isInProgress = true
        val dataSource = when (source) {
            CRDataSource.DISK -> repository.getDataFromDisk()
            CRDataSource.CLOUD -> repository.getDataPageFromCloud(page)
        }

        return if(observableDbMode && dataSource == CRDataSource.CLOUD) {
            //No data transformation is necessary when data comes from cloud and observableDbMode is true,
            //in this case data is returned with ddbb flowable notifications
            dataSource.map { Result<ViewEntity>(size = it.size) }
        } else {
            //Transform data from DataEntity to ViewEntity
            dataSource.map { transformData(it) }.map { Result(it, it.size) }
        }
    }

    private fun transformData(dataEntities: List<DataEntity>): List<ViewEntity> {

        val viewEntities = if (mapper != null) {
            if (mapper is EntityMapper) {
                dataEntities.mapIndexed { index, dataEntity -> mapper.doTransform(index, dataEntity) }
            } else {
                (mapper as CollectionMapper).doTransform(dataEntities)
            }
        } else {
            try {
                dataEntities.map { it as ViewEntity }
            } catch (ex: Exception) {
                throw IllegalArgumentException("You should provide a mapper class with cleanRecyclerView.setMapper()")
            }
        }

        updateCurrentData(viewEntities)

        return viewEntities
    }

    private fun updateCurrentData(dataEntities: List<ViewEntity>) {
        if (source == CRDataSource.DISK) {
            dataLoaded = dataEntities
            mapper?.currentData = dataLoaded
        } else if (!observableDbMode && source == CRDataSource.CLOUD) {
            dataLoaded = if(page == 0) { dataEntities } else { dataEntities + dataLoaded }
            mapper?.currentData = dataLoaded
        }
    }
}


data class Result<T>(val data: List<T> = listOf(), val size: Int)

