package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Univers CINEFLIX",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // La liste des univers demandée dans ton cahier des charges
        val universes = listOf("Marvel", "Star Wars", "Disney", "Pixar", "Avatar")

        // On crée une carte cliquable pour chaque univers
        universes.forEach { universe ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { /* TODO: Naviguer vers la liste des films de cet univers */ }
            ) {
                Text(
                    text = universe,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}