package com.example.petvitals.ui.screens.add_pet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet_image.PetImage
import com.example.petvitals.data.repository.pet_image.PetImageRepository
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.ui.components.DropDownOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class AddEditPetUiState(
    val name: String = "",
    val species: String = "",
    val isDateOfBirthApproximate: Boolean = false,
    val showModal: Boolean = false,
    val birthDateMillis: Long? = null,
    val selectedBirthMonth: Int = 0,
    val monthOptions: List<DropDownOption<Int>> = emptyList(),
    val birthYear: String = "",
    val editMode: Boolean = false,
    val imageUri: Uri? = null
)

@HiltViewModel
class AddEditPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val accountService: AccountService,
    private val petImageRepository: PetImageRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(AddEditPetUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { state ->
            state.copy(monthOptions = populateMonthOptions(context = context))
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(name = name)
        }
    }

    fun onSpeciesChange(species: String) {
        _uiState.update { state ->
            state.copy(species = species)
        }
    }

    fun onDateOfBirthApproximateChange(isApproximate: Boolean) {
        _uiState.update { state ->
            state.copy(isDateOfBirthApproximate = isApproximate)
        }
    }

    fun onShowModalChange(show: Boolean) {
        _uiState.update { state ->
            state.copy(showModal = show)
        }
    }

    fun onBirthDateMillisChange(birthMillis: Long?) {
        _uiState.update { state ->
            state.copy(birthDateMillis = birthMillis)
        }
    }

    fun onBirthMonthChange(month: Int) {
        _uiState.update { state ->
            state.copy(selectedBirthMonth = month)
        }
    }

    fun onBirthYearChange(year: String) {
        _uiState.update { state ->
            state.copy(birthYear = year)
        }
    }

    fun onImageUriChange(uri: Uri?) {
        _uiState.update { state ->
            state.copy(imageUri = uri)
        }
    }

    fun formatDateForDisplay(millis: Long?, context: Context): String {
        return millis?.let {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: context.getString(R.string.tap_to_select_date)
    }

    fun getSpeciesList(): List<String> {
        return listOf(
            "Cat",
            "Dog"
        )
    }

    fun populateMonthOptions(context: Context): List<DropDownOption<Int>> {
        return listOf(
            DropDownOption(
                display = context.getString(R.string.unknown),
                value = 0
            ),
            DropDownOption(
                display = context.getString(R.string.january),
                value = 1
            ),
            DropDownOption(
                display = context.getString(R.string.february),
                value = 2
            ),
            DropDownOption(
                display = context.getString(R.string.march),
                value = 3
            ),
            DropDownOption(
                display = context.getString(R.string.april),
                value = 4
            ),
            DropDownOption(
                display = context.getString(R.string.may),
                value = 5
            ),
            DropDownOption(
                display = context.getString(R.string.june),
                value = 6
            ),
            DropDownOption(
                display = context.getString(R.string.july),
                value = 7
            ),
            DropDownOption(
                display = context.getString(R.string.august),
                value = 8
            ),
            DropDownOption(
                display = context.getString(R.string.september),
                value = 9
            ),
            DropDownOption(
                display = context.getString(R.string.october),
                value = 10
            ),
            DropDownOption(
                display = context.getString(R.string.november),
                value = 11
            ),
            DropDownOption(
                display = context.getString(R.string.december),
                value = 12
            ))
    }

    fun addPet() {
        val birthDate = getBirthDateAsMap()
        val pet = Pet(
            userId = accountService.currentUserId,
            name = uiState.value.name,
            species = uiState.value.species,
            birthDate = birthDate
        )
        val imageUri = uiState.value.imageUri

        viewModelScope.launch {
            try {
                petRepository.addPetToUser(pet)
                if (imageUri != null) {
                    processImageUri(context, imageUri, pet.id)
                }
            } catch (e: Exception) {
                Log.d("AddPetViewModel", "addPet: ${e.message}")
            }
        }
    }

    fun loadPetData(petId: String) {
        viewModelScope.launch {
            val pet: Pet? = petRepository.getPetById(petId)

            pet?.let {
                _uiState.update { state ->
                    state.copy(
                        name = it.name,
                        species = it.species,
                        isDateOfBirthApproximate = it.birthDate.size != 3,
                        birthDateMillis = if (it.birthDate.size == 3) {
                            val date = Calendar.getInstance(Locale.getDefault())
                            date.set(Calendar.DAY_OF_MONTH, it.birthDate["day"]!!)
                            date.set(Calendar.MONTH, it.birthDate["month"]!!)
                            date.set(Calendar.YEAR, it.birthDate["year"]!!)
                            date.timeInMillis
                        } else {
                            null
                        },
                        selectedBirthMonth = if (it.birthDate.size == 2) {
                            it.birthDate["month"]!!
                        } else {
                            0
                        },
                        birthYear = it.birthDate["year"].toString(),
                        editMode = true
                    )
                }
            }
        }
    }

    fun updatePet(petId: String) {
        val birthDate = getBirthDateAsMap()
        val pet = Pet(
            id = petId,
            userId = accountService.currentUserId,
            name = uiState.value.name,
            species = uiState.value.species,
            birthDate = birthDate
        )

        viewModelScope.launch {
            try {
                petRepository.updatePet(pet)
            } catch (e: Exception) {
                Log.d("AddPetViewModel", "updatePet: ${e.message}")
            }
        }
    }

    fun getBirthDateAsMap(): Map<String, Int> {
        return if (uiState.value.isDateOfBirthApproximate) {
            when (uiState.value.selectedBirthMonth) {
                0 -> {
                    mapOf<String, Int>(
                        "year" to uiState.value.birthYear.toInt()
                    )
                }
                else -> {
                    mapOf<String, Int>(
                        "month" to uiState.value.selectedBirthMonth,
                        "year" to uiState.value.birthYear.toInt()
                    )
                }
            }
        } else {
            val date = Calendar.getInstance(Locale.getDefault())
            date.timeInMillis = uiState.value.birthDateMillis ?: 0

            mapOf<String, Int>(
                "day" to date.get(Calendar.DAY_OF_MONTH),
                "month" to date.get(Calendar.MONTH),
                "year" to date.get(Calendar.YEAR)
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun processImageUri(context: Context, uri: Uri, petId: String) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val quality = 75
                    val maxWidth = 400
                    val maxHeight = 400

                    val resizedBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)

                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    val imageBytes = outputStream.toByteArray()

                    val base64Image = Base64.encode(imageBytes)

                    val petImage = PetImage(
                        userId = accountService.currentUserId,
                        petId = petId,
                        imageString = base64Image
                    )

                    petImageRepository.addPetImage(petImage)
                }
            } catch (e: Exception) {
                Log.e("ImageProcessing", "Error: ${e.message}")
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        return bitmap.scale(finalWidth, finalHeight)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    object PastOrPresentSelectableDates: SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= System.currentTimeMillis()
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year <= LocalDate.now().year
        }
    }
}