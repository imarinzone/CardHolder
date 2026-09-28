# Card Wallet (Android)

Card Wallet is a modern, privacy-first Android application built with Kotlin and Jetpack Compose for organizing and carrying your loyalty cards, tickets, membership passes, IDs, and gift cards.

## Features

- **Card Storage & Wallet Deck**: Realistic credit card / pass cards with custom gradients, chip and contactless styling, masked card numbers, cardholder names, and expiry dates.
- **Barcode & QR Code Generator**: Generates high-contrast barcodes and QR codes using ZXing supporting:
  - QR Code
  - Code 128
  - EAN-13
  - Aztec
  - PDF-417
  - Code 39
  - UPC-A
- **Cashier Scan Mode**: Full-screen high-contrast barcode modal for instant scanning at registers and checkouts.
- **3D Card Flip**: Tap any card to flip between front details and back magnetic stripe/signature panel.
- **Organization & Categories**: Group cards by Loyalty, Travel, Membership, Identity, Payment, or custom folders with color tags.
- **Search & Filter**: Real-time search across titles, issuers, numbers, and notes, with quick filter chips for favorites and categories.
- **Archive & Trash**: Soft delete to archive with easy restore or permanent cleanup.
- **Local Persistence**: Built with Android Architecture Components (Room Database, Flow, StateFlow, ViewModel).
- **100% Offline & Private**: All data and barcodes stay strictly on your device.

## Tech Stack

- **UI**: Jetpack Compose, Material Design 3 (M3)
- **Language**: Kotlin 2.1
- **Architecture**: MVVM with Repository Pattern, StateFlow & Coroutines
- **Database**: Android Jetpack Room 2.7 with KSP
- **Barcode Engine**: ZXing (Zebra Crossing) MultiFormatWriter
- **Target SDK**: Android 36 (minSdk 26)
