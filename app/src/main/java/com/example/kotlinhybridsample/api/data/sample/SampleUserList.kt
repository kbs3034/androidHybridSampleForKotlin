package com.example.kotlinhybridsample.api.data.sample

import com.google.gson.annotations.SerializedName

class SampleUserList {
    @SerializedName("page")
    var page: Int? = null

    @SerializedName("per_page")
    var perPage: Int? = null

    @SerializedName("total")
    var total: Int? = null

    @SerializedName("total_pages")
    var totalPages: Int? = null

    @SerializedName("data")
    var data: List<Datum?> = ArrayList()

    inner class Datum {
        @SerializedName("id")
        var id: Int? = null

        @SerializedName("first_name")
        var first_name: String? = null

        @SerializedName("last_name")
        var last_name: String? = null

        @SerializedName("avatar")
        var avatar: String? = null
    }

    override fun toString(): String {
        return "SampleUserList(page=$page, perPage=$perPage, total=$total, totalPages=$totalPages, data=$data)"
    }

}