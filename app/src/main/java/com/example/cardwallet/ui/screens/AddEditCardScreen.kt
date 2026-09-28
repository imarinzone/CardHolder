package com.example.cardwallet.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardwallet.data.model.CardEntity
import com.example.cardwallet.ui.CardViewModel
import com.example.cardwallet.ui.components.BarcodeView
import com.example.cardwallet.ui.components.parseColorSafe
import com.example.cardwallet.ui.theme.PresetColors
import com.example.cardwallet.util.BarcodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: CardViewModel,
    cardToEdit: CardEntity? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()

    var title by remember { mutableStateOf(cardToEdit?.title ?: "") }
    var issuer by remember { mutableStateOf(cardToEdit?.issuer ?: "") }
    var cardNumber by remember { mutableStateOf(cardToEdit?.cardNumber ?: "") }
    var cardholderName by remember { mutableStateOf(cardToEdit?.cardholderName ?: "") }
    var barcodeValue by remember { mutableStateOf(cardToEdit?.barcodeValue ?: "") }
    var barcodeFormat by remember { mutableStateOf(cardToEdit?.barcodeFormat ?: "QR_CODE") }
    var category by remember { mutableStateOf(cardToEdit?.category ?: "Loyalty") }
    var selectedPresetIndex by remember {
        val initialHex = cardToEdit?.colorHex ?: PresetColors[0].first.first
        val idx = PresetColors.indexOfFirst { it.first.first.equals(initialHex, ignoreCase = true) }
        mutableStateOf(if (idx >= 0) idx else 0)
    }
    var notes by remember { mutableStateOf(cardToEdit?.notes ?: "") }
    var expiryDate by remember { mutableStateOf(cardToEdit?.expiryDate ?: "") }
    var isFavorite by remember { mutableStateOf(cardToEdit?.isFavorite ?: false) }

    var formatDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (cardToEdit != null) "Edit Card" else "Add New Card") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_card_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Card title is required", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val preset = PresetColors[selectedPresetIndex]
                            val newCard = CardEntity(
                                id = cardToEdit?.id ?: 0L,
                                title = title.trim(),
                                issuer = issuer.trim(),
                                cardNumber = cardNumber.trim(),
                                cardholderName = cardholderName.trim(),
                                barcodeValue = barcodeValue.trim(),
                                barcodeFormat = barcodeFormat,
                                category = category,
                                colorHex = preset.first.first,
                                secondaryColorHex = preset.first.second,
                                notes = notes.trim(),
                                expiryDate = expiryDate.trim(),
                                isFavorite = isFavorite,
                                isArchived = cardToEdit?.isArchived ?: false,
                                createdAt = cardToEdit?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveCard(newCard) {
                                onSaved()
                            }
                        },
                        modifier = Modifier.testTag("save_card_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // General Details
            Text(
                text = "General Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Card Title *") },
                placeholder = { Text("e.g. Costco Membership, Delta SkyMiles") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_card_title"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuer / Organization") },
                placeholder = { Text("e.g. Costco, Delta Air Lines, Starbucks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_card_issuer"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Cardholder Name") },
                    placeholder = { Text("e.g. Jane Doe") },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("input_cardholder_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry") },
                    placeholder = { Text("MM/YY") },
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("input_expiry_date"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card / Account Number") },
                placeholder = { Text("e.g. 1234 5678 9012") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_card_number"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barcode Section
            Text(
                text = "Barcode & QR Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = barcodeValue,
                onValueChange = { barcodeValue = it },
                label = { Text("Barcode / QR Code Value") },
                placeholder = { Text("Enter string or number to generate code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_barcode_value"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Format dropdown
            ExposedDropdownMenuBox(
                expanded = formatDropdownExpanded,
                onExpandedChange = { formatDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = barcodeFormat.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Barcode Format") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("select_barcode_format"),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = formatDropdownExpanded,
                    onDismissRequest = { formatDropdownExpanded = false }
                ) {
                    BarcodeGenerator.supportedFormats.forEach { fmt ->
                        DropdownMenuItem(
                            text = { Text(fmt.replace("_", " ")) },
                            onClick = {
                                barcodeFormat = fmt
                                formatDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Live preview
            if (barcodeValue.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                BarcodeView(
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    showExpandButton = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category selector
            Text(
                text = "Category / Folder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category.equals(cat.name, ignoreCase = true),
                        onClick = { category = cat.name },
                        label = { Text(cat.name) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Color Preset Theme
            Text(
                text = "Card Visual Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PresetColors.forEachIndexed { index, (colors, name) ->
                    val isSelected = index == selectedPresetIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedPresetIndex = index }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            parseColorSafe(colors.first),
                                            parseColorSafe(colors.second)
                                        )
                                    )
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes and Favorite
            Text(
                text = "Additional Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Instructions") },
                placeholder = { Text("e.g. Pin code, membership tier, phone number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_card_notes"),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Add to Favorites", fontWeight = FontWeight.Medium)
                    Text("Pin this card to top for quick checkout", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Card title is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val preset = PresetColors[selectedPresetIndex]
                        val newCard = CardEntity(
                            id = cardToEdit?.id ?: 0L,
                            title = title.trim(),
                            issuer = issuer.trim(),
                            cardNumber = cardNumber.trim(),
                            cardholderName = cardholderName.trim(),
                            barcodeValue = barcodeValue.trim(),
                            barcodeFormat = barcodeFormat,
                            category = category,
                            colorHex = preset.first.first,
                            secondaryColorHex = preset.first.second,
                            notes = notes.trim(),
                            expiryDate = expiryDate.trim(),
                            isFavorite = isFavorite,
                            isArchived = cardToEdit?.isArchived ?: false,
                            createdAt = cardToEdit?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.saveCard(newCard) {
                            onSaved()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("submit_save_card_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Card")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
