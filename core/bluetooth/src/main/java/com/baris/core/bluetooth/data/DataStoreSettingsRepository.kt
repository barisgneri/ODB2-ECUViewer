package com.baris.core.bluetooth.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.baris.core.model.ObdDataType
import com.baris.core.obd.domain.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Context üzerinden DataStore delegasyonunu tanımlıyoruz
private val Context.dataStore by preferencesDataStore(name = "obd_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val gaugesKey = stringSetPreferencesKey("active_gauges")

    override val activeGauges: Flow<Set<ObdDataType>> = context.dataStore.data
        .map { preferences ->
            val stringSet = preferences[gaugesKey]
            if (stringSet.isNullOrEmpty()) {
                // Eğer ilk açılışsa ve hafıza boşsa varsayılan olarak RPM ve Hız dönsün
                setOf(ObdDataType.Rpm, ObdDataType.Speed)
            } else {
                // Hafızadaki String adlarını nesnelerimize geri çeviriyoruz
                // ObdDataType bir sealed class olduğu için valueOf yok, 
                // bu yüzden basit bir map kullanıyoruz veya static bir map oluşturuyoruz.
                stringSet.mapNotNull { name ->
                    when (name) {
                        "Rpm" -> ObdDataType.Rpm
                        "Speed" -> ObdDataType.Speed
                        "CoolantTemperature" -> ObdDataType.CoolantTemperature
                        else -> null
                    }
                }.toSet()
            }
        }

    override suspend fun saveActiveGauges(gauges: Set<ObdDataType>) {
        context.dataStore.edit { preferences ->
            // Sınıf isimlerini String Set olarak diske yazıyoruz
            preferences[gaugesKey] = gauges.map { it::class.simpleName ?: "" }.toSet()
        }
    }
}
