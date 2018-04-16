package com.vicpin.cleanrecyclerview.domain

import com.vicpin.cleanrecyclerview.repository.IRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource
import com.vicpin.cleanrecyclerview.view.interfaces.CollectionMapper
import com.vicpin.cleanrecyclerview.view.interfaces.EntityMapper
import com.vicpin.cleanrecyclerview.view.interfaces.Mapper
import io.reactivex.Flowable

/**
 * Created by Victor on 20/01/2017.
 */
class GetDataCase<ViewEntity, DataEntity>
constructor(val repository: IRepository<DataEntity>, val mapper : Mapper<ViewEntity, DataEntity>? = null) : CRUseCase<Pair<CRDataSource, List<ViewEntity>>>() {

    var currentPage: Int = 0
    var onlyDisk = false

    override fun buildUseCase(): Flowable<Pair<CRDataSource, List<ViewEntity>>> {
        val result = if(onlyDisk) repository.getDataFromDisk() else repository.getData(currentPage)
        return result.map { pair ->
            Pair(pair.first, transformData(dataEntities = pair.second))
        }
    }

    private fun transformData(dataEntities: List<DataEntity>) : List<ViewEntity> {
        return if(mapper != null) {
            if(mapper is EntityMapper) {
                dataEntities.map { mapper.transform(it) }
            } else {
                (mapper as CollectionMapper).transform(dataEntities)
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


