#include <jni.h>
#include <string>
#include "httplib.h"
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_me_ensine_app_LoginActivity_login(JNIEnv *env, jobject /* this */, jstring email, jstring password) {
    const char *email_cstr = env->GetStringUTFChars(email, nullptr);
    const char *password_cstr = env->GetStringUTFChars(password, nullptr);

    LOGI("Email: %s", email_cstr);
    LOGI("Password: %s", password_cstr);

    httplib::Client cli("https://httpbin.org");
    cli.set_connection_timeout(30); // Timeout de 30 segundos para a conexão
    httplib::Headers headers = { {"Content-Type", "application/json"} };
    std::string body = "{\"email\":\"" + std::string(email_cstr) + "\",\"password\":\"" + std::string(password_cstr) + "\"}";

    LOGI("Request Body: %s", body.c_str());

    auto res = cli.Post("/post", headers, body, "application/json");

    env->ReleaseStringUTFChars(email, email_cstr);
    env->ReleaseStringUTFChars(password, password_cstr);

    if (res) {
        LOGI("Response Status: %d", res->status);
        LOGI("Response Body: %s", res->body.c_str());
    } else {
        LOGE("Request failed");
        if (res.error() == httplib::Error::Connection) {
            LOGE("Connection error");
        } else if (res.error() == httplib::Error::Read) {
            LOGE("Read error");
        } else if (res.error() == httplib::Error::Write) {
            LOGE("Write error");
        } else if (res.error() == httplib::Error::ExceedRedirectCount) {
            LOGE("Exceeded redirect count");
        } else if (res.error() == httplib::Error::Canceled) {
            LOGE("Request canceled");
        } else if (res.error() == httplib::Error::SSLConnection) {
            LOGE("SSL connection error");
        } else if (res.error() == httplib::Error::SSLLoadingCerts) {
            LOGE("SSL loading certificates error");
        } else if (res.error() == httplib::Error::SSLServerVerification) {
            LOGE("SSL server verification error");
        } else if (res.error() == httplib::Error::UnsupportedMultipartBoundaryChars) {
            LOGE("Unsupported multipart boundary characters");
        } else {
            LOGE("Unknown error: %d", static_cast<int>(res.error()));
        }
    }

    if (res && res->status == 200) {
        return env->NewStringUTF(res->body.c_str());
    } else {
        return env->NewStringUTF("");
    }
}
