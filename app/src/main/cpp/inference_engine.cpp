#include "inference_engine.h"
#include <algorithm>
#include <random>
#include <cmath>
#include <iostream>

InferenceEngine::InferenceEngine(ModelLoader* loader, int context_size)
    : model_loader(loader),
      context_size(context_size),
      inference_context(nullptr),
      temperature(0.7f),
      top_p(0.9f),
      top_k(40),
      repeat_penalty(1.1f),
      num_threads(4) {
}

InferenceEngine::~InferenceEngine() {
    if (inference_context) {
        delete[] static_cast<char*>(inference_context);
        inference_context = nullptr;
    }
}

bool InferenceEngine::initialize() {
    try {
        if (!model_loader) {
            throw std::runtime_error("Model loader is null");
        }
        // Initialize inference context
        inference_context = new char[context_size * 1024]; // Allocate memory
        return true;
    } catch (const std::exception& e) {
        std::cerr << "Inference engine initialization error: " << e.what() << std::endl;
        return false;
    }
}

std::string InferenceEngine::run_inference(const std::string& prompt, int max_tokens) {
    try {
        if (!inference_context) {
            return "Error: Inference context not initialized";
        }

        // Apply prompt template
        std::string formatted_prompt = apply_prompt_template(prompt);

        // Tokenize input
        std::vector<int> tokens = tokenize(formatted_prompt);

        // Simulate token generation
        std::string output;
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<> dis(65, 90); // Random letters

        int tokens_to_generate = std::min(max_tokens, 100); // Max 100 tokens for simulation
        
        for (int i = 0; i < tokens_to_generate; ++i) {
            // Simulate token generation (in real implementation, this would use the model)
            char token = static_cast<char>(dis(gen));
            output += token;
            
            if (i % 10 == 0) output += ' ';
        }

        post_process_output(output);
        return output;
    } catch (const std::exception& e) {
        return std::string("Error: ") + e.what();
    }
}

std::vector<int> InferenceEngine::tokenize(const std::string& text) {
    std::vector<int> tokens;
    
    // Simple whitespace tokenizer (in production, use actual GGUF tokenizer)
    std::istringstream iss(text);
    std::string word;
    int token_id = 0;
    
    while (iss >> word) {
        // Each word gets a simple hash as token ID
        for (char c : word) {
            tokens.push_back(token_id++);
        }
    }
    
    return tokens;
}

void InferenceEngine::set_parameters(float temp, float p, int k, float repeat, int threads) {
    temperature = std::max(0.1f, std::min(2.0f, temp));
    top_p = std::max(0.0f, std::min(1.0f, p));
    top_k = std::max(1, k);
    repeat_penalty = std::max(1.0f, repeat);
    num_threads = std::max(1, threads);
}

std::string InferenceEngine::apply_prompt_template(const std::string& prompt) const {
    // Apply a standard prompt template for better results
    return "[INST] " + prompt + " [/INST]";
}

void InferenceEngine::post_process_output(std::string& output) const {
    // Remove control tokens and clean up output
    output.erase(std::remove_if(output.begin(), output.end(),
                               [](unsigned char c) { return c < 32 && c != ' ' && c != '\n'; }),
                output.end());
}
