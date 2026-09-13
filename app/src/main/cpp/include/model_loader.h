#ifndef MODEL_LOADER_H
#define MODEL_LOADER_H

#include <string>
#include <vector>
#include <map>
#include <memory>

class ModelLoader {
public:
    ModelLoader(const std::string& model_path);
    ~ModelLoader();

    bool load();
    std::map<std::string, std::string> get_info() const;
    int get_n_embd() const { return n_embd; }
    int get_n_vocab() const { return n_vocab; }
    int get_n_ctx() const { return n_ctx; }

private:
    std::string model_path;
    void* model_data;
    int n_embd;     // embedding dimensions
    int n_vocab;    // vocabulary size
    int n_ctx;      // context size

    bool read_gguf_header();
    bool validate_model_format();
};

#endif // MODEL_LOADER_H
