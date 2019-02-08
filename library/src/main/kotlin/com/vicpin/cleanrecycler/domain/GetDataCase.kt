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
        val mapper : Mapper<ViewEntity, DataEntity>? = null,
        val source: CRDataSource) : CRUseCase<List<ViewEntity>>() {

    var page: Int = 0

    fun with(page: Int) = apply { this.page = page }

    override fun buildUseCase(): Flowable<List<ViewEntity>> {
        isInProgress = true

        val dataSource = when(source) {
            CRDataSource.DISK -> repository.getDataFromDisk()
            CRDataSource.CLOUD -> repository.getDataPageFromCloud(page)
        }

        return dataSource.map { transformData(it) }
    }

    private fun transformData(dataEntities: List<DataEntity>) : List<ViewEntity> {
        return if(mapper != null) {
            if(mapper is EntityMapper) {
                dataEntities.mapIndexed { index, dataEntity -> mapper.doTransform(index, dataEntity) }
            } else {
                (mapper as CollectionMapper).doTransform(dataEntities)
            }
        }
        else {
            try {
                dataEntities.map { it as ViewEntity }
            } catch (ex: Exception) {
                throw IllegalArgumentException("You should provide a mapper class with cleanRecyclerView.setMapper()")
            }
        }
    }
}


