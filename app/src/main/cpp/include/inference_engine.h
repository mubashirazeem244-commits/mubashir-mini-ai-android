#ifndef INFERENCE_ENGINE_H
#define INFERENCE_ENGINE_H

#include <string>
#include <vector>
#include <memory>
#include "model_loader.h"

class InferenceEngine {
public:
    InferenceEngine(ModelLoader* loader, int context_size);
    ~InferenceEngine();

    bool initialize();
    std::string run_inference(const std::string& prompt, int max_tokens);
    std::vector<int> tokenize(const std::string& text);
    void set_parameters(float temperature, float top_p, int top_k, float repeat_penalty, int num_threads);

private:
    ModelLoader* model_loader;
    int context_size;
    void* inference_context;
    
    // Inference parameters
    float temperature;
    float top_p;
    int top_k;
    float repeat_penalty;
    int num_threads;

    std::string apply_prompt_template(const std::string& prompt) const;
    void post_process_output(std::string& output) const;
};

#endif // INFERENCE_ENGINE_H
