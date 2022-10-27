package com.example.kotlinhybridsample.api.data.sample

import com.google.gson.annotations.SerializedName

class SampleUser {
    @SerializedName("name")
    var name = "morpheus"
    @SerializedName("job")
    var job = "leader"
}