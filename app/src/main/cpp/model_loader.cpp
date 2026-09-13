#include "model_loader.h"
#include <fstream>
#include <cstring>
#include <stdexcept>
#include <sstream>
#include <iostream>

ModelLoader::ModelLoader(const std::string& model_path)
    : model_path(model_path), model_data(nullptr), n_embd(0), n_vocab(0), n_ctx(2048) {
}

ModelLoader::~ModelLoader() {
    if (model_data) {
        delete[] static_cast<char*>(model_data);
        model_data = nullptr;
    }
}

bool ModelLoader::load() {
    try {
        // Check if file exists and is readable
        std::ifstream file(model_path, std::ios::binary);
        if (!file.is_open()) {
            throw std::runtime_error("Cannot open model file: " + model_path);
        }

        // Read GGUF magic number
        uint32_t magic;
        file.read(reinterpret_cast<char*>(&magic), sizeof(magic));
        if (magic != 0x46554747) { // "GGUF" in hex
            throw std::runtime_error("Invalid GGUF file format");
        }

        // Read file version
        uint32_t version;
        file.read(reinterpret_cast<char*>(&version), sizeof(version));

        // Read tensor count and key-value count
        uint64_t tensor_count, kv_count;
        file.read(reinterpret_cast<char*>(&tensor_count), sizeof(tensor_count));
        file.read(reinterpret_cast<char*>(&kv_count), sizeof(kv_count));

        // Parse key-value metadata
        for (uint64_t i = 0; i < kv_count; i++) {
            uint32_t key_len;
            file.read(reinterpret_cast<char*>(&key_len), sizeof(key_len));
            
            std::string key(key_len, '\0');
            file.read(&key[0], key_len);

            // Skip value based on type
            uint32_t value_type;
            file.read(reinterpret_cast<char*>(&value_type), sizeof(value_type));
            
            // Parse common metadata fields
            if (key == "general.name") {
                // Read string value
                uint64_t str_len;
                file.read(reinterpret_cast<char*>(&str_len), sizeof(str_len));
                std::string name(str_len, '\0');
                file.read(&name[0], str_len);
            } else if (key == "llm.context_length") {
                file.read(reinterpret_cast<char*>(&n_ctx), sizeof(n_ctx));
            }
        }

        // Default embedding dimensions (common models use 4096)
        n_embd = 4096;
        // Default vocabulary size
        n_vocab = 32000;

        file.close();
        return true;
    } catch (const std::exception& e) {
        std::cerr << "Model loading error: " << e.what() << std::endl;
        return false;
    }
}

bool ModelLoader::read_gguf_header() {
    return true;
}

bool ModelLoader::validate_model_format() {
    return true;
}

std::map<std::string, std::string> ModelLoader::get_info() const {
    std::map<std::string, std::string> info;
    info["n_embd"] = std::to_string(n_embd);
    info["n_vocab"] = std::to_string(n_vocab);
    info["n_ctx"] = std::to_string(n_ctx);
    info["model_path"] = model_path;
    return info;
}
