#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include "model_loader.h"
#include "inference_engine.h"

static ModelLoader* g_model_loader = nullptr;
static InferenceEngine* g_inference_engine = nullptr;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeLoadModel(
        JNIEnv* env,
        jobject obj,
        jstring model_path,
        jstring params) {
    try {
        const char* path = env->GetStringUTFChars(model_path, nullptr);
        const char* param_str = env->GetStringUTFChars(params, nullptr);

        g_model_loader = new ModelLoader(path);
        if (!g_model_loader->load()) {
            delete g_model_loader;
            g_model_loader = nullptr;
            env->ReleaseStringUTFChars(model_path, path);
            env->ReleaseStringUTFChars(params, param_str);
            return 0;
        }

        env->ReleaseStringUTFChars(model_path, path);
        env->ReleaseStringUTFChars(params, param_str);

        return reinterpret_cast<jlong>(g_model_loader);
    } catch (const std::exception& e) {
        env->ThrowNew(
            env->FindClass("java/lang/RuntimeException"),
            e.what()
        );
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeCreateContext(
        JNIEnv* env,
        jobject obj,
        jlong model_ptr,
        jint context_size) {
    try {
        if (!model_ptr) return 0;
        
        ModelLoader* loader = reinterpret_cast<ModelLoader*>(model_ptr);
        g_inference_engine = new InferenceEngine(loader, context_size);
        
        if (!g_inference_engine->initialize()) {
            delete g_inference_engine;
            g_inference_engine = nullptr;
            return 0;
        }

        return reinterpret_cast<jlong>(g_inference_engine);
    } catch (const std::exception& e) {
        env->ThrowNew(
            env->FindClass("java/lang/RuntimeException"),
            e.what()
        );
        return 0;
    }
}

JNIEXPORT jstring JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeInference(
        JNIEnv* env,
        jobject obj,
        jlong model_ptr,
        jlong context_ptr,
        jstring prompt_str,
        jint max_tokens,
        jfloat temperature,
        jfloat top_p,
        jint top_k,
        jfloat repeat_penalty,
        jint num_threads) {
    try {
        if (!context_ptr) {
            return env->NewStringUTF("Error: Context not initialized");
        }

        const char* prompt = env->GetStringUTFChars(prompt_str, nullptr);
        InferenceEngine* engine = reinterpret_cast<InferenceEngine*>(context_ptr);
        
        engine->set_parameters(temperature, top_p, top_k, repeat_penalty, num_threads);
        std::string result = engine->run_inference(prompt, max_tokens);
        
        env->ReleaseStringUTFChars(prompt_str, prompt);

        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        env->ThrowNew(
            env->FindClass("java/lang/RuntimeException"),
            e.what()
        );
        return env->NewStringUTF("");
    }
}

JNIEXPORT jobject JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeTokenize(
        JNIEnv* env,
        jobject obj,
        jlong context_ptr,
        jstring text_str) {
    try {
        if (!context_ptr) {
            return env->NewObject(
                env->FindClass("java/util/ArrayList"),
                env->GetMethodID(env->FindClass("java/util/ArrayList"), "<init>", "()V")
            );
        }

        const char* text = env->GetStringUTFChars(text_str, nullptr);
        InferenceEngine* engine = reinterpret_cast<InferenceEngine*>(context_ptr);
        
        std::vector<int> tokens = engine->tokenize(text);
        
        env->ReleaseStringUTFChars(text_str, text);

        // Create Java ArrayList<Integer>
        jclass arrayListClass = env->FindClass("java/util/ArrayList");
        jmethodID constructor = env->GetMethodID(arrayListClass, "<init>", "()V");
        jobject list = env->NewObject(arrayListClass, constructor);
        jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
        jclass integerClass = env->FindClass("java/lang/Integer");
        jmethodID integerValueOf = env->GetStaticMethodID(integerClass, "valueOf", "(I)Ljava/lang/Integer;");

        for (int token : tokens) {
            jobject intObj = env->CallStaticObjectMethod(integerClass, integerValueOf, token);
            env->CallBooleanMethod(list, addMethod, intObj);
            env->DeleteLocalRef(intObj);
        }

        return list;
    } catch (const std::exception& e) {
        env->ThrowNew(
            env->FindClass("java/lang/RuntimeException"),
            e.what()
        );
        return nullptr;
    }
}

JNIEXPORT jobject JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeGetModelInfo(
        JNIEnv* env,
        jobject obj,
        jlong model_ptr) {
    try {
        if (!model_ptr) {
            return env->NewObject(
                env->FindClass("java/util/HashMap"),
                env->GetMethodID(env->FindClass("java/util/HashMap"), "<init>", "()V")
            );
        }

        ModelLoader* loader = reinterpret_cast<ModelLoader*>(model_ptr);
        auto info = loader->get_info();

        // Create Java HashMap<String, String>
        jclass hashMapClass = env->FindClass("java/util/HashMap");
        jmethodID constructor = env->GetMethodID(hashMapClass, "<init>", "()V");
        jobject map = env->NewObject(hashMapClass, constructor);
        jmethodID putMethod = env->GetMethodID(
            hashMapClass,
            "put",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
        );

        for (const auto& [key, value] : info) {
            jstring jKey = env->NewStringUTF(key.c_str());
            jstring jValue = env->NewStringUTF(value.c_str());
            env->CallObjectMethod(map, putMethod, jKey, jValue);
            env->DeleteLocalRef(jKey);
            env->DeleteLocalRef(jValue);
        }

        return map;
    } catch (const std::exception& e) {
        env->ThrowNew(
            env->FindClass("java/lang/RuntimeException"),
            e.what()
        );
        return nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeFreeContext(
        JNIEnv* env,
        jobject obj,
        jlong context_ptr) {
    if (context_ptr) {
        InferenceEngine* engine = reinterpret_cast<InferenceEngine*>(context_ptr);
        delete engine;
    }
}

JNIEXPORT void JNICALL
Java_com_mubashir_miniai_llm_LlamaCppInterface_nativeFreeModel(
        JNIEnv* env,
        jobject obj,
        jlong model_ptr) {
    if (model_ptr) {
        ModelLoader* loader = reinterpret_cast<ModelLoader*>(model_ptr);
        delete loader;
    }
}

} // extern "C"
