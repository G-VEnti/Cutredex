package com.example.cutredex

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cutredex.ui.theme.CutredexTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Pokemon(
    var id: Int,
    var name: String,
    var types: List<String>,
    var description: String,
    var heightMeters: Double,
    var weightKg: Double,
    var baseExperience: Int,
    var hp: Int,
    var attack: Int,
    var defense: Int,
    var specialAttack: Int,
    var specialDefense: Int,
    var speed: Int,
    var imageName: String
)

val Context.dataStore by preferencesDataStore(name = "cutredex_preferences")

suspend fun savePokemonCaptured(context: Context, pokemonId: Int, isCaptured: Boolean) {
    val key = booleanPreferencesKey("captured_$pokemonId")
    context.dataStore.edit { preferences ->
        preferences[key] = isCaptured
    }
}

fun readPokemonCaptured(context: Context, pokemonId: Int): Flow<Boolean> {
    val key = booleanPreferencesKey("captured_$pokemonId")
    return context.dataStore.data.map { preferences ->
        preferences[key] ?: false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var pokemonList by remember { mutableStateOf<List<Pokemon>>(emptyList()) }

            LaunchedEffect(Unit) {
                val texto = context.assets.open("pokedex.json")
                    .bufferedReader()
                    .use { it.readText() }
                pokemonList = Json.decodeFromString(texto)
            }

            CutredexTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "listScreen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("listScreen") {
                            PokedexListScreen(
                                pokemonList = pokemonList,
                                onPokemonClick = { id ->
                                    navController.navigate("detailScreen/$id")
                                }
                            )
                        }

                        composable("detailScreen/{pokemonId}") { backStackEntry ->
                            val idString = backStackEntry.arguments?.getString("pokemonId") ?: "1"
                            val id = idString.toInt()
                            val pokemon = pokemonList.find { it.id == id }

                            if (pokemon != null) {
                                PokemonDetailScreen(
                                    pokemon = pokemon,
                                    backOnClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokedexListScreen(
    pokemonList: List<Pokemon>,
    onPokemonClick: (Int) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Cutredex",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pokemonList) { pokemon ->
                val isCaptured by readPokemonCaptured(context, pokemon.id)
                    .collectAsState(initial = false)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPokemonClick(pokemon.id) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${pokemon.imageName} - ${pokemon.name}")
                    Text(text = "Capturado")
                    Text(text = if (isCaptured) "[X]" else "[ ]")
                }
            }
        }
    }
}

fun loadPokemonImageFromAssets(context: Context, imageName: String): ImageBitmap? {
    return try {
        context.assets.open("$imageName.png").use { inputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon,
    backOnClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isCaptured by readPokemonCaptured(context, pokemon.id)
        .collectAsState(initial = false)

    val pokemonBitmap = remember(pokemon.imageName) {
        loadPokemonImageFromAssets(context, pokemon.imageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = backOnClick) {
                Text("Atrás")
            }
        }

        if (pokemonBitmap != null) {
            Image(
                bitmap = pokemonBitmap,
                contentDescription = pokemon.name,
                modifier = Modifier.size(150.dp)
            )
        }

        Text(
            text = "${pokemon.imageName} - ${pokemon.name}",
            fontSize = 24.sp
        )

        Text(text = "Tipo: ${pokemon.types.joinToString(", ")}")
        Text(text = "Altura: ${pokemon.heightMeters} m | Peso: ${pokemon.weightKg} kg")
        Text(text = pokemon.description,
            textAlign = TextAlign.Center)

        Text(
            text = "HP: ${pokemon.hp} | ATK: ${pokemon.attack} | DEF: ${pokemon.defense}"
        )
        Text(
            text = "S: ${pokemon.speed} | S.ATK  ${pokemon.specialAttack} | S.ATK  ${pokemon.specialDefense}"
        )
        Text(
            text = "Puntos de experiencia base: ${pokemon.baseExperience}"
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isCaptured,
                onCheckedChange = { marked ->
                    scope.launch {
                        savePokemonCaptured(context, pokemon.id, marked)
                    }
                }
            )
            Text("Capturado")
        }
    }
}