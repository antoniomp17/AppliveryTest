package viewmodels

import androidx.lifecycle.ViewModel
import usecases.GetLocationInfoUseCase

class LocationInfoViewModel(
    private val getLocationInfoUseCase: GetLocationInfoUseCase
) : ViewModel() {



}