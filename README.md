# Mubashir Mini AI - Android

A complete Android application for **local AI inference** using GGUF models with real Llama.cpp integration. Run powerful language models directly on your Android device without internet connectivity.

## Features

✅ **Real GGUF Model Loading** - Load actual Llama.cpp compatible GGUF format models  
✅ **Local Inference Engine** - Complete C++/JNI binding for Llama.cpp  
✅ **Model Manager** - Browse, load, and unload models with file management  
✅ **Real-time Chat Interface** - Jetpack Compose UI with chat history  
✅ **Configurable Inference** - Adjust temperature, top-p, top-k, and other parameters  
✅ **Database Persistence** - Room database for chat history and model metadata  
✅ **Async Operations** - Coroutines for non-blocking inference  
✅ **Dependency Injection** - Hilt for clean architecture  
✅ **Production Ready** - Proper error handling, logging, and resource management  

## Architecture

```
app/
├── src/main/
│   ├── cpp/                          # Native C++ code
│   │   ├── llama_jni.cpp            # JNI bindings
│   │   ├── model_loader.cpp         # GGUF model loading
│   │   ├── inference_engine.cpp     # Inference implementation
│   │   └── include/
│   │       ├── model_loader.h
│   │       └── inference_engine.h
│   ├── java/com/mubashir/miniai/
│   │   ├── llm/                     # LLM layer
│   │   │   ├── LlamaCppInterface.kt # JNI interface
│   │   │   └── GGUFModelLoader.kt   # Model management
│   │   ├── data/                    # Data layer
│   │   │   ├── db/                  # Room database
│   │   │   ├── dao/                 # Data access objects
│   │   │   ├── entity/              # Database entities
│   │   │   └── repository/          # Repository pattern
│   │   ├── ui/                      # UI layer
│   │   │   ├── screen/              # Compose screens
│   │   │   ├── viewmodel/           # ViewModels
│   │   │   ├── MainActivity.kt
│   │   │   ├── MainApp.kt
│   │   │   └── Theme.kt
│   │   ├── di/                      # Dependency injection
│   │   ├── model/                   # Data models
│   │   ├── service/                 # Services
│   │   └── util/                    # Utilities
│   └── res/                         # Resources
│       ├── values/                  # Strings, colors, themes
│       └── xml/                     # Preferences, file paths
├── build.gradle.kts                 # App build configuration
CMakeLists.txt                       # Native build configuration
```

## Project Structure

### Core Components

**LLM Layer (`llm/`)**
- `LlamaCppInterface.kt` - JNI interface to native Llama.cpp
- `GGUFModelLoader.kt` - GGUF model loading and management

**Data Layer (`data/`)**
- Room database for persistence
- DAOs for data access
- Repositories for business logic
- Entities: ChatEntity, ModelEntity

**UI Layer (`ui/`)**
- **ChatScreen** - Main chat interface with message display
- **ModelManagerScreen** - Model loading and management
- **Theme** - Material Design 3 theming
- **MainActivity** - App entry point

**ViewModels**
- `AIViewModel` - Model loading and inference state
- `ChatViewModel` - Chat message management

**Native Code (`cpp/`)**
- `llama_jni.cpp` - JNI function implementations
- `model_loader.cpp` - GGUF file parsing and loading
- `inference_engine.cpp` - Token generation and inference

## Prerequisites

- Android Studio (latest)
- Android SDK 34+
- NDK r25+
- Gradle 8.0+
- Java 11+

## Installation

### 1. Clone and Setup

```bash
git clone https://github.com/mubashirazeem244-commits/mubashir-mini-ai-android.git
cd mubashir-mini-ai-android
```

### 2. Prepare GGUF Models

Place your GGUF format models in the following directory on your device:

```
/storage/emulated/0/Android/data/com.mubashir.miniai/files/AI_Models/
```

Recommended models:
- TinyLlama (1.1B) - Fast, lightweight
- Mistral (7B) - Good balance
- Llama 2 (7B, 13B) - High quality

Download from:
- [Hugging Face](https://huggingface.co/models?filter=gguf)
- [GGML Model Zoo](https://huggingface.co/spaces/ggml-org/ggml)

### 3. Build the Project

```bash
# Using Gradle
./gradlew build

# Generate APK
./gradlew assembleRelease

# Install on device
./gradlew installRelease
```

### 4. Run on Device/Emulator

```bash
./gradlew run
```

## Building the APK

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (signed)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Build with Verbose Output
```bash
./gradlew assembleDebug --info
```

## Project Dependencies

### Core Android
- androidx.appcompat:appcompat:1.6.1
- androidx.core:core-ktx:1.12.0
- com.google.android.material:material:1.10.0

### Jetpack
- androidx.compose.* (Compose UI)
- androidx.lifecycle:lifecycle-* (Lifecycle management)
- androidx.room:room-* (Database)
- androidx.hilt:hilt-* (Dependency injection)

### Coroutines & Threading
- org.jetbrains.kotlinx:kotlinx-coroutines-*

### Logging
- com.jakewharton.timber:timber:5.0.1

### JSON
- com.squareup.moshi:moshi-* (Serialization)

### NDK & Native
- com.github.ggerganov:llama.cpp (Llama.cpp bindings)

## Configuration

### Inference Parameters

Customize inference behavior in `InferenceConfig`:

```kotlin
data class InferenceConfig(
    val maxTokens: Int = 256,           // Max tokens to generate
    val temperature: Float = 0.7f,      // Randomness (0.0-2.0)
    val topP: Float = 0.9f,            // Nucleus sampling (0.0-1.0)
    val topK: Int = 40,                // Top-K sampling
    val repeatPenalty: Float = 1.1f,   // Penalize repetition
    val numThreads: Int = 4            // CPU threads for inference
)
```

### Storage

Models are stored in: `context.getExternalFilesDir(null)/AI_Models/`

## Usage

### 1. Load a Model

1. Go to **Models** tab
2. View available GGUF models
3. Click **Load** on any model
4. Wait for model to initialize

### 2. Chat with the Model

1. Go to **Chat** tab
2. Type your prompt
3. Click send button
4. Model generates response
5. Chat history is saved automatically

### 3. Manage Models

- **Load** - Load a model for inference
- **Unload** - Free memory and resources
- **View Info** - See model parameters

## Troubleshooting

### Build Errors

**Issue**: `native_load_library failed`
```
Solution: Ensure NDK is installed and CMakeLists.txt paths are correct
```

**Issue**: `Could not find model file`
```
Solution: Place .gguf files in the correct directory:
/storage/emulated/0/Android/data/com.mubashir.miniai/files/AI_Models/
```

**Issue**: Out of memory during inference
```
Solution: Use a smaller model or reduce maxTokens in InferenceConfig
```

### Runtime Issues

**Model won't load**
- Check file permissions
- Verify GGUF format is correct
- Check device storage space

**Slow inference**
- Reduce numThreads in InferenceConfig
- Use a quantized model (Q4_0, Q5_0)
- Close other apps

**App crashes**
- Check logcat: `adb logcat | grep miniai`
- Verify model compatibility
- Check device RAM (recommend 4GB+)

## Logging

View detailed logs:

```bash
adb logcat -s "com.mubashir.miniai"
adb logcat | grep -i llama
adb logcat | grep -i inference
```

## Performance Tips

1. **Use Quantized Models** - Q4_0 or Q5_0 are faster and smaller
2. **Adjust Threads** - Match device core count for best performance
3. **Batch Inference** - Process multiple prompts to amortize overhead
4. **Monitor Memory** - Watch for OOM with large models
5. **GPU Acceleration** - Some devices support Metal or NEON extensions

## File Structure After Build

```
app/
├── build/
│   ├── outputs/
│   │   ├── apk/
│   │   │   ├── debug/app-debug.apk
│   │   │   └── release/app-release.apk
│   │   ├── bundle/
│   │   │   └── release/app-release.aab
│   └── intermediates/
│       ├── classes/
│       ├── aidl/
│       └── res/
└── ...
```

## APK Details

**Debug APK Size**: ~15-20 MB  
**Release APK Size**: ~12-18 MB (after ProGuard)  
**Min SDK**: 24  
**Target SDK**: 34  
**Supported ABIs**: arm64-v8a, armeabi-v7a, x86, x86_64  

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit changes with clear messages
4. Push to branch
5. Create a Pull Request

## License

MIT License - See LICENSE file for details

## Acknowledgments

- **Llama.cpp** - High-performance C++ inference engine (Georgi Gerganov)
- **Jetpack Compose** - Modern Android UI toolkit
- **Room Database** - Type-safe database abstraction
- **Hilt** - Dependency injection for Android

## Resources

- [Llama.cpp GitHub](https://github.com/ggerganov/llama.cpp)
- [GGUF Format](https://github.com/ggerganov/ggml/blob/master/docs/gguf.md)
- [Jetpack Compose Docs](https://developer.android.com/compose)
- [Android NDK](https://developer.android.com/ndk)
- [JNI Documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)

## Support

For issues and questions:
- GitHub Issues: Create an issue in the repository
- Discussions: Use GitHub Discussions
- Email: mubashirazeem244@gmail.com

---

**Built with ❤️ by Mubashir Azeem**

Make AI accessible, offline, and private!
