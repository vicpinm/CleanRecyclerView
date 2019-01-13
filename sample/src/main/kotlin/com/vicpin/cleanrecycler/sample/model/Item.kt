package com.vicpin.cleanrecycler.sample.model

import java.io.Serializable

/**
 * Created by victor on 21/1/17.
 */
data class Item(val title : String, val description : String, val imageUrl : String = "") : Serializable
