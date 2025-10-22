package com.aarav.geowav.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.presentation.components.GeofencePlaceCard
import com.aarav.geowav.ui.theme.nunito

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun YourPlacesScreen(
    placeViewModel: PlaceViewModel,
    navigateToMap: () -> Unit
){

    val placesList by placeViewModel.allPlaces.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Places",
                        fontSize = 36.sp,
                        fontFamily = nunito,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
//                    IconButton(
//                        onClick = {
//                            navigateToMap()
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = null
//                        )
//                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp).fillMaxSize(),
        ) {


            LazyColumn {
                items(placesList){
                        place ->
                    GeofencePlaceCard(place, placeViewModel)
                }
            }
        }
    }
}