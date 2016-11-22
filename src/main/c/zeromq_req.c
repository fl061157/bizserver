#include "com_handwin_zeromq_ZeromqRequest.h"
#include <zmq.h>

#define GET_CHAR(java_string) java_string == NULL ? NULL : (*env)->GetStringUTFChars(env, java_string, NULL);
#define RELEASE_CHAR(c_char_ptr, java_string) if(java_string != NULL && c_char_ptr != NULL){ \
        (*env)->ReleaseStringUTFChars(env, java_string, c_char_ptr); \
    }

#define NEW_BYTE_ARRAY(X, c_char_ptr, len) \
                jbyteArray X = NULL; \
                int _len##X = len;  \
                if (_len##X>0){ \
                        X = (*env)->NewByteArray(env, _len##X); \
                        (*env)->SetByteArrayRegion(env, X , \
                                                        0, _len##X, (jbyte*)c_char_ptr); \
                }

#define BYTE_ARRAY_LENGTH(X) X==NULL ? 0:(*env)->GetArrayLength(env, X);

#define GET_BYTE_ARRAY_CHAR(X, jX) \
    jboolean _isCopy##X = JNI_FALSE; \
    char* X = NULL; \
    if (jX!=NULL){ X = (*env)->GetByteArrayElements(env, jX, 0);}

#define RELEASE_BYTE_ARRAY_CHAR(X, jX) \
    if(X!=NULL) {(*env)->ReleaseByteArrayElements(env, jX, X, 0);}


typedef struct  
{
	void* context ;
	void* request;
} zemq_request_t ;




void jni_free (void *data, void *hint)
{
    free (data);
}


/*
 * Class:     com_handwin_zeromq_ZeromqRequest
 * Method:    native_init
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_handwin_zeromq_ZeromqRequest_native_1init
  (JNIEnv *env, jobject obj, jstring url) {

        zemq_request_t *r = malloc(sizeof( zemq_request_t ));
        r->context = zmq_ctx_new ();
    r->request = zmq_socket (r->context, ZMQ_REQ);
    char* c_url = GET_CHAR( url );
    zmq_connect (r->request, c_url);
    RELEASE_CHAR(c_url , url) ;
    return (jlong) r;
  }

/*
 * Class:     com_handwin_zeromq_ZeromqRequest
 * Method:    native_destory
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_handwin_zeromq_ZeromqRequest_native_1destory
  (JNIEnv *env, jobject obj, jlong ptr) {
         if( ptr < 1 ) {
                return ;
         }
     zemq_request_t *r = (zemq_request_t*) ptr ;
         zmq_close (r->request);
     zmq_ctx_destroy (r->context);
     free(r);
  }



/*
 * Class:     com_handwin_zeromq_ZeromqRequest
 * Method:    native_send
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_handwin_zeromq_ZeromqRequest_native_1send
  (JNIEnv *env, jobject obj, jlong ptr, jbyteArray jdata , jint len) {
  	if( ptr < 1 ) {
        return 0;
    }
    zemq_request_t *r = (zemq_request_t*) ptr ;
    zmq_msg_t sendMsg;
    GET_BYTE_ARRAY_CHAR(data , jdata );
    char* copy_data = malloc(len);
    memcpy(copy_data , data , len) ;
    int rc = zmq_msg_init_data (&sendMsg, copy_data, len, jni_free, NULL);
    rc = zmq_msg_send (&sendMsg, r->request, 0);
    zmq_msg_close(&sendMsg);
    RELEASE_BYTE_ARRAY_CHAR(data , jdata) ;
    if( rc == -1 ) {
        return NULL ;
    }
    zmq_msg_t recvMsg;
    rc = zmq_msg_init(&recvMsg);
    if( rc != 0 ){
        return NULL ;
    }
    rc = zmq_msg_recv (&recvMsg, r->request, 0);
    if( rc == -1 ) {
        return NULL ;
    } else {
        size_t size = zmq_msg_size(&recvMsg);
        void* recvData = zmq_msg_data(&recvMsg);
        NEW_BYTE_ARRAY(jresult, recvData , size) ;
        zmq_msg_close(&recvMsg);
        return jresult;
    }
  }

