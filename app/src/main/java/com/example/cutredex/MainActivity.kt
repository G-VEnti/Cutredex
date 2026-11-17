package com.example.cutredex

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.cutredex.ui.theme.CutredexTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "cutredex_preferences")

private fun capturedKey(pokemonId: Int) = booleanPreferencesKey("captured_$pokemonId")

suspend fun savePokemonCaptured(context: Context, pokemonId: Int, isCaptured: Boolean) {
    context.dataStore.edit { preferences ->
        preferences[capturedKey(pokemonId)] = isCaptured
    }
}

fun readPokemonCaptured(context: Context, pokemonId: Int): Flow<Boolean> {
    return context.dataStore.data.map { preferences ->
        preferences[capturedKey(pokemonId)] ?: false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CutredexTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class Pokemon(
    var id: Int,
    var name: String,
    var types: List<String>,
    var description: String,
    var heightMeters: Float,
    var weightKg: Float,
    var baseExperience: Int,
    var hp: Int,
    var attack: Int,
    var defense: Int,
    var specialAttack: Int,
    var specialDefense: Int,
    var speed: Int,
    var imageName: String
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CutredexTheme {
        Greeting("Android")
    }
}

@Composable
fun MainScreen()
{

}