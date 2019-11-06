package com.vicpin.cleanrecycler.domain


enum class FROM_ERROR{
    CLOUD,CACHE
}

class CleanRecyclerException(val throwable: Throwable,val case:FROM_ERROR)
