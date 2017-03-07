package com.vicpin.cleanrecyclerview.domain

import com.vicpin.cleanrecyclerview.domain.CRUseCase
import com.vicpin.cleanrecyclerview.repository.IRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource

/**
 * Created by Victor on 20/01/2017.
 */
class PagedDataCase<T>
constructor(val repository: IRepository<T>) : CRUseCase<Pair<CRDataSource, List<T>>>() {

    var currentPage: Int = 0

    override fun buildUseCase() = repository.getData(currentPage)

}


