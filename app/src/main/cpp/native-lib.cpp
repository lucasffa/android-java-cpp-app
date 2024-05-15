#include <jni.h>
#include <string>
#include "httplib.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_me_ensine_app_LoginActivity_login(JNIEnv *env, jobject /* this */, jstring email, jstring password) {
    const char *email_cstr = env->GetStringUTFChars(email, nullptr);
    const char *password_cstr = env->GetStringUTFChars(password, nullptr);

    httplib::Client cli("http://baseURL");
    httplib::Headers headers = { {"Content-Type", "application/json"} };
    std::string body = "{\"email\":\"" + std::string(email_cstr) + "\",\"password\":\"" + std::string(password_cstr) + "\"}";

    auto res = cli.Post("/users/login", headers, body, "application/json");

    env->ReleaseStringUTFChars(email, email_cstr);
    env->ReleaseStringUTFChars(password, password_cstr);

    if (res && res->status == 200) {
        return env->NewStringUTF(res->body.c_str());
    } else {
        return env->NewStringUTF("");
    }
}
