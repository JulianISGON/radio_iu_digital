package com.example.iudigitalradio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.iudigitalradio.ui.theme.IUDigitalRadioTheme

private data class RadioStation(
    val id: String,
    val name: String,
    val description: String,
    val streamUrl: String
)

private val stations = listOf(
    RadioStation(
        "news",
        "NPR News",
        "Noticias y actualidad",
        "https://npr-ice.streamguys1.com/live.mp3"
    ),
    RadioStation(
        "jazz",
        "Radio Paradise",
        "Música variada sin publicidad",
        "https://stream.radioparadise.com/mp3-192"
    ),
    RadioStation(
        "classical",
        "KEXP",
        "Música independiente",
        "https://kexp-mp3-128.streamguys1.com/kexp128.mp3"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IUDigitalRadioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RadioApp()
                }
            }
        }
    }
}

@Composable
private fun RadioApp() {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    var selectedStationId by rememberSaveable { mutableStateOf(stations.first().id) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<Bitmap?>(null) }
    val selectedStation = stations.first { it.id == selectedStationId }
    val haptics = remember { HapticFeedbackManager(context) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        profileImage = bitmap
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    RadioScreen(
        selectedStation = selectedStation,
        isPlaying = isPlaying,
        isMuted = isMuted,
        profileImage = profileImage,
        onCameraClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onPlay = {
            haptics.shortPulse()
            player.setMediaItem(MediaItem.fromUri(selectedStation.streamUrl))
            player.prepare()
            player.play()
            isPlaying = true
        },
        onPause = {
            haptics.shortPulse()
            player.pause()
            isPlaying = false
        },
        onMute = {
            haptics.shortPulse()
            isMuted = !isMuted
            player.volume = if (isMuted) 0f else 1f
        },
        onStationSelected = { station ->
            haptics.shortPulse()
            selectedStationId = station.id
            player.setMediaItem(MediaItem.fromUri(station.streamUrl))
            player.prepare()
            player.play()
            isPlaying = true
        }
    )
}

@Composable
private fun RadioScreen(
    selectedStation: RadioStation,
    isPlaying: Boolean,
    isMuted: Boolean,
    profileImage: Bitmap?,
    onCameraClick: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onMute: () -> Unit,
    onStationSelected: (RadioStation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        ProfileSection(profileImage = profileImage, onCameraClick = onCameraClick)
        Spacer(modifier = Modifier.height(24.dp))
        PlayerCard(
            station = selectedStation,
            isPlaying = isPlaying,
            isMuted = isMuted,
            onPlay = onPlay,
            onPause = onPause,
            onMute = onMute
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Emisoras disponibles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stations, key = { it.id }) { station ->
                StationItem(
                    station = station,
                    isSelected = station.id == selectedStation.id,
                    onClick = { onStationSelected(station) }
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(profileImage: Bitmap?, onCameraClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Hola, oyente", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tu radio digital",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onCameraClick),
            contentAlignment = Alignment.Center
        ) {
            if (profileImage == null) {
                Text("Foto", color = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Image(
                    bitmap = profileImage.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun PlayerCard(
    station: RadioStation,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onMute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("REPRODUCIENDO AHORA", style = MaterialTheme.typography.labelMedium)
            Text(
                station.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(station.description, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPlay, enabled = !isPlaying) {
                    Text("Play")
                }
                OutlinedButton(onClick = onPause, enabled = isPlaying) {
                    Text("Pause")
                }
                OutlinedButton(onClick = onMute) {
                    Text(if (isMuted) "Unmute" else "Mute")
                }
            }
        }
    }
}

@Composable
private fun StationItem(
    station: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.SemiBold)
                Text(station.description, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Text("Activa", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private class HapticFeedbackManager(context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun shortPulse() {
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40L)
        }
    }
}
