package com.bennyhuo.tieguanyin.sample

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

@Builder(
    resultTypes = [ResultEntity(name = "userInfo", type = UserInfo::class)]
)
class ParcelableActivity : Activity() {
    @Required
    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcelable)

        textView.text = userInfo.toString()

        textView.setOnClickListener {
            smartFinish(userInfo.copy("bennyhuo123"))
        }
    }

}

data class Company(val name: String, val location: String) : Serializable

@Parcelize
data class UserInfo(val name: String, val age: Int, val company: Company) : Parcelable