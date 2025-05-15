# TypeX Keyboard

🤖Android App https://github.com/TypeX-Keyboard/android  
🌎Server Backend https://github.com/TypeX-Keyboard/backend

## Building Crypto Experience through Input Methods

TypeX Keyboard is a revolutionary mobile keyboard that integrates cryptocurrency functionality
directly into your typing experience. By leveraging the unique properties of mobile input methods,
TypeX creates a seamless bridge between everyday communication and blockchain interactions.

## 🚀 About TypeX

TypeX is a non-custodial Solana wallet integrated directly into a mobile keyboard, enabling
lightning-fast crypto transactions without leaving your current app. Whether you're texting friends,
using social media, or managing tasks, TypeX detects crypto addresses and enables immediate
interactions with them.

## 🔑 Key Features

### Lightning Trading

- **Intelligent Text Detection**: Automatically recognizes wallet addresses and contract addresses
  in your clipboard or typed text
- **Seamless Interactions**: Directly interact with detected addresses through our WebView interface
- **Instant Transactions**: Send, receive, and trade crypto without switching apps

### Keyboard-Native Wallet

- **Non-Custodial Security**: Your private keys never leave your device
- **Hybrid User Interface**: Full wallet functionality accessible through both the main app and
  keyboard extension
- **Real-Time Portfolio Tracking**: Monitor your assets and their performance while messaging

### On-Device Security

- **Private Key Store**: Securely stores and manages keys directly on your device
- **Sign Process**: All transaction signing happens locally
- **No Cloud Interaction**: Your sensitive data never leaves your phone

## 🛠️ Architecture

TypeX is built with a multi-layered architecture designed for security and performance:

### TypeX User Interface

- Keyboard Main App
- Keyboard Extension

### TypeX Core Foundation

- Encrypt JS Bridge
- Wallet Service C++ Implementation
- Hybrid WebView

### Android System Framework

- System Foundation and Services
- Encryption Algorithm Collection
- C++ STL

## 💎 Solana Integration

TypeX Wallet currently supports only the Solana blockchain, providing:

- Fast and low-cost transactions
- Support for SOL and all Solana-based tokens
- Real-time monitoring of your Solana assets

## 🔍 Technical Specifications

- **Android**: Kotlin, C++
- **Cross-Platform Core**: C++ for cryptographic operations
- **Security**: On-device processing with no cloud dependency

## 🧩 How It Works

1. User copies or receives crypto addresses in any application
2. TypeX detects the address type (wallet or contract)
3. The keyboard provides contextual options for interacting with the address
4. The user can view wallet info, send tokens, or perform other actions
5. Transactions are securely signed on-device
6. The process completes without ever leaving the current application

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.
