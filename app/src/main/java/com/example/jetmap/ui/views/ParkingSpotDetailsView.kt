package com.example.jetmap.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jetmap.R
import com.example.jetmap.data.network.ParkingSpot
import com.example.jetmap.data.network.ParkingSpotLocation
import com.example.jetmap.ui.theme.JetMapTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingSpotDetailsView(
    openBottomSheet: (Boolean) -> Unit,
    isBottomSheetOpen: Boolean,
    isUserLocationEnabled: Boolean,
    parkingSpot: ParkingSpot,
    navigateToParkingSpot: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    if (isBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet(false) },
            sheetState = bottomSheetState
        ) {
            ParkingSpotDetails(
                name = parkingSpot.name,
                address = parkingSpot.address,
                cityCountry = context.getString(R.string.city_country_format, parkingSpot.city, parkingSpot.country),
                openingHours = parkingSpot.openingHours,
                isUserLocationEnabled = isUserLocationEnabled
            ) {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        openBottomSheet(false)
                    }
                    navigateToParkingSpot()
                }
            }
        }
    }
}

@Composable
fun ParkingSpotDetails(
    modifier: Modifier = Modifier,
    name: String,
    address: String,
    cityCountry: String,
    openingHours: String,
    isUserLocationEnabled: Boolean,
    navigateToParkingSpot:() -> Unit
) {
    val buttonText = if (isUserLocationEnabled) stringResource(R.string.route) else stringResource(R.string.route_disabled)
    val buttonColor = if (isUserLocationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_parking_spot_location),
                contentDescription = stringResource(R.string.parking_location),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = name)
                Text(text = address)
                Text(text = cityCountry)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_parking_spot_hours),
                contentDescription = stringResource(R.string.parking_opening_hours),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = stringResource(R.string.opening_hours, openingHours))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    modifier = Modifier
                        .clickable(
                            enabled = isUserLocationEnabled,
                            role = Role.Button,
                            onClick = { navigateToParkingSpot() },
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    color = buttonColor
                )
            }
        }
    }
}

@Preview
@Composable
fun ParkingSpotDetailsViewPreview() {
    JetMapTheme {
        ParkingSpotDetailsView(
            openBottomSheet = {},
            isBottomSheetOpen = true,
            isUserLocationEnabled = true,
            ParkingSpot(
                name = "Kvaternik Plaza",
                parkingSpotLocation = ParkingSpotLocation(
                    longitude = 15.9992192,
                    latitude = 45.8147474
                ),
                address = "Kvaternik Plaza, Ulica Antuna Nemčića 7, 10123 City of Zagreb, Croatia",
                city = "Zagreb",
                country = "Croatia",
                openingHours = "24/7"
            )
        ) {}
    }
}
