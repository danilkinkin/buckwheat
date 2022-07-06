package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Storage
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseModule.getInstance(application)

    private val storage = db.storageDao()
}