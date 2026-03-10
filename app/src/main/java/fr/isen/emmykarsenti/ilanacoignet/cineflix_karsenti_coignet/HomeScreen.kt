package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(Unit) {
        val database = Firebase.database.reference
        database.child("categories").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<Category>()
            snapshot.children.forEach { child ->
                val cat = child.getValue(Category::class.java)
                if (cat != null) list.add(cat)
            }
            categories = list
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CINEFLIX", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF05001E))
            )
        },
        containerColor = Color(0xFF05001E)
    ) { padding ->
        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                // CHANGEMENT ICI : itemsIndexed pour avoir l'index de la catégorie (catIndex)
                itemsIndexed(categories) { catIndex, category ->
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = category.categorie,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // CHANGEMENT ICI : itemsIndexed pour avoir l'index de la franchise (franIndex)
                            itemsIndexed(category.franchises ?: emptyList()) { franIndex, franchise ->
                                FranchiseCard(
                                    franchise = franchise,
                                    onClick = {
                                        // On navigue en passant les index dans l'URL !
                                        navController.navigate("franchise_detail/$catIndex/$franIndex")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseCard(franchise: Franchise, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D29)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = franchise.nom,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}