#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "tweetnacl.h"

typedef unsigned char       u8;
typedef unsigned long       u32;
typedef unsigned long long  u64;
typedef long long           i64;
typedef i64                 gf[16];

#define YES 1
#define M   0
#define C   1
#define H   1
#define S   1

/** Utility function to release a JNI byte array reference.
 *
 */
void release(JNIEnv *env,jbyteArray jbytes,u8 *bytes,u64 N,int discard,jboolean copied) {
	if (bytes) {
		(*env)->ReleaseByteArrayElements(env,jbytes,bytes,discard ? JNI_ABORT : 0);
	}

	if (copied) {
		memset(bytes,0,N);
	}
}

// Ref. http://www.daemonology.net/blog/2014-09-05-erratum.html
//
// (seemingly not guaranteed to zero the memory in the face of
//  compiler optimizations, but John Regehr thinks it probably
//  will in most cases, so...)

static void * (* const volatile memset_ptr)(void *, int, size_t) = memset;

static void secure_memzero(void * p, size_t len) {
	(memset_ptr)(p, 0, len);
}

/** jniCryptoBoxKeyPair
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBoxKeyPair(JNIEnv *env,jobject object,jbyteArray publicKey,jbyteArray secretKey) {
	u8 pk[crypto_box_PUBLICKEYBYTES];
	u8 sk[crypto_box_SECRETKEYBYTES];

	int rc = crypto_box_keypair(pk,sk);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,publicKey,0,crypto_box_PUBLICKEYBYTES,pk);
		(*env)->SetByteArrayRegion(env,secretKey,0,crypto_box_SECRETKEYBYTES,sk);
	}

    secure_memzero(pk,crypto_box_PUBLICKEYBYTES);
    secure_memzero(sk,crypto_box_SECRETKEYBYTES);

    return (jint) rc;
}

/** jniCryptoBox
 *
 *  JNI wrapper function for crypto_box.
 *
 *  The code is structured around the assumption that GetByteArrayElements will
 *  succeed a lot more often than it will fail.
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBox(JNIEnv *env,jobject object,jbyteArray ciphertext,jbyteArray message,jbyteArray nonce,jbyteArray publicKey,jbyteArray secretKey) {
	int N    = (*env)->GetArrayLength(env,message);
	u8 *m    = (*env)->GetByteArrayElements(env,message,   JNI_FALSE);
	u8 *c    = (*env)->GetByteArrayElements(env,ciphertext,JNI_FALSE);
	u8 *n    = (*env)->GetByteArrayElements(env,nonce,     JNI_FALSE);
	u8 *pk   = (*env)->GetByteArrayElements(env,publicKey, JNI_FALSE);
	u8 *sk   = (*env)->GetByteArrayElements(env,secretKey, JNI_FALSE);

	int rc = crypto_box(c,m,N,n,pk,sk);

    (*env)->ReleaseByteArrayElements(env,secretKey, sk,JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,publicKey, pk,JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,nonce,     n, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,ciphertext,c, 0);
    (*env)->ReleaseByteArrayElements(env,message,   m, JNI_ABORT);

    return (jint) rc;
}

/** jniCryptoBoxOpen
 *
 *  JNI wrapper function for crypto_box_open.
 *
 *  The code is structured around the assumption that GetByteArrayElements will succeed
 *  a lot more often than it will fail.
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBoxOpen(JNIEnv *env,jobject object,jbyteArray message,jbyteArray ciphertext,jbyteArray nonce,jbyteArray publicKey,jbyteArray secretKey) {
	int rc   = -2;
	int clen = (*env)->GetArrayLength(env,ciphertext);
	int mlen = (*env)->GetArrayLength(env,message);
	int N    = clen + crypto_box_BOXZEROBYTES;
	u8 *c    = (u8 *) malloc(N);
	u8 *m    = (u8 *) malloc(N);
	u8  n [crypto_box_NONCEBYTES];
	u8  pk[crypto_box_PUBLICKEYBYTES];
	u8  sk[crypto_box_SECRETKEYBYTES];

	if (m && c) {
		memset(c,0,crypto_box_BOXZEROBYTES);

	    (*env)->GetByteArrayRegion(env,ciphertext,0,clen,&c[crypto_box_BOXZEROBYTES]);
	    (*env)->GetByteArrayRegion(env,nonce,     0,crypto_box_NONCEBYTES,n);
	    (*env)->GetByteArrayRegion(env,publicKey, 0,crypto_box_PUBLICKEYBYTES,pk);
	    (*env)->GetByteArrayRegion(env,secretKey, 0,crypto_box_SECRETKEYBYTES,sk);

		if ((rc = crypto_box_open(m,c,N,n,pk,sk)) == 0) {
			(*env)->SetByteArrayRegion(env,message,0,mlen,&m[crypto_box_ZEROBYTES]);
		}
	}

	secure_memzero(c, N);
	secure_memzero(m, N);
	secure_memzero(n, crypto_box_NONCEBYTES);
	secure_memzero(pk,crypto_box_PUBLICKEYBYTES);
	secure_memzero(sk,crypto_box_SECRETKEYBYTES);

	free(m);
	free(c);

    return (jint) rc;
}

/** jniCryptoBoxBeforeNM
 *
 *  JNI wrapper function for crypto_box_beforenm.
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBoxBeforeNM(JNIEnv *env,jobject object,jbyteArray key,jbyteArray publicKey,jbyteArray secretKey) {
	u8 k [crypto_box_BEFORENMBYTES];
	u8 pk[crypto_box_PUBLICKEYBYTES];
	u8 sk[crypto_box_SECRETKEYBYTES];

    (*env)->GetByteArrayRegion(env,publicKey,0,crypto_box_PUBLICKEYBYTES,pk);
    (*env)->GetByteArrayRegion(env,secretKey,0,crypto_box_SECRETKEYBYTES,sk);

	int rc = crypto_box_beforenm(k,pk,sk);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,key,0,crypto_box_BEFORENMBYTES,k);
	}

	secure_memzero(k, crypto_box_BEFORENMBYTES);
	secure_memzero(pk,crypto_box_PUBLICKEYBYTES);
	secure_memzero(sk,crypto_box_SECRETKEYBYTES);

    return (jint) rc;
}

/** jniCryptoBoxAfterNM
 *
 *  JNI wrapper function for crypto_box_afternm.
 *
 *  The code is structured around the assumption that GetByteArrayElements will succeed
 *  a lot more often than it will fail.
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBoxAfterNM(JNIEnv *env,jobject object,jbyteArray ciphertext,jbyteArray message,jbyteArray nonce,jbyteArray key) {
	int rc   = -2;
	int mlen = (*env)->GetArrayLength(env,message);
	int clen = (*env)->GetArrayLength(env,ciphertext);
	int N    = mlen + crypto_box_ZEROBYTES;
	u8 *m    = (u8 *) malloc(N);
	u8 *c    = (u8 *) malloc(N);
	u8  n[crypto_box_NONCEBYTES];
	u8  k[crypto_box_BEFORENMBYTES];

	if (m && c) {
		memset(m,0,crypto_box_ZEROBYTES);

	    (*env)->GetByteArrayRegion(env,message,0,mlen,&m[crypto_box_ZEROBYTES]);
		(*env)->GetByteArrayRegion(env,nonce,  0,crypto_box_NONCEBYTES,   n);
		(*env)->GetByteArrayRegion(env,key,    0,crypto_box_BEFORENMBYTES,k);

		if ((rc = crypto_box_afternm(c,m,N,n,k)) == 0) {
			(*env)->SetByteArrayRegion(env,ciphertext,0,clen,&c[crypto_box_BOXZEROBYTES]);
		}
	}

	secure_memzero(m,N);
	secure_memzero(c,N);
	secure_memzero(n,crypto_box_NONCEBYTES);
	secure_memzero(k,crypto_box_BEFORENMBYTES);

	free(c);
	free(m);

    return (jint) rc;
}

/** jniCryptoBoxOpenAfterNM
 *
 *  JNI wrapper function for crypto_box_open_afternm.
 *
 *  The code is structured around the assumption that GetByteArrayElements will succeed
 *  a lot more often than it will fail.
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoBoxOpenAfterNM(JNIEnv *env,jobject object,jbyteArray message,jbyteArray ciphertext,jbyteArray nonce,jbyteArray key) {
	int rc   = -2;
	int mlen = (*env)->GetArrayLength(env,message);
	int clen = (*env)->GetArrayLength(env,ciphertext);
	int N    = clen + crypto_box_BOXZEROBYTES;
	u8 *c    = (u8 *) malloc(N);
	u8 *m    = (u8 *) malloc(N);
	u8  n[crypto_box_NONCEBYTES];
	u8  k[crypto_box_BEFORENMBYTES];

	if (m && c) {
		memset(c,0,crypto_box_BOXZEROBYTES);

		(*env)->GetByteArrayRegion(env,ciphertext,0,clen,&c[crypto_box_BOXZEROBYTES]);
		(*env)->GetByteArrayRegion(env,nonce,     0,crypto_box_NONCEBYTES,   n);
		(*env)->GetByteArrayRegion(env,key,       0,crypto_box_BEFORENMBYTES,k);

		if ((rc = crypto_box_open_afternm(m,c,N,n,k)) == 0) {
			(*env)->SetByteArrayRegion(env,message,0,mlen,&m[crypto_box_ZEROBYTES]);
		}
	}

	secure_memzero(c,N);
	secure_memzero(m,N);
	secure_memzero(n,crypto_box_NONCEBYTES);
	secure_memzero(k,crypto_box_BEFORENMBYTES);

	free(m);
	free(c);

    return (jint) rc;
}

/** jniCryptoCoreHSalsa20
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoCoreHSalsa20(JNIEnv *env,jobject object,jbyteArray out,jbyteArray in,jbyteArray key,jbyteArray constant) {
	u8 o[crypto_core_hsalsa20_OUTPUTBYTES];
	u8 i[crypto_core_hsalsa20_INPUTBYTES];
	u8 k[crypto_core_hsalsa20_KEYBYTES];
	u8 c[crypto_core_hsalsa20_CONSTBYTES];

    (*env)->GetByteArrayRegion(env,in,      0,crypto_core_hsalsa20_INPUTBYTES,i);
    (*env)->GetByteArrayRegion(env,key,     0,crypto_core_hsalsa20_KEYBYTES,  k);
    (*env)->GetByteArrayRegion(env,constant,0,crypto_core_hsalsa20_CONSTBYTES,c);

	int rc = crypto_core_hsalsa20(o,i,k,c);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,out,0,crypto_core_hsalsa20_OUTPUTBYTES,o);
	}

	secure_memzero(o,crypto_core_hsalsa20_OUTPUTBYTES);
	secure_memzero(i,crypto_core_hsalsa20_INPUTBYTES);
	secure_memzero(k,crypto_core_hsalsa20_KEYBYTES);
	secure_memzero(c,crypto_core_hsalsa20_CONSTBYTES);

    return (jint) rc;
}

/** jniCryptoCoreSalsa20
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoCoreSalsa20(JNIEnv *env,jobject object,jbyteArray out,jbyteArray in,jbyteArray key,jbyteArray constant) {
	u8 o[crypto_core_salsa20_OUTPUTBYTES];
	u8 i[crypto_core_salsa20_INPUTBYTES];
	u8 k[crypto_core_salsa20_KEYBYTES];
	u8 c[crypto_core_salsa20_CONSTBYTES];

    (*env)->GetByteArrayRegion(env,in,      0,crypto_core_salsa20_INPUTBYTES,i);
    (*env)->GetByteArrayRegion(env,key,     0,crypto_core_salsa20_KEYBYTES,  k);
    (*env)->GetByteArrayRegion(env,constant,0,crypto_core_salsa20_CONSTBYTES,c);

	int rc = crypto_core_salsa20(o,i,k,c);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,out,0,crypto_core_salsa20_OUTPUTBYTES,o);
	}

	secure_memzero(o,crypto_core_salsa20_OUTPUTBYTES);
	secure_memzero(i,crypto_core_salsa20_INPUTBYTES);
	secure_memzero(k,crypto_core_salsa20_KEYBYTES);
	secure_memzero(c,crypto_core_salsa20_CONSTBYTES);

    return (jint) rc;
}

/** jniCryptoHash
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoHash(JNIEnv *env,jobject object,jbyteArray hash,jbyteArray message) {
	jboolean copied;
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,&copied);
	u8       h[crypto_hash_BYTES];

	if (m) {
		if ((rc = crypto_hash(h,m,(u64) N)) == 0) {
			(*env)->SetByteArrayRegion(env,hash,0,crypto_hash_BYTES,h);
		}
	}

	release       (env,message,m,N,YES,copied);
	secure_memzero(h,crypto_hash_BYTES);

    return (jint) rc;
}

/** jniCryptoHashBlocks
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoHashBlocks(JNIEnv *env,jobject object,jbyteArray hash,jbyteArray message) {
	jboolean copied[2];
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,&copied[M]);
	u8      *h  = (u8 *) (*env)->GetByteArrayElements(env,hash,   &copied[H]);

	if (m && h) {
	    rc = crypto_hashblocks(h,m,N);
	}

	release(env,message,m,N,YES,copied[M]);
	release(env,hash,   h,N,rc, copied[H]);

    return (jint) rc;
}

/** jniCryptoOneTimeAuth
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoOneTimeAuth(JNIEnv *env,jobject object,jbyteArray auth,jbyteArray message,jbyteArray key) {
	jboolean copied;
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,&copied);
	u8       k[crypto_onetimeauth_KEYBYTES];
	u8       a[crypto_onetimeauth_BYTES];

	if (m) {
		(*env)->GetByteArrayRegion(env,key,0,crypto_onetimeauth_KEYBYTES,k);

		if ((rc = crypto_onetimeauth(a,m,N,k)) == 0) {
			(*env)->SetByteArrayRegion(env,auth,0,crypto_onetimeauth_BYTES,a);
		}
	}

	release(env,message,m,N,YES,copied);

	secure_memzero(k,crypto_onetimeauth_KEYBYTES);
	secure_memzero(a,crypto_onetimeauth_BYTES);

    return (jint) rc;
}

/** jniCryptoOneTimeAuthVerify
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoOneTimeAuthVerify(JNIEnv *env,jobject object,jbyteArray auth,jbyteArray message,jbyteArray key) {
	jboolean copied;
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,&copied);
	u8       k[crypto_onetimeauth_KEYBYTES];
	u8       a[crypto_onetimeauth_BYTES];

	if (m) {
		(*env)->GetByteArrayRegion(env,auth,0,crypto_onetimeauth_BYTES,   a);
		(*env)->GetByteArrayRegion(env,key, 0,crypto_onetimeauth_KEYBYTES,k);

	    rc = crypto_onetimeauth_verify(a,m,N,k);
	}

	release(env,message,m,N,YES,copied);

	secure_memzero(k,crypto_onetimeauth_KEYBYTES);
	secure_memzero(a,crypto_onetimeauth_BYTES);

	return (jint) rc;
}

/** jniCryptoScalarMultBase
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoScalarMultBase(JNIEnv *env,jobject object,jbyteArray Q,jbyteArray N) {
	u8 n[crypto_scalarmult_SCALARBYTES];
	u8 q[crypto_scalarmult_BYTES];

    (*env)->GetByteArrayRegion(env,N,0,crypto_scalarmult_SCALARBYTES,n);

	int rc = crypto_scalarmult_base(q,n);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,Q,0,crypto_scalarmult_BYTES,q);
	}

	secure_memzero(n,crypto_scalarmult_SCALARBYTES);
	secure_memzero(q,crypto_scalarmult_BYTES);

    return (jint) rc;
}

/** jniCryptoScalarMult
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoScalarMult(JNIEnv *env,jobject object,jbyteArray Q,jbyteArray N,jbyteArray P) {
	u8 n[crypto_scalarmult_SCALARBYTES];
	u8 p[crypto_scalarmult_BYTES];
	u8 q[crypto_scalarmult_BYTES];

    (*env)->GetByteArrayRegion(env,N,0,crypto_scalarmult_SCALARBYTES,n);
    (*env)->GetByteArrayRegion(env,P,0,crypto_scalarmult_BYTES,      p);

	int rc = crypto_scalarmult(q,n,p);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,Q,0,crypto_scalarmult_BYTES,q);
	}

	secure_memzero(n,crypto_scalarmult_SCALARBYTES);
	secure_memzero(p,crypto_scalarmult_BYTES);
	secure_memzero(q,crypto_scalarmult_BYTES);

    return (jint) rc;
}

/** jniCryptoSecretBox
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoSecretBox(JNIEnv *env,jobject object,jbyteArray ciphertext,jbyteArray message,jbyteArray nonce,jbyteArray key) {
	int rc   = -2;
	int mlen = (*env)->GetArrayLength(env,message);
	int clen = (*env)->GetArrayLength(env,ciphertext);
	int N    = mlen + crypto_secretbox_ZEROBYTES;
	u8 *m    = (u8 *) malloc(N);
	u8 *c    = (u8 *) malloc(N);
	u8  n[crypto_secretbox_NONCEBYTES];
	u8  k[crypto_secretbox_KEYBYTES];

	if (m && c) {
		memset(m,0,crypto_secretbox_ZEROBYTES);

		(*env)->GetByteArrayRegion(env,message,0,mlen,&m[crypto_secretbox_ZEROBYTES]);
		(*env)->GetByteArrayRegion(env,nonce,  0,crypto_secretbox_NONCEBYTES,n);
		(*env)->GetByteArrayRegion(env,key,    0,crypto_secretbox_KEYBYTES,  k);

		if ((rc = crypto_secretbox(c,m,N,n,k)) == 0) {
			(*env)->SetByteArrayRegion(env,ciphertext,0,clen,&c[crypto_secretbox_BOXZEROBYTES]);
		}
	}

	secure_memzero(m,N);
	secure_memzero(c,N);
	secure_memzero(n,crypto_secretbox_NONCEBYTES);
	secure_memzero(k,crypto_secretbox_KEYBYTES);

	free(c);
	free(m);

    return (jint) rc;
}

/** jniCryptoSecretBoxOpen
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoSecretBoxOpen(JNIEnv *env,jobject object,jbyteArray message,jbyteArray ciphertext,jbyteArray nonce,jbyteArray key) {
	int rc   = -2;
	int mlen = (*env)->GetArrayLength(env,message);
	int clen = (*env)->GetArrayLength(env,ciphertext);
	int N    = clen + crypto_secretbox_BOXZEROBYTES;;
	u8 *m    = (u8 *) malloc(N);
	u8 *c    = (u8 *) malloc(N);
	u8  n[crypto_secretbox_NONCEBYTES];
	u8  k[crypto_secretbox_KEYBYTES];

	if (m && c) {
		memset(c,0,crypto_secretbox_BOXZEROBYTES);

		(*env)->GetByteArrayRegion(env,ciphertext,0,clen,&c[crypto_secretbox_BOXZEROBYTES]);
		(*env)->GetByteArrayRegion(env,nonce,     0,crypto_secretbox_NONCEBYTES,n);
		(*env)->GetByteArrayRegion(env,key,       0,crypto_secretbox_KEYBYTES,  k);

	    if ((rc = crypto_secretbox_open(m,c,N,n,k)) == 0) {
			(*env)->SetByteArrayRegion(env,message,0,mlen,&m[crypto_secretbox_ZEROBYTES]);
	    }
	}

	secure_memzero(m,N);
	secure_memzero(c,N);
	secure_memzero(n,crypto_secretbox_NONCEBYTES);
	secure_memzero(k,crypto_secretbox_KEYBYTES);

	free(m);
	free(c);

    return (jint) rc;
}

/** jniCryptoStream
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoStream(JNIEnv *env,jobject object,jbyteArray stream,jbyteArray nonce,jbyteArray key) {
	jboolean copied;
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,stream);
	u8      *c  = (u8 *) (*env)->GetByteArrayElements(env,stream,&copied);
	u8       n[crypto_stream_NONCEBYTES];
	u8       k[crypto_stream_KEYBYTES];

	if (c) {
		(*env)->GetByteArrayRegion(env,nonce,0,crypto_stream_NONCEBYTES,n);
		(*env)->GetByteArrayRegion(env,key,  0,crypto_stream_KEYBYTES,  k);

		rc = crypto_stream(c,N,n,k);
	}

	release(env,stream,c,N,rc,copied);

	secure_memzero(n,crypto_stream_NONCEBYTES);
	secure_memzero(k,crypto_stream_KEYBYTES);

    return (jint) rc;
}

/** jniCryptoStreamXor
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoStreamXor(JNIEnv *env,jobject object,jbyteArray ciphertext,jbyteArray message,jbyteArray nonce,jbyteArray key) {
	jboolean copied[2];
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *c  = (u8 *) (*env)->GetByteArrayElements(env,ciphertext,&copied[C]);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,   &copied[M]);
	u8       n[crypto_stream_NONCEBYTES];
	u8       k[crypto_stream_KEYBYTES];

	if (m && c) {
		(*env)->GetByteArrayRegion(env,nonce,0,crypto_stream_NONCEBYTES,n);
		(*env)->GetByteArrayRegion(env,key,  0,crypto_stream_KEYBYTES,  k);

		rc = crypto_stream_xor(c,m,N,n,k);
	}

	release(env,ciphertext,c,N,rc, copied[C]);
	release(env,message,   m,N,YES,copied[M]);

	secure_memzero(n,crypto_stream_NONCEBYTES);
	secure_memzero(k,crypto_stream_KEYBYTES);

    return (jint) rc;
}

/** jniCryptoStreamSalsa20
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoStreamSalsa20(JNIEnv *env,jobject object,jbyteArray stream,jbyteArray nonce,jbyteArray key) {
	jboolean copied;
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,stream);
	u8      *c  = (u8 *) (*env)->GetByteArrayElements(env,stream,&copied);
	u8       n[crypto_stream_salsa20_NONCEBYTES];
	u8       k[crypto_stream_salsa20_KEYBYTES];

	if (c) {
		(*env)->GetByteArrayRegion(env,nonce,0,crypto_stream_salsa20_NONCEBYTES,n);
		(*env)->GetByteArrayRegion(env,key,  0,crypto_stream_salsa20_KEYBYTES,  k);

		rc = crypto_stream_salsa20(c,N,n,k);
	}

	release(env,stream,c,N,rc,copied);

	secure_memzero(n,crypto_stream_salsa20_NONCEBYTES);
	secure_memzero(k,crypto_stream_salsa20_KEYBYTES);

    return (jint) rc;
}

/** jniCryptoStreamSalsa20Xor
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoStreamSalsa20Xor(JNIEnv *env,jobject object,jbyteArray ciphertext,jbyteArray message,jbyteArray nonce,jbyteArray key) {
	jboolean copied[2];
	int      rc = -2;
	int      N  = (*env)->GetArrayLength(env,message);
	u8      *c  = (u8 *) (*env)->GetByteArrayElements(env,ciphertext,&copied[C]);
	u8      *m  = (u8 *) (*env)->GetByteArrayElements(env,message,   &copied[M]);
	u8       n[crypto_stream_salsa20_NONCEBYTES];
	u8       k[crypto_stream_salsa20_KEYBYTES];

	if (m && c) {
	    (*env)->GetByteArrayRegion(env,nonce,0,crypto_stream_salsa20_NONCEBYTES,n);
	    (*env)->GetByteArrayRegion(env,key,  0,crypto_stream_salsa20_KEYBYTES,  k);

		rc = crypto_stream_salsa20_xor(c,m,N,n,k);
	}

	release(env,ciphertext,c,N,rc, copied[C]);
	release(env,message,   m,N,YES,copied[M]);

	secure_memzero(n,crypto_stream_salsa20_NONCEBYTES);
	secure_memzero(k,crypto_stream_salsa20_KEYBYTES);

    return (jint) rc;
}

/** jniCryptoSignKeyPair
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoSignKeyPair(JNIEnv *env,jobject object,jbyteArray publicKey,jbyteArray secretKey) {
	u8 pk[crypto_sign_PUBLICKEYBYTES];
	u8 sk[crypto_sign_SECRETKEYBYTES];

	int rc = crypto_sign_keypair(pk,sk);

	if (rc == 0) {
		(*env)->SetByteArrayRegion(env,publicKey,0,crypto_sign_PUBLICKEYBYTES,pk);
		(*env)->SetByteArrayRegion(env,secretKey,0,crypto_sign_SECRETKEYBYTES,sk);
	}

	secure_memzero(pk,crypto_sign_PUBLICKEYBYTES);
	secure_memzero(sk,crypto_sign_SECRETKEYBYTES);

    return (jint) rc;
}

/** jniCryptoSign
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoSign(JNIEnv *env,jobject object,jbyteArray signedm,jbyteArray message,jbyteArray secretKey) {
	jboolean copied[2];
	int      rc    = -2;
	int      N     = (*env)->GetArrayLength(env,message);
	u64      smlen = (*env)->GetArrayLength(env,signedm);
	u8      *m     = (u8 *) (*env)->GetByteArrayElements(env,message,&copied[M]);
	u8      *sm    = (u8 *) (*env)->GetByteArrayElements(env,signedm,&copied[S]);
	u8       sk[crypto_sign_SECRETKEYBYTES];

	if (m && sm) {
		(*env)->GetByteArrayRegion(env,secretKey,0,crypto_sign_SECRETKEYBYTES,sk);

		rc = crypto_sign(sm,&smlen,m,N,sk);
	}

	release(env,signedm,sm,smlen,rc, copied[S]);
	release(env,message,m, N,    YES,copied[M]);

	secure_memzero(sk,crypto_sign_SECRETKEYBYTES);

    return (jint) rc;
}

/** jniCryptoSignOpen
 *
 *  This is the one function where using malloc/free performs fractionally better
 *  than GetByteArrayElements. Allocating three arrays seems to be unavoidable
 *  without returning message that is too long to the requester.
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoSignOpen(JNIEnv *env,jobject object,jbyteArray message,jbyteArray signedm,jbyteArray key) {
	jboolean copied;
	int      rc    = -2;
	int      N     = (*env)->GetArrayLength(env,signedm);
	u64      mlen  = (*env)->GetArrayLength(env,message) - crypto_sign_BYTES;
	u8      *sm    = (u8 *) (*env)->GetByteArrayElements(env,signedm,&copied);
	u8      *m     = (u8 *) malloc(N);
	u8       pk[crypto_sign_PUBLICKEYBYTES];

	if (sm && m) {
		(*env)->GetByteArrayRegion(env,key,0,crypto_sign_PUBLICKEYBYTES,pk);

		if ((rc = crypto_sign_open(m,&mlen,sm,N,pk)) == 0) {
			(*env)->SetByteArrayRegion(env,message,0,mlen,m);
		}
	}

	release(env,signedm,sm,N,YES,copied);

	secure_memzero(m, N);
	secure_memzero(pk,crypto_sign_PUBLICKEYBYTES);

	free(m);

    return (jint) rc;
}


/** jniCryptoVerify16
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoVerify16(JNIEnv *env,jobject object,jbyteArray X,jbyteArray Y) {
	u8 x[crypto_verify_16_BYTES];
	u8 y[crypto_verify_16_BYTES];

    (*env)->GetByteArrayRegion(env,X,0,crypto_verify_16_BYTES,x);
    (*env)->GetByteArrayRegion(env,Y,0,crypto_verify_16_BYTES,y);

	int rc = crypto_verify_16(x,y);

	secure_memzero(x,crypto_verify_16_BYTES);
	secure_memzero(y,crypto_verify_16_BYTES);

    return (jint) rc;
}

/** jniCryptoVerify32
 *
 */
jint Java_za_co_twyst_tweetnacl_TweetNaClZ_jniCryptoVerify32(JNIEnv *env,jobject object,jbyteArray X,jbyteArray Y) {
	u8 x[crypto_verify_32_BYTES];
	u8 y[crypto_verify_32_BYTES];

    (*env)->GetByteArrayRegion(env,X,0,crypto_verify_32_BYTES,x);
    (*env)->GetByteArrayRegion(env,Y,0,crypto_verify_32_BYTES,y);

	int rc = crypto_verify_32(x,y);

	secure_memzero(x,crypto_verify_32_BYTES);
	secure_memzero(y,crypto_verify_32_BYTES);

    return (jint) rc;
}
