package com.ubox.app.pagedrecyclerview

import com.vicpin.cleanrecyclerview.CRUseCase
import com.vicpin.cleanrecyclerview.repository.PagedListRepository
import com.vicpin.cleanrecyclerview.repository.datasource.CRDataSource

/**
 * Created by Victor on 20/01/2017.
 */
class PagedDataCase<T>
constructor(val repository: PagedListRepository<T>) : CRUseCase<Pair<CRDataSource, List<T>>>() {

    var currentPage: Int = 0

    override fun buildUseCase() = repository.getData(currentPage)

}


